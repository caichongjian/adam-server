package org.caichongjian.server.http;

import com.alibaba.fastjson.asm.ByteVector;
import com.google.common.base.Preconditions;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>封装http请求的输入流</p>
 * <p/>
 * <p>SocketInputStream调用readAllBytes()时会一直阻塞，从而导致服务器无法正常运行。</p>
 * <p>而调用readNBytes()需要知道整个请求报文的长度，socketInputStream.available()获得的不一定是http请求报文的全部长度,
 * 因此socketInputStream.readNBytes(socketInputStream.available())也是不可行的。</p>
 * <p>为了解决读取输入流出现的各种问题，引入RequestStream来按HTTP协议约定的格式读取输入流。</p>
 */
public class RequestStream implements Closeable {

    private final InputStream socketInputStream;

    public static final int BUFFER_SIZE = 128; // 开发时方便调试，可根据实际需要调整

    /**
     * 以byte数组存储的\r\n\r\n(两个连续的CRLF)
     */
    private static final byte[] DOUBLE_CRLF_AS_BYTE_ARRAY = new byte[]{'\r', '\n', '\r', '\n'};

    /**
     * 读取请求行和请求头时，如果读取过量(将请求体的一小部分也从socketInputStream中读出来了)，将过量的部分缓存到这里
     */
    private byte[] bodyBuffer;

    public RequestStream(InputStream socketInputStream) {
        this.socketInputStream = socketInputStream;
    }

    /**
     * 从socketInputStream中读取请求行和请求头，并将它们作为字符串返回
     * 在一次请求中，此方法必须调用且仅能调用一次
     * 内部方法，仅供adam-server-core内部调用
     *
     * @return 请求行和请求头
     */
    public String readRequestLineAndHeaders() throws IOException {

        // 说明一下为何要将StringBuilder更换成ByteVector。原来那种基于StringBuilder的方案存在一个在某些特定情况下(也许可能大概好像是网络不稳定等情况，我在开发环境上是通过客户端sleep测出来的)解析请求头失败的bug，为了解决这个bug需要修改读取过量的处理逻辑，StringBuilder在这种场景下可以用但使用起来不是很方便，而使用ByteVector需要自己实现indexOf()方法(经JMeter测试吞吐量差不多)。
        // TODO 我测试的客户端(浏览器)都是以两个连续的CRLF(\r\n)来分隔请求头和请求体，也许我没考虑周全？
        // 读取请求行和请求头,如果读到\r\n\r\n说明请求头读取完毕，如果read()方法返回-1说明可能是读取完毕或者请求取消，否则一直读取直到读到\r\n\r\n为止
        ByteVector byteVector = new ByteVector(BUFFER_SIZE); // 基于byte基本数据类型的可变长数组。可以换成其他类库中的asm.ByteVector，可以换成StringBuilder，也可以自己实现一个可变长数组。
        byte[] lineAndHeadersBuffer = new byte[BUFFER_SIZE];
        int headersEndIndex;
        do {
            int bytesRead = socketInputStream.read(lineAndHeadersBuffer, 0, BUFFER_SIZE);
            if (bytesRead == -1) {
                return "";  // 因各种原因(单线程打断点在某些情况下能重现)导致请求头不完整时，返回空字符串
            }
            byteVector.putByteArray(lineAndHeadersBuffer, 0, bytesRead);  // 应该不会有人在请求头里直接放非ASCII字符吧？？？
            headersEndIndex = strStr(byteVector, DOUBLE_CRLF_AS_BYTE_ARRAY); // 如果有需要的话可以修改BUFFER_SIZE，或者修改代码使得不用每次都判断整个完整的字符串。
        } while (headersEndIndex == -1);

        // 存在请求体时会有读取过量的情况，需要处理一下。
        final int bodyBytesStartIndex = headersEndIndex + 4;
        bodyBuffer = new byte[byteVector.length - bodyBytesStartIndex]; // 即使没读取过量也生成一个空数组，防空指针并提高代码可读性
        System.arraycopy(byteVector.data, bodyBytesStartIndex, bodyBuffer, 0, bodyBuffer.length);  // 将byteVector中\r\n\r\n之后的数据复制到bodyBuffer中

        // 返回请求行和请求头
        return new String(byteVector.data, 0, headersEndIndex);
    }

    /**
     * 从socketInputStream中读取请求体，并将其作为byte数组返回
     * 在一次请求中，此方法仅能调用一次
     * 内部方法，仅供adam-server-core内部调用
     *
     * @return 请求体内容
     */
    public byte[] readRequestBody(int contentLength) throws IOException {

        final int lengthInBodyBuffer = bodyBuffer.length;
        final int lengthInSocketStream = contentLength - lengthInBodyBuffer;

        Preconditions.checkState(lengthInSocketStream >= 0, "Invalid request.");

        if (lengthInSocketStream == 0) {
            return bodyBuffer;  // 不是很懂System.arraycopy()和new byte[contentLength]的性能开销。如果性能开销小可以为了代码可读性删除这一判断
        }

        // 从socketInputStream中读取剩余的请求体
        final byte[] bytesInSocketStream = socketInputStream.readNBytes(lengthInSocketStream);
        if (lengthInBodyBuffer == 0) {
            return bytesInSocketStream;  // 不是很懂System.arraycopy()和new byte[contentLength]的性能开销。如果性能开销小可以为了代码可读性删除这一判断
        }

        // 将从socketInputStream中读取剩余的请求体与bodyBuffer拼到一起
        byte[] requestContentBytes = new byte[contentLength];
        System.arraycopy(bodyBuffer, 0, requestContentBytes, 0, lengthInBodyBuffer);
        System.arraycopy(bytesInSocketStream, 0, requestContentBytes, lengthInBodyBuffer, lengthInSocketStream);
        return requestContentBytes;
    }

    @Override
    public void close() throws IOException {
        socketInputStream.close();
    }

    /**
     * <p>给定一个 haystack 字符串和一个 needle 字符串，在 haystack 字符串中找出 needle 字符串出现的第一个位置 (从0开始)。</p>
     * <p>类似于String.indexOf()，因为这里的字符串是使用ByteVector类型存储的，没法直接使用String.indexOf()，所以自己实现了一个低配的方法。</p>
     * <p>方法命名源自C/C++的strstr()函数，方法名称与说明参考了LeetCode的一道题目并使用了LeetCode上面的测试用例</p>
     * <p/>
     *
     * <p>如果needle不是haystack的子串，返回-1</p>
     * <p>如果needle是haystack的子串，返回其在haystack中第一次出现的位置</p>
     * <p>空串是任何串的子串，且出现位置为0</p>
     *
     * @param haystack ByteVector类型存储的字符串
     * @param needle byte[]类型存储的字符串
     */
    private int strStr(ByteVector haystack, byte[] needle) {
        int len2 = needle.length;
        if (len2 == 0)
            return 0;

        // 判断needle是不是haystack的子串
        byte[] haystackData = haystack.data;
        int difference = haystack.length - len2;
        for (int i = 0; i <= difference; i++) {
            if (haystackData[i] == needle[0]) {
                int j = 1;
                for (; j < len2 && haystackData[i + j] == needle[j]; j++)
                    ;
                if (j == len2)
                    return i;
            }
        }
        return -1;
    }
}
