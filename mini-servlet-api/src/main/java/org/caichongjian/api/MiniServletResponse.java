package org.caichongjian.api;

/**
 * javax.servlet-api的ServletResponse需要实现的方法太多了。
 * 简易服务器不会有真实用户，没必要实现那么多方法，所以自定义了简易版的接口。
 * 各个接口方法的定义比起javax.servlet-api来略有不同。
 * <p>
 * There are too many methods that javax.servlet-api's ServletResponse needs to implement.
 * Simple server will not have real users, there is no need to implement so many methods, so the interface of the simple version is customized.
 * The definition of each interface method is slightly different than javax.servlet-api.
 *
 * @see javax.servlet.http.HttpServletResponse
 * @see javax.servlet.ServletResponse
 */
public interface MiniServletResponse {

    /**
     * Returns the content type used for the MIME body
     * sent in this response. The content type proper must
     * have been specified using {@link #setContentType}
     * before the response is committed. If no content type
     * has been specified, this method returns null.
     * If a content type has been specified, and a
     * character encoding has been explicitly or implicitly
     * specified as described in {@link #getCharacterEncoding}
     * or {@link #getWriter} has been called,
     * the charset parameter is included in the string returned.
     * If no character encoding has been specified, the
     * charset parameter is omitted.
     *
     * @return a <code>String</code> specifying the content type,
     * for example, <code>text/html; charset=UTF-8</code>, or null
     *
     * @since Servlet 2.4
     */
    String getContentType();

    /**
     * Sets the content type of the response being sent to
     * the client, if the response has not been committed yet.
     * The given content type may include a character encoding
     * specification, for example, <code>text/html;charset=UTF-8</code>.
     * The response's character encoding is only set from the given
     * content type if this method is called before <code>getWriter</code>
     * is called.
     * <p>This method may be called repeatedly to change content type and
     * character encoding.
     * This method has no effect if called after the response
     * has been committed. It does not set the response's character
     * encoding if it is called after <code>getWriter</code>
     * has been called or after the response has been committed.
     * <p>Containers must communicate the content type and the character
     * encoding used for the servlet response's writer to the client if
     * the protocol provides a way for doing so. In the case of HTTP,
     * the <code>Content-Type</code> header is used.
     *
     * @param type a <code>String</code> specifying the MIME
     * type of the content
     *
     * @see #setLocale
     * @see #setCharacterEncoding
     * @see #getOutputStream
     * @see #getWriter
     *
     */
    void setContentType(String type);
}
