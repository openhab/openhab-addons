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
package org.openhab.binding.pioneeravr.internal.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.pioneeravr.internal.PioneerAvrBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An UpnpDiscoveryParticipant which allows to discover Pioneer AVRs.
 *
 * @author Antoine Besnard - Initial contribution
 */
@Component
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
            if (autoDiscoveryPropertyValue != null && !autoDiscoveryPropertyValue.isEmpty()) {
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
            String friendlyName = device.getDetails().getFriendlyName();
            String label = friendlyName == null || friendlyName.isEmpty() ? device.getDisplayString() : friendlyName;
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
            String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
            if (manufacturer != null
                    && manufacturer.toLowerCase().contains(PioneerAvrBindingConstants.MANUFACTURER.toLowerCase())) {
                logger.debug("Manufacturer matched: search: {}, device value: {}.",
                        PioneerAvrBindingConstants.MANUFACTURER, manufacturer);
                String type = device.getType().getType();
                if (type != null
                        && type.toLowerCase().contains(PioneerAvrBindingConstants.UPNP_DEVICE_TYPE.toLowerCase())) {
                    logger.debug("Device type matched: search: {}, device value: {}.",
                            PioneerAvrBindingConstants.UPNP_DEVICE_TYPE, type);

                    String deviceModel = device.getDetails().getModelDetails() != null
                            ? device.getDetails().getModelDetails().getModelName()
                            : null;

                    ThingTypeUID thingTypeUID = PioneerAvrBindingConstants.IP_AVR_THING_TYPE;

                    if (isSupportedDeviceModel(deviceModel, PioneerAvrBindingConstants.SUPPORTED_DEVICE_MODELS2020)) {
                        thingTypeUID = PioneerAvrBindingConstants.IP_AVR_THING_TYPE2020;
                    } else if (isSupportedDeviceModel(deviceModel,
                            PioneerAvrBindingConstants.SUPPORTED_DEVICE_MODELS2019)) {
                        thingTypeUID = PioneerAvrBindingConstants.IP_AVR_THING_TYPE2019;
                    } else if (isSupportedDeviceModel(deviceModel,
                            PioneerAvrBindingConstants.SUPPORTED_DEVICE_MODELS2018)) {
                        thingTypeUID = PioneerAvrBindingConstants.IP_AVR_THING_TYPE2018;
                    } else if (isSupportedDeviceModel(deviceModel,
                            PioneerAvrBindingConstants.SUPPORTED_DEVICE_MODELS2017)) {
                        thingTypeUID = PioneerAvrBindingConstants.IP_AVR_THING_TYPE2017;
                    } else if (isSupportedDeviceModel(deviceModel,
                            PioneerAvrBindingConstants.SUPPORTED_DEVICE_MODELS2016)) {
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
        return deviceModel != null && !deviceModel.isBlank() && supportedDeviceModels.stream()
                .anyMatch(input -> deviceModel.toLowerCase().startsWith(input.toLowerCase()));
    }
}
