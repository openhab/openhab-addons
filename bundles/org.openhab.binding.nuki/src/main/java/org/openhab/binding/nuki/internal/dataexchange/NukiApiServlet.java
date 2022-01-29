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
package org.openhab.binding.nuki.internal.dataexchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.nuki.internal.constants.NukiBindingConstants;
import org.openhab.binding.nuki.internal.constants.NukiLinkBuilder;
import org.openhab.binding.nuki.internal.dto.BridgeApiLockStateRequestDto;
import org.openhab.binding.nuki.internal.dto.NukiHttpServerStatusResponseDto;
import org.openhab.binding.nuki.internal.handler.AbstractNukiDeviceHandler;
import org.openhab.binding.nuki.internal.handler.NukiBridgeHandler;
import org.openhab.core.thing.Thing;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link NukiApiServlet} class is responsible for handling the callbacks from the Nuki Bridge.
 *
 * @author Markus Katter - Initial contribution
 * @contributer Christian Hoefler - Door sensor integration
 * @contributer Jan Vybíral - Added Opener support, improved callback handling
 */
@NonNullByDefault
public class NukiApiServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(NukiApiServlet.class);
    private static final long serialVersionUID = -3601163473320027239L;
    private static final String APPLICATION_JSON = "application/json";

    private final HttpService httpService;
    private final List<NukiBridgeHandler> nukiBridgeHandlers = new ArrayList<>();
    private final Gson gson;

    public NukiApiServlet(HttpService httpService) {
        this.httpService = httpService;
        gson = new Gson();
    }

    public void add(NukiBridgeHandler nukiBridgeHandler) {
        logger.trace("Adding NukiBridgeHandler[{}] for Bridge[{}] to NukiApiServlet.",
                nukiBridgeHandler.getThing().getUID(),
                nukiBridgeHandler.getThing().getConfiguration().get(NukiBindingConstants.CONFIG_IP));
        if (nukiBridgeHandlers.isEmpty()) {
            this.activate();
        }
        nukiBridgeHandlers.add(nukiBridgeHandler);
    }

    public void remove(NukiBridgeHandler nukiBridgeHandler) {
        logger.trace("Removing NukiBridgeHandler[{}] for Bridge[{}] from NukiApiServlet.",
                nukiBridgeHandler.getThing().getUID(),
                nukiBridgeHandler.getThing().getConfiguration().get(NukiBindingConstants.CONFIG_IP));
        nukiBridgeHandlers.remove(nukiBridgeHandler);
        if (nukiBridgeHandlers.isEmpty()) {
            this.deactivate();
        }
    }

    private void activate() {
        Dictionary<String, String> servletParams = new Hashtable<>();
        try {
            httpService.registerServlet(NukiLinkBuilder.CALLBACK_ENDPOINT, this, servletParams,
                    httpService.createDefaultHttpContext());
            logger.debug("Started NukiApiServlet at path[{}]", NukiLinkBuilder.CALLBACK_ENDPOINT);
        } catch (ServletException | NamespaceException e) {
            logger.error("Error activating NukiApiServlet: {}", e.getMessage(), e);
        }
    }

    private void deactivate() {
        httpService.unregister(NukiLinkBuilder.CALLBACK_ENDPOINT);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.debug("Servlet Request at URI[{}] request[{}]", request.getRequestURI(), request);
        BridgeApiLockStateRequestDto bridgeApiLockStateRequestDto = getBridgeApiLockStateRequestDto(request);

        ResponseEntity responseEntity;
        if (bridgeApiLockStateRequestDto == null) {
            logger.warn(
                    "Could not handle Bridge CallBack Request, Please report a bug, if this request was done by the Nuki Bridge! - {}",
                    request);

            responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST_400,
                    new NukiHttpServerStatusResponseDto("Invalid BCB-Request!"));
        } else {
            responseEntity = doHandle(bridgeApiLockStateRequestDto, request.getParameter("bridgeId"));
        }

        setHeaders(response);
        response.setStatus(responseEntity.getStatus());
        response.getWriter().write(gson.toJson(responseEntity.getData()));
    }

    private ResponseEntity doHandle(BridgeApiLockStateRequestDto request, @Nullable String bridgeId) {
        String nukiId = request.getNukiId().toString();
        for (NukiBridgeHandler nukiBridgeHandler : nukiBridgeHandlers) {
            logger.trace("Searching Bridge[{}] with NukiBridgeHandler[{}] for nukiId[{}].",
                    nukiBridgeHandler.getThing().getConfiguration().get(NukiBindingConstants.CONFIG_IP),
                    nukiBridgeHandler.getThing().getUID(), nukiId);
            List<Thing> allSmartLocks = nukiBridgeHandler.getThing().getThings();
            for (Thing thing : allSmartLocks) {
                String nukiIdThing = String
                        .valueOf(thing.getConfiguration().get(NukiBindingConstants.PROPERTY_NUKI_ID));
                if (nukiIdThing.equals(nukiId)) {
                    logger.debug("Processing ThingUID[{}] - nukiId[{}]", thing.getUID(), nukiId);
                    AbstractNukiDeviceHandler<?> nsh = getDeviceHandler(thing);
                    if (nsh != null) {
                        nsh.refreshState(request);
                        return new ResponseEntity(HttpStatus.OK_200, new NukiHttpServerStatusResponseDto("OK"));
                    }
                }
            }
        }

        logger.debug("Smart Lock with nukiId[{}] not found!", nukiId);
        return new ResponseEntity(HttpStatus.NOT_FOUND_404,
                new NukiHttpServerStatusResponseDto("Smart Lock not found!"));
    }

    private @Nullable BridgeApiLockStateRequestDto getBridgeApiLockStateRequestDto(HttpServletRequest request) {
        String requestContent = null;
        try (BufferedReader reader = request.getReader()) {
            requestContent = readAll(reader);
            BridgeApiLockStateRequestDto bridgeApiLockStateRequestDto = gson.fromJson(requestContent,
                    BridgeApiLockStateRequestDto.class);
            if (bridgeApiLockStateRequestDto != null && bridgeApiLockStateRequestDto.getNukiId() != null) {
                logger.trace("requestContent[{}]", requestContent);
                return bridgeApiLockStateRequestDto;
            } else {
                logger.debug("Invalid BCB-Request payload data! {}", requestContent);
            }
        } catch (IOException e) {
            logger.debug("Could not read payload from BCB-Request! Message[{}]", e.getMessage());
        } catch (Exception e) {
            logger.error("Could not create BridgeApiLockStateRequestDto from BCB-Request! Message[{}]", e.getMessage());
            logger.error("requestContent[{}]", requestContent);
        }
        return null;
    }

    private String readAll(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int read = 0;
        while ((read = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, read);
        }
        return sb.toString();
    }

    private @Nullable AbstractNukiDeviceHandler<?> getDeviceHandler(Thing thing) {
        AbstractNukiDeviceHandler<?> nsh = (AbstractNukiDeviceHandler<?>) thing.getHandler();
        if (nsh == null) {
            logger.debug("Could not get AbstractNukiDeviceHandler for ThingUID[{}]!", thing.getUID());
            return null;
        }
        return nsh;
    }

    private void setHeaders(HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(APPLICATION_JSON);
    }

    private static class ResponseEntity {
        private final int status;
        private final Object data;

        private ResponseEntity(int status, Object data) {
            this.status = status;
            this.data = data;
        }

        public int getStatus() {
            return status;
        }

        public Object getData() {
            return data;
        }
    }
}
