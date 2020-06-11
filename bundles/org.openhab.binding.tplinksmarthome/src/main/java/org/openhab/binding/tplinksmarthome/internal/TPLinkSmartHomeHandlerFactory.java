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
package org.openhab.binding.tplinksmarthome.internal;

import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.*;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.tplinksmarthome.internal.device.BulbDevice;
import org.openhab.binding.tplinksmarthome.internal.device.DimmerDevice;
import org.openhab.binding.tplinksmarthome.internal.device.EnergySwitchDevice;
import org.openhab.binding.tplinksmarthome.internal.device.PowerStripDevice;
import org.openhab.binding.tplinksmarthome.internal.device.RangeExtenderDevice;
import org.openhab.binding.tplinksmarthome.internal.device.SmartHomeDevice;
import org.openhab.binding.tplinksmarthome.internal.device.SwitchDevice;
import org.openhab.binding.tplinksmarthome.internal.handler.SmartHomeHandler;
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

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Nullable
    @Override
    protected ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        final TPLinkSmartHomeThingType type = TPLinkSmartHomeThingType.THING_TYPE_MAP.get(thingTypeUID);

        if (type == null) {
            return null;
        }
        final SmartHomeDevice device;

        switch (type.getDeviceType()) {
            case BULB:
                if (TPLinkSmartHomeThingType.isBulbDeviceWithTemperatureColor1(thingTypeUID)) {
                    device = new BulbDevice(thingTypeUID, COLOR_TEMPERATURE_1_MIN, COLOR_TEMPERATURE_1_MAX);
                } else if (TPLinkSmartHomeThingType.isBulbDeviceWithTemperatureColor2(thingTypeUID)) {
                    device = new BulbDevice(thingTypeUID, COLOR_TEMPERATURE_2_MIN, COLOR_TEMPERATURE_2_MAX);
                } else {
                    device = new BulbDevice(thingTypeUID);
                }
                break;
            case DIMMER:
                device = new DimmerDevice();
                break;
            case PLUG:
                if (HS110.is(thingTypeUID)) {
                    device = new EnergySwitchDevice();
                } else {
                    device = new SwitchDevice();
                }
                break;
            case SWITCH:
                device = new SwitchDevice();
                break;
            case RANGE_EXTENDER:
                device = new RangeExtenderDevice();
                break;
            case STRIP:
                device = new PowerStripDevice(thingTypeUID);
                break;
            default:
                return null;
        }
        return new SmartHomeHandler(thing, device, type, ipAddressService);
    }

    @Reference
    protected void setTPLinkIpAddressCache(TPLinkIpAddressService ipAddressCache) {
        this.ipAddressService = ipAddressCache;
    }

    protected void unsetTPLinkIpAddressCache(TPLinkIpAddressService ipAddressCache) {
        this.ipAddressService = null;
    }
}
