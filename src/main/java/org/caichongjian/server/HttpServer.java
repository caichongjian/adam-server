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

    public static void main(String[] args) {


        try (ServerSocket ss = new ServerSocket(PORT)) {

            LOGGER.info("starting the server....");

            while (true) {
                try (Socket s = ss.accept();
                     InputStream inputStream = s.getInputStream();
                     OutputStream outputStream = s.getOutputStream()) {

                    LOGGER.debug("客户端:" + s.getInetAddress().getHostAddress() + "已连接到服务器");

                    Request request = new Request(inputStream);
                    request.parse();

                    final String webRootPath = ServletContext.getInstance().getWebRootPath();
                    LOGGER.info(webRootPath);

                    Response response = new Response(outputStream);
                    response.sendStaticResource(request.getUri());

                    if (EXIT_COMMAND.equals(request.getUri())) {
                        break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
