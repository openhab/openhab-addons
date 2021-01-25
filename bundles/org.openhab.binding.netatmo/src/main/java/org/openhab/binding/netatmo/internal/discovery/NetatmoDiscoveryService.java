/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.AircareApi;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.ConnectionListener;
import org.openhab.binding.netatmo.internal.api.ConnectionStatus;
import org.openhab.binding.netatmo.internal.api.EnergyApi;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.WeatherApi;
import org.openhab.binding.netatmo.internal.api.dto.NADevice;
import org.openhab.binding.netatmo.internal.api.dto.NADeviceDataBody;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAMain;
import org.openhab.binding.netatmo.internal.api.dto.NAPlug;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetatmoDiscoveryService} searches for available Netatmo
 * devices and modules connected to the API console
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
@Component(service = DiscoveryService.class, configurationPid = "binding.netatmo")
@NonNullByDefault
public class NetatmoDiscoveryService extends AbstractDiscoveryService implements ConnectionListener {
    private static final int DISCOVER_TIMEOUT_SECONDS = 10;
    private final Bundle bundle = FrameworkUtil.getBundle(this.getClass());
    private final Logger logger = LoggerFactory.getLogger(NetatmoDiscoveryService.class);
    // private final Map<String, Object> configProperties;
    private final ApiBridge apiBridge;

    // TODO : il ne faudrait pas que le discovery soit validé / lancé si l'API bridge n'est pas correctement connecté
    @Activate
    public NetatmoDiscoveryService(@Reference ApiBridge apiBridge, @Reference LocaleProvider localeProvider,
            @Reference TranslationProvider translationProvider/* , ComponentContext componentContext */) {

        super(Stream.of(ModuleType.values()).map(supported -> supported.thingTypeUID).collect(Collectors.toSet()),
                DISCOVER_TIMEOUT_SECONDS);
        this.apiBridge = apiBridge;
        this.localeProvider = localeProvider;
        this.i18nProvider = translationProvider;
        // this.configProperties = BindingUtils.ComponentContextToMap(componentContext);
        apiBridge.addConnectionListener(this);
    }

    @Override
    public void notifyStatusChange(ConnectionStatus connectionStatus) {
        if (connectionStatus.isConnected()) {
            super.activate(null /* configProperties */);
        } else {
            super.deactivate();
        }
    }

    // @Override
    // public void activate(@Nullable Map<String, Object> configProperties) {
    // super.activate(configProperties);
    // // netatmoBridgeHandler.registerDataListener(this);
    // }
    //
    // @Override
    // public void deactivate() {
    // // netatmoBridgeHandler.unregisterDataListener(this);
    // super.deactivate();
    // }

    @Override
    public void startScan() {
        apiBridge.getWeatherApi().ifPresent(api -> searchWeatherStation(api));
        apiBridge.getEnergyApi().ifPresent(api -> searchThermostat(api));
        apiBridge.getAirCareApi().ifPresent(api -> searchHomeCoach(api));
        apiBridge.getSecurityApi().ifPresent(api -> searchCameras(api));
    }

    private void searchCameras(SecurityApi api) {
        try {
            NAHomeData result = api.getWelcomeDataBody();
            result.getHomes().forEach(device -> discoverHome(device));
        } catch (NetatmoException e) {
            logger.warn("Error retrieving camras(s)", e);
        }
    }

    private void searchHomeCoach(AircareApi api) {
        // try {
        // NAMain result = api.getHomeCoachDataBody(null);
        // result.getDevices().forEach(device -> discoverHomeCoach(device));
        // } catch (NetatmoException e) {
        // logger.warn("Error retrieving thermostat(s)", e);
        // }
    }

    private void searchThermostat(EnergyApi api) {
        try {
            NADeviceDataBody<NAPlug> search = api.getThermostatsDataBody(null);
            for (NAPlug plug : search.getDevices().values()) {
                String plugId = plug.getId();
                NAHomeData result = apiBridge.getHomeApi().getHomesData(plug.getType());
                for (NAHome home : result.getHomes()) {
                    if (home.getChild(plugId) != null) {
                        discoverThermostat(plug, home);
                        return;
                    }
                }
                throw new NetatmoException("No home attached to the thermostat plug");
            }
        } catch (NetatmoException e) {
            logger.warn("Error retrieving thermostat(s)", e);
        }
    }

    private void searchWeatherStation(WeatherApi api) {
        try {
            NADeviceDataBody<NAMain> search = api.getStationsDataBody(null);
            for (NAMain station : search.getDevices().values()) {
                // String stationId = station.getId();
                // NAHome attachedHome = null;
                // NAHomeData result = apiBridge.getHomeApi().getHomesData(station.getType());
                // for (NAHome home : result.getHomes()) {
                // for (NAThing child : home.getChilds()) {
                // if (stationId.equals(child.getId())) {
                // attachedHome = home;
                // break;
                // }
                // }
                // }
                // if (attachedHome != null) {
                discoverWeatherStation(station);
                // } else {
                // throw new NetatmoException("No home attached to the thermostat plug");
                // }
            }
        } catch (NetatmoException e) {
            logger.warn("Error retrieving own weather stations", e);
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        // removeOlderResults(getTimestampOfLastScan(), bridgeUID);
    }

    /*
     * @Override
     * public void onDataRefreshed(Object data) {
     * if (!isBackgroundDiscoveryEnabled()) {
     * return;
     * }
     * if (data instanceof NAMain) {
     * discoverWeatherStation((NAMain) data);
     * } else if (data instanceof NAPlug) {
     * discoverThermostat((NAPlug) data);
     * } else if (data instanceof NAHealthyHomeCoach) {
     * discoverHomeCoach((NAHealthyHomeCoach) data);
     * } else if (data instanceof NAWelcomeHome) {
     * discoverWelcomeHome((NAWelcomeHome) data);
     * }
     * }
     */

    private void discoverThermostat(NAPlug plug, NAHome home) {
        ThingUID homeUID = onDeviceAddedInternal(home.getId(), null, ModuleType.NAHomeEnergy, home.getName(), null);
        ThingUID plugUID = onDeviceAddedInternal(plug.getId(), homeUID, plug.getType(), plug.getName(),
                plug.getFirmware());
        plug.getChilds().values().stream().forEach(thermostat -> {
            onDeviceAddedInternal(thermostat.getId(), plugUID, thermostat.getType(), thermostat.getName(),
                    thermostat.getFirmware());
        });
    }

    private void discoverWeatherStation(NAMain station) {
        final boolean isFavorite = station.isFavorite();
        final String weatherStationName = createNAThingName(station, isFavorite);

        ThingUID stationUID = onDeviceAddedInternal(station.getId(), null, station.getType(), weatherStationName,
                station.getFirmware());

        station.getChilds().values().forEach(module -> {
            onDeviceAddedInternal(module.getId(), stationUID, module.getType(), createNAThingName(module, isFavorite),
                    module.getFirmware());
        });
    }

    private void discoverHomeCoach(NADevice<?> homecoach) {
        onDeviceAddedInternal(homecoach.getId(), null, homecoach.getType(), homecoach.getName(),
                homecoach.getFirmware());
    }

    private void discoverHome(NAHome home) {
        Collection<NAWelcome> cameras = home.getChilds().values();
        if (!cameras.isEmpty()) {// Thermostat homes are also reported here by Netatmo API, so ignore homes that have an
                                 // empty list of cameras
            ThingUID homeUID = onDeviceAddedInternal(home.getId(), null, ModuleType.NAHomeSecurity, home.getName(),
                    null);
            cameras.forEach(camera -> {
                onDeviceAddedInternal(camera.getId(), homeUID, camera.getType(), camera.getName(), null);
            });
            home.getKnownPersons().forEach(
                    person -> onDeviceAddedInternal(person.getId(), homeUID, person.getType(), person.getName(), null));
        }
    }

    private ThingUID onDeviceAddedInternal(String id, @Nullable ThingUID brigdeUID, ModuleType type,
            @Nullable String name, @Nullable Integer firmwareVersion/* , @Nullable String homeId */) {
        ThingUID thingUID = findThingUID(type, id, brigdeUID);
        Map<String, Object> properties = new HashMap<>();

        properties.put(EQUIPMENT_ID, id);
        // if (homeId != null) {
        // properties.put(HOME_ID, homeId);
        // }
        if (firmwareVersion != null) {
            properties.put(Thing.PROPERTY_MODEL_ID, type);
            properties.put(Thing.PROPERTY_VENDOR, VENDOR);
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, firmwareVersion);
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, id);
        }
        addDiscoveredThing(thingUID, properties, name != null ? name : id, brigdeUID);
        return thingUID;
    }

    private void addDiscoveredThing(ThingUID thingUID, Map<String, Object> properties, String displayLabel,
            @Nullable ThingUID brigdeUID) {
        DiscoveryResultBuilder resultBuilder = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withLabel(displayLabel).withRepresentationProperty(EQUIPMENT_ID);
        if (brigdeUID != null) {
            resultBuilder = resultBuilder.withBridge(brigdeUID);
        }
        thingDiscovered(resultBuilder.build());
    }

    /*
     * private @Nullable ThingUID findThingUID2(NAMain station, ThingUID brigdeUID) throws IllegalArgumentException {
     *
     * ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, station.getType().toString());
     * String label = localizeLabel(station.getType());
     * List<String> supportedBridgeTypeUids = new ArrayList<>();
     * supportedBridgeTypeUids.add(NAHOME_THING_TYPE.toString());
     * String description = localizeDescription(station.getType());
     *
     * List<ChannelGroupDefinition> groupDefinitions = new ArrayList<>();
     * for (MeasureType dataType : station.getDataType()) {
     * String id = dataType.name().toLowerCase();
     * ChannelGroupTypeUID groupType = new ChannelGroupTypeUID(BINDING_ID, id + "-group");
     * groupDefinitions.add(new ChannelGroupDefinition(id, groupType));
     * }
     * if (station.canDeriveWeather()) {
     * String id = "derived";
     * ChannelGroupTypeUID groupType = new ChannelGroupTypeUID(BINDING_ID, id + "-group");
     * groupDefinitions.add(new ChannelGroupDefinition(id, groupType));
     * }
     *
     * String id = "device-common";
     * ChannelGroupTypeUID groupType = new ChannelGroupTypeUID(BINDING_ID, id + "-group");
     * groupDefinitions.add(new ChannelGroupDefinition(id, groupType));
     *
     * Map<String, String> properties = new HashMap<>();
     * properties.put(Thing.PROPERTY_VENDOR, VENDOR);
     * properties.put(Thing.PROPERTY_MODEL_ID, station.getType().toString());
     * properties.putAll(MetadataUtils.getProperties(station.getType()));
     *
     * List<String> extensibleChannelTypeIds = MetadataUtils.getExtensions(station.getType());
     *
     * URI configDescriptionURI = getConfigDescriptionURI();
     * if (configDescriptionURI != null) {
     *
     * ThingType thingType = ThingTypeBuilder.instance(thingTypeUID, label)
     * .withExtensibleChannelTypeIds(extensibleChannelTypeIds)
     * .withSupportedBridgeTypeUIDs(supportedBridgeTypeUids).withDescription(description)
     * .withChannelGroupDefinitions(groupDefinitions).withProperties(properties)
     * .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS)
     * .withConfigDescriptionURI(configDescriptionURI).build();
     *
     * String thingId = station.getId().replaceAll("[^a-zA-Z0-9_]", "");
     * return new ThingUID(thingType.getUID(), brigdeUID, thingId);
     * }
     * return null;
     *
     * }
     */

    // private @Nullable URI getConfigDescriptionURI() {
    // try {
    // return new URI("thing-type:netatmo:device");
    // } catch (URISyntaxException ex) {
    // logger.warn("Can't create configDescriptionURI for device type");
    // return null;
    // }
    // }

    private ThingUID findThingUID(ModuleType thingType, String thingId, @Nullable ThingUID brigdeUID)
            throws IllegalArgumentException {

        for (ThingTypeUID supported : getSupportedThingTypes()) {
            if (supported.getId().equalsIgnoreCase(thingType.name())) {
                String id = thingId.replaceAll("[^a-zA-Z0-9_]", "");
                if (brigdeUID == null) {
                    return new ThingUID(supported, id);
                }
                return new ThingUID(supported, brigdeUID, id);
            }
        }
        // String thingTypeName = thingType.name();
        // for (ThingTypeUID supportedThingTypeUID : getSupportedThingTypes()) {
        // String uid = supportedThingTypeUID.getId();
        // for (ThingTypeUID bridgeUid : SUPPORTED_BRIDGES_TYPES_UIDS) {
        // String bridgeUidId = bridgeUid.getId();
        // if (bridgeUidId.equals(thingTypeName)) {
        // return new ThingUID(supportedThingTypeUID, thingId.replaceAll("[^a-zA-Z0-9_]", ""));
        // }
        // }
        // }
        throw new IllegalArgumentException("Unsupported device type discovered : " + thingType);
    }

    private String createNAThingName(NAThing thing, boolean isFavorite) {
        StringBuilder nameBuilder = new StringBuilder();
        if (thing.getName() != null) {
            nameBuilder.append(thing.getName());
            nameBuilder.append(" - ");
        }
        nameBuilder.append(localizeLabel(thing.getType()));
        if (isFavorite) {
            nameBuilder.append(" (favorite)");
        }
        return nameBuilder.toString();
    }

    // private String createWeatherModuleName(NAMain station, NAStationModule module, boolean isFavorite) {
    // String modulePart = createNAThingName(module, false);
    // String stationPart = createNAThingName(station, isFavorite);
    // return modulePart + (" (" + stationPart + ")");
    // }

    private String localizeLabel(ModuleType moduleType) {
        String typeName = moduleType.name();
        String localizedType = i18nProvider.getText(bundle, "thing-type.netatmo." + typeName + ".label", null,
                localeProvider.getLocale());

        return localizedType != null ? localizedType : typeName;
    }

    // private String localizeDescription(ModuleType moduleType) {
    // String typeName = moduleType.name();
    // String localizedType = i18nProvider.getText(bundle, "thing-type.netatmo." + typeName + ".description", null,
    // localeProvider.getLocale());
    //
    // return localizedType != null ? localizedType : typeName;
    // }
}
