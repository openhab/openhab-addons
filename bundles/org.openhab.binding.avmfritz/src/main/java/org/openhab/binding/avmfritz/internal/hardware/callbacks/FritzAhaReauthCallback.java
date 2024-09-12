/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.avmfritz.internal.hardware.callbacks;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;

/**
 * Callback implementation for reauthorization and retry
 *
 * @author Robert Bausdorf, Christian Brauers - Initial contribution
 */
@NonNullByDefault
public class FritzAhaReauthCallback implements FritzAhaCallback {

    public static final String WEBSERVICE_PATH = "webservices/homeautoswitch.lua";
    /**
     * Path to HTTP interface
     */
    private final String path;
    /**
     * Arguments to use
     */
    private final String args;
    /**
     * Web interface to use
     */
    private final FritzAhaWebInterface webIface;
    /**
     * Method used
     */
    private final HttpMethod httpMethod;
    /**
     * Number of remaining retries
     */
    private int retries;
    /**
     * Callback to execute on next retry
     */
    private FritzAhaCallback retryCallback;
    /**
     * Whether the request returned a valid response
     */
    private boolean validRequest;

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
     * Sets different Callback to use on retry (initial value: same callback after decremented retry counter)
     *
     * @param retryCallback Callback to retry with
     */
    public void setRetryCallback(FritzAhaCallback retryCallback) {
        this.retryCallback = retryCallback;
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
                switch (httpMethod) {
                    case GET:
                        webIface.asyncGet(path, args, retryCallback);
                        break;
                    case POST:
                        webIface.asyncPost(path, args, retryCallback);
                        break;
                    default:
                        break;
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
     *            Path to HTTP interface
     * @param args
     *            Arguments to use
     * @param webIface
     *            Web interface to use
     * @param httpMethod
     *            Method used
     * @param retries
     *            Number of retries
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
