/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.api;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RachioImageServlet} Rachio sometimes returns an incorrect media type for images. This servlet is a work around
 * for that. It rewrites the image url to point to the binding and adds the correct media type before returning the url
 * to the channel/item.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = {}, configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
public class RachioImageServlet extends HttpServlet {
    private static final long serialVersionUID = 8706067059503685993L;
    private static final String HTTP_METHOD_GET = "GET";
    private final Logger logger = LoggerFactory.getLogger(RachioImageServlet.class);

    private final Object registrationLock = new Object();
    private final HttpClient httpClient;
    private @Nullable HttpService httpService;
    private boolean servletRegistered;

    @Activate
    public RachioImageServlet(@Reference HttpClientFactory httpClientFactory) {
        this(httpClientFactory.getCommonHttpClient());
    }

    RachioImageServlet(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * OSGi HttpService bind callback.
     *
     * @param httpService the HTTP service used for manual servlet registration
     */
    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    protected void bindHttpService(HttpService httpService) {
        synchronized (registrationLock) {
            if (Objects.equals(this.httpService, httpService) && servletRegistered) {
                logger.debug("RachioImage: Image servlet already registered at {}, skipping duplicate bind",
                        SERVLET_IMAGE_PATH);
                return;
            }
            if (servletRegistered) {
                unregisterServletLocked();
            }

            this.httpService = httpService;
            registerServletLocked(httpService);
        }
    }

    protected void unbindHttpService(HttpService httpService) {
        synchronized (registrationLock) {
            if (!Objects.equals(this.httpService, httpService)) {
                logger.debug("RachioImage: Ignoring HttpService unbind for non-current service");
                return;
            }

            unregisterServletLocked();
            this.httpService = null;
        }
    }

    /**
     * OSGi deactivation callback.
     */
    @Deactivate
    protected void deactivate() {
        synchronized (registrationLock) {
            unregisterServletLocked();
            httpService = null;
        }
    }

    private void registerServletLocked(HttpService httpService) {
        try {
            logger.debug("RachioImage: Registering image servlet alias {}", SERVLET_IMAGE_PATH);
            httpService.registerServlet(SERVLET_IMAGE_PATH, this, null, httpService.createDefaultHttpContext());
            servletRegistered = true;
        } catch (ServletException | NamespaceException e) {
            servletRegistered = false;
            logger.warn("RachioImage: Could not register image servlet alias {}: {}", SERVLET_IMAGE_PATH,
                    e.getMessage());
        }
    }

    private void unregisterServletLocked() {
        HttpService currentHttpService = httpService;
        if (!servletRegistered || currentHttpService == null) {
            return;
        }

        try {
            logger.debug("RachioImage: Unregistering image servlet alias {}", SERVLET_IMAGE_PATH);
            currentHttpService.unregister(SERVLET_IMAGE_PATH);
        } catch (IllegalArgumentException e) {
            logger.debug("RachioImage: Image servlet alias {} was already unregistered", SERVLET_IMAGE_PATH);
        } finally {
            servletRegistered = false;
        }
    }

    @Override
    protected void service(@Nullable HttpServletRequest request, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (request == null || resp == null) {
            return;
        }

        setHeaders(resp);
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        String requestUri = request.getRequestURI();
        logger.trace("RachioImage: Request from {}:{}{} ({}:{}, {})", ipAddress, request.getRemotePort(), requestUri,
                request.getRemoteHost(), request.getServerPort(), request.getProtocol());

        if (requestUri == null || requestUri.length() <= SERVLET_IMAGE_PATH.length()
                || !requestUri.regionMatches(true, 0, SERVLET_IMAGE_PATH, 0, SERVLET_IMAGE_PATH.length())) {
            logger.debug("RachioImage: Invalid request received - path = {}", requestUri);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (requestUri.charAt(SERVLET_IMAGE_PATH.length()) != '/') {
            logger.debug("RachioImage: Invalid request received - path = {}", requestUri);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!HTTP_METHOD_GET.equalsIgnoreCase(request.getMethod())) {
            logger.debug("RachioImage: Unexpected method='{}'", request.getMethod());
            resp.setHeader("Allow", HTTP_METHOD_GET);
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        String uri = requestUri.substring(requestUri.lastIndexOf("/") + 1);
        if (uri.isBlank()) {
            logger.debug("RachioImage: Invalid image request path = {}", requestUri);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String imageUrl = SERVLET_IMAGE_URL_BASE + uri;
        logger.debug("RachioImage: {} image '{}'", request.getMethod(), uri);
        Request imageRequest = httpClient.newRequest(imageUrl).method(HTTP_METHOD_GET)
                .timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .header(HttpHeader.USER_AGENT, SERVLET_WEBHOOK_USER_AGENT);
        InputStreamResponseListener listener = new InputStreamResponseListener();
        try {
            imageRequest.send(listener);
            Response imageResponse = listener.get(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            int status = imageResponse.getStatus();
            if (status < HttpStatus.OK_200 || status >= HttpStatus.MULTIPLE_CHOICES_300) {
                throw new IOException("upstream HTTP status " + status);
            }

            InputStream reader = listener.getInputStream();
            OutputStream writer = resp.getOutputStream();
            try (reader) {
                reader.transferTo(writer);
            }
            writer.flush();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            imageRequest.abort(e);
            sendBadGateway(resp, uri, e);
        } catch (ExecutionException | TimeoutException | IOException e) {
            imageRequest.abort(e);
            sendBadGateway(resp, uri, e);
        }
    }

    private void sendBadGateway(HttpServletResponse response, String imageId, Exception e) throws IOException {
        logger.debug("RachioImage: Unable to fetch image '{}': {}", imageId, e.getMessage());
        if (!response.isCommitted()) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
        }
    }

    private void setHeaders(HttpServletResponse response) {
        response.setContentType(SERVLET_IMAGE_MIME_TYPE);
        response.setHeader("Access-Control-Allow-Origin", "*");
    }
}
