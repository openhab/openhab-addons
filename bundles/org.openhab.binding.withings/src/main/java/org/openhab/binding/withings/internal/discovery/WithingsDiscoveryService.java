/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.discovery;

import static org.openhab.binding.withings.internal.WithingsBindingConstants.PERSON_THING_TYPE;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.withings.internal.WithingsBindingConstants;
import org.openhab.binding.withings.internal.api.device.DevicesHandler;
import org.openhab.binding.withings.internal.api.device.DevicesResponseDTO;
import org.openhab.binding.withings.internal.handler.WithingsBridgeHandler;
import org.openhab.binding.withings.internal.service.AccessTokenService;
import org.openhab.binding.withings.internal.service.person.Person;
import org.openhab.binding.withings.internal.service.person.PersonHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WithingsDiscoveryService} searches for available Withings
 * devices/things.
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class WithingsDiscoveryService extends AbstractDiscoveryService {

    private static final int SEARCH_TIME_SECONDS = 5;

    private final Logger logger = LoggerFactory.getLogger(WithingsDiscoveryService.class);

    private final WithingsBridgeHandler bridgeHandler;
    private final AccessTokenService accessTokenService;
    private final HttpClient httpClient;

    public WithingsDiscoveryService(WithingsBridgeHandler bridgeHandler, AccessTokenService accessTokenService,
            HttpClient httpClient, LocaleProvider localeProvider, TranslationProvider translationProvider) {
        super(WithingsBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME_SECONDS);
        this.bridgeHandler = bridgeHandler;
        this.accessTokenService = accessTokenService;
        this.httpClient = httpClient;
        this.localeProvider = localeProvider;
        this.i18nProvider = translationProvider;
    }

    @Override
    public void activate(@Nullable Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void startScan() {
        logger.debug("Starting scan for Withings devices...");

        DevicesHandler devicesHandler = new DevicesHandler(accessTokenService, httpClient);
        List<DevicesResponseDTO.Device> devices = devicesHandler.loadDevices();
        for (DevicesResponseDTO.Device device : devices) {

            Optional<ThingUID> thingUID = findThingUID(device.getType(), device.getDeviceId());
            if (thingUID.isPresent()) {

                Map<String, Object> properties = new TreeMap<>();
                properties.put(WithingsBindingConstants.PROPERTY_DEVICE_ID, String.valueOf(device.getDeviceId()));
                properties.put(Thing.PROPERTY_MODEL_ID, String.valueOf(device.getModelId()));
                properties.put(WithingsBindingConstants.PROPERTY_DEVICE_MODEL, device.getModel());

                String name = createDeviceThingName(device);

                discovered(thingUID.get(), name, properties);
            }
        }

        PersonHandler personHandler = new PersonHandler(accessTokenService, httpClient);
        Optional<Person> person = personHandler.loadPerson();
        if (person.isPresent()) {
            Optional<String> userIdOptional = accessTokenService.getUserId();
            if (userIdOptional.isPresent()) {
                String userId = userIdOptional.get();

                ThingUID thingUID = new ThingUID(PERSON_THING_TYPE, bridgeHandler.getThing().getUID(), userId);

                Map<String, Object> properties = new TreeMap<>();
                properties.put(WithingsBindingConstants.PROPERTY_PERSON_USER_ID, userId);

                String name = WithingsBindingConstants.VENDOR + " Person";

                discovered(thingUID, name, properties);
            }
        }
    }

    private Optional<ThingUID> findThingUID(String thingType, String thingId) throws IllegalArgumentException {
        final String thingTypeNormalized = thingType.replaceAll("\\s", "");

        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String thingTypeUID = supportedThingTypeUID.getId();

            if (thingTypeUID.equalsIgnoreCase(thingTypeNormalized)) {
                return Optional.of(new ThingUID(supportedThingTypeUID, bridgeHandler.getThing().getUID(), thingId));
            }
        }

        logger.debug("Unsupported device type discovered: {}", thingType);
        return Optional.empty();
    }

    private void discovered(ThingUID thingUID, String name, Map<String, Object> properties) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(bridgeHandler.getThing().getUID()).withLabel(name).build();

        thingDiscovered(discoveryResult);
    }

    private static String createDeviceThingName(DevicesResponseDTO.Device device) {
        if (WithingsBindingConstants.SCALE_THING_TYPE.getId().equalsIgnoreCase(device.getType())) {
            return WithingsBindingConstants.VENDOR + ' ' + device.getModel() + " Scale";
        }
        return WithingsBindingConstants.VENDOR + ' ' + device.getType();
    }
}
