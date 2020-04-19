package org.caichongjian.server.http;

import com.google.common.base.Preconditions;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * 封装http请求的输入流
 * <p>
 * SocketInputStream调用readAllBytes()时会阻塞；而调用readNBytes()需要知道整个请求报文的长度，available()获得的也不一定是http请求的全部长度。
 * 为了解决这一问题，需要按照HTTP协议约定的格式读取输入流。
 */
public class RequestStream implements Closeable {

    private InputStream socketInputStream;

    public static final int BUFFER_SIZE = 128;

    /**
     * 读取请求行和请求头时，如果读取过量(将请求体的一小部分也从socketInputStream中读出来了)，将过量的部分缓存到这里
     */
    private byte[] bodyBuffer;

    public RequestStream(InputStream socketInputStream) {
        this.socketInputStream = socketInputStream;
    }

    /**
     * 从socketInputStream中读取请求行和请求头，并将它们作为字符串返回
     * 此方法必须调用且仅能调用一次
     *
     * @return 请求行和请求头
     */
    public String readRequestLineAndHeaders() throws IOException {

        // 读取请求行和请求头,如果读到\r\n\r\n说明请求头读取完毕，否则一直读取直到读到\r\n\r\n为止
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        byte[] lineAndHeadersBuffer = new byte[BUFFER_SIZE];
        int bytesRead;
        do {
            bytesRead = socketInputStream.read(lineAndHeadersBuffer, 0, BUFFER_SIZE);
            for (int j = 0; j < bytesRead; j++) {
                sb.append((char) lineAndHeadersBuffer[j]); // 应该不会有人在请求头里直接放汉字吧？？？
            }
        } while (sb.indexOf("\r\n\r\n") == -1);

        // 存在请求体时会有读取过量的情况，需要处理一下
        final int headersEndIndex = sb.indexOf("\r\n\r\n");
        final int bodyBytesStartIndex = ((headersEndIndex + 3) % BUFFER_SIZE) + 1;
        bodyBuffer = new byte[bytesRead - bodyBytesStartIndex]; // 即使没读取过量也生成一个空数组，防空指针并提高代码可读性
        if (bodyBuffer.length > 0) {
            System.arraycopy(lineAndHeadersBuffer, bodyBytesStartIndex, bodyBuffer, 0, bodyBuffer.length);
        }
        return sb.substring(0, headersEndIndex);
    }

    /**
     * 从socketInputStream中读取请求体，并将其作为byte数组返回
     * 此方法必须调用且仅能调用一次
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
