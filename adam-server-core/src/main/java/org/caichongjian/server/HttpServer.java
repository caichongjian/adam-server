package org.caichongjian.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    public static final String EXIT_COMMAND = "EXIT";

    public static final int PORT = 8888;

    public static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    private static boolean running = true;

    public static void main(String[] args) {


        try (ServerSocket ss = new ServerSocket(PORT)) {

            LOGGER.info("starting the server....");

            while (running) {
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
             InputStream inputStream = s.getInputStream();
             OutputStream outputStream = s.getOutputStream()) {

            LOGGER.debug("客户端: {} 已连接到服务器", s.getInetAddress().getHostAddress());

            Request request = new Request(inputStream);
            request.parse();

            Response response = new Response(outputStream);

            if (request.getUri().startsWith("/servlet")) {
                ServletProcessor processor = new ServletProcessor();
                processor.process(request, response);
            } else {
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
            }

            running = !EXIT_COMMAND.equals(request.getUri());

        } catch (IOException e) {
            LOGGER.error("an I/O error occurs: ", e);
        }
    }
}
