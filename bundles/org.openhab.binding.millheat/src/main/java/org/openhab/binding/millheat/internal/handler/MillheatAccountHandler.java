/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.millheat.internal.handler;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.millheat.internal.MillheatCommunicationException;
import org.openhab.binding.millheat.internal.client.MillheatCloudApiClient;
import org.openhab.binding.millheat.internal.client.RequestLogger;
import org.openhab.binding.millheat.internal.config.MillheatAccountConfiguration;
import org.openhab.binding.millheat.internal.discovery.MillheatDiscoveryService;
import org.openhab.binding.millheat.internal.dto.DeviceDTO;
import org.openhab.binding.millheat.internal.dto.DeviceSettingsPatchRequest;
import org.openhab.binding.millheat.internal.dto.HeaterShadowDTO;
import org.openhab.binding.millheat.internal.dto.HouseDTO;
import org.openhab.binding.millheat.internal.dto.HousesResponse;
import org.openhab.binding.millheat.internal.dto.RoomDevicesDTO;
import org.openhab.binding.millheat.internal.dto.RoomInfoDTO;
import org.openhab.binding.millheat.internal.dto.RoomTemperatureRequest;
import org.openhab.binding.millheat.internal.dto.VacationModeRequest;
import org.openhab.binding.millheat.internal.model.Heater;
import org.openhab.binding.millheat.internal.model.Home;
import org.openhab.binding.millheat.internal.model.MillheatModel;
import org.openhab.binding.millheat.internal.model.ModeType;
import org.openhab.binding.millheat.internal.model.Room;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Owns the connection to the Mill cloud service and the model snapshot every other handler reads
 * from.
 *
 * @author Arne Seime - Initial contribution
 * @author Petter L. H. Eide - Rewritten for the MillNorway cloud API
 */
@NonNullByDefault
public class MillheatAccountHandler extends BaseBridgeHandler {
    /** Vacation properties a home handler can write. */
    public static final String VACATION_PROP_MODE = "mode";
    public static final String VACATION_PROP_ADVANCED = "advanced";
    public static final String VACATION_PROP_START = "start";
    public static final String VACATION_PROP_END = "end";
    public static final String VACATION_PROP_TEMP = "temperature";

    private static final int MIN_TIME_BETWEEN_MODEL_UPDATES_MS = 30_000;
    private static final int REINITIALIZE_DELAY_SECONDS = 30;

    private final Logger logger = LoggerFactory.getLogger(MillheatAccountHandler.class);
    private final MillheatCloudApiClient client;
    private final Gson gson;

    private MillheatModel model = new MillheatModel(0);
    private @Nullable ScheduledFuture<?> statusFuture;
    private @Nullable MillheatDiscoveryService discoveryService;
    private @NonNullByDefault({}) MillheatAccountConfiguration config;

    public MillheatAccountHandler(final Bridge bridge, final HttpClient httpClient, final BundleContext context) {
        super(bridge);
        gson = new GsonBuilder().create();
        client = new MillheatCloudApiClient(httpClient, gson, new RequestLogger(bridge.getUID().getId(), gson));
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MillheatDiscoveryService.class);
    }

    /** Called by the discovery service so the bridge can trigger the first scan itself. */
    public void setDiscoveryService(final @Nullable MillheatDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public MillheatModel getModel() {
        return model;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        logger.debug("Bridge does not support any commands, but received command {} for channelUID {}", command,
                channelUID);
    }

    @Override
    public void initialize() {
        config = getConfigAs(MillheatAccountConfiguration.class);
        scheduler.execute(this::connect);
    }

    /** Authenticates with the configured credentials. */
    public void signIn() throws MillheatCommunicationException {
        // Normally populated by initialize(); resolved here as well so the handler can sign in
        // without having gone through the full thing lifecycle.
        MillheatAccountConfiguration localConfig = config;
        if (localConfig == null) {
            localConfig = getConfigAs(MillheatAccountConfiguration.class);
            config = localConfig;
        }
        client.signIn(localConfig.username, localConfig.password);
    }

    private void connect() {
        try {
            signIn();
        } catch (final MillheatCommunicationException e) {
            // Bad credentials are a configuration problem; anything else is worth retrying.
            if (e.getHttpStatus() == 401 || e.getHttpStatus() == 400) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Could not sign in to the Mill cloud API: " + e.getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                scheduleReinitialize();
            }
            return;
        }

        try {
            model = refreshModel();
            updateStatus(ThingStatus.ONLINE);
            updateThingStatuses();
            initPolling();
            // The background discovery job deliberately waits a full period before its first run,
            // so kick off a scan here now that there is a model to scan.
            final MillheatDiscoveryService localDiscoveryService = discoveryService;
            if (localDiscoveryService != null) {
                scheduler.execute(localDiscoveryService::scanNow);
            }
        } catch (final MillheatCommunicationException e) {
            model = new MillheatModel(0);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error fetching initial data: " + e.getMessage());
            logger.debug("Error initializing Mill data", e);
            scheduleReinitialize();
        }
    }

    private void scheduleReinitialize() {
        scheduler.schedule(this::connect, REINITIALIZE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        stopPolling();
        client.clearTokens();
        super.dispose();
    }

    private void initPolling() {
        stopPolling();
        statusFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                updateModelFromServerWithRetry(true);
            } catch (final RuntimeException e) {
                logger.debug("Error refreshing model", e);
            }
        }, config.refreshInterval, config.refreshInterval, TimeUnit.SECONDS);
    }

    private void stopPolling() {
        final ScheduledFuture<?> localFuture = statusFuture;
        if (localFuture != null && !localFuture.isCancelled()) {
            localFuture.cancel(true);
        }
        statusFuture = null;
    }

    /**
     * Rebuilds the model. One request per house returns every device with its settings and
     * telemetry embedded; a further request per room supplies the room's setpoints and active mode.
     */
    public MillheatModel refreshModel() throws MillheatCommunicationException {
        final MillheatModel newModel = new MillheatModel(System.currentTimeMillis());
        final HousesResponse houses = client.getHouses();

        final MillheatModel previousModel = model;
        for (final HouseDTO houseDto : allHouses(houses)) {
            final Home home = new Home(houseDto);
            previousModel.findHomeById(home.getId()).ifPresent(home::carryStagedVacationSettings);
            newModel.addHome(home);

            for (final RoomDevicesDTO roomDevices : client.getHouseDevices(home.getId())) {
                final RoomInfoDTO roomInfo = client.getRoomInfo(roomDevices.roomId());
                final Room room = roomInfo != null ? new Room(roomInfo, home)
                        : new Room(roomDevices.roomId(), nameOf(roomDevices), home);
                home.addRoom(room);

                final List<DeviceDTO> devices = roomDevices.devices();
                if (devices != null) {
                    for (final DeviceDTO device : devices) {
                        room.addHeater(new Heater(device, room));
                    }
                }
            }

            for (final DeviceDTO device : client.getIndependentDevices(home.getId())) {
                home.addHeater(new Heater(device, null));
            }
        }
        return newModel;
    }

    private static String nameOf(final RoomDevicesDTO roomDevices) {
        final String name = roomDevices.roomName();
        return name == null ? roomDevices.roomId() : name;
    }

    private static List<HouseDTO> allHouses(final HousesResponse houses) {
        final List<HouseDTO> own = houses.ownHouses();
        final List<HouseDTO> shared = houses.sharedHouses();
        if (own == null) {
            return shared == null ? List.of() : shared;
        }
        if (shared == null) {
            return own;
        }
        return java.util.stream.Stream.concat(own.stream(), shared.stream()).toList();
    }

    public void updateModelFromServerWithRetry(final boolean forceUpdate) {
        if (!forceUpdate && System.currentTimeMillis() - model.getLastUpdated() <= MIN_TIME_BETWEEN_MODEL_UPDATES_MS) {
            return;
        }
        if (Instant.now().isBefore(client.rateLimitedUntil())) {
            logger.debug("Skipping refresh, the account's request budget is exhausted until {}",
                    client.rateLimitedUntil());
            return;
        }
        try {
            updateModel();
        } catch (final MillheatCommunicationException e) {
            if (e.isRateLimited()) {
                logger.debug("Request budget exhausted, will retry on a later cycle");
                return;
            }
            logger.debug("Error refreshing model, signing in again and retrying: {}", e.getMessage());
            try {
                client.clearTokens();
                signIn();
                updateModel();
            } catch (final MillheatCommunicationException retryFailure) {
                logger.debug("Retry failed, waiting for the next refresh cycle", retryFailure);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, retryFailure.getMessage());
            }
        }
    }

    private void updateModel() throws MillheatCommunicationException {
        model = refreshModel();
        updateThingStatuses();
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateThingStatuses() {
        for (final Thing thing : getThing().getThings()) {
            final ThingHandler handler = thing.getHandler();
            if (handler instanceof MillheatBaseThingHandler millheatHandler) {
                millheatHandler.updateState(model);
            }
        }
    }

    /** Changes one of a room's three program setpoints. */
    public void updateRoomTemperature(final String roomId, final Command command, final ModeType mode) {
        if (!(command instanceof QuantityType<?> quantityCommand)) {
            logger.debug("Cannot set temperature for room {}, expected a QuantityType but got {}", roomId, command);
            return;
        }
        final double newTemp = quantityCommand.doubleValue();
        final RoomTemperatureRequest request = switch (mode) {
            case SLEEP -> new RoomTemperatureRequest(null, newTemp, null);
            case AWAY -> new RoomTemperatureRequest(null, null, newTemp);
            case COMFORT, NORMAL -> new RoomTemperatureRequest(newTemp, null, null);
            default -> null;
        };
        if (request == null) {
            logger.info("Cannot set room temperature for mode {}", mode);
            return;
        }
        try {
            client.setRoomTemperatures(roomId, request);
            updateModelFromServerWithRetry(true);
        } catch (final MillheatCommunicationException e) {
            logger.debug("Error updating temperature for room {}", roomId, e);
        }
    }

    /**
     * Applies setpoint, power and fan changes to a heater that is not following a room program.
     * All three are written through the device's settings shadow.
     */
    public void updateIndependentHeaterProperties(final @Nullable String macAddress, final @Nullable String heaterId,
            final @Nullable Command temperatureCommand, final @Nullable Command masterOnOffCommand,
            final @Nullable Command fanCommand) {
        final Optional<Heater> optionalHeater = model.findHeaterByMacOrId(macAddress, heaterId);
        if (optionalHeater.isEmpty()) {
            logger.debug("Cannot find heater with mac {} or id {}", macAddress, heaterId);
            return;
        }
        final Heater heater = optionalHeater.get();

        final Map<String, Object> settings = new HashMap<>();
        boolean powerOn = heater.powerStatus();
        if (masterOnOffCommand != null) {
            powerOn = masterOnOffCommand == OnOffType.ON;
        }
        settings.put("operation_mode", powerOn ? HeaterShadowDTO.MODE_CONTROL_INDIVIDUALLY : HeaterShadowDTO.MODE_OFF);

        if (temperatureCommand instanceof QuantityType<?> temperature) {
            settings.put("temperature_normal", temperature.doubleValue());
        }
        if (fanCommand != null) {
            settings.put("fan_state", fanCommand == OnOffType.ON ? "on" : "off");
        }

        try {
            client.patchDeviceSettings(heater.getId(),
                    new DeviceSettingsPatchRequest(heater.getFamily(), powerOn, settings));
            // Reflect the change locally so the channel does not flip back before the next poll.
            heater.setPowerStatus(powerOn);
            if (temperatureCommand instanceof QuantityType<?> temperature) {
                heater.setTargetTemp(temperature.doubleValue());
            }
            if (fanCommand != null) {
                heater.setFanActive(fanCommand == OnOffType.ON);
            }
        } catch (final MillheatCommunicationException e) {
            logger.debug("Error updating heater {}", heater.getId(), e);
        }
    }

    /** Writes one of the vacation mode properties of a house. */
    public void updateVacationProperty(final Home home, final String property, final Command command) {
        try {
            switch (property) {
                case VACATION_PROP_MODE -> {
                    if (command == OnOffType.OFF) {
                        client.clearVacationMode(home.getId());
                        home.setVacationModeActive(false);
                    } else if (home.getVacationModeStart() == null || home.getVacationModeEnd() == null) {
                        logger.debug("Cannot enable vacation mode before start and end time are set");
                    } else {
                        client.setVacationMode(home.getId(), vacationRequest(home, true), false);
                        home.setVacationModeActive(true);
                    }
                }
                case VACATION_PROP_ADVANCED -> {
                    home.setAdvancedVacationMode(command == OnOffType.ON);
                    patchVacationIfActive(home);
                }
                case VACATION_PROP_START -> {
                    if (command instanceof DateTimeType dateTime) {
                        home.setVacationModeStart(dateTime.getInstant());
                        patchVacationIfActive(home);
                    }
                }
                case VACATION_PROP_END -> {
                    if (command instanceof DateTimeType dateTime) {
                        home.setVacationModeEnd(dateTime.getInstant());
                        patchVacationIfActive(home);
                    }
                }
                case VACATION_PROP_TEMP -> {
                    if (command instanceof QuantityType<?> temperature) {
                        home.setVacationTemperature(temperature.doubleValue());
                        patchVacationIfActive(home);
                    }
                }
                default -> logger.debug("Unknown vacation property {}", property);
            }
        } catch (final MillheatCommunicationException e) {
            logger.debug("Failure setting vacation property {}: {}", property, e.getMessage());
        }
    }

    /**
     * Vacation settings can only be written while vacation mode is running; before that they are
     * held locally until the mode is switched on.
     */
    private void patchVacationIfActive(final Home home) throws MillheatCommunicationException {
        if (home.isVacationModeActive()) {
            client.setVacationMode(home.getId(), vacationRequest(home, true), true);
        }
    }

    private static VacationModeRequest vacationRequest(final Home home, final boolean active) {
        final Instant start = home.getVacationModeStart();
        final Instant end = home.getVacationModeEnd();
        return new VacationModeRequest(start == null ? null : start.getEpochSecond(),
                end == null ? null : end.getEpochSecond(), home.getVacationTemperature(), home.getVacationModeType(),
                active);
    }
}
