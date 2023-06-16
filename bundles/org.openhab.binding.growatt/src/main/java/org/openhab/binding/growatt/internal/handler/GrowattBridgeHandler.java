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
package org.openhab.binding.growatt.internal.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.growatt.internal.dto.GrottDevice;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link GrowattBridgeHandler} is a bridge handler for accessing Growatt inverters via the Grott application.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattBridgeHandler extends BaseBridgeHandler {

    private static final String GROWATT_SERVLET_PATH_ALIAS = "/growatt";

    /**
     * Inner servlet instance class to handle POST data from the Grott application.
     */
    private class GrottServlet extends HttpServlet {

        private static final long serialVersionUID = 36178542423191036L;

        @Override
        protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
            response.setStatus(HttpServletResponse.SC_OK);
            handleGrottContent(request.getContentLength() <= 0 ? ""
                    : new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private final Logger logger = LoggerFactory.getLogger(GrowattBridgeHandler.class);
    private final Gson gson = new Gson();
    private final HttpService httpService;

    public GrowattBridgeHandler(Bridge bridge, HttpService httpService) {
        super(bridge);
        this.httpService = httpService;
    }

    @Override
    public void dispose() {
        httpService.unregister(GROWATT_SERVLET_PATH_ALIAS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // everything is read only so do nothing
    }

    /**
     * Process JSON content posted by the Grott application to our servlet.
     */
    @SuppressWarnings("null")
    protected void handleGrottContent(String json) {
        logger.trace("handleGrottContent() json:{}", json);
        JsonElement jsonElement;
        try {
            jsonElement = JsonParser.parseString(json);
        } catch (JsonSyntaxException e) {
            logger.debug("onContent() invalid JSON string '{}'", json, e);
            return;
        }
        List<GrottDevice> grottDevices = new ArrayList<>();
        try {
            if (jsonElement.isJsonObject()) {
                GrottDevice device = gson.fromJson(jsonElement, GrottDevice.class);
                if (device != null) {
                    grottDevices.add(device);
                }
            } else if (jsonElement.isJsonArray()) {
                List<GrottDevice> devices = gson.fromJson(jsonElement, GrottDevice.GROTT_DEVICE_ARRAY);
                if (devices != null) {
                    grottDevices.addAll(devices);
                }
            } else {
                throw new JsonSyntaxException("Unsupported element type");
            }
        } catch (JsonSyntaxException e) {
            logger.debug("onContent() error parsing JSON '{}'", json, e);
            return;
        }
        getThing().getThings().stream().map(thing -> thing.getHandler())
                .filter(handler -> (handler instanceof GrowattInverterHandler))
                .forEach(handler -> ((GrowattInverterHandler) handler).handleGrottDevices(grottDevices));
    }

    @Override
    public void initialize() {
        try {
            httpService.registerServlet(GROWATT_SERVLET_PATH_ALIAS, new GrottServlet(), null, null);
            updateStatus(ThingStatus.ONLINE);
        } catch (ServletException | NamespaceException e) {
            logger.debug("serverStart() exception '{}'", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }
}
