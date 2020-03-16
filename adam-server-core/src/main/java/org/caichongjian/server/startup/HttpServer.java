package org.caichongjian.server.startup;

import org.caichongjian.server.RestProcessor;
import org.caichongjian.server.StaticResourceProcessor;
import org.caichongjian.server.http.Request;
import org.caichongjian.server.http.RequestStream;
import org.caichongjian.server.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpServer {

    public static final String EXIT_COMMAND = "EXIT";

    public static final int PORT = 8888;

    public static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    private static AtomicBoolean runningFlag = new AtomicBoolean(true);

    public static void start() {

        try (ServerSocket ss = new ServerSocket(PORT)) {

            LOGGER.info("starting the server....");

            while (runningFlag.get()) {
                handleRequest(ss);
            }
        } catch (IOException e) {
            LOGGER.error("an I/O error occurs: ", e);
        }
    }

    /**
     * 处理http请求，并返回相应资源
     *
     * @param ss server socket
     */
    private static void handleRequest(ServerSocket ss) {
        try (Socket s = ss.accept();
             RequestStream requestStream = new RequestStream(s.getInputStream());
             OutputStream outputStream = s.getOutputStream()) {

            LOGGER.debug("客户端: {} 已连接到服务器", s.getInetAddress().getHostAddress());

//            s.setSoTimeout(10 * 1000);  // Ten seconds
            Request request = new Request(requestStream);
            request.parse();

            Response response = new Response(outputStream);

            if (RestProcessor.containsUriMapping(request.getRequestURI())) {
                RestProcessor processor = new RestProcessor();
                processor.process(request, response);
            } else {
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
            }

            runningFlag.set(!EXIT_COMMAND.equals(request.getRequestURI()));

        } catch (IOException e) {
            LOGGER.error("an I/O error occurs: ", e);
        }
    }
}
