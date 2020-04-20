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

import java.util.Enumeration;
import java.util.Map;

/**
 * javax.servlet-api的ServletRequest需要实现的方法太多了。
 * 简易服务器不会有真实用户，没必要实现那么多方法，所以自定义了简易版的接口。
 * 接口方法的数量比起javax.servlet-api来少了很多。
 * 如果你觉得adam-server对你有帮助，请去下面的链接点★Star
 * <p>
 * There are too many methods that javax.servlet-api's ServletRequest needs to implement.
 * Simple server will not have real users, there is no need to implement so many methods, so the interface of the simple version is customized.
 * The number of interface methods is much less than javax.servlet-api.
 * If you think adam-server is helpful to you, please go to the link below and click ★Star
 *
 * @author Various
 *
 * @see javax.servlet.http.HttpServletRequest
 * @see javax.servlet.ServletRequest
 * @see https://github.com/oracle
 * @see https://github.com/javaee/servlet-spec
 * @see https://github.com/eclipse-ee4j/servlet-api
 */
public interface MiniServletRequest {

    /**
     * Returns the length, in bytes, of the request body and made available by
     * the input stream, or -1 if the length is not known ir is greater than
     * Integer.MAX_VALUE. For HTTP servlets,
     * same as the value of the CGI variable CONTENT_LENGTH.
     *
     * @return an integer containing the length of the request body or -1 if
     * the length is not known or is greater than Integer.MAX_VALUE.
     */
    int getContentLength();

    /**
     * Returns the MIME type of the body of the request, or
     * <code>null</code> if the type is not known. For HTTP servlets,
     * same as the value of the CGI variable CONTENT_TYPE.
     *
     * @return a <code>String</code> containing the name of the MIME type
     * of the request, or null if the type is not known
     */
    String getContentType();

    /**
     * Returns the value of a request parameter as a <code>String</code>,
     * or <code>null</code> if the parameter does not exist. Request parameters
     * are extra information sent with the request.  For HTTP servlets,
     * parameters are contained in the query string or posted form data.
     *
     * <p>You should only use this method when you are sure the
     * parameter has only one value. If the parameter might have
     * more than one value, use {@link #getParameterValues}.
     *
     * <p>If you use this method with a multivalued
     * parameter, the value returned is equal to the first value
     * in the array returned by <code>getParameterValues</code>.
     *
     * <p>If the parameter data was sent in the request body, such as occurs
     * with an HTTP POST request, then reading the body directly via {@link
     * #getInputStream} or {@link #getReader} can interfere
     * with the execution of this method.
     *
     * @param name a <code>String</code> specifying the name of the parameter
     *
     * @return a <code>String</code> representing the single value of
     * the parameter
     *
     * @see #getParameterValues
     */
    String getParameter(String name);

    /**
     *
     * Returns an <code>Enumeration</code> of <code>String</code>
     * objects containing the names of the parameters contained
     * in this request. If the request has
     * no parameters, the method returns an empty <code>Enumeration</code>.
     *
     * @return an <code>Enumeration</code> of <code>String</code>
     * objects, each <code>String</code> containing the name of
     * a request parameter; or an empty <code>Enumeration</code>
     * if the request has no parameters
     */
    Enumeration<String> getParameterNames();

    /**
     * Returns an array of <code>String</code> objects containing
     * all of the values the given request parameter has, or
     * <code>null</code> if the parameter does not exist.
     *
     * <p>If the parameter has a single value, the array has a length
     * of 1.
     *
     * @param name a <code>String</code> containing the name of
     * the parameter whose value is requested
     *
     * @return an array of <code>String</code> objects
     * containing the parameter's values
     *
     * @see #getParameter
     */
    String[] getParameterValues(String name);

    /**
     * Returns a java.util.Map of the parameters of this request.
     *
     * <p>Request parameters are extra information sent with the request.
     * For HTTP servlets, parameters are contained in the query string or
     * posted form data.
     *
     * @return an immutable java.util.Map containing parameter names as
     * keys and parameter values as map values. The keys in the parameter
     * map are of type String. The values in the parameter map are of type
     * String array.
     */
    Map<String, String[]> getParameterMap();
}
