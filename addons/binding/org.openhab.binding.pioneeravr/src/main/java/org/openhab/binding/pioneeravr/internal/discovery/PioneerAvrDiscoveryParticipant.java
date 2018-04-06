/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.pioneeravr.PioneerAvrBindingConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * An UpnpDiscoveryParticipant which allows to discover Pioneer AVRs.
 *
 * @author Antoine Besnard - Initial contribution
 */
@Component(immediate = true)
public class PioneerAvrDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(PioneerAvrDiscoveryParticipant.class);

    private boolean isAutoDiscoveryEnabled;
    private Set<ThingTypeUID> supportedThingTypes;

    public PioneerAvrDiscoveryParticipant() {
        this.isAutoDiscoveryEnabled = true;
        this.supportedThingTypes = PioneerAvrBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    }

    /**
     * Called at the service activation.
     *
     * @param componentContext
     */
    @Activate
    protected void activate(ComponentContext componentContext) {
        if (componentContext.getProperties() != null) {
            String autoDiscoveryPropertyValue = (String) componentContext.getProperties().get("enableAutoDiscovery");
            if (StringUtils.isNotEmpty(autoDiscoveryPropertyValue)) {
                isAutoDiscoveryEnabled = Boolean.valueOf(autoDiscoveryPropertyValue);
            }
        }
        supportedThingTypes = isAutoDiscoveryEnabled ? PioneerAvrBindingConstants.SUPPORTED_THING_TYPES_UIDS
                : new HashSet<>();
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
            properties.put(PioneerAvrBindingConstants.HOST_PARAMETER,
                    device.getIdentity().getDescriptorURL().getHost());
            properties.put(PioneerAvrBindingConstants.PROTOCOL_PARAMETER, PioneerAvrBindingConstants.IP_PROTOCOL_NAME);

            result = DiscoveryResultBuilder.create(thingUid).withLabel(label).withProperties(properties).build();
        }

        return result;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        ThingUID result = null;
        if (isAutoDiscoveryEnabled) {
            if (StringUtils.containsIgnoreCase(device.getDetails().getManufacturerDetails().getManufacturer(),
                    PioneerAvrBindingConstants.MANUFACTURER)) {
                logger.debug("Manufacturer matched: search: {}, device value: {}.",
                        PioneerAvrBindingConstants.MANUFACTURER,
                        device.getDetails().getManufacturerDetails().getManufacturer());
                if (StringUtils.containsIgnoreCase(device.getType().getType(),
                        PioneerAvrBindingConstants.UPNP_DEVICE_TYPE)) {
                    logger.debug("Device type matched: search: {}, device value: {}.",
                            PioneerAvrBindingConstants.UPNP_DEVICE_TYPE, device.getType().getType());

                    String deviceModel = device.getDetails().getModelDetails() != null
                            ? device.getDetails().getModelDetails().getModelName()
                            : null;

                    ThingTypeUID thingTypeUID = PioneerAvrBindingConstants.IP_AVR_THING_TYPE;

                    if (isSupportedDeviceModel(deviceModel, PioneerAvrBindingConstants.SUPPORTED_DEVICE_MODELS2016)) {
                        thingTypeUID = PioneerAvrBindingConstants.IP_AVR_THING_TYPE2016;
                    } else if (isSupportedDeviceModel(deviceModel,
                            PioneerAvrBindingConstants.SUPPORTED_DEVICE_MODELS2015)) {
                        thingTypeUID = PioneerAvrBindingConstants.IP_AVR_THING_TYPE2015;
                    } else if (isSupportedDeviceModel(deviceModel,
                            PioneerAvrBindingConstants.SUPPORTED_DEVICE_MODELS2014)) {
                        thingTypeUID = PioneerAvrBindingConstants.IP_AVR_THING_TYPE2014;
                    } else if (!isSupportedDeviceModel(deviceModel,
                            PioneerAvrBindingConstants.SUPPORTED_DEVICE_MODELS)) {
                        logger.debug("Device model {} not supported. Odd behaviors may happen.", deviceModel);
                        thingTypeUID = PioneerAvrBindingConstants.IP_AVR_UNSUPPORTED_THING_TYPE;
                    }

                    result = new ThingUID(thingTypeUID, device.getIdentity().getUdn().getIdentifierString());
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
    private boolean isSupportedDeviceModel(String deviceModel, Set<String> supportedDeviceModels) {
        return StringUtils.isNotBlank(deviceModel)
                && !Collections2.filter(supportedDeviceModels, new Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        return StringUtils.startsWithIgnoreCase(deviceModel, input);
                    }
                }).isEmpty();
    }
}
