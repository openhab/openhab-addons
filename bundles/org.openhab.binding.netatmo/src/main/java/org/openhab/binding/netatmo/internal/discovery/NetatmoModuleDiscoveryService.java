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
package org.openhab.binding.netatmo.internal.discovery;

import static org.openhab.binding.netatmo.internal.APIUtils.*;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.handler.NetatmoBridgeHandler;
import org.openhab.binding.netatmo.internal.handler.NetatmoDataListener;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import io.swagger.client.model.NAHealthyHomeCoach;
import io.swagger.client.model.NAMain;
import io.swagger.client.model.NAPlug;
import io.swagger.client.model.NAStationModule;
import io.swagger.client.model.NAWelcomeCamera;
import io.swagger.client.model.NAWelcomeHome;

/**
 * The {@link NetatmoModuleDiscoveryService} searches for available Netatmo
 * devices and modules connected to the API console
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
@NonNullByDefault
public class NetatmoModuleDiscoveryService extends AbstractDiscoveryService implements NetatmoDataListener {
    private static final int SEARCH_TIME = 5;
    private final NetatmoBridgeHandler netatmoBridgeHandler;

    public NetatmoModuleDiscoveryService(NetatmoBridgeHandler netatmoBridgeHandler, LocaleProvider localeProvider,
            TranslationProvider translationProvider) {
        super(SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME);
        this.netatmoBridgeHandler = netatmoBridgeHandler;
        this.localeProvider = localeProvider;
        this.i18nProvider = translationProvider;
    }

    @Override
    public void activate(@Nullable Map<String, Object> configProperties) {
        super.activate(configProperties);
        netatmoBridgeHandler.registerDataListener(this);
    }

    @Override
    public void deactivate() {
        netatmoBridgeHandler.unregisterDataListener(this);
        super.deactivate();
    }

    @Override
    public void startScan() {
        if (netatmoBridgeHandler.configuration.readStation) {
            netatmoBridgeHandler.getStationsDataBody(null).ifPresent(dataBody -> {
                nonNullList(dataBody.getDevices()).forEach(station -> {
                    discoverWeatherStation(station);
                });
            });
        }
        if (netatmoBridgeHandler.configuration.readHealthyHomeCoach) {
            netatmoBridgeHandler.getHomecoachDataBody(null).ifPresent(dataBody -> {
                nonNullList(dataBody.getDevices()).forEach(homecoach -> {
                    discoverHomeCoach(homecoach);
                });
            });
        }
        if (netatmoBridgeHandler.configuration.readThermostat) {
            netatmoBridgeHandler.getThermostatsDataBody(null).ifPresent(dataBody -> {
                nonNullList(dataBody.getDevices()).forEach(plug -> {
                    discoverThermostat(plug);
                });
            });
        }
        if (netatmoBridgeHandler.configuration.readWelcome || netatmoBridgeHandler.configuration.readPresence) {
            netatmoBridgeHandler.getWelcomeDataBody(null).ifPresent(dataBody -> {
                nonNullList(dataBody.getHomes()).forEach(home -> {
                    discoverWelcomeHome(home);
                });
            });
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan(), netatmoBridgeHandler.getThing().getUID());
    }

    @Override
    public void onDataRefreshed(Object data) {
        if (!isBackgroundDiscoveryEnabled()) {
            return;
        }
        if (data instanceof NAMain) {
            discoverWeatherStation((NAMain) data);
        } else if (data instanceof NAPlug) {
            discoverThermostat((NAPlug) data);
        } else if (data instanceof NAHealthyHomeCoach) {
            discoverHomeCoach((NAHealthyHomeCoach) data);
        } else if (data instanceof NAWelcomeHome) {
            discoverWelcomeHome((NAWelcomeHome) data);
        }
    }

    private void discoverThermostat(NAPlug plug) {
        onDeviceAddedInternal(plug.getId(), null, plug.getType(), plug.getStationName(), plug.getFirmware());
        nonNullList(plug.getModules()).forEach(thermostat -> {
            onDeviceAddedInternal(thermostat.getId(), plug.getId(), thermostat.getType(), thermostat.getModuleName(),
                    thermostat.getFirmware());
        });
    }

    private void discoverHomeCoach(NAHealthyHomeCoach homecoach) {
        onDeviceAddedInternal(homecoach.getId(), null, homecoach.getType(), homecoach.getName(),
                homecoach.getFirmware());
    }

    private void discoverWeatherStation(NAMain station) {
        final boolean isFavorite = station.isFavorite() != null && station.isFavorite();
        final String weatherStationName = createWeatherStationName(station, isFavorite);

        onDeviceAddedInternal(station.getId(), null, station.getType(), weatherStationName, station.getFirmware());
        nonNullList(station.getModules()).forEach(module -> {
            onDeviceAddedInternal(module.getId(), station.getId(), module.getType(),
                    createWeatherModuleName(station, module, isFavorite), module.getFirmware());
        });
    }

    private void discoverWelcomeHome(NAWelcomeHome home) {
        // I observed that Thermostat homes are also reported here by Netatmo API
        // So I ignore homes that have an empty list of cameras
        List<NAWelcomeCamera> cameras = nonNullList(home.getCameras());
        if (!cameras.isEmpty()) {
            onDeviceAddedInternal(home.getId(), null, WELCOME_HOME_THING_TYPE.getId(), home.getName(), null);
            // Discover Cameras
            cameras.forEach(camera -> {
                onDeviceAddedInternal(camera.getId(), home.getId(), camera.getType(), camera.getName(), null);
            });

            // Discover Known Persons
            nonNullStream(home.getPersons()).filter(person -> person.getPseudo() != null).forEach(person -> {
                onDeviceAddedInternal(person.getId(), home.getId(), WELCOME_PERSON_THING_TYPE.getId(),
                        person.getPseudo(), null);
            });
        }
    }

    private void onDeviceAddedInternal(String id, @Nullable String parentId, String type, String name,
            @Nullable Integer firmwareVersion) {
        ThingUID thingUID = findThingUID(type, id);
        Map<String, Object> properties = new HashMap<>();

        properties.put(EQUIPMENT_ID, id);
        if (parentId != null) {
            properties.put(PARENT_ID, parentId);
        }
        if (firmwareVersion != null) {
            properties.put(Thing.PROPERTY_VENDOR, VENDOR);
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
            properties.put(Thing.PROPERTY_MODEL_ID, type);
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, id);
        }
        addDiscoveredThing(thingUID, properties, name);
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel) {
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(netatmoBridgeHandler.getThing().getUID()).withLabel(displayLabel)
                .withRepresentationProperty(EQUIPMENT_ID).build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID findThingUID(String thingType, String thingId) throws IllegalArgumentException {
        for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
            String uid = supportedThingTypeUID.getId();

            if (uid.equalsIgnoreCase(thingType)) {
                return new ThingUID(supportedThingTypeUID, netatmoBridgeHandler.getThing().getUID(),
                        thingId.replaceAll("[^a-zA-Z0-9_]", ""));
            }
        }

        throw new IllegalArgumentException("Unsupported device type discovered : " + thingType);
    }

    private String createWeatherStationName(NAMain station, boolean isFavorite) {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(localizeType(station.getType()));
        if (station.getStationName() != null) {
            nameBuilder.append(' ');
            nameBuilder.append(station.getStationName());
        }
        if (isFavorite) {
            nameBuilder.append(" (favorite)");
        }
        return nameBuilder.toString();
    }

    private String createWeatherModuleName(NAMain station, NAStationModule module, boolean isFavorite) {
        StringBuilder nameBuilder = new StringBuilder();
        if (module.getModuleName() != null) {
            nameBuilder.append(module.getModuleName());
        } else {
            nameBuilder.append(localizeType(module.getType()));
        }
        if (station.getStationName() != null) {
            nameBuilder.append(' ');
            nameBuilder.append(station.getStationName());
        }
        if (isFavorite) {
            nameBuilder.append(" (favorite)");
        }
        return nameBuilder.toString();
    }

    private String localizeType(String typeName) {
        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        @Nullable
        String localizedType = i18nProvider.getText(bundle, "thing-type.netatmo." + typeName + ".label", typeName,
                localeProvider.getLocale());
        if (localizedType != null) {
            return localizedType;
        }
        return typeName;
    }
}
