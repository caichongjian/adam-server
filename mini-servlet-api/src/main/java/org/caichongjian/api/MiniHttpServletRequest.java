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
import java.util.Enumeration;

/**
 * javax.servlet-api的HttpServletRequest需要实现的方法太多了。
 * 简易服务器不会有真实用户，没必要实现那么多方法，所以自定义了简易版的接口。
 * 接口方法的数量比起javax.servlet-api来少了很多。
 * 如果你觉得adam-server对你有帮助，请去下面的链接点★Star
 * <p>
 * There are too many methods that javax.servlet-api's HttpServletRequest needs to implement.
 * Simple server will not have real users, there is no need to implement so many methods, so the interface of the simple version is customized.
 * The number of interface methods is much less than javax.servlet-api.
 * If you think adam-server is helpful to you, please go to the link below and click ★Star
 *
 * @author 	Various
 *
 * @see javax.servlet.http.HttpServletRequest
 * @see javax.servlet.ServletRequest
 * @see https://github.com/oracle
 * @see https://github.com/javaee/servlet-spec
 * @see https://github.com/eclipse-ee4j/servlet-api
 */
public interface MiniHttpServletRequest extends MiniServletRequest {

    /**
     * Returns an array containing all of the <code>Cookie</code>
     * objects the client sent with this request.
     * This method returns <code>null</code> if no cookies were sent.
     *
     * @return		an array of all the <code>Cookies</code>
     *			included with this request, or <code>null</code>
     *			if the request has no cookies
     */
    Cookie[] getCookies();

    /**
     * Returns the value of the specified request header
     * as a <code>String</code>. If the request did not include a header
     * of the specified name, this method returns <code>null</code>.
     * If there are multiple headers with the same name, this method
     * returns the first head in the request.
     * The header name is case insensitive. You can use
     * this method with any request header.
     *
     * @param name		a <code>String</code> specifying the
     *				header name
     *
     * @return			a <code>String</code> containing the
     *				value of the requested
     *				header, or <code>null</code>
     *				if the request does not
     *				have a header of that name
     */
    String getHeader(String name);

    /**
     * Returns an enumeration of all the header names
     * this request contains. If the request has no
     * headers, this method returns an empty enumeration.
     *
     * <p>Some servlet containers do not allow
     * servlets to access headers using this method, in
     * which case this method returns <code>null</code>
     *
     * @return			an enumeration of all the
     *				header names sent with this
     *				request; if the request has
     *				no headers, an empty enumeration;
     *				if the servlet container does not
     *				allow servlets to use this method,
     *				<code>null</code>
     */
    Enumeration<String> getHeaderNames();

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

    /**
     * Returns the query string that is contained in the request
     * URL after the path. This method returns <code>null</code>
     * if the URL does not have a query string. Same as the value
     * of the CGI variable QUERY_STRING.
     *
     * @return		a <code>String</code> containing the query
     *			string or <code>null</code> if the URL
     *			contains no query string. The value is not
     *			decoded by the container.
     */
    String getQueryString();

    /**
     * Returns the part of this request's URL from the protocol
     * name up to the query string in the first line of the HTTP request.
     * The web container does not decode this String.
     * For example:
     *
     * <table summary="Examples of Returned Values">
     * <tr align=left><th>First line of HTTP request      </th>
     * <th>     Returned Value</th>
     * <tr><td>POST /some/path.html HTTP/1.1<td><td>/some/path.html
     * <tr><td>GET http://foo.bar/a.html HTTP/1.0
     * <td><td>/a.html
     * <tr><td>HEAD /xyz?a=b HTTP/1.1<td><td>/xyz
     * </table>
     *
     * <p>To reconstruct an URL with a scheme and host, use
     * {@link HttpUtils#getRequestURL}.
     *
     * @return		a <code>String</code> containing
     *			the part of the URL from the
     *			protocol name up to the query string
     *
     * @see     HttpUtils#getRequestURL
     */
    String getRequestURI();
}
