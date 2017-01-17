/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mpower.internal.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.mpower.MpowerBindingConstants;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;

/**
 * An UpnpDiscoveryParticipant which allows to discover mPower devices.
 *
 * @author Marko Donke - Initial contribution
 *
 */
public class MpowerBridgeDiscovery implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(MpowerBridgeDiscovery.class);
    private boolean isAutoDiscoveryEnabled;
    private Set<ThingTypeUID> supportedThingTypes;

    public MpowerBridgeDiscovery() {
        this.isAutoDiscoveryEnabled = true;
        this.supportedThingTypes = MpowerBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    /**
     * Called at the service activation.
     *
     * @param componentContext
     */
    protected void activate(ComponentContext componentContext) {
        if (componentContext.getProperties() != null) {
            String autoDiscoveryPropertyValue = (String) componentContext.getProperties().get("enableAutoDiscovery");
            if (StringUtils.isNotEmpty(autoDiscoveryPropertyValue)) {
                isAutoDiscoveryEnabled = Boolean.valueOf(autoDiscoveryPropertyValue);
            }
        }
        supportedThingTypes = isAutoDiscoveryEnabled ? MpowerBindingConstants.SUPPORTED_THING_TYPES_UIDS
                : new HashSet<ThingTypeUID>();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return supportedThingTypes;
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        DiscoveryResult result = null;
        ThingUID thingUid = getThingUID(device);
        if (thingUid != null) {

            String label = StringUtils.isEmpty(device.getDetails().getFriendlyName()) ? device.getDisplayString()
                    : device.getDetails().getFriendlyName();
            Map<String, Object> properties = new HashMap<>(2, 1);
            String deviceModel = device.getDetails().getModelDetails().getModelNumber();
            properties.put(MpowerBindingConstants.DEVICE_MODEL_PROP_NAME, deviceModel);
            properties.put(MpowerBindingConstants.HOST_PROP_NAME, device.getIdentity().getDescriptorURL().getHost());

            result = DiscoveryResultBuilder.create(thingUid).withThingType(MpowerBindingConstants.THING_TYPE_MPOWER)
                    .withLabel(label).withProperties(properties).build();
        }
        return result;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        ThingUID result = null;
        if (isAutoDiscoveryEnabled) {
            if (StringUtils.containsIgnoreCase(device.getDetails().getManufacturerDetails().getManufacturer(),
                    MpowerBindingConstants.MANUFACTURER)) {

                String deviceModel = device.getDetails().getModelDetails().getModelNumber();
                ThingTypeUID thingTypeUID = MpowerBindingConstants.THING_TYPE_MPOWER;
                if (isSupportedDeviceModel(deviceModel)) {
                    logger.debug("Found new mPower {}, device {}.", MpowerBindingConstants.MANUFACTURER, deviceModel);
                    result = new ThingUID(thingTypeUID, device.getDetails().getSerialNumber().replace(":", "_"));
                }
            }
        }
        return result;
    }

    /**
     * Return true only if the given device model is supported.
     *
     * @param deviceModel
     * @return
     */
    private boolean isSupportedDeviceModel(final String deviceModel) {
        return StringUtils.isNotBlank(deviceModel) && !Collections2
                .filter(MpowerBindingConstants.SUPPORTED_DEVICE_MODELS, new com.google.common.base.Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        return StringUtils.startsWithIgnoreCase(deviceModel, input);
                    }
                }).isEmpty();
    }
}