package org.caichongjian.api;

/**
 * javax.servlet-api的HttpServletRequest需要实现的方法太多了，简易服务器小国寡民，自定义简易版的接口
 */
public interface MiniHttpServletRequest {

    /**
     * Returns the name of the HTTP method with which this
     * request was made, for example, GET, POST, or PUT.
     * Same as the value of the CGI variable REQUEST_METHOD.
     *
     * @return			a <code>String</code>
     *				specifying the name
     *				of the method with which
     *				this request was made
     */
    String getMethod();
}
