/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nuki.internal.dataexchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.nuki.NukiBindingConstants;
import org.openhab.binding.nuki.handler.NukiSmartLockHandler;
import org.openhab.binding.nuki.internal.dto.BridgeApiLockStateRequestDto;
import org.openhab.binding.nuki.internal.dto.NukiHttpServerStatusResponseDto;
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

    private static final long serialVersionUID = -3601163473320027239L;
    private final Logger logger = LoggerFactory.getLogger(NukiApiServlet.class);

    private static final String PATH = "/nuki/bcb";
    private static final String CHARSET = "utf-8";
    private static final String APPLICATION_JSON = "application/json";

    private HttpService httpService;
    private ThingRegistry thingRegistry;
    private Gson gson;

    public NukiApiServlet() {
        logger.trace("Instantiating NukiApiServlet()");
        gson = new Gson();
    }

    public HttpService getHttpService() {
        return httpService;
    }

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public ThingRegistry getThingRegistry() {
        return thingRegistry;
    }

    public void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    protected void activate(Map<String, Object> config) {
        logger.trace("NukiApiServlet:activate({})", config);
        Dictionary<String, String> servletParams = new Hashtable<String, String>();
        try {
            httpService.registerServlet(PATH, this, servletParams, httpService.createDefaultHttpContext());
            logger.debug("Started NukiApiServlet at path[{}]", PATH);
        } catch (ServletException | NamespaceException e) {
            logger.error("ERROR: {}", e.getMessage(), e);
        }
    }

    protected void modified(Map<String, Object> config) {
        logger.trace("NukiApiServlet:modified({})", config);
    }

    protected void deactivate() {
        logger.trace("NukiApiServlet:deactivate()");
        httpService.unregister(PATH);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.debug("NukiApiServlet:service URI[{}] request[{}]", request.getRequestURI(), request);
        BridgeApiLockStateRequestDto bridgeApiLockStateRequestDto = getBridgeApiLockStateRequestDto(request);
        if (bridgeApiLockStateRequestDto == null) {
            logger.warn("Could not handle request - Discarding the Bridge Callback Request!");
            setHeaders(response);
            response.getWriter().println(gson.toJson(new NukiHttpServerStatusResponseDto("OK")));
            return;
        }
        String nukiId = Integer.toString(bridgeApiLockStateRequestDto.getNukiId());
        String nukiIdThing;
        for (Thing thing : thingRegistry.getAll()) {
            nukiIdThing = thing.getConfiguration().containsKey(NukiBindingConstants.CONFIG_NUKI_ID)
                    ? (String) thing.getConfiguration().get(NukiBindingConstants.CONFIG_NUKI_ID)
                    : null;
            if (nukiIdThing != null && nukiIdThing.equals(nukiId)) {
                logger.debug("Processing ThingUID[{}]", thing.getUID());
                NukiSmartLockHandler nsh = getSmartLockHandler(thing);
                if (nsh == null) {
                    logger.warn("Could not update channels for ThingUID[{}] because Handler is null!", thing.getUID());
                    break;
                }
                Channel channel = thing.getChannel(NukiBindingConstants.CHANNEL_SMARTLOCK_UNLOCK);
                if (channel != null) {
                    State state = bridgeApiLockStateRequestDto.getState() == NukiBindingConstants.LOCK_STATES_LOCKED
                            ? OnOffType.ON
                            : OnOffType.OFF;
                    nsh.handleApiServletUpdate(channel.getUID(), state);
                }
                channel = thing.getChannel(NukiBindingConstants.CHANNEL_SMARTLOCK_LOCK_ACTION);
                if (channel != null) {
                    State state = new DecimalType(bridgeApiLockStateRequestDto.getState());
                    nsh.handleApiServletUpdate(channel.getUID(), state);
                }
                channel = thing.getChannel(NukiBindingConstants.CHANNEL_SMARTLOCK_LOW_BATTERY);
                if (channel != null) {
                    State state = bridgeApiLockStateRequestDto.isBatteryCritical() == true ? OnOffType.ON
                            : OnOffType.OFF;
                    nsh.handleApiServletUpdate(channel.getUID(), state);
                }
            }
        }
        setHeaders(response);
        response.getWriter().println(gson.toJson(new NukiHttpServerStatusResponseDto("OK")));
    }

    private BridgeApiLockStateRequestDto getBridgeApiLockStateRequestDto(HttpServletRequest request) {
        logger.trace("NukiApiServlet:getBridgeApiLockStateRequestDto(...)");
        StringBuffer requestContent = new StringBuffer();
        String line = null;
        try {
            BufferedReader bufferedReader = request.getReader();
            while ((line = bufferedReader.readLine()) != null) {
                requestContent.append(line);
            }
            logger.trace("requestContent[{}]", requestContent);
            return gson.fromJson(requestContent.toString(), BridgeApiLockStateRequestDto.class);
        } catch (Exception e) {
            logger.warn("Could not build BridgeApiLockStateRequestDto from ServletRequest! Message[{}]", e.getMessage(),
                    e);
            return null;
        }
    }

    private NukiSmartLockHandler getSmartLockHandler(Thing thing) {
        logger.trace("NukiApiServlet:getSmartLockHandler(...)");
        NukiSmartLockHandler nsh = (NukiSmartLockHandler) thing.getHandler();
        if (nsh == null) {
            logger.warn("Could not get NukiSmartLockHandler for ThingUID[{}]!", thing.getUID());
            return null;
        }
        return nsh;
    }

    private void setHeaders(HttpServletResponse response) {
        response.setCharacterEncoding(CHARSET);
        response.setContentType(APPLICATION_JSON);
    }

}
