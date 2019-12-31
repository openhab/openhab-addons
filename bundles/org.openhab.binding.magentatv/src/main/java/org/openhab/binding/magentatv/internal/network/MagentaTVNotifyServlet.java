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
package org.openhab.binding.magentatv.internal.network;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.magentatv.internal.MagentaTVHandlerFactory;
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
 * Main OSGi service and HTTP servlet for MagentaTV NOTIFY.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = HttpServlet.class, configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
public class MagentaTVNotifyServlet extends HttpServlet {
    private static final long serialVersionUID = 2119809008606371618L;
    private final Logger logger = LoggerFactory.getLogger(MagentaTVNotifyServlet.class);

    private @Nullable HttpService httpService;
    private @Nullable MagentaTVHandlerFactory handlerFactory;

    @SuppressWarnings("null")
    @Activate
    protected void activate(Map<String, Object> config) {
        try {
            httpService.registerServlet(PAIRING_NOTIFY_URI, this, null, httpService.createDefaultHttpContext());
            logger.info("Servlet started at {}", PAIRING_NOTIFY_URI);
            if ((handlerFactory != null) && !handlerFactory.getNotifyServletStatus()) {
                handlerFactory.setNotifyServletStatus(true);
            }
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start MagentaTVNotifyServlet: {}", e.getMessage());
        }
    }

    @SuppressWarnings("null")
    @Deactivate
    protected void deactivate() {
        httpService.unregister(PAIRING_NOTIFY_URI);
        logger.debug("MagentaTV Servlet stopped");
    }

    /**
     * Notify servlet handler (will be called by jetty
     *
     * Format of SOAP message:
     * <e:propertyset xmlns:e="urn:schemas-upnp-org:event-1-0"> <e:property>
     * <uniqueDeviceID>1C18548DAF7DE9BC231249DB28D2A650</uniqueDeviceID>
     * </e:property> <e:property> <messageBody>X-pairingCheck:5218C0AA</messageBody>
     * </e:property> </e:propertyset>
     *
     * Format of event message: <?xml version="1.0"?>
     * <e:propertyset xmlns:e="urn:schemas-upnp-org:event-1-0"> <e:property>
     * <STB_Mac>AC6FBB61B1E5</STB_Mac> </e:property> <e:property>
     * <STB_playContent>{&quot;new_play_mode&quot;:0,&quot;playBackState&quot;:1,&
     * quot;mediaType&quot;:1,&quot;mediaCode&quot;:&quot;3682&quot;}</
     * STB_playContent> </e:property> </e:propertyset>
     *
     * @param request
     * @param resp
     *
     * @throws ServletException, IOException
     */
    @SuppressWarnings("null")
    @Override
    protected void service(@Nullable HttpServletRequest request, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        String data = inputStreamToString(request);
        try {
            String ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            String path = request.getRequestURI();
            logger.trace("Reqeust from {}:{}{} ({}, {})", ipAddress, request.getRemotePort(), path,
                    request.getRemoteHost(), request.getProtocol());
            if (!path.equalsIgnoreCase(PAIRING_NOTIFY_URI)) {
                logger.debug("Invalid request received - path = {}", path);
                return;
            }

            if (data.contains(NOTIFY_PAIRING_CODE)) {
                String deviceId = data.substring(data.indexOf("<uniqueDeviceID>") + "<uniqueDeviceID>".length(),
                        data.indexOf("</uniqueDeviceID>"));
                String pairingCode = data.substring(data.indexOf(NOTIFY_PAIRING_CODE) + NOTIFY_PAIRING_CODE.length(),
                        data.indexOf("</messageBody>"));
                logger.debug("Pairing code {} received for deviceID {}", pairingCode, deviceId);
                if (!handlerFactory.notifyPairingResult(deviceId, ipAddress, pairingCode)) {
                    logger.trace("Pairing data={}", data);
                }
            } else {
                if (data.contains("STB_")) {
                    data = data.replaceAll("&quot;", "\"");
                    String stbMac = StringUtils.substringBetween(data, "<STB_Mac>", "</STB_Mac>");
                    String stbEvent = "";
                    if (data.contains("<STB_playContent>")) {
                        stbEvent = StringUtils.substringBetween(data, "<STB_playContent>", "</STB_playContent>");
                    } else if (data.contains("<STB_EitChanged>")) {
                        stbEvent = StringUtils.substringBetween(data, "<STB_EitChanged>", "</STB_EitChanged>");
                    } else {
                        logger.debug("Unknown STB event: {}", data);
                    }
                    if (!stbEvent.isEmpty()) {
                        if (!handlerFactory.notifyMREvent(stbMac, stbEvent)) {
                            logger.debug("Event not processed, data={}", data);
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            logger.debug("Unable to process http request, data={}", data != null ? data : "<empty>");
        } finally {
            // send response
            resp.setCharacterEncoding(UTF_8);
            resp.getWriter().write("");
        }
    }

    @SuppressWarnings("resource")
    private String inputStreamToString(@Nullable HttpServletRequest request) throws IOException {
        @SuppressWarnings("null")
        Scanner scanner = new Scanner(request.getInputStream()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    @SuppressWarnings("null")
    @Reference
    public void setMagentaTVHandlerFactory(MagentaTVHandlerFactory handlerFactory) {
        logger.debug("HandlerFactory bound");
        this.handlerFactory = handlerFactory;
        if (handlerFactory != null) {
            handlerFactory.setNotifyServletStatus(true);
        }
    }

    @SuppressWarnings("null")
    public void unsetMagentaTVHandlerFactory(MagentaTVHandlerFactory handlerFactory) {
        if (handlerFactory != null) {
            handlerFactory.setNotifyServletStatus(false);
        }
        this.handlerFactory = null;
        logger.debug("HandlerFactory unbound");
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
        logger.debug("httpService bound");
    }

    public void unsetHttpService(HttpService httpService) {
        this.httpService = null;
        logger.debug("httpService unbound");
    }
}
