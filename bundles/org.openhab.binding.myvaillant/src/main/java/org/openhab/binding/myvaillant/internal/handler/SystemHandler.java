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
package org.openhab.binding.myvaillant.internal.handler;

import static org.openhab.binding.myvaillant.internal.MyVaillantBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myvaillant.internal.action.SystemActions;
import org.openhab.binding.myvaillant.internal.api.EnergyDataType;
import org.openhab.binding.myvaillant.internal.api.JsonUtil;
import org.openhab.binding.myvaillant.internal.api.MyVaillantApi;
import org.openhab.binding.myvaillant.internal.api.MyVaillantApiException;
import org.openhab.binding.myvaillant.internal.api.MyVaillantDevice;
import org.openhab.binding.myvaillant.internal.api.MyVaillantException;
import org.openhab.binding.myvaillant.internal.api.SystemData;
import org.openhab.binding.myvaillant.internal.config.AccountConfiguration;
import org.openhab.binding.myvaillant.internal.config.SystemConfiguration;
import org.openhab.binding.myvaillant.internal.discovery.SystemDiscoveryService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Bridge for one system (one myVAILLANT home). It updates the system with all its zones, circuits, hot water and
 * devices in one go and hands the data to the Things of these components.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class SystemHandler extends BaseBridgeHandler {

    private static final Duration ENERGY_START_DELAY = Duration.ofSeconds(30);
    private static final Duration LINK_ENERGY_DELAY = Duration.ofSeconds(15);
    /** The devices rarely change, reading them less often saves requests (the quota is per account) */
    private static final Duration CURRENT_SYSTEM_CACHE_DURATION = Duration.ofMinutes(30);
    private static final DateTimeFormatter EXPORT_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final Logger logger = LoggerFactory.getLogger(SystemHandler.class);
    private final Object pollLock = new Object();
    private final Object energyLock = new Object();

    private volatile String systemId = "";
    private volatile boolean active;
    private volatile @Nullable SystemData data;
    private volatile Map<String, Map<EnergyDataType, Double>> energyToday = Map.of();
    private volatile @Nullable Consumer<SystemData> dataListener;
    private volatile @Nullable DeviceCache deviceCache;
    private @Nullable ScheduledFuture<?> pollFuture;
    private @Nullable ScheduledFuture<?> energyFuture;
    private @Nullable ScheduledFuture<?> refreshFuture;
    private @Nullable ScheduledFuture<?> energyRefreshFuture;

    /** The devices of a system as read from {@code currentSystem} */
    private record DeviceCache(String systemId, JsonObject currentSystem, Instant fetched) {
    }

    public SystemHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        String id = getConfigAs(SystemConfiguration.class).systemId.trim();
        systemId = id;
        if (id.isEmpty()) {
            updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "@text/offline.conf-error.no-system-id");
            return;
        }
        active = true;
        updateStatus(ThingStatus.UNKNOWN);
        Bridge bridge = getBridge();
        if (bridge == null || !(bridge.getHandler() instanceof AccountHandler)) {
            updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "@text/offline.conf-error.no-account-bridge");
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            startPolling();
        } else {
            updateStatus(ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void dispose() {
        active = false;
        stopPolling();
        // The handler is initialized again after a configuration change, possibly for another system
        data = null;
        energyToday = Map.of();
        deviceCache = null;
    }

    /** Whether an update that started for the given system may still publish its result */
    private boolean isCurrent(String id) {
        return active && id.equals(systemId);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (!active) {
            return;
        }
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            startPolling();
        } else {
            stopPolling();
            updateStatus(ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        SystemData current = data;
        if (current != null && childHandler instanceof ComponentHandler componentHandler) {
            componentHandler.updateData(current);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        if (CHANNEL_HEATING_ENERGY_EFFICIENCY.equals(channelUID.getId())) {
            scheduleEnergyRefresh();
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SystemDiscoveryService.class, SystemActions.class);
    }

    private synchronized void startPolling() {
        stopPolling();
        AccountConfiguration config = getAccountConfiguration();
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 0, Math.max(60, config.refreshInterval),
                TimeUnit.SECONDS);
        if (config.energyRefreshInterval > 0) {
            long interval = config.energyRefreshInterval * 60L;
            // Before the first update of the system the devices are unknown, that update starts the energy update
            long initialDelay = data == null ? interval : ENERGY_START_DELAY.toSeconds();
            energyFuture = scheduler.scheduleWithFixedDelay(this::pollEnergy, initialDelay, interval, TimeUnit.SECONDS);
        }
    }

    private synchronized void stopPolling() {
        cancel(pollFuture);
        cancel(energyFuture);
        cancel(refreshFuture);
        cancel(energyRefreshFuture);
        pollFuture = null;
        energyFuture = null;
        refreshFuture = null;
        energyRefreshFuture = null;
    }

    private static void cancel(@Nullable ScheduledFuture<?> future) {
        if (future != null) {
            future.cancel(false);
        }
    }

    /** Updates the system after the configured delay, repeated calls within the delay update once */
    public synchronized void scheduleRefresh() {
        if (!active) {
            return;
        }
        cancel(refreshFuture);
        refreshFuture = scheduler.schedule(this::poll, Math.max(0, getAccountConfiguration().refreshDelay),
                TimeUnit.SECONDS);
    }

    /** Updates the energy values soon, e.g. after an energy channel was linked */
    public synchronized void scheduleEnergyRefresh() {
        if (!active || getAccountConfiguration().energyRefreshInterval <= 0) {
            return;
        }
        cancel(energyRefreshFuture);
        energyRefreshFuture = scheduler.schedule(this::pollEnergy, LINK_ENERGY_DELAY.toSeconds(), TimeUnit.SECONDS);
    }

    /** The data of the last update, {@code null} before the first one */
    public @Nullable SystemData getData() {
        return data;
    }

    /** ID of the system in the myVAILLANT cloud */
    public String getSystemId() {
        return systemId;
    }

    /** Sets the listener that gets the data of each update, {@code null} removes it */
    public void setDataListener(@Nullable Consumer<SystemData> listener) {
        dataListener = listener;
    }

    private @Nullable AccountHandler getAccountHandler() {
        Bridge bridge = getBridge();
        return bridge != null && bridge.getHandler() instanceof AccountHandler handler ? handler : null;
    }

    /** Configuration of the account bridge, the defaults without bridge */
    public AccountConfiguration getAccountConfiguration() {
        AccountHandler account = getAccountHandler();
        return account == null ? new AccountConfiguration() : account.getAccountConfiguration();
    }

    /** The API client while the account is online */
    public @Nullable MyVaillantApi getApi() {
        AccountHandler account = getAccountHandler();
        return account == null ? null : account.getApi();
    }

    /**
     * Passes failures that concern the whole account (credentials, quota) to the account.
     *
     * @return whether the account handled the failure
     */
    public boolean handleAccountFailure(MyVaillantException e) {
        AccountHandler account = getAccountHandler();
        return account != null && account.handleAccountFailure(e);
    }

    /**
     * Reports a failed command or action. Failures of the account are passed to the account. A request the API
     * answered with an error (e.g. a value it does not accept) is logged as a warning, communication problems only for
     * debugging: they show in the status with the next update.
     *
     * @param request description of the command or action for the log
     */
    public void handleRequestFailure(String request, MyVaillantException e) {
        if (handleAccountFailure(e)) {
            return;
        }
        if (e instanceof MyVaillantApiException) {
            logger.warn("{} failed: {}", request, e.getMessage());
        } else {
            logger.debug("{} failed: {}", request, e.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Updates

    private void poll() {
        MyVaillantApi api = getApi();
        String id = systemId;
        if (api == null || !active) {
            return;
        }
        AccountConfiguration config = getAccountConfiguration();
        synchronized (pollLock) {
            try {
                String controlIdentifier = api.getControlIdentifier(id);
                if (!MyVaillantApi.CONTROL_IDENTIFIER_TLI.equals(controlIdentifier)) {
                    if (isCurrent(id)) {
                        updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                                "@text/offline.conf-error.unsupported-control [\"" + controlIdentifier + "\"]");
                    }
                    return;
                }
                JsonObject home = api.getHome(id);
                if (home == null) {
                    if (isCurrent(id)) {
                        updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                                "@text/offline.conf-error.unknown-system [\"" + id + "\"]");
                    }
                    return;
                }
                ZoneId timeZone = api.getTimeZone(id);
                JsonObject tli = api.getSystem(id);
                JsonObject currentSystem = getCurrentSystem(api, id);
                JsonObject rts = config.fetchRts ? optional("RTS", id, () -> api.getRts(id)) : null;
                JsonObject mpc = config.fetchMpc ? optional("MPC", id, () -> api.getMpc(id)) : null;
                Boolean connected = config.fetchConnectionStatus
                        ? optional("connection status", id, () -> api.getConnectionStatus(id))
                        : null;
                JsonArray troubleCodes = config.fetchTroubleCodes
                        ? optional("trouble codes", id, () -> api.getDiagnosticTroubleCodes(id))
                        : null;
                SystemData newData = new SystemData(id, timeZone, home, tli, currentSystem, rts, mpc, connected,
                        troubleCodes);
                if (!isCurrent(id)) {
                    return;
                }
                boolean firstUpdate = data == null;
                data = newData;
                updateStatus(ThingStatus.ONLINE);
                updateProperties(newData, controlIdentifier);
                updateChannels(newData);
                for (Thing child : getThing().getThings()) {
                    if (child.getHandler() instanceof ComponentHandler componentHandler) {
                        componentHandler.updateData(newData);
                    }
                }
                Consumer<SystemData> listener = dataListener;
                if (listener != null) {
                    listener.accept(newData);
                }
                if (firstUpdate) {
                    scheduleEnergyRefresh();
                }
            } catch (MyVaillantException e) {
                if (isCurrent(id) && !handleAccountFailure(e)) {
                    logger.debug("Update of system {} failed: {}", id, e.getMessage());
                    updateStatus(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                }
            }
        }
    }

    /**
     * The devices rarely change, so they are kept for some time. The system ID guards against a changed configuration.
     */
    private JsonObject getCurrentSystem(MyVaillantApi api, String id) throws MyVaillantException {
        DeviceCache cache = deviceCache;
        if (cache != null && cache.systemId().equals(id)
                && cache.fetched().plus(CURRENT_SYSTEM_CACHE_DURATION).isAfter(Instant.now())) {
            return cache.currentSystem();
        }
        JsonObject fetched = api.getCurrentSystem(id);
        if (isCurrent(id)) {
            deviceCache = new DeviceCache(id, fetched, Instant.now());
        }
        return fetched;
    }

    private interface Request<T> {
        T execute() throws MyVaillantException;
    }

    /** Optional data, the update continues without it if the API returns an error */
    private <T> @Nullable T optional(String description, String id, Request<T> request) throws MyVaillantException {
        try {
            return request.execute();
        } catch (MyVaillantApiException e) {
            logger.debug("Could not get {} of system {}: {}", description, id, e.getMessage());
            return null;
        }
    }

    private void updateProperties(SystemData systemData, String controlIdentifier) {
        Map<String, String> properties = new HashMap<>(editProperties());
        JsonObject home = systemData.getHome();
        JsonObject system = systemData.getSystem();
        putProperty(properties, "homeName", JsonUtil.getString(home, "home_name"));
        putProperty(properties, "nomenclature", JsonUtil.getString(home, "nomenclature"));
        putProperty(properties, Thing.PROPERTY_SERIAL_NUMBER, JsonUtil.getString(home, "serial_number"));
        putProperty(properties, Thing.PROPERTY_FIRMWARE_VERSION, JsonUtil.getString(home, "firmware_version"));
        putProperty(properties, Thing.PROPERTY_MODEL_ID, JsonUtil.getString(home, "product_information"));
        putProperty(properties, "controllerType", JsonUtil.getString(system, "controller_type"));
        putProperty(properties, "systemScheme", JsonUtil.getString(system, "system_scheme"));
        putProperty(properties, "backupHeaterType", JsonUtil.getString(system, "backup_heater_type"));
        putProperty(properties, "backupHeaterAllowedFor", JsonUtil.getString(system, "backup_heater_allowed_for"));
        putProperty(properties, "controlIdentifier", controlIdentifier);
        putProperty(properties, "timeZone", systemData.getTimeZone().getId());
        if (!properties.equals(editProperties())) {
            updateProperties(properties);
        }
    }

    private static void putProperty(Map<String, String> properties, String name, @Nullable String value) {
        if (value != null) {
            properties.put(name, value);
        }
    }

    private void updateChannels(SystemData systemData) {
        JsonObject system = systemData.getSystem();
        JsonObject home = systemData.getHome();
        ZoneId zone = systemData.getTimeZone();
        updateState(CHANNEL_OUTDOOR_TEMPERATURE, States.temperature(JsonUtil.getDouble(system, "outdoor_temperature")));
        updateState(CHANNEL_OUTDOOR_TEMPERATURE_AVERAGE_24H,
                States.temperature(JsonUtil.getDouble(system, "outdoor_temperature_average24h")));
        updateState(CHANNEL_WATER_PRESSURE,
                States.quantity(JsonUtil.getDouble(system, "system_water_pressure"), Units.BAR));
        updateState(CHANNEL_SYSTEM_FLOW_TEMPERATURE,
                States.temperature(JsonUtil.getDouble(system, "system_flow_temperature")));
        updateState(CHANNEL_CYLINDER_TOP_DHW,
                States.temperature(JsonUtil.getDouble(system, "cylinder_temperature_sensor_top_d_h_w")));
        updateState(CHANNEL_CYLINDER_BOTTOM_DHW,
                States.temperature(JsonUtil.getDouble(system, "cylinder_temperature_sensor_bottom_d_h_w")));
        updateState(CHANNEL_CYLINDER_TOP_CH,
                States.temperature(JsonUtil.getDouble(system, "cylinder_temperature_sensor_top_c_h")));
        updateState(CHANNEL_CYLINDER_BOTTOM_CH,
                States.temperature(JsonUtil.getDouble(system, "cylinder_temperature_sensor_bottom_c_h")));
        updateState(CHANNEL_ENERGY_MANAGER_STATE, States.string(JsonUtil.getString(system, "energy_manager_state")));
        updateState(CHANNEL_SYSTEM_OFF, States.onOff(JsonUtil.getBoolean(system, "system_off")));
        updateState(CHANNEL_FIRMWARE_UPDATE_REQUIRED,
                States.onOff(JsonUtil.getBoolean(JsonUtil.getObject(home, "firmware"), "update_required")));
        updateState(CHANNEL_ONLINE, States.onOff(systemData.getConnected()));
        updateTroubleCodes(systemData.getTroubleCodes());

        Instant holidayStart = getHolidayDate(systemData, "holiday_start_date_time");
        Instant holidayEnd = getHolidayDate(systemData, "holiday_end_date_time");
        updateState(CHANNEL_HOLIDAY, OnOffType.from(holidayEnd != null && holidayEnd.isAfter(Instant.now())));
        updateState(CHANNEL_HOLIDAY_START, States.dateTime(holidayStart, zone));
        updateState(CHANNEL_HOLIDAY_END, States.dateTime(holidayEnd, zone));

        Instant coolingStart = JsonUtil.getInstant(system, "manual_cooling_start_date");
        Instant coolingEnd = JsonUtil.getInstant(system, "manual_cooling_end_date");
        updateState(CHANNEL_MANUAL_COOLING, OnOffType.from(coolingEnd != null && coolingEnd.isAfter(Instant.now())));
        updateState(CHANNEL_MANUAL_COOLING_START, States.dateTime(coolingStart, zone));
        updateState(CHANNEL_MANUAL_COOLING_END, States.dateTime(coolingEnd, zone));

        updateState(CHANNEL_VENTILATION_BOOST, OnOffType.from(systemData.getZones().values().stream().anyMatch(
                zoneJson -> "VENTILATION_BOOST".equals(JsonUtil.getString(zoneJson, "current_special_function")))));
        updateState(CHANNEL_LAST_UPDATE, States.dateTime(systemData.getTimestamp(), zone));
    }

    private void updateTroubleCodes(@Nullable JsonArray troubleCodes) {
        if (troubleCodes == null) {
            updateState(CHANNEL_TROUBLE_CODES, UnDefType.UNDEF);
            updateState(CHANNEL_HAS_TROUBLE_CODES, UnDefType.UNDEF);
            return;
        }
        List<String> codes = new ArrayList<>();
        for (JsonElement device : troubleCodes) {
            JsonArray deviceCodes = device.isJsonObject() ? JsonUtil.getArray(device.getAsJsonObject(), "codes") : null;
            if (deviceCodes != null) {
                for (JsonElement code : deviceCodes) {
                    if (code.isJsonObject()) {
                        String title = JsonUtil.getString(code.getAsJsonObject(), "title");
                        codes.add(JsonUtil.getString(code.getAsJsonObject(), "code")
                                + (title == null ? "" : " " + title));
                    }
                }
            }
        }
        updateState(CHANNEL_TROUBLE_CODES, new StringType(String.join(", ", codes)));
        updateState(CHANNEL_HAS_TROUBLE_CODES, OnOffType.from(!codes.isEmpty()));
    }

    /** Holidays are set for the whole system, the API reports them with each zone */
    private static @Nullable Instant getHolidayDate(SystemData systemData, String key) {
        for (JsonObject zone : systemData.getZones().values()) {
            Instant date = JsonUtil.getInstant(JsonUtil.getObject(zone, "general"), key);
            if (date != null) {
                return date;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Energy

    private void pollEnergy() {
        MyVaillantApi api = getApi();
        SystemData systemData = data;
        if (api == null || systemData == null || !active) {
            return;
        }
        String id = systemData.getSystemId();
        synchronized (energyLock) {
            Map<String, Set<EnergyDataType>> required = getRequiredEnergyDataTypes(systemData);
            if (required.isEmpty()) {
                return;
            }
            ZoneId zone = systemData.getTimeZone();
            ZonedDateTime today = ZonedDateTime.now(zone).truncatedTo(ChronoUnit.DAYS);
            LocalDate todayDate = today.toLocalDate();
            // From yesterday like Home Assistant, the last hours of a day are only complete some time after midnight
            ZonedDateTime from = today.minusDays(1);
            ZonedDateTime to = today.plusDays(1);
            Map<String, Map<EnergyDataType, Double>> result = new HashMap<>();
            try {
                for (Map.Entry<String, Set<EnergyDataType>> entry : required.entrySet()) {
                    Map<EnergyDataType, Double> deviceResult = new HashMap<>();
                    for (EnergyDataType type : entry.getValue()) {
                        JsonObject buckets = api.getDeviceBuckets(id, entry.getKey(), "HOUR", type.operationMode(),
                                type.valueType(), from, to);
                        deviceResult.put(type, sumBuckets(buckets, zone, todayDate));
                    }
                    result.put(entry.getKey(), deviceResult);
                }
            } catch (MyVaillantException e) {
                if (isCurrent(id) && !handleAccountFailure(e)) {
                    logger.debug("Update of the energy values of system {} failed: {}", id, e.getMessage());
                }
                return;
            }
            if (!isCurrent(id)) {
                return;
            }
            energyToday = result;
            for (Thing child : getThing().getThings()) {
                if (child.getHandler() instanceof DeviceHandler deviceHandler) {
                    Map<EnergyDataType, Double> values = result.get(deviceHandler.getDeviceUuid());
                    if (values != null) {
                        deviceHandler.updateEnergy(values);
                    }
                }
            }
            updateState(CHANNEL_HEATING_ENERGY_EFFICIENCY, getHeatingEfficiency(result.values()));
        }
    }

    /** Only the values of linked channels are fetched, every value costs one request */
    private Map<String, Set<EnergyDataType>> getRequiredEnergyDataTypes(SystemData systemData) {
        Map<String, Set<EnergyDataType>> required = new HashMap<>();
        for (Thing child : getThing().getThings()) {
            if (child.getHandler() instanceof DeviceHandler deviceHandler) {
                Set<EnergyDataType> types = deviceHandler.getRequiredEnergyDataTypes();
                if (!types.isEmpty()) {
                    requiredSet(required, deviceHandler.getDeviceUuid()).addAll(types);
                }
            }
        }
        if (isLinked(CHANNEL_HEATING_ENERGY_EFFICIENCY)) {
            for (MyVaillantDevice device : systemData.getDevices()) {
                for (EnergyDataType type : device.getEnergyDataTypes()) {
                    if (isHeatingEfficiencyType(type)) {
                        requiredSet(required, device.deviceUuid()).add(type);
                    }
                }
            }
        }
        // Devices can disappear between updates
        required.keySet().removeIf(uuid -> systemData.getDevice(uuid) == null);
        required.forEach((uuid, types) -> {
            MyVaillantDevice device = systemData.getDevice(uuid);
            if (device != null) {
                types.retainAll(new HashSet<>(device.getEnergyDataTypes()));
            }
        });
        required.values().removeIf(Set::isEmpty);
        return required;
    }

    private static Set<EnergyDataType> requiredSet(Map<String, Set<EnergyDataType>> required, String deviceUuid) {
        Set<EnergyDataType> types = required.get(deviceUuid);
        if (types == null) {
            types = new LinkedHashSet<>();
            required.put(deviceUuid, types);
        }
        return types;
    }

    static boolean isHeatingEfficiencyType(EnergyDataType type) {
        return OPERATION_MODE_HEATING.equals(type.operationMode())
                && (VALUE_TYPE_CONSUMED_ELECTRICAL_ENERGY.equals(type.valueType())
                        || VALUE_TYPE_HEAT_GENERATED.equals(type.valueType()));
    }

    /** Heat generated per consumed electrical energy for heating, of one device or summed over devices */
    static State getHeatingEfficiency(Collection<Map<EnergyDataType, Double>> devices) {
        double consumed = 0;
        double generated = 0;
        for (Map<EnergyDataType, Double> values : devices) {
            consumed += values.getOrDefault(
                    new EnergyDataType(OPERATION_MODE_HEATING, VALUE_TYPE_CONSUMED_ELECTRICAL_ENERGY), 0.0);
            generated += values.getOrDefault(new EnergyDataType(OPERATION_MODE_HEATING, VALUE_TYPE_HEAT_GENERATED),
                    0.0);
        }
        return consumed > 0 ? States.decimal(Math.round(generated / consumed * 100) / 100.0) : UnDefType.UNDEF;
    }

    /** Sum of the hourly values (Wh) that start on the given day in the time zone of the system */
    static double sumBuckets(JsonObject buckets, ZoneId zone, LocalDate day) {
        double sum = 0;
        JsonArray data = JsonUtil.getArray(buckets, "data");
        if (data != null) {
            for (JsonElement bucket : data) {
                if (bucket.isJsonObject()) {
                    Instant start = JsonUtil.getInstant(bucket.getAsJsonObject(), "start_date");
                    Double value = JsonUtil.getDouble(bucket.getAsJsonObject(), "value");
                    if (start != null && value != null && start.atZone(zone).toLocalDate().equals(day)) {
                        sum += value;
                    }
                }
            }
        }
        return sum;
    }

    /**
     * Energy values of all devices: one entry per device with the buckets of each value type. Dates are ISO dates with
     * the offset of the system's time zone.
     *
     * @param resolution {@code HOUR} (at most 3 days per request), {@code DAY} (60 days) or {@code MONTH}
     * @param from start, inclusive
     * @param to end, exclusive
     * @param operationMode only this operation mode (e.g. {@code HEATING}), {@code null} for all; every device and
     *            value type costs one request
     * @throws MyVaillantException if the system is offline or a request fails, see
     *             {@link #handleRequestFailure(String, MyVaillantException)}
     */
    public String exportEnergyData(String resolution, ZonedDateTime from, ZonedDateTime to,
            @Nullable String operationMode) throws MyVaillantException {
        MyVaillantApi api = getApi();
        SystemData systemData = data;
        if (api == null || systemData == null) {
            throw new MyVaillantException("The system is not online");
        }
        ZoneId zone = systemData.getTimeZone();
        JsonArray export = new JsonArray();
        for (MyVaillantDevice device : systemData.getDevices()) {
            JsonArray deviceData = new JsonArray();
            for (EnergyDataType type : device.getEnergyDataTypes()) {
                if (operationMode != null && !operationMode.isBlank()
                        && !operationMode.equalsIgnoreCase(type.operationMode())) {
                    continue;
                }
                JsonObject buckets = api.getDeviceBuckets(systemData.getSystemId(), device.deviceUuid(), resolution,
                        type.operationMode(), type.valueType(), from.withZoneSameInstant(zone),
                        to.withZoneSameInstant(zone));
                deviceData.add(toExportData(buckets, type, zone));
            }
            if (!deviceData.isEmpty()) {
                JsonObject deviceInfo = new JsonObject();
                deviceInfo.addProperty("device_uuid", device.deviceUuid());
                deviceInfo.addProperty("name", device.getName());
                deviceInfo.addProperty("product_name", JsonUtil.getString(device.json(), "product_name"));
                deviceInfo.addProperty("device_type", device.getDeviceType());
                deviceInfo.addProperty("type", device.type());
                JsonObject entry = new JsonObject();
                entry.add("device", deviceInfo);
                entry.add("data", deviceData);
                export.add(entry);
            }
        }
        return GSON.toJson(export);
    }

    private static JsonObject toExportData(JsonObject buckets, EnergyDataType type, ZoneId zone) {
        JsonObject result = new JsonObject();
        result.addProperty("operation_mode", type.operationMode());
        String energyType = JsonUtil.getString(buckets, "energy_type");
        result.addProperty("energy_type", energyType == null ? type.valueType() : energyType);
        result.addProperty("value_type", type.valueType());
        result.addProperty("resolution", JsonUtil.getString(buckets, "resolution"));
        result.addProperty("start_date", formatExportDate(JsonUtil.getInstant(buckets, "start_date"), zone));
        result.addProperty("end_date", formatExportDate(JsonUtil.getInstant(buckets, "end_date"), zone));
        result.addProperty("total_consumption", JsonUtil.getDouble(buckets, "total_consumption"));
        JsonArray data = new JsonArray();
        JsonArray bucketArray = JsonUtil.getArray(buckets, "data");
        if (bucketArray != null) {
            for (JsonElement bucket : bucketArray) {
                if (bucket.isJsonObject()) {
                    JsonObject item = new JsonObject();
                    item.addProperty("start_date",
                            formatExportDate(JsonUtil.getInstant(bucket.getAsJsonObject(), "start_date"), zone));
                    item.addProperty("end_date",
                            formatExportDate(JsonUtil.getInstant(bucket.getAsJsonObject(), "end_date"), zone));
                    item.addProperty("value", JsonUtil.getDouble(bucket.getAsJsonObject(), "value"));
                    data.add(item);
                }
            }
        }
        result.add("data", data);
        return result;
    }

    private static @Nullable String formatExportDate(@Nullable Instant instant, ZoneId zone) {
        return instant == null ? null : EXPORT_DATE_TIME.format(instant.atZone(zone));
    }

    /** Today's values of the devices, keyed by device UUID */
    Map<String, Map<EnergyDataType, Double>> getEnergyToday() {
        return energyToday;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Commands

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            SystemData systemData = data;
            if (systemData != null) {
                updateChannels(systemData);
            }
            return;
        }
        // Requests can take long, e.g. if the access token has to be renewed first
        scheduler.execute(() -> sendCommand(channelUID, command));
    }

    private void sendCommand(ChannelUID channelUID, Command command) {
        SystemData systemData = data;
        MyVaillantApi api = getApi();
        if (!active || api == null || systemData == null) {
            logger.debug("Ignoring command {} to {}, the system is not online", command, channelUID);
            return;
        }
        AccountConfiguration config = getAccountConfiguration();
        Instant now = Instant.now();
        try {
            switch (channelUID.getId()) {
                case CHANNEL_HOLIDAY -> {
                    if (command == OnOffType.ON) {
                        api.setHoliday(systemId, now, now.plus(Duration.ofDays(config.holidayDuration)));
                    } else if (command == OnOffType.OFF) {
                        api.cancelHoliday(systemId);
                    }
                }
                case CHANNEL_HOLIDAY_START, CHANNEL_HOLIDAY_END -> {
                    Instant date = States.toInstant(command);
                    if (date == null) {
                        return;
                    }
                    Instant start = getHolidayDate(systemData, "holiday_start_date_time");
                    Instant end = getHolidayDate(systemData, "holiday_end_date_time");
                    if (CHANNEL_HOLIDAY_START.equals(channelUID.getId())) {
                        start = date;
                        end = end == null || !end.isAfter(date) ? date.plus(Duration.ofDays(config.holidayDuration))
                                : end;
                    } else {
                        end = date;
                        start = start == null || !start.isBefore(date) ? now : start;
                    }
                    api.setHoliday(systemId, start, end);
                }
                case CHANNEL_MANUAL_COOLING -> {
                    if (command == OnOffType.ON) {
                        api.setCoolingForDays(systemId, now, now.plus(Duration.ofDays(config.manualCoolingDuration)));
                    } else if (command == OnOffType.OFF) {
                        api.cancelCoolingForDays(systemId);
                    }
                }
                case CHANNEL_MANUAL_COOLING_END -> {
                    Instant end = States.toInstant(command);
                    if (end == null) {
                        return;
                    }
                    Instant start = JsonUtil.getInstant(systemData.getSystem(), "manual_cooling_start_date");
                    api.setCoolingForDays(systemId, start == null || !start.isBefore(end) ? now : start, end);
                }
                case CHANNEL_VENTILATION_BOOST -> {
                    if (command == OnOffType.ON) {
                        api.setVentilationBoost(systemId);
                    } else if (command == OnOffType.OFF) {
                        api.cancelVentilationBoost(systemId);
                    }
                }
                default -> {
                    return;
                }
            }
            if (command instanceof OnOffType || command instanceof StringType) {
                updateState(channelUID, (State) command);
            }
            scheduleRefresh();
        } catch (MyVaillantException e) {
            handleRequestFailure("Command " + command + " to " + channelUID, e);
        }
    }
}
