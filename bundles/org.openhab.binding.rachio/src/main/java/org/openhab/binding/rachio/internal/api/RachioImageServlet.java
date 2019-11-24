/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
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
@Component(service = HttpServlet.class, configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
public class RachioImageServlet extends HttpServlet {
    private static final long serialVersionUID = 8706067059503685993L;
    private final Logger logger = LoggerFactory.getLogger(RachioImageServlet.class);

    @Nullable
    private HttpService httpService;
    @SuppressWarnings("unused")
    @Nullable
    private RachioHandlerFactory rachioHandlerFactory;

    /**
     * OSGi activation callback.
     *
     * @param config Service config.
     */
    @Activate
    @SuppressWarnings("null")
    protected void activate(Map<String, Object> config) {
        try {
            Validate.notNull(httpService);
            httpService.registerServlet(SERVLET_IMAGE_PATH, this, null, httpService.createDefaultHttpContext());
            logger.debug("Started RachioImage servlet at {}", SERVLET_IMAGE_PATH);
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start RachioImage servlet: {}", e.getMessage(), e);
        }
    }

    /**
     * OSGi deactivation callback.
     */
    @Deactivate
    @SuppressWarnings("null")
    protected void deactivate() {
        Validate.notNull(httpService);
        httpService.unregister(SERVLET_IMAGE_PATH);
        logger.debug("RachioImage: Servlet stopped");
    }

    @SuppressWarnings("null")
    @Override
    protected void service(@Nullable HttpServletRequest request, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        InputStream reader = null;
        OutputStream writer = null;
        try {
            String ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            String path = request.getRequestURI().substring(0, SERVLET_IMAGE_PATH.length());
            logger.trace("RachioImage: Reqeust from {}:{}{} ({}:{}, {})", ipAddress, request.getRemotePort(), path,
                    request.getRemoteHost(), request.getServerPort(), request.getProtocol());
            if (!request.getMethod().equalsIgnoreCase(HttpMethod.GET)) {
                logger.warn("RachioImage: Unexpected method='{}'", request.getMethod());
            }
            if (!path.equalsIgnoreCase(SERVLET_IMAGE_PATH)) {
                logger.warn("RachioImage: Invalid request received - path = {}", path);
                return;
            }

            String uri = request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1);
            String imageUrl = SERVLET_IMAGE_URL_BASE + uri;
            logger.debug("RachioImage: {} image '{}' from '{}'", request.getMethod(), uri, imageUrl);
            setHeaders(resp);
            URL url = new URL(imageUrl);
            URLConnection conn = url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            reader = conn.getInputStream();
            writer = resp.getOutputStream();

            // read data in 4k chunks
            byte[] data = new byte[4096];
            int n;
            while (((n = reader.read(data)) != -1)) {
                writer.write(data, 0, n);
            }
        } catch (RuntimeException e) {
            logger.debug("RachioImage: Unable to process request: {}", e.getMessage());
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void setHeaders(@Nullable HttpServletResponse response) {
        Validate.notNull(response);
        response.setContentType(SERVLET_IMAGE_MIME_TYPE);
        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    @SuppressWarnings({ "null" })
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setRachioHandlerFactory(@Nullable RachioHandlerFactory rachioHandlerFactory) {
        Validate.notNull(rachioHandlerFactory);
        if (rachioHandlerFactory != null) {
            this.rachioHandlerFactory = rachioHandlerFactory;
            logger.debug("RachioImage: HandlerFactory bound");
        }
    }

    public void unsetRachioHandlerFactory(@Nullable RachioHandlerFactory rachioHandlerFactory) {
        this.rachioHandlerFactory = null;
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

}
