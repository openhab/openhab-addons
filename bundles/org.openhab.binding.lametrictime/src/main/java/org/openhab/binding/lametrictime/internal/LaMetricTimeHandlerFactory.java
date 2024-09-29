/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal;

import static org.openhab.binding.lametrictime.internal.LaMetricTimeBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lametrictime.internal.discovery.LaMetricTimeAppDiscoveryService;
import org.openhab.binding.lametrictime.internal.handler.ClockAppHandler;
import org.openhab.binding.lametrictime.internal.handler.CountdownAppHandler;
import org.openhab.binding.lametrictime.internal.handler.LaMetricTimeHandler;
import org.openhab.binding.lametrictime.internal.handler.RadioAppHandler;
import org.openhab.binding.lametrictime.internal.handler.StopwatchAppHandler;
import org.openhab.binding.lametrictime.internal.handler.WeatherAppHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LaMetricTimeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gregory Moyer - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.lametrictime")
@NonNullByDefault
public class LaMetricTimeHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Set.of(THING_TYPE_DEVICE, THING_TYPE_CLOCK_APP,
            THING_TYPE_COUNTDOWN_APP, THING_TYPE_RADIO_APP, THING_TYPE_STOPWATCH_APP, THING_TYPE_WEATHER_APP);

    private static final int EVENT_STREAM_CONNECT_TIMEOUT = 10;
    private static final int EVENT_STREAM_READ_TIMEOUT = 10;

    private final Logger logger = LoggerFactory.getLogger(LaMetricTimeHandlerFactory.class);

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceReg = new HashMap<>();

    private final ClientBuilder clientBuilder;

    private final StateDescriptionOptionsProvider stateDescriptionProvider;

    @Activate
    public LaMetricTimeHandlerFactory(@Reference ClientBuilder clientBuilder,
            @Reference StateDescriptionOptionsProvider stateDescriptionProvider) {
        this.clientBuilder = clientBuilder //
                .connectTimeout(EVENT_STREAM_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(EVENT_STREAM_READ_TIMEOUT, TimeUnit.SECONDS);
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            logger.debug("Creating handler for LaMetric Time device {}", thing);

            LaMetricTimeHandler deviceHandler = new LaMetricTimeHandler((Bridge) thing, stateDescriptionProvider,
                    clientBuilder);
            registerAppDiscoveryService(deviceHandler);

            return deviceHandler;
        } else if (THING_TYPE_CLOCK_APP.equals(thingTypeUID)) {
            logger.debug("Creating handler for LaMetric Time clock app {}", thing);
            return new ClockAppHandler(thing);
        } else if (THING_TYPE_COUNTDOWN_APP.equals(thingTypeUID)) {
            logger.debug("Creating handler for LaMetric Time countdown app {}", thing);
            return new CountdownAppHandler(thing);
        } else if (THING_TYPE_RADIO_APP.equals(thingTypeUID)) {
            logger.debug("Creating handler for LaMetric Time radio app {}", thing);
            return new RadioAppHandler(thing);
        } else if (THING_TYPE_STOPWATCH_APP.equals(thingTypeUID)) {
            logger.debug("Creating handler for LaMetric Time stopwatch app {}", thing);
            return new StopwatchAppHandler(thing);
        } else if (THING_TYPE_WEATHER_APP.equals(thingTypeUID)) {
            logger.debug("Creating handler for LaMetric Time weather app {}", thing);
            return new WeatherAppHandler(thing);
        }

        return null;
    }

    @Override
    protected void removeHandler(final ThingHandler thingHandler) {
        if (!(thingHandler instanceof LaMetricTimeHandler)) {
            return;
        }

        unregisterAppDiscoveryService((LaMetricTimeHandler) thingHandler);
    }

    /**
     * Register the given device handler to participate in discovery of new apps.
     *
     * @param deviceHandler the device handler to register (must not be <code>null</code>)
     */
    private synchronized void registerAppDiscoveryService(final LaMetricTimeHandler deviceHandler) {
        logger.debug("Registering app discovery service");
        LaMetricTimeAppDiscoveryService discoveryService = new LaMetricTimeAppDiscoveryService(deviceHandler);
        discoveryServiceReg.put(deviceHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    /**
     * Unregister the given device handler from participating in discovery of new apps.
     *
     * @param deviceHandler the device handler to unregister (must not be <code>null</code>)
     */
    private synchronized void unregisterAppDiscoveryService(final LaMetricTimeHandler deviceHandler) {
        ThingUID thingUID = deviceHandler.getThing().getUID();
        ServiceRegistration<?> serviceReg = discoveryServiceReg.remove(thingUID);
        if (serviceReg != null) {
            logger.debug("Unregistering app discovery service");
            serviceReg.unregister();
        }
    }
}
