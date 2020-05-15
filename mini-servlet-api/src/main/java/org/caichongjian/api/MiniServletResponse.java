/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates and others.
 * All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.caichongjian.api;

/**
 * jakarta.servlet-api的ServletResponse需要实现的方法太多了。
 * 简易服务器不会有真实用户，没必要实现那么多方法，所以自定义了简易版的接口。
 * 接口方法的数量比起jakarta.servlet-api来少了很多。
 * 如果你觉得adam-server对你有帮助，请去下面的链接点★Star
 * <p>
 * There are too many methods that jakarta.servlet-api's ServletResponse needs to implement.
 * Simple server will not have real users, there is no need to implement so many methods, so the interface of the simple version is customized.
 * The number of interface methods is much less than jakarta.servlet-api.
 * If you think adam-server is helpful to you, please go to the link below and click ★Star
 *
 * @author Various
 *
 * @see jakarta.servlet.http.HttpServletResponse
 * @see jakarta.servlet.ServletResponse
 * @see https://github.com/oracle
 * @see https://github.com/eclipse-ee4j
 * @see https://github.com/javaee/servlet-spec
 * @see https://github.com/eclipse-ee4j/servlet-api
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
