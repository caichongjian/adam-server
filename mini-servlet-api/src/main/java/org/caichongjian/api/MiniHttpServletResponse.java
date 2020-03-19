package org.caichongjian.api;

import javax.servlet.http.Cookie;

/**
 * javax.servlet-api的HttpServletResponse需要实现的方法太多了。
 * 简易服务器不会有真实用户，没必要实现那么多方法，所以自定义了简易版的接口。
 * 各个接口方法的定义比起javax.servlet-api来略有不同。
 * <p>
 * There are too many methods that javax.servlet-api's HttpServletResponse needs to implement.
 * Simple server will not have real users, there is no need to implement so many methods, so the interface of the simple version is customized.
 * The definition of each interface method is slightly different than javax.servlet-api.
 *
 * @see javax.servlet.http.HttpServletResponse
 * @see javax.servlet.ServletResponse
 */
public interface MiniHttpServletResponse extends MiniServletResponse {

    /**
     * Adds the specified cookie to the response.  This method can be called
     * multiple times to set more than one cookie.
     *
     * @param cookie the Cookie to return to the client
     */
    void addCookie(Cookie cookie);

    /**
     * Sets a response header with the given name and value.
     * If the header had already been set, the new value overwrites the
     * previous one.  The <code>getHeader</code> method can be
     * used to test for the presence of a header before setting its
     * value.
     *
     * @param name  the name of the header
     * @param value the header value  If it contains octet string,
     *              it should be encoded according to RFC 2047
     *              (http://www.ietf.org/rfc/rfc2047.txt)
     * @see #getHeader
     */
    void setHeader(String name, String value);

    /**
     * Gets the value of the response header with the given name.
     *
     * <p>If a response header with the given name exists and contains
     * multiple values, the value that was added first will be returned.
     *
     * <p>This method considers only response headers set or added via
     * {@link #setHeader}, respectively.
     *
     * @param name the name of the response header whose value to return
     * @return the value of the response header with the given name,
     * or <tt>null</tt> if no header with the given name has been set
     * on this response
     * @since Servlet 3.0
     */
    public String getHeader(String name);

}
