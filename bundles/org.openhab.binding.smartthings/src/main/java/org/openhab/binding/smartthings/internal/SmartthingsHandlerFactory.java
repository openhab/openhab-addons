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
package org.openhab.binding.smartthings.internal;

import static org.openhab.binding.smartthings.internal.SmartthingsBindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStateData;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartthingsThingHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SmartthingsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class, SmartthingsHubCommand.class,
        EventHandler.class }, configurationPid = "binding.smarthings", property = "event.topics=org/openhab/binding/smartthings/state")
public class SmartthingsHandlerFactory extends BaseThingHandlerFactory
        implements ThingHandlerFactory, EventHandler, SmartthingsHubCommand {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsHandlerFactory.class);

    private @Nullable SmartthingsBridgeHandler bridgeHandler = null;
    private @Nullable ThingUID bridgeUID;
    private Gson gson;
    private List<SmartthingsThingHandler> thingHandlers = Collections.synchronizedList(new ArrayList<>());

    private @NonNullByDefault({}) HttpClient httpClient;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return THING_TYPE_SMARTTHINGS.equals(thingTypeUID) || SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    public SmartthingsHandlerFactory() {
        // Get a Gson instance
        gson = new Gson();
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_SMARTTHINGS)) {
            // This binding only supports one bridge. If the user tries to add a second bridge register and error and
            // ignore
            if (bridgeHandler != null) {
                logger.warn(
                        "The Smartthings binding only supports one bridge. Please change your configuration to only use one Bridge. This bridge {} will be ignored.",
                        thing.getUID().getAsString());
                return null;
            }
            bridgeHandler = new SmartthingsBridgeHandler((Bridge) thing, this, bundleContext);
            bridgeUID = thing.getUID();
            logger.debug("SmartthingsHandlerFactory created BridgeHandler for {}", thingTypeUID.getAsString());
            return bridgeHandler;
        } else if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            // Everything but the bridge is handled by this one handler
            // Make sure this thing belongs to the registered Bridge
            if (bridgeUID != null && !bridgeUID.equals(thing.getBridgeUID())) {
                logger.warn("Thing: {} is being ignored because it does not belong to the registered bridge.",
                        thing.getLabel());
                return null;
            }
            SmartthingsThingHandler thingHandler = new SmartthingsThingHandler(thing, this);
            thingHandlers.add(thingHandler);
            logger.debug("SmartthingsHandlerFactory created ThingHandler for {}, {}",
                    thing.getConfiguration().get("smartthingsName"), thing.getUID().getAsString());
            return thingHandler;
        }
        return null;
    }

    /**
     * Send a command to the Smartthings Hub
     *
     * @param path http path which tells Smartthings what to execute
     * @param data data to send
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    @Override
    public void sendDeviceCommand(String path, int timeout, String data)
            throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse response = httpClient
                .newRequest(bridgeHandler.getSmartthingsIp(), bridgeHandler.getSmartthingsPort())
                .timeout(timeout, TimeUnit.SECONDS).path(path).method(HttpMethod.POST)
                .content(new StringContentProvider(data), "application/json").send();

        int status = response.getStatus();
        if (status == 202) {
            logger.debug(
                    "Sent message \"{}\" with path \"{}\" to the Smartthings hub, received HTTP status {} (This is the normal code from Smartthings)",
                    data, path, status);
        } else {
            logger.warn("Sent message \"{}\" with path \"{}\" to the Smartthings hub, received HTTP status {}", data,
                    path, status);
        }
    }

    /**
     * Messages sent to the Smartthings binding from the hub via the SmartthingsServlet arrive here and are then
     * dispatched to the correct thing's handleStateMessage function
     *
     * @param event The event sent
     */
    @Override
    public synchronized void handleEvent(@Nullable Event event) {
        if (event != null) {
            String data = (String) event.getProperty("data");
            SmartthingsStateData stateData = new SmartthingsStateData();
            stateData = gson.fromJson(data, stateData.getClass());
            if (stateData == null) {
                return;
            }
            SmartthingsThingHandler handler = findHandler(stateData);
            if (handler != null) {
                handler.handleStateMessage(stateData);
            }
        }
    }

    private @Nullable SmartthingsThingHandler findHandler(SmartthingsStateData stateData) {
        synchronized (thingHandlers) {
            for (SmartthingsThingHandler handler : thingHandlers) {
                if (handler.getSmartthingsName().equals(stateData.deviceDisplayName)) {
                    for (Channel ch : handler.getThing().getChannels()) {
                        String chId = ch.getUID().getId();
                        if (chId.equals(stateData.capabilityAttribute)) {
                            return handler;
                        }
                    }
                }
            }
        }

        logger.warn(
                "Unable to locate handler for display name: {} with attribute: {}. If this thing is included in your OpenHabAppV2 SmartApp in the Smartthings App on your phone it must also be configured in openHAB",
                stateData.deviceDisplayName, stateData.capabilityAttribute);
        return null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory() {
        this.httpClient = null;
    }

    @Nullable
    public SmartthingsBridgeHandler getBridgeHandler() {
        return bridgeHandler;
    }

    @Override
    @Nullable
    public ThingUID getBridgeUID() {
        return bridgeHandler.getThing().getUID();
    }
}
