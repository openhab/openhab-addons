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
package org.openhab.binding.dsmr.internal;

import static org.openhab.binding.dsmr.internal.DSMRBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.dsmr.internal.discovery.DSMRMeterDiscoveryService;
import org.openhab.binding.dsmr.internal.handler.DSMRBridgeHandler;
import org.openhab.binding.dsmr.internal.handler.DSMRMeterHandler;
import org.openhab.binding.dsmr.internal.meter.DSMRMeterType;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DSMRHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Refactored discovery service to use standard discovery class methods.
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.dsmr")
public class DSMRHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(DSMRHandlerFactory.class);

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private @NonNullByDefault({}) SerialPortManager serialPortManager;
    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;

    /**
     * Returns if the specified ThingTypeUID is supported by this handler.
     *
     * This handler support the THING_TYPE_DSMR_BRIDGE type and all ThingTypesUID that
     * belongs to the supported DSMRMeterType objects
     *
     * @param thingTypeUID {@link ThingTypeUID} to check
     * @return true if the specified ThingTypeUID is supported, false otherwise
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        if (THING_TYPE_DSMR_BRIDGE.equals(thingTypeUID) || THING_TYPE_SMARTY_BRIDGE.equals(thingTypeUID)) {
            logger.debug("DSMR Bridge Thing {} supported", thingTypeUID);
            return true;
        } else {
            boolean thingTypeUIDIsMeter = DSMRMeterType.METER_THING_TYPES.contains(thingTypeUID);

            if (thingTypeUIDIsMeter) {
                logger.trace("{} is a supported DSMR Meter thing", thingTypeUID);
            }
            return thingTypeUIDIsMeter;
        }
    }

    /**
     * Create the ThingHandler for the corresponding Thing
     *
     * There are two handlers supported:
     * - DSMRBridgeHandler that handle the Thing that corresponds to the physical DSMR device and does the serial
     * communication
     * - MeterHandler that handles the Meter things that are a logical part of the physical device
     *
     * @param thing The Thing to create a ThingHandler for
     * @return ThingHandler for the given Thing or null if the Thing is not supported
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("Searching for thingTypeUID {}", thingTypeUID);

        if (THING_TYPE_DSMR_BRIDGE.equals(thingTypeUID) || THING_TYPE_SMARTY_BRIDGE.equals(thingTypeUID)) {
            DSMRBridgeHandler handler = new DSMRBridgeHandler((Bridge) thing, serialPortManager);
            registerDiscoveryService(handler);
            return handler;
        } else if (DSMRMeterType.METER_THING_TYPES.contains(thingTypeUID)) {
            return new DSMRMeterHandler(thing);
        }

        return null;
    }

    /**
     * Registers a meter discovery service for the bridge handler.
     *
     * @param bridgeHandler handler to register service for
     */
    private synchronized void registerDiscoveryService(DSMRBridgeHandler bridgeHandler) {
        DSMRMeterDiscoveryService discoveryService = new DSMRMeterDiscoveryService(bridgeHandler);

        discoveryService.setLocaleProvider(localeProvider);
        discoveryService.setTranslationProvider(i18nProvider);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof DSMRBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                DSMRMeterDiscoveryService service = (DSMRMeterDiscoveryService) getBundleContext()
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (service != null) {
                    service.unsetLocaleProvider();
                    service.unsetTranslationProvider();
                }
            }
        }
    }

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = null;
    }

    @Reference
    protected void setLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(final LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }
}
