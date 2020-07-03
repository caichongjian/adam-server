package org.caichongjian.server.startup;

import org.apache.commons.lang3.StringUtils;
import org.caichongjian.server.Constants;
import org.caichongjian.server.RestProcessor;
import org.caichongjian.server.StaticResourceProcessor;
import org.caichongjian.server.http.Request;
import org.caichongjian.server.http.RequestStream;
import org.caichongjian.server.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

class HttpServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    private static ThreadPool threadPool;

    private HttpServer() {
    }

    static void start() {

        // 初始化线程池
        threadPool = new ThreadPool(Constants.Server.THREAD_POOL_SIZE);
        // 程序退出前(kill -9和其他强制结束进程方式除外)通知所有工作线程，并等待其正常终止
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                threadPool.drop();
            } catch (InterruptedException e) {
                LOGGER.error("InterruptedException: ", e);
                Thread.currentThread().interrupt();
            }
        }, "ShutdownHookThread"));

        // 监听并处理连接
        try (ServerSocket ss = new ServerSocket(Constants.Server.PORT)) {

            LOGGER.info("服务器已启动，请访问http://localhost:{}", Constants.Server.PORT);
            while (true) {
                final Socket socket = ss.accept();
                threadPool.execute(() -> handleConnection(socket));
            }
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException: ", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOGGER.error("an error occurs: ", e);
        }
    }

    /**
     * 处理 http 连接
     */
    private static void handleConnection(Socket socket) {

        try (Socket s = socket;
             RequestStream requestStream = new RequestStream(s.getInputStream());
             OutputStream outputStream = s.getOutputStream()) {

            LOGGER.debug("客户端: {} 已连接到服务器", s.getInetAddress().getHostAddress());

            // 设置连接超时时间
            Integer connectionTimeoutMillis = Constants.Server.CONNECTION_TIMEOUT_MILLIS;
            if (connectionTimeoutMillis != null && connectionTimeoutMillis > 0) {
                s.setSoTimeout(connectionTimeoutMillis);
            }

            Request request = new Request(requestStream);
            request.parseRequestLineAndHeaders();

            Response response = new Response(outputStream);

            if (RestProcessor.containsUriMapping(request.getRequestURI())) {
                RestProcessor processor = new RestProcessor();
                processor.process(request, response);
            } else if (StringUtils.isBlank(request.getMethod())) {
                // TODO 此种情况下说明请求头因各种原因没发送完整，处理逻辑还没想好
            } else {
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
            }

        } catch (Exception e) {
            LOGGER.error("an error occurs: ", e);
        }
    }
}
