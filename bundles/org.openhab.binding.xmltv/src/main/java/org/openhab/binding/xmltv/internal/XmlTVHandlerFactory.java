/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.xmltv.internal;

import static org.openhab.binding.xmltv.internal.XmlTVBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.xmltv.internal.discovery.XmlTVDiscoveryService;
import org.openhab.binding.xmltv.internal.handler.ChannelHandler;
import org.openhab.binding.xmltv.internal.handler.XmlTVHandler;
import org.openhab.binding.xmltv.internal.jaxb.Tv;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.TimeZoneProvider;
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

/**
 * The {@link XmlTVHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.xmltv", service = ThingHandlerFactory.class)
public class XmlTVHandlerFactory extends BaseThingHandlerFactory {
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final XMLInputFactory xif = XMLInputFactory.newFactory();
    private final TimeZoneProvider timeZoneProvider;
    private final Unmarshaller unmarshaller;

    @Activate
    public XmlTVHandlerFactory(final @Reference TimeZoneProvider timeZoneProvider) throws JAXBException {
        this.timeZoneProvider = timeZoneProvider;
        this.unmarshaller = JAXBContext.newInstance(Tv.class).createUnmarshaller();
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (XMLTV_FILE_BRIDGE_TYPE.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (XMLTV_CHANNEL_THING_TYPE.equals(thingTypeUID)) {
            ThingUID newThingUID;
            if (bridgeUID != null && thingUID != null) {
                newThingUID = new ThingUID(thingTypeUID, bridgeUID, thingUID.getId());
            } else {
                newThingUID = thingUID;
            }
            return super.createThing(thingTypeUID, configuration, newThingUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the XmlTV binding.");
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (XMLTV_FILE_BRIDGE_TYPE.equals(thingTypeUID)) {
            XmlTVHandler bridgeHandler = new XmlTVHandler((Bridge) thing, xif, unmarshaller);
            registerDeviceDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (XMLTV_CHANNEL_THING_TYPE.equals(thingTypeUID)) {
            return new ChannelHandler(thing, timeZoneProvider.getTimeZone());
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof XmlTVHandler) {
            Thing thing = thingHandler.getThing();
            unregisterDeviceDiscoveryService(thing);
        }
        super.removeHandler(thingHandler);
    }

    private synchronized void registerDeviceDiscoveryService(XmlTVHandler bridgeHandler) {
        XmlTVDiscoveryService discoveryService = new XmlTVDiscoveryService(bridgeHandler);
        discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    private synchronized void unregisterDeviceDiscoveryService(Thing thing) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(thing.getUID());
        serviceReg.unregister();
    }
}
