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
package org.openhab.binding.tplinksmarthome.internal;

import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType.SUPPORTED_THING_TYPES;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tplinksmarthome.internal.device.BulbDevice;
import org.openhab.binding.tplinksmarthome.internal.device.DimmerDevice;
import org.openhab.binding.tplinksmarthome.internal.device.EnergySwitchDevice;
import org.openhab.binding.tplinksmarthome.internal.device.LightStripDevice;
import org.openhab.binding.tplinksmarthome.internal.device.PowerStripDevice;
import org.openhab.binding.tplinksmarthome.internal.device.RangeExtenderDevice;
import org.openhab.binding.tplinksmarthome.internal.device.SmartHomeDevice;
import org.openhab.binding.tplinksmarthome.internal.device.SwitchDevice;
import org.openhab.binding.tplinksmarthome.internal.handler.SmartHomeHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link TPLinkSmartHomeHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Christian Fischer - Initial contribution
 * @author Hilbrand Bouwkamp - Specific handlers for different type of devices.
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.tplinksmarthome")
public class TPLinkSmartHomeHandlerFactory extends BaseThingHandlerFactory {

    private @NonNullByDefault({}) TPLinkIpAddressService ipAddressService;
    private final TPLinkStateDescriptionProvider stateDescriptionProvider;

    @Activate
    public TPLinkSmartHomeHandlerFactory(final @Reference TPLinkStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Nullable
    @Override
    protected ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        final TPLinkSmartHomeThingType type = TPLinkSmartHomeThingType.THING_TYPE_MAP.get(thingTypeUID);

        if (type == null) {
            return null;
        }
        final SmartHomeDevice device;

        switch (type.getDeviceType()) {
            case BULB:
                device = new BulbDevice(type);
                break;
            case DIMMER:
                device = new DimmerDevice();
                break;
            case LIGHT_STRIP:
                device = new LightStripDevice(type);
                break;
            case PLUG:
                device = new SwitchDevice();
                break;
            case PLUG_WITH_ENERGY:
                device = new EnergySwitchDevice();
                break;
            case STRIP:
                device = new PowerStripDevice(type);
                break;
            case SWITCH:
                device = new SwitchDevice();
                break;
            case RANGE_EXTENDER:
                device = new RangeExtenderDevice();
                break;
            default:
                return null;
        }
        return new SmartHomeHandler(thing, device, type, ipAddressService, stateDescriptionProvider);
    }

    @Reference
    protected void setTPLinkIpAddressCache(final TPLinkIpAddressService ipAddressCache) {
        this.ipAddressService = ipAddressCache;
    }

    protected void unsetTPLinkIpAddressCache(final TPLinkIpAddressService ipAddressCache) {
        this.ipAddressService = null;
    }
}
