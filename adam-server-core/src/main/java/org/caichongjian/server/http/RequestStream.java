package org.caichongjian.server.http;

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

    public static final int BUFFER_SIZE = 128; // 开发时方便测试，可根据实际需要调整

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

        // TODO 我测试的客户端(浏览器)都是以两个连续的CRLF(\r\n)来分隔请求头和请求体，也许我没考虑周全？
        // 读取请求行和请求头,如果读到\r\n\r\n说明请求头读取完毕，如果read()方法返回-1说明可能是读取完毕或者请求取消，否则一直读取直到读到\r\n\r\n为止
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        byte[] lineAndHeadersBuffer = new byte[BUFFER_SIZE];
        int bytesRead;
        do {
            bytesRead = socketInputStream.read(lineAndHeadersBuffer, 0, BUFFER_SIZE);
            for (int j = 0; j < bytesRead; j++) {
                sb.append((char) lineAndHeadersBuffer[j]); // 应该不会有人在请求头里直接放汉字吧？？？
            }
        } while (bytesRead >= 0 && sb.indexOf("\r\n\r\n") == -1);  // 为了代码整洁美观，可以考虑将\r\n\r\n抽取为常量

        // 存在请求体时会有读取过量的情况，需要处理一下。另外，如果需要性能的话，是可以少调用一次indexOf()方法的
        final int headersEndIndex = sb.indexOf("\r\n\r\n");
        final int bodyBytesStartIndex = ((headersEndIndex + 3) % BUFFER_SIZE) + 1;
        if (headersEndIndex == -1) {
            return "";  // 因各种原因(单线程打断点在某些情况下能重现)导致请求头不完整时，返回空字符串
        }
        bodyBuffer = new byte[bytesRead - bodyBytesStartIndex]; // 即使没读取过量也生成一个空数组，防空指针并提高代码可读性
        if (bodyBuffer.length > 0) {
            System.arraycopy(lineAndHeadersBuffer, bodyBytesStartIndex, bodyBuffer, 0, bodyBuffer.length);
        }
        return sb.substring(0, headersEndIndex);
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
}
