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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

class HttpServer {

    public static final String EXIT_URI = "/EXIT";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    // TODO 关闭服务器的方式需要重新设计
    private static AtomicBoolean runningFlag = new AtomicBoolean(true);

    private static BlockingQueue<Socket> socketBlockingQueue;

    // TODO 关闭服务器的方式需要重新设计
    private static ExecutorService executorService;

    private HttpServer() {
    }

    static void start() {

        socketBlockingQueue = new ArrayBlockingQueue<>(Constants.Server.THREAD_POOL_SIZE * 2);
        executorService = Executors.newFixedThreadPool(Constants.Server.THREAD_POOL_SIZE);
        for (int i = 0; i < Constants.Server.THREAD_POOL_SIZE; i++) {
            executorService.execute(HttpServer::handleRequest);
        }

        try (ServerSocket ss = new ServerSocket(Constants.Server.PORT)) {

            LOGGER.info("服务器已启动，请访问http://localhost:{}", Constants.Server.PORT);

            while (runningFlag.get()) {
                final Socket socket = ss.accept();
                socketBlockingQueue.put(socket);
            }
            executorService.shutdownNow();
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (Exception e) {
            LOGGER.error("an error occurs: ", e);
        }
    }

    /**
     * 处理http请求，并返回相应资源
     */
    private static void handleRequest() {

        while (runningFlag.get()) {

            try (Socket s = socketBlockingQueue.take();
                 RequestStream requestStream = new RequestStream(s.getInputStream());
                 OutputStream outputStream = s.getOutputStream()) {

                LOGGER.debug("客户端: {} 已连接到服务器", s.getInetAddress().getHostAddress());

//            s.setSoTimeout(10 * 1000);  // Ten seconds
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

                // TODO 关闭服务器的方式需要重新设计
                runningFlag.compareAndSet(true, !EXIT_URI.equals(request.getRequestURI()));

            } catch (InterruptedException e) { // TODO 关闭服务器的方式需要重新设计
                LOGGER.info("线程{}因中断而停止", Thread.currentThread().getName());
            } catch (Exception e) {
                LOGGER.error("an error occurs: ", e);
            }
        }
    }
}
