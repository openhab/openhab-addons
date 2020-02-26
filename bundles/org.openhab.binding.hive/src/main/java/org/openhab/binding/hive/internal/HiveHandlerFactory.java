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
package org.openhab.binding.hive.internal;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.hive.internal.client.DefaultHiveClientFactory;
import org.openhab.binding.hive.internal.client.HiveClientFactory;
import org.openhab.binding.hive.internal.discovery.DefaultHiveDiscoveryService;
import org.openhab.binding.hive.internal.handler.DefaultHiveAccountHandler;
import org.openhab.binding.hive.internal.handler.DefaultHiveThingHandler;
import org.openhab.binding.hive.internal.handler.strategy.*;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HiveHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.hive", service = ThingHandlerFactory.class)
public final class HiveHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(HiveHandlerFactory.class);

    private final Set<ThingHandlerStrategy> strategiesForBoilerModule;
    private final Set<ThingHandlerStrategy> strategiesForHeating;
    private final Set<ThingHandlerStrategy> strategiesForHotWater;
    private final Set<ThingHandlerStrategy> strategiesForHub;
    private final Set<ThingHandlerStrategy> strategiesForThermostat;
    private final Set<ThingHandlerStrategy> strategiesForTrvGroup;
    private final Set<ThingHandlerStrategy> strategiesForTrv;

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServices = new HashMap<>();

    private final HttpClient httpClient;
    private final HiveClientFactory hiveClientFactory;

    @Activate
    public HiveHandlerFactory(@Reference final HttpClientFactory httpClientFactory) {
        final HttpClient httpClient = httpClientFactory.createHttpClient("HiveBinding");
        try {
            httpClient.start();
        } catch (Exception ex) {
            // Wrap up all exceptions and throw a runtime exception as there
            // is nothing we can do to recover.
            throw new IllegalStateException("Could not start HttpClient.", ex);
        }

        // Keep a copy of HttpClient so we can clean up later
        this.httpClient = httpClient;
        this.hiveClientFactory = new DefaultHiveClientFactory(httpClient);
        
        // Create all the ThingHandlerStrategies we will need.
        final AutoBoostHandlerStrategy autoBoostHandlerStrategy = new AutoBoostHandlerStrategy();
        final BatteryDeviceHandlerStrategy batteryDeviceHandlerStrategy = new BatteryDeviceHandlerStrategy();
        final BoostTimeRemainingHandlerStrategy boostTimeRemainingHandlerStrategy = new BoostTimeRemainingHandlerStrategy();
        final HeatingThermostatEasyHandlerStrategy heatingThermostatEasyHandlerStrategy = new HeatingThermostatEasyHandlerStrategy();
        final HeatingThermostatHandlerStrategy heatingThermostatHandlerStrategy = new HeatingThermostatHandlerStrategy();
        final HeatingTransientModeHandlerStrategy heatingTransientModeHandlerStrategy = new HeatingTransientModeHandlerStrategy();
        final OnOffDeviceHandlerStrategy onOffDeviceHandlerStrategy = new OnOffDeviceHandlerStrategy();
        final PhysicalDeviceHandlerStrategy physicalDeviceHandlerStrategy = new PhysicalDeviceHandlerStrategy();
        final TemperatureSensorHandlerStrategy temperatureSensorHandlerStrategy = new TemperatureSensorHandlerStrategy();
        final TransientModeHandlerStrategy transientModeHandlerStrategy = new TransientModeHandlerStrategy();
        final WaterHeaterEasyHandlerStrategy waterHeaterEasyHandlerStrategy = new WaterHeaterEasyHandlerStrategy();
        final WaterHeaterHandlerStrategy waterHeaterHandlerStrategy = new WaterHeaterHandlerStrategy();
        final ZigbeeDeviceHandlerStrategy zigbeeDeviceHandlerStrategy = new ZigbeeDeviceHandlerStrategy();
        
        // Create the sets of ThingHandlerStrategies needed for each kind of thing.
        this.strategiesForBoilerModule = Collections.unmodifiableSet(Stream.of(
                physicalDeviceHandlerStrategy,
                zigbeeDeviceHandlerStrategy
        ).collect(Collectors.toSet()));

        this.strategiesForHeating = Collections.unmodifiableSet(Stream.of(
                autoBoostHandlerStrategy,
                boostTimeRemainingHandlerStrategy,
                heatingThermostatHandlerStrategy,
                onOffDeviceHandlerStrategy,
                temperatureSensorHandlerStrategy,
                transientModeHandlerStrategy,
                heatingTransientModeHandlerStrategy,
                heatingThermostatEasyHandlerStrategy
        ).collect(Collectors.toSet()));

        this.strategiesForHotWater = Collections.unmodifiableSet(Stream.of(
                boostTimeRemainingHandlerStrategy,
                onOffDeviceHandlerStrategy,
                transientModeHandlerStrategy,
                waterHeaterHandlerStrategy,
                waterHeaterEasyHandlerStrategy
        ).collect(Collectors.toSet()));

        this.strategiesForHub = Collections.unmodifiableSet(Stream.of(
                physicalDeviceHandlerStrategy
        ).collect(Collectors.toSet()));

        this.strategiesForThermostat = Collections.unmodifiableSet(Stream.of(
                batteryDeviceHandlerStrategy,
                physicalDeviceHandlerStrategy,
                zigbeeDeviceHandlerStrategy
        ).collect(Collectors.toSet()));

        this.strategiesForTrvGroup = Collections.unmodifiableSet(Stream.of(
                boostTimeRemainingHandlerStrategy,
                heatingThermostatHandlerStrategy,
                onOffDeviceHandlerStrategy,
                temperatureSensorHandlerStrategy,
                transientModeHandlerStrategy,
                heatingTransientModeHandlerStrategy,
                heatingThermostatEasyHandlerStrategy
        ).collect(Collectors.toSet()));

        this.strategiesForTrv = Collections.unmodifiableSet(Stream.of(
                batteryDeviceHandlerStrategy,
                physicalDeviceHandlerStrategy,
                temperatureSensorHandlerStrategy,
                zigbeeDeviceHandlerStrategy
        ).collect(Collectors.toSet()));
    }

    @Override
    protected void activate(final ComponentContext componentContext) {
        super.activate(componentContext);

        // Help keep track of when openHAB reloads binding while debugging.
        this.logger.trace("HandlerFactory has been activated");
    }

    @Override
    protected void deactivate(final ComponentContext componentContext) {
        super.deactivate(componentContext);

        // Help keep track of when openHAB reloads binding while debugging.
        this.logger.trace("HandlerFactory has been deactivated");

        // Clean up HttpClient
        try {
            httpClient.stop();
        } catch (Exception ignored) {
            // Don't care, just trying to clean up.
        }
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return HiveBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (HiveBindingConstants.THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            // Create the handler
            final DefaultHiveAccountHandler handler = new DefaultHiveAccountHandler(
                    this.hiveClientFactory,
                    DefaultHiveDiscoveryService::new,
                    (Bridge) thing
            );

            // Register the discovery service and keep track of it so we can
            // unregister it later.
            final ServiceRegistration<?> serviceReg = bundleContext.registerService(
                    DiscoveryService.class.getName(),
                    handler.getDiscoveryService(),
                    new Hashtable<>()
            );
            final ThingUID uid = thing.getUID();
            this.discoveryServices.put(uid, serviceReg);

            return handler;
        } else if (HiveBindingConstants.THING_TYPE_BOILER_MODULE.equals(thingTypeUID)) {
            return new DefaultHiveThingHandler(thing, this.strategiesForBoilerModule);
        } else if (HiveBindingConstants.THING_TYPE_HEATING.equals(thingTypeUID)) {
            return new DefaultHiveThingHandler(thing, this.strategiesForHeating);
        } else if (HiveBindingConstants.THING_TYPE_HOT_WATER.equals(thingTypeUID)) {
            return new DefaultHiveThingHandler(thing, this.strategiesForHotWater);
        } else if (HiveBindingConstants.THING_TYPE_HUB.equals(thingTypeUID)) {
            return new DefaultHiveThingHandler(thing, this.strategiesForHub);
        } else if (HiveBindingConstants.THING_TYPE_THERMOSTAT.equals(thingTypeUID)) {
            return new DefaultHiveThingHandler(thing, this.strategiesForThermostat);
        } else if (HiveBindingConstants.THING_TYPE_TRV.equals(thingTypeUID)) {
            return new DefaultHiveThingHandler(thing, this.strategiesForTrv);
        } else if (HiveBindingConstants.THING_TYPE_TRV_GROUP.equals(thingTypeUID)) {
            return new DefaultHiveThingHandler(thing, this.strategiesForTrvGroup);
        }

        return null;
    }

    @Override
    protected void removeHandler(final ThingHandler thingHandler) {
        if (thingHandler instanceof DefaultHiveAccountHandler) {
            this.logger.trace("Removing bridge");
            // Clean up associated discovery service.
            final @Nullable ServiceRegistration<?> serviceReg = this.discoveryServices.remove(thingHandler.getThing().getUID());

            if (serviceReg != null) {
                this.logger.trace("Unregistered discovery service");
                serviceReg.unregister();
            }
        }

        super.removeHandler(thingHandler);
    }
}
