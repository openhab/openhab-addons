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
package org.openhab.binding.hue.internal.discovery;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.net.URL;
import java.util.Dictionary;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.hue.internal.handler.HueBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueBridgeUPNPDiscoveryParticipant} is responsible for discovering new and removed Hue Bridges. It uses the
 * central {@link org.openhab.core.config.discovery.upnp.internal.UpnpDiscoveryService}.
 *
 * The discovery through UPnP was replaced by mDNS discovery for recent bridges (V2).
 * For old bridges (V1), the UPnP discovery is still required (as mDNS is not implemented).
 * This class allows discovering only old bridges using UPnP.
 *
 * @author Laurent Garnier - Initial contribution
 */
@Component(configurationPid = "discovery.hue")
@NonNullByDefault
public class HueBridgeUPNPDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private static final String EXPECTED_MODEL_NAME_PREFIX = "Philips hue bridge";

    private final Logger logger = LoggerFactory.getLogger(HueBridgeUPNPDiscoveryParticipant.class);

    private long removalGracePeriod = 50L;

    private boolean isAutoDiscoveryEnabled = true;

    @Activate
    protected void activate(ComponentContext componentContext) {
        activateOrModifyService(componentContext);
    }

    @Modified
    protected void modified(ComponentContext componentContext) {
        activateOrModifyService(componentContext);
    }

    private void activateOrModifyService(ComponentContext componentContext) {
        Dictionary<String, @Nullable Object> properties = componentContext.getProperties();
        String autoDiscoveryPropertyValue = (String) properties
                .get(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY);
        if (autoDiscoveryPropertyValue != null && !autoDiscoveryPropertyValue.isBlank()) {
            isAutoDiscoveryEnabled = Boolean.valueOf(autoDiscoveryPropertyValue);
        }
        String removalGracePeriodPropertyValue = (String) properties.get(REMOVAL_GRACE_PERIOD);
        if (removalGracePeriodPropertyValue != null && !removalGracePeriodPropertyValue.isBlank()) {
            try {
                removalGracePeriod = Long.parseLong(removalGracePeriodPropertyValue);
            } catch (NumberFormatException e) {
                logger.warn("Configuration property '{}' has invalid value: {}", REMOVAL_GRACE_PERIOD,
                        removalGracePeriodPropertyValue);
            }
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return HueBridgeHandler.SUPPORTED_THING_TYPES;
    }

    @Override
    public @Nullable DiscoveryResult createResult(RemoteDevice device) {
        if (!isAutoDiscoveryEnabled) {
            return null;
        }
        DeviceDetails details = device.getDetails();
        ThingUID uid = getThingUID(device);
        if (details == null || uid == null) {
            return null;
        }
        URL baseUrl = details.getBaseURL();
        String serialNumber = details.getSerialNumber();
        if (baseUrl == null || serialNumber == null || serialNumber.isBlank()) {
            return null;
        }
        String label = String.format(DISCOVERY_LABEL_PATTERN, baseUrl.getHost());
        String modelName = EXPECTED_MODEL_NAME_PREFIX;
        ModelDetails modelDetails = details.getModelDetails();
        if (modelDetails != null && modelDetails.getModelName() != null && modelDetails.getModelNumber() != null) {
            modelName = String.format("%s (%s)", modelDetails.getModelName(), modelDetails.getModelNumber());
        }
        return DiscoveryResultBuilder.create(uid) //
                .withProperties(Map.of( //
                        HOST, baseUrl.getHost(), //
                        PORT, baseUrl.getPort(), //
                        PROTOCOL, baseUrl.getProtocol(), //
                        Thing.PROPERTY_MODEL_ID, modelName, //
                        Thing.PROPERTY_SERIAL_NUMBER, serialNumber.toLowerCase())) //
                .withLabel(label) //
                .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER) //
                .build();
    }

    @Override
    public @Nullable ThingUID getThingUID(RemoteDevice device) {
        DeviceDetails details = device.getDetails();
        if (details == null) {
            return null;
        }
        String serialNumber = details.getSerialNumber();
        ModelDetails modelDetails = details.getModelDetails();
        if (serialNumber == null || serialNumber.isBlank() || modelDetails == null) {
            return null;
        }
        String modelName = modelDetails.getModelName();
        // Model name has the format "Philips hue bridge <year>" with <year> being 2012
        // for a hue bridge V1 or 2015 for a hue bridge V2.
        if (modelName == null || !modelName.startsWith(EXPECTED_MODEL_NAME_PREFIX)) {
            return null;
        }
        try {
            Pattern pattern = Pattern.compile("\\d{4}");
            Matcher matcher = pattern.matcher(modelName);
            int year = Integer.parseInt(matcher.find() ? matcher.group() : "9999");
            // The bridge is ignored if year is greater or equal to 2015
            if (year >= 2015) {
                return null;
            }
        } catch (PatternSyntaxException | NumberFormatException e) {
            // No int value found, this bridge is ignored
            return null;
        }
        return new ThingUID(THING_TYPE_BRIDGE, serialNumber.toLowerCase());
    }

    @Override
    public long getRemovalGracePeriodSeconds(RemoteDevice device) {
        return removalGracePeriod;
    }
}
