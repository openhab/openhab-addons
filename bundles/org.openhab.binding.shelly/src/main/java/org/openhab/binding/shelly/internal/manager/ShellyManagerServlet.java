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
package org.openhab.binding.shelly.internal.manager;

import static org.openhab.binding.shelly.internal.manager.ShellyManagerConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.manager.ShellyManagerPage.ShellyMgrResponse;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShellyManagerServlet} implements the Shelly Manager - a simple device overview/management
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = HttpServlet.class, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class ShellyManagerServlet extends HttpServlet {
    private static final long serialVersionUID = 1393403713585449126L;
    private final Logger logger = LoggerFactory.getLogger(ShellyManagerServlet.class);

    private static final String SERVLET_URI = SHELLY_MANAGER_URI;
    private final ShellyManager manager;
    private final String className;

    private final HttpService httpService;

    @Activate
    public ShellyManagerServlet(@Reference ConfigurationAdmin configurationAdmin,
            @Reference NetworkAddressService networkAddressService, @Reference HttpService httpService,
            @Reference HttpClientFactory httpClientFactory, @Reference ShellyHandlerFactory handlerFactory,
            @Reference ShellyTranslationProvider translationProvider, ComponentContext componentContext,
            Map<String, Object> config) {
        className = substringAfterLast(getClass().toString(), ".");
        this.httpService = httpService;
        String localIp = getString(networkAddressService.getPrimaryIpv4HostAddress());
        Integer localPort = HttpServiceUtil.getHttpServicePort(componentContext.getBundleContext());
        this.manager = new ShellyManager(configurationAdmin, translationProvider,
                httpClientFactory.getCommonHttpClient(), localIp, localPort, handlerFactory);

        try {
            httpService.registerServlet(SERVLET_URI, this, null, httpService.createDefaultHttpContext());
            // Promote Shelly Manager usage
            logger.info("{}", translationProvider.get("status.managerstarted", localIp, localPort.toString()));
        } catch (NamespaceException | ServletException | IllegalArgumentException e) {
            logger.warn("{}: Unable to initialize bindingConfig", className, e);
        }
    }

    @Deactivate
    protected void deactivate() {
        httpService.unregister(SERVLET_URI);
        logger.debug("{} stopped", className);
    }

    @Override
    protected void service(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException, IllegalArgumentException {
        if ((request == null) || (response == null)) {
            logger.debug("request or resp must not be null!");
            return;
        }

        String path = getString(request.getRequestURI()).toLowerCase();
        String ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        ShellyMgrResponse output = new ShellyMgrResponse();
        PrintWriter print = null;
        OutputStream bin = null;
        try {
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            Map<String, String[]> parameters = request.getParameterMap();
            logger.debug("{}: {} Request from {}:{}{}?{}", className, request.getProtocol(), ipAddress,
                    request.getRemotePort(), path, parameters.toString());
            if (!path.toLowerCase().startsWith(SERVLET_URI)) {
                logger.warn("{} received unknown request: path = {}", className, path);
                return;
            }

            output = manager.generateContent(path, parameters);
            response.setContentType(output.mimeType);
            if (output.mimeType.equals("text/html")) {
                // Make sure it's UTF-8 encoded
                response.setCharacterEncoding(UTF_8);
                print = response.getWriter();
                print.write((String) output.data);
            } else {
                // binary data
                byte[] data = (byte[]) output.data;
                if (data != null) {
                    response.setContentLength(data.length);
                    bin = response.getOutputStream();
                    bin.write(data, 0, data.length);
                }
            }
        } catch (ShellyApiException | RuntimeException e) {
            logger.debug("{}: Exception uri={}, parameters={}", className, path, request.getParameterMap().toString(),
                    e);
            response.setContentType("text/html");
            print = response.getWriter();
            print.write("Exception:" + e.toString() + "<br/>Check openHAB.log for details."
                    + "<p/><a href=\"/shelly/manager\">Return to Overview</a>");
            logger.debug("{}: {}", className, output);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (print != null) {
                print.close();
            }
            if (bin != null) {
                bin.close();
            }
        }
    }
}
