/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sonyaudio.internal.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.sonyaudio.internal.SonyAudioBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.upnp.UpnpDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class identifies SONY products by their Upnp service information.
 *
 * @author David Åberg - Initial contribution
 */
@Component
public class SonyAudioDiscoveryParticipant implements UpnpDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(SonyAudioDiscoveryParticipant.class);

    private Set<ThingTypeUID> supportedThingTypes;

    public SonyAudioDiscoveryParticipant() {
        this.supportedThingTypes = SonyAudioBindingConstants.SUPPORTED_THING_TYPES_UIDS;
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
            URL descriptorURL = device.getIdentity().getDescriptorURL();
            String host = descriptorURL.getHost();
            int port = descriptorURL.getPort();
            String path = descriptorURL.getPath();
            try {
                Map<String, Object> properties = getDescription(host, port, path);
                properties.put(SonyAudioBindingConstants.HOST_PARAMETER, descriptorURL.getHost());
                result = DiscoveryResultBuilder.create(thingUid).withLabel(label).withProperties(properties).build();
            } catch (IOException e) {
                return null;
            }
        }
        return result;
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        ThingUID result = null;

        String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
        if (manufacturer == null
                || !manufacturer.toLowerCase().contains(SonyAudioBindingConstants.MANUFACTURER.toLowerCase())) {
            return result;
        }

        logger.debug("Manufacturer matched: search: {}, device value: {}.", SonyAudioBindingConstants.MANUFACTURER,
                manufacturer);
        String type = device.getType().getType();
        if (type == null || !type.toLowerCase().contains(SonyAudioBindingConstants.UPNP_DEVICE_TYPE.toLowerCase())) {
            return result;
        }
        logger.debug("Device type matched: search: {}, device value: {}.", SonyAudioBindingConstants.UPNP_DEVICE_TYPE,
                type);
        logger.debug("Device services: {}", device.getServices().toString());
        String deviceModel = device.getDetails().getModelDetails() != null
                ? device.getDetails().getModelDetails().getModelName()
                : null;
        logger.debug("Device model: {}.", deviceModel);
        ThingTypeUID thingTypeUID = findThingType(deviceModel);
        if (thingTypeUID != null) {
            result = new ThingUID(thingTypeUID, device.getIdentity().getUdn().getIdentifierString());
        }
        return result;
    }

    private ThingTypeUID findThingType(String deviceModel) {
        ThingTypeUID thingTypeUID = null;
        for (ThingTypeUID thingType : SonyAudioBindingConstants.SUPPORTED_THING_TYPES_UIDS) {
            if (thingType.getId().equalsIgnoreCase(deviceModel)) {
                return thingType;
            }
        }

        return thingTypeUID;
    }

    private Map<String, Object> getDescription(String host, int port, String path) throws IOException {
        Map<String, Object> properties = new HashMap<>(2, 1);
        URL url = new URL("http", host, port, path);
        logger.debug("URL: {}", url.toString());
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String s;
            StringBuilder builder = new StringBuilder();
            while ((s = bufferedReader.readLine()) != null) {
                builder.append(s);
            }
            Pattern ScalarWebAPImatch = Pattern.compile("<av:X_ScalarWebAPI_BaseURL>(.*)</av:X_ScalarWebAPI_BaseURL>");
            Pattern baseURLmatch = Pattern.compile("http://(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d+)([^<]*)");

            Matcher tagmatch = ScalarWebAPImatch.matcher(builder.toString());
            if (tagmatch.find()) {
                Matcher matcher = baseURLmatch.matcher(tagmatch.group());
                matcher.find();
                // String scalar_host = matcher.group(0);
                int scalar_port = Integer.parseInt(matcher.group(2));
                String scalar_path = matcher.group(3);

                properties.put(SonyAudioBindingConstants.SCALAR_PORT_PARAMETER, scalar_port);
                properties.put(SonyAudioBindingConstants.SCALAR_PATH_PARAMETER, scalar_path);
            }
            return properties;
        }
    }
}
