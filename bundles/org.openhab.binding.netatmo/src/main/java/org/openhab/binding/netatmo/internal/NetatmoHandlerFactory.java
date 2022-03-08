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
package org.openhab.binding.netatmo.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.config.BindingConfiguration;
import org.openhab.binding.netatmo.internal.deserialization.NADeserializer;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.NABridgeHandler;
import org.openhab.binding.netatmo.internal.handler.NACommonInterface;
import org.openhab.binding.netatmo.internal.handler.NAThingHandler;
import org.openhab.binding.netatmo.internal.handler.capability.AirCareCapability;
import org.openhab.binding.netatmo.internal.handler.capability.CameraCapability;
import org.openhab.binding.netatmo.internal.handler.capability.Capability;
import org.openhab.binding.netatmo.internal.handler.capability.ChannelHelperCapability;
import org.openhab.binding.netatmo.internal.handler.capability.EventCapability;
import org.openhab.binding.netatmo.internal.handler.capability.HomeCapability;
import org.openhab.binding.netatmo.internal.handler.capability.MeasureCapability;
import org.openhab.binding.netatmo.internal.handler.capability.ModuleCapability;
import org.openhab.binding.netatmo.internal.handler.capability.PersonCapability;
import org.openhab.binding.netatmo.internal.handler.capability.PresenceCapability;
import org.openhab.binding.netatmo.internal.handler.capability.RoomCapability;
import org.openhab.binding.netatmo.internal.handler.capability.WeatherCapability;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.MeasuresChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetatmoHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.netatmo")
public class NetatmoHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(NetatmoHandlerFactory.class);

    private final NetatmoDescriptionProvider stateDescriptionProvider;
    private final HttpClient httpClient;
    private final NADeserializer deserializer;
    private final HttpService httpService;
    private final BindingConfiguration configuration = new BindingConfiguration();

    @Activate
    public NetatmoHandlerFactory(@Reference NetatmoDescriptionProvider stateDescriptionProvider,
            @Reference HttpClientFactory factory, @Reference NADeserializer deserializer,
            @Reference HttpService httpService, Map<String, @Nullable Object> config) {
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.httpClient = factory.getCommonHttpClient();
        this.httpService = httpService;
        this.deserializer = deserializer;
        configChanged(config);
    }

    @Modified
    public void configChanged(Map<String, @Nullable Object> config) {
        BindingConfiguration newConf = ConfigParser.configurationAs(config, BindingConfiguration.class);
        if (newConf != null) {
            configuration.update(newConf);
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ModuleType.AS_SET.stream().anyMatch(mt -> mt.thingTypeUID.equals(thingTypeUID));
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        return ModuleType.AS_SET.stream().filter(mt -> mt.thingTypeUID.equals(thingTypeUID)).findFirst()
                .map(mt -> buildNAHandler(thing, mt)).orElse(null);
    }

    private BaseThingHandler buildNAHandler(Thing thing, ModuleType moduleType) {
        if (ModuleType.NABridge.equals(moduleType)) {
            return new ApiBridgeHandler((Bridge) thing, httpClient, httpService, deserializer, configuration);
        }
        NACommonInterface handler = moduleType.isABridge() ? new NABridgeHandler((Bridge) thing)
                : new NAThingHandler(thing);

        List<ChannelHelper> helpers = new ArrayList<>();
        moduleType.channelHelpers.forEach(helperClass -> {
            try {
                ChannelHelper helper = helperClass.getConstructor().newInstance();
                helpers.add(helper);
                if (helper instanceof MeasuresChannelHelper) {
                    handler.getCapabilities().put(new MeasureCapability(handler, (MeasuresChannelHelper) helper));
                }
            } catch (ReflectiveOperationException e) {
                logger.warn("Error creating or initializing helper class : {}", e.getMessage());
            }
        });
        if (!helpers.isEmpty()) {
            handler.getCapabilities().put(new ChannelHelperCapability(handler, helpers));
        }

        moduleType.capabilities.forEach(capability -> {
            Capability newCap = null;
            if (capability == ModuleCapability.class) {
                newCap = new ModuleCapability(handler);
            } else if (capability == AirCareCapability.class) {
                newCap = new AirCareCapability(handler);
            } else if (capability == EventCapability.class) {
                newCap = new EventCapability(handler);
            } else if (capability == HomeCapability.class) {
                newCap = new HomeCapability(handler, stateDescriptionProvider);
            } else if (capability == WeatherCapability.class) {
                newCap = new WeatherCapability(handler);
            } else if (capability == RoomCapability.class) {
                newCap = new RoomCapability(handler);
            } else if (capability == PersonCapability.class) {
                newCap = new PersonCapability(handler, stateDescriptionProvider);
            } else if (capability == CameraCapability.class) {
                newCap = new CameraCapability(handler, stateDescriptionProvider, helpers);
            } else if (capability == PresenceCapability.class) {
                newCap = new PresenceCapability(handler, stateDescriptionProvider, helpers);
            }
            if (newCap != null) {
                handler.getCapabilities().put(newCap);
            } else {
                logger.warn("No factory entry defined to create Capability : {}", capability);
            }
        });

        return (BaseThingHandler) handler;
    }
}
