/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.factory;

import static org.openhab.binding.knx.KNXBindingConstants.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.knx.ets.ETSDeviceThingHandler;
import org.openhab.binding.knx.ets.KNXProjectProvider;
import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.ets.provider.KNXProjectThingProvider;
import org.openhab.binding.knx.internal.handler.DeviceThingHandler;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.handler.SerialBridgeThingHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link AdvancedKNXHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Simon Kaufmann - Initial contribution and API
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.knx")
public class KNXHandlerFactory extends BaseThingHandlerFactory {

    public static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Arrays.asList(THING_TYPE_DEVICE,
            THING_TYPE_IP_BRIDGE, THING_TYPE_SERIAL_BRIDGE, THING_TYPE_GENERIC, THING_TYPE_ETS_BRIDGE);

    private static final String[] PROVIDER_INTERFACES = new String[] { KNXProjectProvider.class.getName(),
            ThingProvider.class.getName() };

    protected NetworkAddressService networkAddressService;
    private final Map<ThingUID, ServiceRegistration> knxProjectProviderServiceRegs = new HashMap<>();
    private final Collection<KNXBridgeBaseThingHandler> bridgeHandlers = new HashSet<KNXBridgeBaseThingHandler>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (THING_TYPE_IP_BRIDGE.equals(thingTypeUID)) {
            ThingUID IPBridgeUID = getIPBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, IPBridgeUID, null);
        }
        if (THING_TYPE_SERIAL_BRIDGE.equals(thingTypeUID)) {
            ThingUID serialBridgeUID = getSerialBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, serialBridgeUID, null);
        }
        if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }
        if (THING_TYPE_ETS_BRIDGE.equals(thingTypeUID)) {
            ThingUID IPBridgeUID = getIPBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, IPBridgeUID, null);
        }
        if (THING_TYPE_GENERIC.equals(thingTypeUID)) {
            ThingUID gaUID = getGenericThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, gaUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the KNX binding.");
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(THING_TYPE_IP_BRIDGE)) {
            return new IPBridgeThingHandler((Bridge) thing, networkAddressService);
        } else if (thing.getThingTypeUID().equals(THING_TYPE_SERIAL_BRIDGE)) {
            return new SerialBridgeThingHandler((Bridge) thing);
        } else if (thing.getThingTypeUID().equals(THING_TYPE_DEVICE)) {
            return new DeviceThingHandler(thing);
        } else if (thing.getThingTypeUID().equals(THING_TYPE_ETS_BRIDGE)) {
            return new IPBridgeThingHandler((Bridge) thing, networkAddressService);
        } else if (thing.getThingTypeUID().equals(THING_TYPE_GENERIC)) {
            return new ETSDeviceThingHandler(thing);
        }
        return null;
    }

    protected ThingUID getIPBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID != null) {
            return thingUID;
        }
        String ipAddress = (String) configuration.get(IP_ADDRESS);
        return new ThingUID(thingTypeUID, ipAddress);
    }

    protected ThingUID getSerialBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID,
            Configuration configuration) {
        if (thingUID != null) {
            return thingUID;
        }
        String serialPort = (String) configuration.get(SERIAL_PORT);
        return new ThingUID(thingTypeUID, serialPort);
    }

    private ThingUID getGenericThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        if (thingUID != null) {
            return thingUID;
        }
        String address = ((String) configuration.get(ADDRESS));
        if (address != null) {
            return new ThingUID(thingTypeUID, address.replace(".", "_"), bridgeUID.getId());
        } else {
            String randomID = RandomStringUtils.randomAlphabetic(16).toLowerCase(Locale.ENGLISH);
            return new ThingUID(thingTypeUID, randomID, bridgeUID.getId());
        }
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }

    @Override
    public ThingHandler registerHandler(Thing thing) {
        ThingHandler handler = super.registerHandler(thing);
        if (handler instanceof KNXBridgeBaseThingHandler
                && thing.getThingTypeUID().equals(THING_TYPE_ETS_BRIDGE)) {
            KNXBridgeBaseThingHandler bridgeHandler = (KNXBridgeBaseThingHandler) handler;
            bridgeHandlers.add(bridgeHandler);
            // typeMappers.forEach(it -> bridgeHandler.addKNXTypeMapper(it));
            registerProjectProviderService(bridgeHandler);
        }
        return handler;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        if (thing.getHandler() instanceof KNXBridgeBaseThingHandler
                && thing.getThingTypeUID().equals(THING_TYPE_ETS_BRIDGE)) {
            unregisterProjectProviderService(thing);
            bridgeHandlers.remove(thing.getHandler());
        }
        super.unregisterHandler(thing);
    }

    private synchronized void registerProjectProviderService(KNXBridgeBaseThingHandler bridgeHandler) {
        KNXProjectThingProvider provider = new KNXProjectThingProvider(bridgeHandler.getThing(), this);
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        ServiceRegistration reg = bundleContext.registerService(PROVIDER_INTERFACES, provider, properties);
        this.knxProjectProviderServiceRegs.put(bridgeHandler.getThing().getUID(), reg);
    }

    private synchronized void unregisterProjectProviderService(Thing thing) {
        ServiceRegistration reg = knxProjectProviderServiceRegs.remove(thing.getUID());
        if (reg != null) {
            reg.unregister();
        }
    }

}
