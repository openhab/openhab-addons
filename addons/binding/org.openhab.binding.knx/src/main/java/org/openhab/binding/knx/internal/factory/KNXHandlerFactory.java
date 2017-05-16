/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.factory;

import static org.openhab.binding.knx.KNXBindingConstants.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingProvider;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.openhab.binding.knx.KNXProjectProvider;
import org.openhab.binding.knx.discovery.IndividualAddressDiscoveryService;
import org.openhab.binding.knx.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.handler.KNXGenericThingHandler;
import org.openhab.binding.knx.handler.SerialBridgeThingHandler;
import org.openhab.binding.knx.internal.dpt.KNXTypeMapper;
import org.openhab.binding.knx.internal.provider.KNXProjectThingProvider;
import org.osgi.framework.ServiceRegistration;

import com.google.common.collect.Lists;

/**
 * The {@link KNXHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Karel Goderis - Initial contribution
 */
@SuppressWarnings("rawtypes")
public class KNXHandlerFactory extends BaseThingHandlerFactory {

    private static final String[] PROVIDER_INTERFACES = new String[] { KNXProjectProvider.class.getName(),
            ThingProvider.class.getName() };

    public final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(THING_TYPE_GENERIC,
            THING_TYPE_IP_BRIDGE, THING_TYPE_SERIAL_BRIDGE);

    private final Map<ThingUID, ServiceRegistration> discoveryServiceRegs = new HashMap<>();
    private final Map<ThingUID, ServiceRegistration> knxProjectProviderServiceRegs = new HashMap<>();
    private final Collection<KNXTypeMapper> typeMappers = new HashSet<KNXTypeMapper>();
    private final Collection<KNXBridgeBaseThingHandler> bridgeHandlers = new HashSet<KNXBridgeBaseThingHandler>();
    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private ThingTypeRegistry thingTypeRegistry;
    private LocaleProvider localeProvider;

    protected void setItemChannelLinkRegistry(ItemChannelLinkRegistry registry) {
        itemChannelLinkRegistry = registry;
    }

    protected void unsetItemChannelLinkRegistry(ItemChannelLinkRegistry registry) {
        itemChannelLinkRegistry = null;
    }

    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = null;
    }

    protected void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    public void addKNXTypeMapper(KNXTypeMapper typeMapper) {
        typeMappers.add(typeMapper);
        for (KNXBridgeBaseThingHandler aBridge : bridgeHandlers) {
            aBridge.addKNXTypeMapper(typeMapper);
        }
    }

    public void removeKNXTypeMapper(KNXTypeMapper typeMapper) {
        typeMappers.remove(typeMapper);
        for (KNXBridgeBaseThingHandler aBridge : bridgeHandlers) {
            aBridge.removeKNXTypeMapper(typeMapper);
        }
    }

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
        if (THING_TYPE_GENERIC.equals(thingTypeUID)) {
            ThingUID gaUID = getGenericThingUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, gaUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the KNX binding.");
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(THING_TYPE_IP_BRIDGE)) {
            return new IPBridgeThingHandler((Bridge) thing);
        } else if (thing.getThingTypeUID().equals(THING_TYPE_SERIAL_BRIDGE)) {
            return new SerialBridgeThingHandler((Bridge) thing);
        } else if (thing.getThingTypeUID().equals(THING_TYPE_GENERIC)) {
            return new KNXGenericThingHandler(thing, itemChannelLinkRegistry);
        }
        return null;
    }

    private ThingUID getIPBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID != null) {
            return thingUID;
        }
        String ipAddress = (String) configuration.get(IP_ADDRESS);
        return new ThingUID(thingTypeUID, ipAddress);
    }

    private ThingUID getSerialBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID,
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

    private synchronized void registerAddressDiscoveryService(KNXBridgeBaseThingHandler bridgeHandler) {
        IndividualAddressDiscoveryService service = new IndividualAddressDiscoveryService(bridgeHandler);
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        ServiceRegistration reg = bundleContext.registerService(DiscoveryService.class.getName(), service, properties);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), reg);
    }

    private synchronized void unregisterAddressDiscoveryService(Thing thing) {
        ServiceRegistration reg = discoveryServiceRegs.remove(thing.getUID());
        if (reg != null) {
            reg.unregister();
        }
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

    public ThingType getThingType(ThingTypeUID thingTypeUID) {
        return thingTypeRegistry.getThingType(thingTypeUID, localeProvider.getLocale());
    }

    @Override
    public ThingHandler registerHandler(Thing thing) {
        ThingHandler handler = super.registerHandler(thing);
        if (handler instanceof KNXBridgeBaseThingHandler) {
            KNXBridgeBaseThingHandler bridgeHandler = (KNXBridgeBaseThingHandler) handler;
            bridgeHandlers.add(bridgeHandler);
            typeMappers.forEach(it -> bridgeHandler.addKNXTypeMapper(it));
            registerProjectProviderService(bridgeHandler);
            registerAddressDiscoveryService(bridgeHandler);
        }
        return handler;
    }

    @Override
    public void unregisterHandler(Thing thing) {
        if (thing.getHandler() instanceof KNXBridgeBaseThingHandler) {
            unregisterProjectProviderService(thing);
            unregisterAddressDiscoveryService(thing);
            bridgeHandlers.remove(thing.getHandler());
        }
        super.unregisterHandler(thing);
    }

}
