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
package org.openhab.binding.lametrictime.internal;

import static org.openhab.binding.lametrictime.internal.LaMetricTimeBindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.lametrictime.internal.discovery.LaMetricTimeAppDiscoveryService;
import org.openhab.binding.lametrictime.internal.handler.ClockAppHandler;
import org.openhab.binding.lametrictime.internal.handler.CountdownAppHandler;
import org.openhab.binding.lametrictime.internal.handler.LaMetricTimeHandler;
import org.openhab.binding.lametrictime.internal.handler.RadioAppHandler;
import org.openhab.binding.lametrictime.internal.handler.StopwatchAppHandler;
import org.openhab.binding.lametrictime.internal.handler.WeatherAppHandler;
import org.osgi.framework.ServiceRegistration;
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
public class LaMetricTimeHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_DEVICE, THING_TYPE_CLOCK_APP, THING_TYPE_COUNTDOWN_APP, THING_TYPE_RADIO_APP,
                    THING_TYPE_STOPWATCH_APP, THING_TYPE_WEATHER_APP).collect(Collectors.toSet()));

    private final Logger logger = LoggerFactory.getLogger(LaMetricTimeHandlerFactory.class);

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceReg = new HashMap<>();

    private StateDescriptionOptionsProvider stateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            logger.debug("Creating handler for LaMetric Time device {}", thing);

            LaMetricTimeHandler deviceHandler = new LaMetricTimeHandler((Bridge) thing, stateDescriptionProvider);
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
        discoveryServiceReg.put(deviceHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
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

    @Reference
    protected void setDynamicStateDescriptionProvider(StateDescriptionOptionsProvider provider) {
        this.stateDescriptionProvider = provider;
    }

    protected void unsetDynamicStateDescriptionProvider(StateDescriptionOptionsProvider provider) {
        this.stateDescriptionProvider = null;
    }
}
