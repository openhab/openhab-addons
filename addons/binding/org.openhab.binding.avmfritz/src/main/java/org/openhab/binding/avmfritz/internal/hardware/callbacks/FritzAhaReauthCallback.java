/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.hardware.callbacks;

import static org.eclipse.jetty.http.HttpMethod.*;

import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;

/**
 * Callback implementation for reauthorization and retry
 *
 * @author Robert Bausdorf, Christian Brauers - Initial contribution
 */
public class FritzAhaReauthCallback implements FritzAhaCallback {

    public static final String WEBSERVICE_PATH = "webservices/homeautoswitch.lua";
    /**
     * Path to HTTP interface
     */
    private String path;
    /**
     * Arguments to use
     */
    private String args;
    /**
     * Web interface to use
     */
    private FritzAhaWebInterface webIface;
    /**
     * Method used
     */
    private HttpMethod httpMethod;
    /**
     * Number of remaining retries
     */
    private int retries;
    /**
     * Whether the request returned a valid response
     */
    private boolean validRequest;
    /**
     * Callback to execute on next retry
     */
    private FritzAhaCallback retryCallback;

    /**
     * Returns whether the request returned a valid response
     *
     * @return Validity of response
     */
    public boolean isValidRequest() {
        return validRequest;
    }

    /**
     * Returns whether there will be another retry on an invalid response
     *
     * @return true if there will be no more retries, false otherwise
     */
    public boolean isFinalAttempt() {
        return retries <= 0;
    }

    /**
     * Sets different Callback to use on retry (initial value: same callback
     * after decremented retry counter)
     *
     * @param newRetryCallback
     *                             Callback to retry with
     */
    public void setRetryCallback(FritzAhaCallback newRetryCallback) {
        retryCallback = newRetryCallback;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getArgs() {
        return args;
    }

    public FritzAhaWebInterface getWebIface() {
        return webIface;
    }

    @Override
    public void execute(int status, String response) {
        if (status != 200 || "".equals(response) || ".".equals(response)) {
            validRequest = false;
            if (retries >= 1) {
                webIface.authenticate();
                retries--;
                if (httpMethod == GET) {
                    webIface.asyncGet(path, args, retryCallback);
                } else if (httpMethod == POST) {
                    webIface.asyncPost(path, args, retryCallback);
                }
            }
        } else {
            validRequest = true;
        }
    }

    /**
     * Constructor for retryable authentication
     *
     * @param path
     *                       Path to HTTP interface
     * @param args
     *                       Arguments to use
     * @param webIface
     *                       Web interface to use
     * @param httpMethod
     *                       Method used
     * @param retries
     *                       Number of retries
     */
    public FritzAhaReauthCallback(String path, String args, FritzAhaWebInterface webIface, HttpMethod httpMethod,
            int retries) {
        this.path = path;
        this.args = args;
        this.webIface = webIface;
        this.httpMethod = httpMethod;
        this.retries = retries;
        retryCallback = this;
    }
}
