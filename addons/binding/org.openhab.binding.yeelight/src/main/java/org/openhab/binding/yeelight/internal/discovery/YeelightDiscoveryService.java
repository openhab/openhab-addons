/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.internal.discovery;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.yeelight.internal.YeelightBindingConstants;
import org.openhab.binding.yeelight.internal.YeelightHandlerFactory;
import org.openhab.binding.yeelight.internal.lib.device.DeviceBase;
import org.openhab.binding.yeelight.internal.lib.listeners.DeviceListener;
import org.openhab.binding.yeelight.internal.lib.services.DeviceManager;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YeelightDiscoveryService} is responsible for search and discovery of new devices.
 *
 * @author Coaster Li - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.yeelight")
public class YeelightDiscoveryService extends AbstractDiscoveryService implements DeviceListener {

    private final Logger logger = LoggerFactory.getLogger(YeelightDiscoveryService.class.getSimpleName());

    public YeelightDiscoveryService() {
        super(YeelightHandlerFactory.SUPPORTED_THING_TYPES_UIDS, 2, false);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Scan");
        DeviceManager.getInstance().registerDeviceListener(this);
        DeviceManager.getInstance().startDiscovery();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug(": stopScan");
        DeviceManager.getInstance().stopDiscovery();
        DeviceManager.getInstance().unregisterDeviceListener(this);
    }

    @Override
    public void onDeviceFound(DeviceBase device) {
        logger.info("onDeviceFound, id: {}", device.getDeviceId());
        ThingUID thingUID = getThingUID(device);

        if (thingUID == null) {
            // We don't know about this thing type
            logger.info("Skipping device {}, unknown type.", device.getDeviceId());
            return;
        }

        ThingTypeUID thingTypeUID = getThingTypeUID(device);
        String deviceName = device.getDeviceName().isEmpty() ? DeviceManager.getDefaultName(device)
                : device.getDeviceName();

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withLabel(deviceName).withProperties(device.getBulbInfo())
                .withProperty(YeelightBindingConstants.PARAMETER_DEVICE_ID, device.getDeviceId()).build();

        thingDiscovered(discoveryResult);
    }

    @Override
    public void onDeviceLost(DeviceBase device) {
        logger.debug("onDeviceLost, id: {}", device.getDeviceId());
    }

    private ThingUID getThingUID(DeviceBase device) {
        switch (device.getDeviceType()) {
            case ceiling:
                return new ThingUID(YeelightBindingConstants.THING_TYPE_CEILING, device.getDeviceId());
            case ceiling1:
                return new ThingUID(YeelightBindingConstants.THING_TYPE_CEILING, device.getDeviceId());
            case ceiling3:
                return new ThingUID(YeelightBindingConstants.THING_TYPE_CEILING, device.getDeviceId());
            case color:
                return new ThingUID(YeelightBindingConstants.THING_TYPE_WONDER, device.getDeviceId());
            case mono:
                return new ThingUID(YeelightBindingConstants.THING_TYPE_DOLPHIN, device.getDeviceId());
            case ct_bulb:
                return new ThingUID(YeelightBindingConstants.THING_TYPE_CTBULB, device.getDeviceId());
            case stripe:
                return new ThingUID(YeelightBindingConstants.THING_TYPE_STRIPE, device.getDeviceId());
            default:
                return null;
        }
    }

    private ThingTypeUID getThingTypeUID(DeviceBase device) {
        switch (device.getDeviceType()) {
            case ceiling:
                return YeelightBindingConstants.THING_TYPE_CEILING;
            case ceiling1:
                return YeelightBindingConstants.THING_TYPE_CEILING1;
            case ceiling3:
                return YeelightBindingConstants.THING_TYPE_CEILING3;
            case color:
                return YeelightBindingConstants.THING_TYPE_WONDER;
            case mono:
                return YeelightBindingConstants.THING_TYPE_DOLPHIN;
            case ct_bulb:
                return YeelightBindingConstants.THING_TYPE_CTBULB;
            case stripe:
                return YeelightBindingConstants.THING_TYPE_STRIPE;
            default:
                return null;
        }
    }
}
