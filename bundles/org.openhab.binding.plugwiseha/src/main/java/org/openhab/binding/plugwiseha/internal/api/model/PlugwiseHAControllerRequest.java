/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.plugwiseha.internal.api.model;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHABadRequestException;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAException;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAForbiddenException;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHATimeoutException;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * The {@link PlugwiseHAControllerRequest} class is a utility class to create
 * API requests to the Plugwise Home Automation controller and to deserialize
 * incoming XML into the appropriate model objects to be used by the {@link
 * PlugwiseHAController}.
 * 
 * @author B. van Wetten - Initial contribution
 */
@NonNullByDefault
public class PlugwiseHAControllerRequest<T> {

    private static final String CONTENT_TYPE_TEXT_XML = MimeTypes.Type.TEXT_XML_8859_1.toString();
    private static final long TIMEOUT_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(PlugwiseHAControllerRequest.class);
    private final XStream xStream;
    private final HttpClient httpClient;
    private final String host;
    private final int port;
    private final Class<T> resultType;
    private final @Nullable Transformer transformer;

    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> queryParameters = new HashMap<>();
    private @Nullable Object bodyParameter;
    private String serverDateTime = "";
    private String path = "/";

    // Constructor

    <X extends XStream> PlugwiseHAControllerRequest(Class<T> resultType, X xStream, @Nullable Transformer transformer,
            HttpClient httpClient, String host, int port, String username, String password) {
        this.resultType = resultType;
        this.xStream = xStream;
        this.transformer = transformer;
        this.httpClient = httpClient;
        this.host = host;
        this.port = port;

        setHeader(HttpHeader.ACCEPT.toString(), CONTENT_TYPE_TEXT_XML);

        // Create Basic Auth header if username and password are supplied
        if (!username.isBlank() && !password.isBlank()) {
            setHeader(HttpHeader.AUTHORIZATION.toString(), "Basic " + Base64.getEncoder()
                    .encodeToString(String.format("%s:%s", username, password).getBytes(StandardCharsets.UTF_8)));
        }
    }

    // Public methods

    public void setPath(String path) {
        this.setPath(path, (HashMap<String, String>) null);
    }

    public void setPath(String path, @Nullable HashMap<String, String> pathParameters) {
        this.path = path;

        if (pathParameters != null) {
            this.path += pathParameters.entrySet().stream().map(Object::toString).collect(Collectors.joining(";"));
        }
    }

    public void setHeader(String key, Object value) {
        this.headers.put(key, String.valueOf(value));
    }

    public void addPathParameter(String key) {
        this.path += String.format(";%s", key);
    }

    public void addPathParameter(String key, Object value) {
        this.path += String.format(";%s=%s", key, value);
    }

    public void addPathFilter(String key, String operator, Object value) {
        this.path += String.format(";%s:%s:%s", key, operator, value);
    }

    public void setQueryParameter(String key, Object value) {
        this.queryParameters.put(key, String.valueOf(value));
    }

    public void setBodyParameter(Object body) {
        this.bodyParameter = body;
    }

    public String getServerDateTime() {
        return this.serverDateTime;
    }

    @SuppressWarnings("unchecked")
    public @Nullable T execute() throws PlugwiseHAException {
        T result;
        String xml = getContent();

        if (String.class.equals(resultType)) {
            if (this.transformer != null) {
                result = (T) this.transformXML(xml);
            } else {
                result = (T) xml;
            }
        } else if (!Void.class.equals(resultType)) {
            if (this.transformer != null) {
                result = (T) this.xStream.fromXML(this.transformXML(xml));
            } else {
                result = (T) this.xStream.fromXML(xml);
            }
        } else {
            return null;
        }

        return result;
    }

    // Protected and private methods

    private String transformXML(String xml) throws PlugwiseHAException {
        StringReader input = new StringReader(xml);
        StringWriter output = new StringWriter();
        Transformer localTransformer = this.transformer;
        if (localTransformer != null) {
            try {
                localTransformer.transform(new StreamSource(input), new StreamResult(output));
            } catch (TransformerException e) {
                logger.debug("Could not apply XML stylesheet", e);
                throw new PlugwiseHAException("Could not apply XML stylesheet", e);
            }
        } else {
            throw new PlugwiseHAException("Could not transform XML stylesheet, the transformer is null");
        }

        return output.toString();
    }

    private String getContent() throws PlugwiseHAException {
        String content;
        ContentResponse response;

        try {
            response = getContentResponse();
        } catch (PlugwiseHATimeoutException e) {
            // Retry
            response = getContentResponse();
        }

        int status = response.getStatus();
        switch (status) {
            case HttpStatus.OK_200:
            case HttpStatus.ACCEPTED_202:
                content = response.getContentAsString();
                if (logger.isTraceEnabled()) {
                    logger.trace("<< {} {} \n{}", status, HttpStatus.getMessage(status), content);
                }
                break;
            case HttpStatus.BAD_REQUEST_400:
                throw new PlugwiseHABadRequestException("Bad request");
            case HttpStatus.UNAUTHORIZED_401:
                throw new PlugwiseHAUnauthorizedException("Unauthorized");
            case HttpStatus.FORBIDDEN_403:
                throw new PlugwiseHAForbiddenException("Forbidden");
            default:
                throw new PlugwiseHAException("Unknown HTTP status code " + status + " returned by the controller");
        }

        this.serverDateTime = response.getHeaders().get("Date");

        return content;
    }

    private ContentResponse getContentResponse() throws PlugwiseHAException {
        Request request = newRequest();
        ContentResponse response;

        if (logger.isTraceEnabled()) {
            logger.trace(">> {} {}", request.getMethod(), request.getURI());
        }

        try {
            response = request.send();
        } catch (TimeoutException | InterruptedException e) {
            throw new PlugwiseHATimeoutException(e);
        } catch (ExecutionException e) {
            // Unwrap the cause and try to cleanly handle it
            Throwable cause = e.getCause();
            if (cause instanceof UnknownHostException) {
                // Invalid hostname
                throw new PlugwiseHAException(cause);
            } else if (cause instanceof ConnectException) {
                // Cannot connect
                throw new PlugwiseHAException(cause);
            } else if (cause instanceof SocketTimeoutException) {
                throw new PlugwiseHATimeoutException(cause);
            } else if (cause == null) {
                // Unable to unwrap
                throw new PlugwiseHAException(e);
            } else {
                // Catch all
                throw new PlugwiseHAException(cause);
            }
        }
        return response;
    }

    private Request newRequest() {
        HttpMethod method = bodyParameter == null ? HttpMethod.GET : HttpMethod.PUT;
        HttpURI uri = new HttpURI(HttpScheme.HTTP.asString(), this.host, this.port, this.path);
        Request request = httpClient.newRequest(uri.toString()).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .method(method);

        for (Entry<String, String> entry : this.headers.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }

        for (Entry<String, String> entry : this.queryParameters.entrySet()) {
            request.param(entry.getKey(), entry.getValue());
        }

        if (this.bodyParameter != null) {
            String xmlBody = getRequestBodyAsXml();
            ContentProvider content = new StringContentProvider(CONTENT_TYPE_TEXT_XML, xmlBody, StandardCharsets.UTF_8);
            request = request.content(content);
        }
        return request;
    }

    private String getRequestBodyAsXml() {
        return this.xStream.toXML(this.bodyParameter);
    }
}
