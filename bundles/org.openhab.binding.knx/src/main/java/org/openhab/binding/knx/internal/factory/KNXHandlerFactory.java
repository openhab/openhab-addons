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
package org.openhab.binding.knx.internal.factory;

import static org.openhab.binding.knx.internal.KNXBindingConstants.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.SerialTransportAdapter;
import org.openhab.binding.knx.internal.handler.DeviceThingHandler;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.handler.SerialBridgeThingHandler;
import org.openhab.binding.knx.internal.i18n.KNXTranslationProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link KNXHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Simon Kaufmann - Initial contribution and API
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class, KNXHandlerFactory.class }, configurationPid = "binding.knx")
public class KNXHandlerFactory extends BaseThingHandlerFactory {

    public static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_DEVICE,
            THING_TYPE_IP_BRIDGE, THING_TYPE_SERIAL_BRIDGE);

    @Nullable
    private final NetworkAddressService networkAddressService;
    private final SerialPortManager serialPortManager;
    private final Map<ThingUID, KNXBridgeBaseThingHandler> bridges = new ConcurrentHashMap<>();

    @Activate
    public KNXHandlerFactory(final @Reference NetworkAddressService networkAddressService, Map<String, Object> config,
            final @Reference TranslationProvider translationProvider, final @Reference LocaleProvider localeProvider,
            final @Reference SerialPortManager serialPortManager) {
        KNXTranslationProvider.I18N.setProvider(localeProvider, translationProvider);
        this.networkAddressService = networkAddressService;
        this.serialPortManager = serialPortManager;
        SerialTransportAdapter.setSerialPortManager(serialPortManager);
        modified(config);
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        disableUoM = (boolean) config.getOrDefault(CONFIG_DISABLE_UOM, false);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (THING_TYPE_IP_BRIDGE.equals(thingTypeUID)) {
            ThingUID ipBridgeUID = getIPBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, ipBridgeUID, null);
        }
        if (THING_TYPE_SERIAL_BRIDGE.equals(thingTypeUID)) {
            ThingUID serialBridgeUID = getSerialBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, serialBridgeUID, null);
        }
        if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }
        return null;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(THING_TYPE_IP_BRIDGE)) {
            KNXBridgeBaseThingHandler bridgeHandler = new IPBridgeThingHandler((Bridge) thing, networkAddressService);
            bridges.put(thing.getUID(), bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_SERIAL_BRIDGE)) {
            KNXBridgeBaseThingHandler bridgeHandler = new SerialBridgeThingHandler((Bridge) thing, serialPortManager);
            bridges.put(thing.getUID(), bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_DEVICE)) {
            return new DeviceThingHandler(thing);
        }
        return null;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        bridges.remove(thing.getUID());
        super.unregisterHandler(thing);
    }

    private ThingUID getIPBridgeThingUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration) {
        if (thingUID != null) {
            return thingUID;
        }
        String ipAddress = (String) configuration.get(IP_ADDRESS);
        return new ThingUID(thingTypeUID, ipAddress);
    }

    private ThingUID getSerialBridgeThingUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration) {
        if (thingUID != null) {
            return thingUID;
        }
        String serialPort = (String) configuration.get(SERIAL_PORT);
        return new ThingUID(thingTypeUID, serialPort);
    }

    public Collection<KNXBridgeBaseThingHandler> getBridges() {
        return Set.copyOf(bridges.values());
    }
}
