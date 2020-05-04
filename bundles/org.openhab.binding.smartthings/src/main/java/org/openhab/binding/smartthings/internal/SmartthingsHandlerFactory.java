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
package org.openhab.binding.smartthings.internal;

import static org.openhab.binding.smartthings.internal.SmartthingsBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStateData;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartthingsThingHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
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
@Component(service = { ThingHandlerFactory.class,
        EventHandler.class }, immediate = true, configurationPid = "binding.smarthings", property = "event.topics=org/openhab/binding/smartthings/state")
public class SmartthingsHandlerFactory extends BaseThingHandlerFactory implements ThingHandlerFactory, EventHandler {

    private Logger logger = LoggerFactory.getLogger(SmartthingsHandlerFactory.class);
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private @NonNullByDefault({}) SmartthingsBridgeHandler bridgeHandler;
    private @NonNullByDefault({}) ChannelTypeRegistry channelTypeRegistry;
    private Gson gson;
    private List<SmartthingsThingHandler> thingHandlers = new ArrayList<SmartthingsThingHandler>();

    private @NonNullByDefault({}) HttpClient httpClient;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return THING_TYPE_SMARTTHINGS.equals(thingTypeUID) || SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    public SmartthingsHandlerFactory() {
        // Get a Gson instance
        gson = new Gson();
        // testing by bob
        ChannelTypeRegistry ctr = getChannelTypeRegistry();
    }

    @Override
    @Activate
    public void activate(org.osgi.service.component.ComponentContext componentContext) {
        super.activate(componentContext);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("SmartthingsHandlerFactory is now processing ThingTypeUID {}", thingTypeUID.getAsString());

        if (thingTypeUID.equals(THING_TYPE_SMARTTHINGS)) {
            bridgeHandler = new SmartthingsBridgeHandler((Bridge) thing, this, bundleContext);
            return bridgeHandler;
        } else if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            // Everything but the bridge is handled by this one handler
            SmartthingsThingHandler thingHandler = new SmartthingsThingHandler(thing, this);
            thingHandlers.add(thingHandler);
            return thingHandler;
        }
        return null;
    }

    /**
     * Remove handler of things.
     */

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SmartthingsBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            serviceReg.unregister();
            discoveryServiceRegs.remove(thingHandler.getThing().getUID());
        }
    }

    /**
     * Send a command to the Smartthings Hub
     *
     * @param path http path which tells Smartthings what to execute
     * @param data data to send
     * @return Response from Smartthings
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    public @Nullable Map<String, Object> sendDeviceCommand(String path, String data)
            throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse response = httpClient
                .newRequest(bridgeHandler.getSmartthingsIp(), bridgeHandler.getSmartthingsPort())
                .timeout(3, TimeUnit.SECONDS).path(path).method(HttpMethod.POST)
                .content(new StringContentProvider(data), "application/json").send();

        Map<String, Object> result = null;
        int status = response.getStatus();
        if (status == 200) {
            String responseStr = response.getContentAsString();
            if (responseStr != null && responseStr.length() > 0) {
                result = new HashMap<String, Object>();
                result = gson.fromJson(responseStr, result.getClass());
            }
        } else if (status == 202) {
            logger.info(
                    "Sent message \"{}\" with path \"{}\" to the Smartthings hub, recieved HTTP status {} (This is the normal code from Smartthings)",
                    data, path, status);
        } else {
            logger.info("Sent message \"{}\" with path \"{}\" to the Smartthings hub, recieved HTTP status {}", data,
                    path, status);
        }
        return result;
    }

    /**
     * Messages sent to the Smartthings binding from the hub via the SmartthingsServlet arrive here and are then
     * dispatched to the correct thing's handleStateMessage function
     *
     * @param event The event sent
     */
    @Override
    public void handleEvent(@Nullable Event event) {
        if (event != null) {
            String topic = event.getTopic();
            String data = (String) event.getProperty("data");
            logger.trace("Event received on topic: {}", topic);
            SmartthingsStateData stateData = new SmartthingsStateData();
            stateData = gson.fromJson(data, stateData.getClass());
            SmartthingsThingHandler handler = findHandler(stateData);
            if (handler != null) {
                handler.handleStateMessage(stateData);
            }
        }
    }

    private @Nullable SmartthingsThingHandler findHandler(SmartthingsStateData stateData) {
        for (SmartthingsThingHandler handler : thingHandlers) {
            // There have been some reports of handler.getSmartthingsName() returning a null.
            // Need to find out where null is coming from
            if (handler.getSmartthingsName() == null) {
                logger.warn(
                        "A thing handler \"smartthings name\" is unexpectedly null: for thing {} with display name: {} and with attribute: {}",
                        handler.toString(), stateData.deviceDisplayName, stateData.capabilityAttribute);
                return null;
            }
            if (handler.getSmartthingsName().equals(stateData.deviceDisplayName)) {
                for (Channel ch : handler.getThing().getChannels()) {
                    String chId = ch.getUID().getId();
                    if (chId.equals(stateData.capabilityAttribute)) {
                        return handler;
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
    public void setChannelTypeService(ChannelTypeRegistry registry) {
        channelTypeRegistry = registry;
    }

    public void unsetChannelTypeService(ChannelTypeRegistry registry) {
        channelTypeRegistry = null;
    }

    public ChannelTypeRegistry getChannelTypeRegistry() {
        return channelTypeRegistry;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        logger.debug("setHttpClientFactory this: {}", this.toString());
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    protected void unsetHttpClientFactory() {
        logger.debug("unsetHttpClientFactory this: {}", this.toString());
        this.httpClient = null;
    }

    public SmartthingsBridgeHandler getBridgeHandler() {
        return bridgeHandler;
    }

}
