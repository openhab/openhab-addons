/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onkyo.internal.discovery;

import static org.openhab.binding.onkyo.OnkyoBindingConstants.*;

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
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An UpnpDiscoveryParticipant which allows to discover Onkyo AVRs.
 *
 * @author Paul Frank - Initial contribution
 */
@Component(service = UpnpDiscoveryParticipant.class, immediate = true)
public class OnkyoUpnpDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(OnkyoUpnpDiscoveryParticipant.class);

    private boolean isAutoDiscoveryEnabled;
    private Set<ThingTypeUID> supportedThingTypes;

    public OnkyoUpnpDiscoveryParticipant() {
        this.isAutoDiscoveryEnabled = true;
        this.supportedThingTypes = SUPPORTED_THING_TYPES_UIDS;
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
        supportedThingTypes = isAutoDiscoveryEnabled ? SUPPORTED_THING_TYPES_UIDS : new HashSet<>();
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
            properties.put(HOST_PARAMETER, device.getIdentity().getDescriptorURL().getHost());
            properties.put(UDN_PARAMETER, device.getIdentity().getUdn().getIdentifierString());

            result = DiscoveryResultBuilder.create(thingUid).withLabel(label).withProperties(properties).build();
        }

        return result;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        ThingUID result = null;
        if (isAutoDiscoveryEnabled) {
            if (StringUtils.containsIgnoreCase(device.getDetails().getManufacturerDetails().getManufacturer(),
                    MANUFACTURER)) {
                logger.debug("Manufacturer matched: search: {}, device value: {}.", MANUFACTURER,
                        device.getDetails().getManufacturerDetails().getManufacturer());
                if (StringUtils.containsIgnoreCase(device.getType().getType(), UPNP_DEVICE_TYPE)) {
                    logger.debug("Device type matched: search: {}, device value: {}.", UPNP_DEVICE_TYPE,
                            device.getType().getType());

                    String deviceModel = device.getDetails().getModelDetails() != null
                            ? device.getDetails().getModelDetails().getModelName()
                            : null;

                    logger.debug("Device model: {}.", deviceModel);

                    ThingTypeUID thingTypeUID = findThingType(deviceModel);
                    result = new ThingUID(thingTypeUID, device.getIdentity().getUdn().getIdentifierString());
                }
            }
        }

        return result;
    }

    private ThingTypeUID findThingType(String deviceModel) {
        ThingTypeUID thingTypeUID = THING_TYPE_ONKYO_UNSUPPORTED;

        for (ThingTypeUID thingType : SUPPORTED_THING_TYPES_UIDS) {
            if (thingType.getId().equalsIgnoreCase(deviceModel)) {
                return thingType;
            }
        }

        if (isSupportedDeviceModel(deviceModel)) {
            thingTypeUID = THING_TYPE_ONKYOAV;
        }

        return thingTypeUID;
    }

    /**
     * Return true only if the given device model is supported.
     *
     * @param deviceModel
     * @return
     */
    private boolean isSupportedDeviceModel(final String deviceModel) {
        return StringUtils.isNotBlank(deviceModel) && SUPPORTED_DEVICE_MODELS.stream()
                .filter(device -> StringUtils.startsWithIgnoreCase(deviceModel, device)).count() > 0;
    }

}
