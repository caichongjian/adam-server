package org.caichongjian.server;

import org.caichongjian.server.http.Request;
import org.caichongjian.server.http.Response;

import java.io.IOException;

public class StaticResourceProcessor {

    /**
     * Process static resource request.
     *
     * @param request  http request
     * @param response http response
     * @throws IOException if an I/O error occurs
     */
    public void process(Request request, Response response) throws IOException {
        response.sendStaticResource(request.getRequestURI());
    }
}
