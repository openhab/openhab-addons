/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.km200.internal;

import java.io.IOException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

/**
 * The KM200Comm class does the communication to the device and does any encryption/decryption/converting jobs
 *
 * @author Markus Eckhardt - Initial contribution
 */
public class KM200Comm<KM200BindingProvider> {

    private final Logger logger = LoggerFactory.getLogger(KM200Comm.class);
    private HttpClient client;
    private final KM200Device remoteDevice;
    private Integer maxNbrRepeats;

    public KM200Comm(KM200Device device) {
        this.remoteDevice = device;
        maxNbrRepeats = Integer.valueOf(10);
        if (client == null) {
            client = new HttpClient();
        }
    }

    /**
     * This function sets the maximum number of repeats.
     *
     */
    public void setMaxNbrRepeats(Integer maxNbrRepeats) {
        this.maxNbrRepeats = maxNbrRepeats;
    }

    /**
     * This function does the GET http communication to the device
     *
     */
    public byte[] getDataFromService(String service) {
        byte[] responseBodyB64 = null;
        int statusCode = 0;
        GetMethod method = null;
        // Create an instance of HttpClient.
        synchronized (client) {
            logger.debug("Starting receive connection...");
            try {
                for (int i = 0; i < maxNbrRepeats.intValue() && statusCode != HttpStatus.SC_OK; i++) {
                    // Create a method instance.
                    method = new GetMethod("http://" + remoteDevice.getIP4Address() + service);

                    // Provide custom retry handler is necessary
                    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                            new DefaultHttpMethodRetryHandler(3, false));
                    // Set the right header
                    method.setRequestHeader("Accept", "application/json");
                    method.addRequestHeader("User-Agent", "TeleHeater/2.2.3");
                    // Execute the method.
                    statusCode = client.executeMethod(method);
                    // Release the connection.
                    switch (statusCode) {
                        case HttpStatus.SC_OK:
                            remoteDevice.setCharSet(method.getResponseCharSet());
                            responseBodyB64 = ByteStreams.toByteArray(method.getResponseBodyAsStream());
                            break;
                        case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                            /* Unknown problem with the device, wait and try again */
                            method.releaseConnection();
                            logger.debug("HTTP GET failed: 500, internal server error, repeating.. ");
                            Thread.sleep(100L * i + 1);
                            continue;
                        case HttpStatus.SC_FORBIDDEN:
                            /* Service is available but not readable, return a byte array with a size of 1 as code */
                            byte[] serviceIsProtected = new byte[1];
                            responseBodyB64 = serviceIsProtected;
                            break;
                        case HttpStatus.SC_NOT_FOUND:
                            /* Should only happen on discovery */
                            method.releaseConnection();
                            responseBodyB64 = null;
                            break;
                        default:
                            logger.debug("HTTP GET failed: {}", method.getStatusLine());
                            method.releaseConnection();
                            responseBodyB64 = null;
                            break;
                    }
                }
            } catch (HttpException e) {
                logger.debug("Fatal protocol violation: {}", e.getMessage());
            } catch (InterruptedException e) {
                logger.debug("Sleep was interrupted: {}", e.getMessage());
            } catch (IOException e) {
                logger.debug("Fatal transport error: {}", e.getMessage());
            } finally {
                // Release the connection.
                if (method != null) {
                    method.releaseConnection();
                }
            }
            return responseBodyB64;
        }
    }

    /**
     * This function does the SEND http communication to the device
     *
     */
    public Integer sendDataToService(String service, byte[] data) {
        // Create an instance of HttpClient.
        int rCode = 0;
        PostMethod method = null;
        synchronized (client) {
            logger.debug("Starting send connection...");
            try {
                for (int i = 0; i < maxNbrRepeats.intValue() && rCode != HttpStatus.SC_NO_CONTENT; i++) {
                    // Create a method instance.
                    method = new PostMethod("http://" + remoteDevice.getIP4Address() + service);
                    // Provide custom retry handler is necessary
                    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                            new DefaultHttpMethodRetryHandler(3, false));
                    // Set the right header
                    method.setRequestHeader("Accept", "application/json");
                    method.addRequestHeader("User-Agent", "TeleHeater/2.2.3");
                    method.setRequestEntity(new ByteArrayRequestEntity(data));
                    rCode = client.executeMethod(method);
                    method.releaseConnection();
                    switch (rCode) {
                        case HttpStatus.SC_NO_CONTENT: // The default return value
                            break;
                        case HttpStatus.SC_LOCKED:
                            /* Unknown problem with the device, wait and try again */
                            logger.debug("HTTP POST failed: 423, locked, repeating.. ");
                            Thread.sleep(1000L * i + 1);
                            continue;
                        default:
                            logger.debug("HTTP POST failed: {}", method.getStatusLine());
                            rCode = 0;
                            break;
                    }
                }
            } catch (IOException e) {
                logger.debug("Failed to send data {}", e);
            } catch (InterruptedException e) {
                logger.debug("Sleep was interrupted: {}", e.getMessage());
            } finally {
                // Release the connection.
                if (method != null) {
                    method.releaseConnection();
                }
            }
            logger.debug("Returncode: {}", rCode);
            return rCode;
        }
    }
}
