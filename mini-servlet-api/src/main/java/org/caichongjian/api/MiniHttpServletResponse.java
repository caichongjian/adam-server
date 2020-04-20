/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.servlet.http.Cookie;

/**
 * javax.servlet-api的HttpServletResponse需要实现的方法太多了。
 * 简易服务器不会有真实用户，没必要实现那么多方法，所以自定义了简易版的接口。
 * 接口方法的数量比起javax.servlet-api来少了很多。
 * 如果你觉得adam-server对你有帮助，请去下面的链接点★Star
 * <p>
 * There are too many methods that javax.servlet-api's HttpServletResponse needs to implement.
 * Simple server will not have real users, there is no need to implement so many methods, so the interface of the simple version is customized.
 * The number of interface methods is much less than javax.servlet-api.
 * If you think adam-server is helpful to you, please go to the link below and click ★Star
 *
 * @author	Various
 *
 * @see javax.servlet.http.HttpServletResponse
 * @see javax.servlet.ServletResponse
 * @see https://github.com/oracle
 * @see https://github.com/javaee/servlet-spec
 * @see https://github.com/eclipse-ee4j/servlet-api
 */
public interface MiniHttpServletResponse extends MiniServletResponse {

    /**
     * Adds the specified cookie to the response.  This method can be called
     * multiple times to set more than one cookie.
     *
     * @param cookie the Cookie to return to the client
     *
     */
    void addCookie(Cookie cookie);

    /**
     *
     * Sets a response header with the given name and value.
     * If the header had already been set, the new value overwrites the
     * previous one.  The <code>containsHeader</code> method can be
     * used to test for the presence of a header before setting its
     * value.
     *
     * @param	name	the name of the header
     * @param	value	the header value  If it contains octet string,
     *		it should be encoded according to RFC 2047
     *		(http://www.ietf.org/rfc/rfc2047.txt)
     *
     * @see #containsHeader
     * @see #addHeader
     */
    void setHeader(String name, String value);

    /**
     * Gets the value of the response header with the given name.
     *
     * <p>If a response header with the given name exists and contains
     * multiple values, the value that was added first will be returned.
     *
     * <p>This method considers only response headers set or added via
     * {@link #setHeader}, {@link #addHeader}, {@link #setDateHeader},
     * {@link #addDateHeader}, {@link #setIntHeader}, or
     * {@link #addIntHeader}, respectively.
     *
     * @param name the name of the response header whose value to return
     *
     * @return the value of the response header with the given name,
     * or <tt>null</tt> if no header with the given name has been set
     * on this response
     *
     * @since Servlet 3.0
     */
    String getHeader(String name);

}
