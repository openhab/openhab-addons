/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.openhab.binding.magentatv.internal.MagentaTVUtil.substringBetween;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.magentatv.internal.MagentaTVHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
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
@Component(service = HttpServlet.class, configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class MagentaTVNotifyServlet extends HttpServlet {
    private static final long serialVersionUID = 2119809008606371618L;
    private final Logger logger = LoggerFactory.getLogger(MagentaTVNotifyServlet.class);

    private final MagentaTVHandlerFactory handlerFactory;

    @Activate
    public MagentaTVNotifyServlet(@Reference MagentaTVHandlerFactory handlerFactory, @Reference HttpService httpService,
            Map<String, Object> config) {
        this.handlerFactory = handlerFactory;
        try {
            httpService.registerServlet(PAIRING_NOTIFY_URI, this, null, httpService.createDefaultHttpContext());
            logger.debug("Servlet started at {}", PAIRING_NOTIFY_URI);
            if (!handlerFactory.getNotifyServletStatus()) {
                handlerFactory.setNotifyServletStatus(true);
            }
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start MagentaTVNotifyServlet: {}", e.getMessage());
        }
    }

    /**
     * Notify servlet handler (will be called by jetty
     *
     * <p>
     * Format of SOAP message:
     *
     * <p>
     * {@code
     * <e:propertyset xmlns:e="urn:schemas-upnp-org:event-1-0"> <e:property>
     * <uniqueDeviceID>1C18548DAF7DE9BC231249DB28D2A650</uniqueDeviceID>
     * </e:property> <e:property> <messageBody>X-pairingCheck:5218C0AA</messageBody>
     * </e:property> </e:propertyset>
     * }
     *
     * <p>
     * Format of event message: {@code <?xml version="1.0"?>}
     *
     * <p>
     * {@code
     * <e:propertyset xmlns:e="urn:schemas-upnp-org:event-1-0"> <e:property>
     * <STB_Mac>AC6FBB61B1E5</STB_Mac> </e:property> <e:property>
     * <STB_playContent>{&quot;new_play_mode&quot;:0,&quot;playBackState&quot;:1,&
     * quot;mediaType&quot;:1,&quot;mediaCode&quot;:&quot;3682&quot;}</
     * STB_playContent> </e:property> </e:propertyset>
     * }
     *
     * @param request
     * @param response
     *
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void service(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        String data = inputStreamToString(request);
        try {
            if ((request == null) || (response == null)) {
                return;
            }
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
                    data = data.replace("&quot;", "\"");
                    String stbMac = substringBetween(data, "<STB_Mac>", "</STB_Mac>");
                    String stbEvent = "";
                    if (data.contains("<STB_playContent>")) {
                        stbEvent = substringBetween(data, "<STB_playContent>", "</STB_playContent>");
                    } else if (data.contains("<STB_EitChanged>")) {
                        stbEvent = substringBetween(data, "<STB_EitChanged>", "</STB_EitChanged>");
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
            if (response != null) {
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().write("");
            }
        }
    }

    @SuppressWarnings("resource")
    private String inputStreamToString(@Nullable HttpServletRequest request) throws IOException {
        if (request == null) {
            return "";
        }
        Scanner scanner = new Scanner(request.getInputStream()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}
