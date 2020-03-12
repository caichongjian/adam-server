package org.caichongjian.server;

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
        response.sendStaticResource(request.getUri());
    }
}
