/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class HttpRequest.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class HttpRequest implements AutoCloseable {

    /** The logger. */
    // Logger
    private final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    /** The client. */
    private final Client _client;

    /** The headers. */
    private final Map<String, String> headers = new HashMap<String, String>();

    /** The device name. */
    private final String deviceName;

    /** The device id. */
    private final String deviceId;

    /**
     * Instantiates a new http request.
     */
    public HttpRequest() {
        this(null, null);
    }

    /**
     * Instantiates a new http request.
     *
     * @param deviceName the device name
     * @param deviceId the device id
     */
    public HttpRequest(String deviceName, String deviceId) {
        _client = ClientBuilder.newClient();

        _client.register(new Slf4jLoggingFilter(logger, true));

        this.deviceId = deviceId == null ? NetUtilities.getDeviceId() : deviceId;
        this.deviceName = deviceName == null ? NetUtilities.getDeviceName(this.deviceId) : deviceName;
    }

    /**
     * Gets the device name.
     *
     * @return the device name
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Gets the device id.
     *
     * @return the device id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Register.
     *
     * @param obj the obj
     */
    public void register(Object obj) {
        _client.register(obj);
    }

    /**
     * Send get command.
     *
     * @param uri the uri
     * @param tempHeaders the temp headers
     * @return the http response
     */
    public HttpResponse sendGetCommand(String uri, Header... tempHeaders) {
        try {
            Builder rqst = _client.target(uri).request();

            for (Entry<String, String> h : headers.entrySet()) {
                rqst = rqst.header(h.getKey(), h.getValue());
            }

            if (tempHeaders != null) {
                for (Header hdr : tempHeaders) {
                    rqst = rqst.header(hdr.getName(), hdr.getValue());
                }
            }

            final Response content = rqst.get();

            try {
                final HttpResponse httpResponse = new HttpResponse(content);
                return httpResponse;
            } finally {
                content.close();
            }
        } catch (IOException e) {
            return new HttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, e.getMessage());
        }

    }

    /**
     * Send post xml command.
     *
     * @param uri the uri
     * @param body the body
     * @param tempHeaders the temp headers
     * @return the http response
     */
    public HttpResponse sendPostXmlCommand(String uri, String body, Header... tempHeaders) {
        return sendPostCommand(uri, body, MediaType.TEXT_XML + ";charset=utf-8", tempHeaders);
    }

    /**
     * Send post json command.
     *
     * @param uri the uri
     * @param json the json
     * @return the http response
     */
    public HttpResponse sendPostJsonCommand(String uri, String json) {
        return sendPostJsonCommand(uri, json, new Header[0]);
    }

    /**
     * Send post json command.
     *
     * @param uri the uri
     * @param body the body
     * @param tempHeaders the temp headers
     * @return the http response
     */
    public HttpResponse sendPostJsonCommand(String uri, String body, Header... tempHeaders) {
        return sendPostCommand(uri, body, MediaType.APPLICATION_JSON, tempHeaders);
    }

    /**
     * Send post command.
     *
     * @param uri the uri
     * @param body the body
     * @param mediaType the media type
     * @param tempHeaders the temp headers
     * @return the http response
     */
    public HttpResponse sendPostCommand(String uri, String body, String mediaType, Header... tempHeaders) {
        try {
            Builder rqst = _client.target(uri).request(mediaType);

            for (Entry<String, String> h : headers.entrySet()) {
                rqst = rqst.header(h.getKey(), h.getValue());
            }

            if (tempHeaders != null) {
                for (Header hdr : tempHeaders) {
                    rqst = rqst.header(hdr.getName(), hdr.getValue());
                }
            }

            final Response content = rqst.post(Entity.entity(body == null ? "" : body, mediaType));

            try {
                final HttpResponse httpResponse = new HttpResponse(content);
                return httpResponse;
            } finally {
                content.close();
            }
        } catch (IOException e) {
            return new HttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, e.getMessage());
        }

    }

    /**
     * Send delete command.
     *
     * @param uri the uri
     * @param body the body
     * @param tempHeaders the temp headers
     * @return the http response
     */
    public HttpResponse sendDeleteCommand(String uri, String body, Header... tempHeaders) {
        try {
            Builder rqst = _client.target(uri).request();

            for (Entry<String, String> h : headers.entrySet()) {
                rqst = rqst.header(h.getKey(), h.getValue());
            }

            if (tempHeaders != null) {
                for (Header hdr : tempHeaders) {
                    rqst = rqst.header(hdr.getName(), hdr.getValue());
                }
            }

            final Response content = rqst.delete();

            try {
                final HttpResponse httpResponse = new HttpResponse(content);
                return httpResponse;
            } finally {
                content.close();
            }
        } catch (IOException e) {
            return new HttpResponse(HttpStatus.SC_SERVICE_UNAVAILABLE, e.getMessage());
        }

    }

    /**
     * Adds the header.
     *
     * @param id the id
     * @param value the value
     */
    public void addHeader(String id, String value) {
        for (int i = headers.size() - 1; i >= 0; i--) {
            if (headers.containsKey(id)) {
                headers.remove(i);
            }

        }
        headers.put(id, value);
    }

    /**
     * Gets the client.
     *
     * @return the client
     */
    public Client getClient() {
        return _client;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        _client.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    public void finalize() {
        try {
            close();
        } catch (Exception e) {
            logger.debug("Exception while closing httpclient: {}", e);
        }
    }

}
