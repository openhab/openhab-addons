/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dlinkupnpcamera.internal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.dlinkupnpcamera.DlinkUpnpCameraBindingConstants;
import org.openhab.binding.dlinkupnpcamera.config.DlinkUpnpCameraConfiguration;
import org.openhab.binding.dlinkupnpcamera.handler.DlinkUpnpCameraHandler;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link upnpcameraHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Yacine Ndiaye
 * @author Antoine Blanc
 * @author Christopher Law
 */
public class DlinkUpnpCameraHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(DlinkUpnpCameraHandlerFactory.class);

    private UpnpIOService upnpIOService;
    private DiscoveryServiceRegistry discoveryServiceRegistry;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {

        return DlinkUpnpCameraBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.debug("ThingHandler createHandler");
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(DlinkUpnpCameraBindingConstants.CAMERA_THING_TYPE_UID)) {
            return new DlinkUpnpCameraHandler(thing, upnpIOService, discoveryServiceRegistry);
        }

        return null;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        logger.debug("activate upnpcameraHandler");
    };

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (DlinkUpnpCameraBindingConstants.CAMERA_THING_TYPE_UID.equals(thingTypeUID)) {
            ThingUID cameraUID = getCameraUID(thingTypeUID, thingUID, configuration);
            logger.debug("Creating a Camera thing with ID '{}'", cameraUID);
            return super.createThing(thingTypeUID, configuration, cameraUID, null);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the DlinkUpnpCamera binding.");
    }

    private ThingUID getCameraUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        String udn = (String) configuration.get(DlinkUpnpCameraConfiguration.UDN);

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, udn);
        }

        return thingUID;
    }

    protected void setUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = upnpIOService;
    }

    protected void unsetUpnpIOService(UpnpIOService upnpIOService) {
        this.upnpIOService = null;
    }

    protected void setDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }

    protected void unsetDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = null;
    }
}