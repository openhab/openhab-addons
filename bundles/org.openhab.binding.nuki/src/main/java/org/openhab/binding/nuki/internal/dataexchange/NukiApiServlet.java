/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.nuki.internal.NukiBindingConstants;
import org.openhab.binding.nuki.internal.dto.BridgeApiLockStateRequestDto;
import org.openhab.binding.nuki.internal.dto.NukiHttpServerStatusResponseDto;
import org.openhab.binding.nuki.internal.handler.NukiBridgeHandler;
import org.openhab.binding.nuki.internal.handler.NukiSmartLockHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link NukiApiServlet} class is responsible for handling the callbacks from the Nuki Bridge.
 *
 * @author Markus Katter - Initial contribution
 */
public class NukiApiServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(NukiApiServlet.class);
    private static final long serialVersionUID = -3601163473320027239L;
    private static final String CHARSET = "utf-8";
    private static final String APPLICATION_JSON = "application/json";

    private HttpService httpService;
    private List<NukiBridgeHandler> nukiBridgeHandlers = new ArrayList<>();
    private String path;
    private Gson gson;

    public NukiApiServlet(HttpService httpService) {
        logger.debug("Instantiating NukiApiServlet({})", httpService);
        this.httpService = httpService;
        gson = new Gson();
    }

    public void add(@NonNull NukiBridgeHandler nukiBridgeHandler) {
        logger.trace("Adding NukiBridgeHandler[{}] for Bridge[{}] to NukiApiServlet.",
                nukiBridgeHandler.getThing().getUID(),
                nukiBridgeHandler.getThing().getConfiguration().get(NukiBindingConstants.CONFIG_IP));
        if (!nukiBridgeHandler.isInitializable()) {
            logger.debug("NukiBridgeHandler[{}] is not initializable, check required configuration!",
                    nukiBridgeHandler.getThing().getUID());
            return;
        }
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

    public int countNukiBridgeHandlers() {
        return nukiBridgeHandlers.size();
    }

    private void activate() {
        logger.debug("Activating NukiApiServlet.");
        path = NukiBindingConstants.CALLBACK_ENDPOINT;
        Dictionary<String, String> servletParams = new Hashtable<>();
        try {
            httpService.registerServlet(path, this, servletParams, httpService.createDefaultHttpContext());
            logger.debug("Started NukiApiServlet at path[{}]", path);
        } catch (ServletException | NamespaceException e) {
            logger.error("ERROR: {}", e.getMessage(), e);
        }
    }

    private void deactivate() {
        logger.trace("deactivate()");
        httpService.unregister(path);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.debug("Servlet Request at URI[{}] request[{}]", request.getRequestURI(), request);
        BridgeApiLockStateRequestDto bridgeApiLockStateRequestDto = getBridgeApiLockStateRequestDto(request);
        if (bridgeApiLockStateRequestDto == null) {
            logger.error("Could not handle Bridge CallBack Request - Discarding!");
            logger.error("Please report a bug, if this request was done by the Nuki Bridge!");
            setHeaders(response);
            response.setStatus(HttpStatus.BAD_REQUEST_400);
            response.getWriter().println(gson.toJson(new NukiHttpServerStatusResponseDto("Invalid BCB-Request!")));
            return;
        }
        String nukiId = String.format("%08X", (bridgeApiLockStateRequestDto.getNukiId()));
        String nukiIdThing;
        for (NukiBridgeHandler nukiBridgeHandler : nukiBridgeHandlers) {
            logger.trace("Searching Bridge[{}] with NukiBridgeHandler[{}] for nukiId[{}].",
                    nukiBridgeHandler.getThing().getConfiguration().get(NukiBindingConstants.CONFIG_IP),
                    nukiBridgeHandler.getThing().getUID(), nukiId);
            List<@NonNull Thing> allSmartLocks = nukiBridgeHandler.getThing().getThings();
            for (Thing thing : allSmartLocks) {
                nukiIdThing = thing.getConfiguration().containsKey(NukiBindingConstants.CONFIG_NUKI_ID)
                        ? (String) thing.getConfiguration().get(NukiBindingConstants.CONFIG_NUKI_ID)
                        : null;
                if (nukiIdThing != null && nukiIdThing.equals(nukiId)) {
                    logger.debug("Processing ThingUID[{}] - nukiId[{}]", thing.getUID(), nukiId);
                    NukiSmartLockHandler nsh = getSmartLockHandler(thing);
                    if (nsh == null) {
                        logger.debug("Could not update channels for ThingUID[{}] because Handler is null!",
                                thing.getUID());
                        break;
                    }
                    Channel channel = thing.getChannel(NukiBindingConstants.CHANNEL_SMARTLOCK_LOCK);
                    if (channel != null) {
                        State state = bridgeApiLockStateRequestDto.getState() == NukiBindingConstants.LOCK_STATES_LOCKED
                                ? OnOffType.ON
                                : OnOffType.OFF;
                        nsh.handleApiServletUpdate(channel.getUID(), state);
                    }
                    channel = thing.getChannel(NukiBindingConstants.CHANNEL_SMARTLOCK_STATE);
                    if (channel != null) {
                        State state = new DecimalType(bridgeApiLockStateRequestDto.getState());
                        nsh.handleApiServletUpdate(channel.getUID(), state);
                    }
                    channel = thing.getChannel(NukiBindingConstants.CHANNEL_SMARTLOCK_LOW_BATTERY);
                    if (channel != null) {
                        State state = bridgeApiLockStateRequestDto.isBatteryCritical() ? OnOffType.ON : OnOffType.OFF;
                        nsh.handleApiServletUpdate(channel.getUID(), state);
                    }
                    setHeaders(response);
                    response.getWriter().println(gson.toJson(new NukiHttpServerStatusResponseDto("OK")));
                    return;
                }
            }
        }
        logger.debug("Smart Lock with nukiId[{}] not found!", nukiId);
        setHeaders(response);
        response.setStatus(HttpStatus.NOT_FOUND_404);
        response.getWriter().println(gson.toJson(new NukiHttpServerStatusResponseDto("Smart Lock not found!")));
    }

    private BridgeApiLockStateRequestDto getBridgeApiLockStateRequestDto(HttpServletRequest request) {
        logger.trace("getBridgeApiLockStateRequestDto(...)");
        String requestContent = null;
        try {
            requestContent = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            BridgeApiLockStateRequestDto bridgeApiLockStateRequestDto = gson.fromJson(requestContent,
                    BridgeApiLockStateRequestDto.class);
            if (bridgeApiLockStateRequestDto.getNukiId() != 0) {
                logger.trace("requestContent[{}]", requestContent);
                return bridgeApiLockStateRequestDto;
            } else {
                logger.error("Invalid BCB-Request payload data!");
                logger.error("requestContent[{}]", requestContent);
            }
        } catch (IOException e) {
            logger.error("Could not read payload from BCB-Request! Message[{}]", e.getMessage());
        } catch (Exception e) {
            logger.error("Could not create BridgeApiLockStateRequestDto from BCB-Request! Message[{}]", e.getMessage());
            logger.error("requestContent[{}]", requestContent);
        }
        return null;
    }

    private NukiSmartLockHandler getSmartLockHandler(Thing thing) {
        logger.trace("getSmartLockHandler(...) from thing[{}]", thing.getUID());
        NukiSmartLockHandler nsh = (NukiSmartLockHandler) thing.getHandler();
        if (nsh == null) {
            logger.debug("Could not get NukiSmartLockHandler for ThingUID[{}]!", thing.getUID());
            return null;
        }
        return nsh;
    }

    private void setHeaders(HttpServletResponse response) {
        response.setCharacterEncoding(CHARSET);
        response.setContentType(APPLICATION_JSON);
    }

}
