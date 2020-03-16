package org.caichongjian.api;

import java.util.Enumeration;
import java.util.Map;

/**
 * javax.servlet-api的ServletRequest需要实现的方法太多了，简易服务器小国寡民，自定义简易版的接口
 */
public interface MiniServletRequest {

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
