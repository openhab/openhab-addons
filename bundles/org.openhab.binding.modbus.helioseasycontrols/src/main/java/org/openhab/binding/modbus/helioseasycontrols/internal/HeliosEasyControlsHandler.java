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
package org.openhab.binding.modbus.helioseasycontrols.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link HeliosEasyControlsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernhard Bauer - Initial contribution
 */
@NonNullByDefault
public class HeliosEasyControlsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HeliosEasyControlsHandler.class);

    private final HeliosEasyControlsTranslationProvider translationProvider;

    private @Nullable HeliosEasyControlsConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable Map<String, HeliosVariable> variableMap;

    /**
     * This flag is used to ensure read requests (consisting of a write and subsequent read) are not influenced by
     * another transaction
     */
    private final Map<ModbusSlaveEndpoint, Semaphore> transactionLocks = new ConcurrentHashMap<>();

    private final Gson gson = new Gson();

    private @Nullable ModbusCommunicationInterface comms;

    private int dateFormat = -1;
    private ZonedDateTime sysDate = ZonedDateTime.now(); // initialize with local system time as a best guess
                                                         // before reading from device
    private long errors = 0;
    private int warnings = 0;
    private int infos = 0;
    private String statusFlags = "";

    private static class BypassDate {
        private static final int[] MONTH_MAX_DAYS = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

        // initialization to avoid issues when updating before all variables were read
        private int month = 1;
        private int day = 1;

        public BypassDate() {
        }

        public BypassDate(int day, int month) {
            this.setDay(day);
            this.setMonth(month);
        }

        public void setMonth(int month) {
            if (month < 1) {
                this.month = 1;
            } else if (month > 12) {
                this.month = 12;
            } else {
                this.month = month;
            }
        }

        public int getMonth() {
            return this.month;
        }

        public void setDay(int day) {
            if (day < 1) {
                this.day = 1;
            } else {
                this.day = Math.min(day, MONTH_MAX_DAYS[month - 1]);
            }
        }

        public int getDay() {
            return this.day;
        }

        public DateTimeType toDateTimeType() {
            return new DateTimeType(ZonedDateTime.of(1900, this.month, this.day, 0, 0, 0, 0, ZoneId.of("UTC+00:00")));
        }
    }

    private @Nullable BypassDate bypassFrom, bypassTo;

    public HeliosEasyControlsHandler(Thing thing, HeliosEasyControlsTranslationProvider translationProvider) {
        super(thing);
        this.translationProvider = translationProvider;
    }

    /**
     * Reads variable definitions from JSON file and store them in variableMap
     */
    private void readVariableDefinition() {
        Type vMapType = new TypeToken<Map<String, HeliosVariable>>() {
        }.getType();
        try (InputStreamReader jsonFile = new InputStreamReader(
                getClass().getResourceAsStream(HeliosEasyControlsBindingConstants.VARIABLES_DEFINITION_FILE));
                BufferedReader reader = new BufferedReader(jsonFile)) {
            this.variableMap = gson.fromJson(reader, vMapType);
        } catch (IOException e) {
            this.handleError("Error reading variable definition file", ThingStatusDetail.CONFIGURATION_ERROR);
        }
        if (variableMap != null) {
            // add the name to the variable itself
            for (Map.Entry<String, HeliosVariable> entry : this.variableMap.entrySet()) {
                entry.getValue().setName(entry.getKey()); // workaround to set the variable name inside the
                                                          // HeliosVariable object
                if (!entry.getValue().isOk()) {
                    this.handleError("Variables definition file contains inconsistent data",
                            ThingStatusDetail.CONFIGURATION_ERROR);
                }
            }
        } else {
            this.handleError("Variables definition file not found or of illegal format",
                    ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    /**
     * Get the endpoint handler from the bridge this handler is connected to
     * Checks that we're connected to the right type of bridge
     *
     * @return the endpoint handler or null if the bridge does not exist
     */
    private @Nullable ModbusEndpointThingHandler getEndpointThingHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Bridge is null");
            return null;
        }
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            logger.debug("Bridge is not online");
            return null;
        }

        ThingHandler handler = bridge.getHandler();
        if (handler == null) {
            logger.debug("Bridge handler is null");
            return null;
        }

        if (handler instanceof ModbusEndpointThingHandler) {
            return (ModbusEndpointThingHandler) handler;
        } else {
            logger.debug("Unexpected bridge handler: {}", handler);
            return null;
        }
    }

    /**
     * Get a reference to the modbus endpoint
     */
    private void connectEndpoint() {
        if (this.comms != null) {
            return;
        }

        ModbusEndpointThingHandler slaveEndpointThingHandler = getEndpointThingHandler();
        if (slaveEndpointThingHandler == null) {
            @SuppressWarnings("null")
            String label = Optional.ofNullable(getBridge()).map(b -> b.getLabel()).orElse("<null>");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    String.format("Bridge '%s' is offline", label));
            logger.debug("No bridge handler available -- aborting init for {}", label);
            return;
        }

        comms = slaveEndpointThingHandler.getCommunicationInterface();

        if (comms == null) {
            @SuppressWarnings("null")
            String label = Optional.ofNullable(getBridge()).map(b -> b.getLabel()).orElse("<null>");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    String.format("Bridge '%s' not completely initialized", label));
            logger.debug("Bridge not initialized fully (no endpoint) -- aborting init for {}", this);
            return;
        }
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(HeliosEasyControlsConfiguration.class);
        this.readVariableDefinition();
        this.connectEndpoint();
        if ((this.comms != null) && (this.variableMap != null) && (this.config != null)) {
            this.transactionLocks.putIfAbsent(this.comms.getEndpoint(), new Semaphore(1, true));
            updateStatus(ThingStatus.UNKNOWN);

            // background initialization
            scheduler.execute(() -> {
                readValue(HeliosEasyControlsBindingConstants.DATE_FORMAT);
                // status will be updated to ONLINE by the read callback function (via processResponse)
            });

            // poll for status updates regularly
            HeliosEasyControlsConfiguration config = this.config;
            if (config != null) {
                this.pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                    if (variableMap != null) {
                        for (Map.Entry<String, HeliosVariable> entry : variableMap.entrySet()) {
                            if (this.isProperty(entry.getKey()) || isLinked(entry.getValue().getGroupAndName())
                                    || HeliosEasyControlsBindingConstants.ALWAYS_UPDATE_VARIABLES
                                            .contains(entry.getKey())) {
                                readValue(entry.getKey());
                            }
                        }
                    } else {
                        handleError("Variable definition is null", ThingStatusDetail.CONFIGURATION_ERROR);
                    }
                }, config.getRefreshInterval(), config.getRefreshInterval(), TimeUnit.MILLISECONDS);
            }
        } else { // at least one null assertion has failed, let's log the problem and update the thing status
            if (this.comms == null) {
                this.handleError("Modbus communication interface is unavailable",
                        ThingStatusDetail.COMMUNICATION_ERROR);
            }
            if (this.variableMap == null) {
                this.handleError("Variable definition is unavailable", ThingStatusDetail.CONFIGURATION_ERROR);
            }
            if (this.config == null) {
                this.handleError("Binding configuration is unavailable", ThingStatusDetail.CONFIGURATION_ERROR);
            }
        }
    }

    @Override
    public void dispose() {
        if (this.pollingJob != null) {
            this.pollingJob.cancel(true);
        }
        this.comms = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
            if (channelId.equals(HeliosEasyControlsBindingConstants.SYS_DATE)) {
                scheduler.submit(() -> readValue(HeliosEasyControlsBindingConstants.DATE));
                scheduler.submit(() -> readValue(HeliosEasyControlsBindingConstants.TIME));
            } else if (channelId.equals(HeliosEasyControlsBindingConstants.BYPASS_FROM)) {
                scheduler.submit(() -> readValue(HeliosEasyControlsBindingConstants.BYPASS_FROM_DAY));
                scheduler.submit(() -> readValue(HeliosEasyControlsBindingConstants.BYPASS_FROM_MONTH));
            } else if (channelId.equals(HeliosEasyControlsBindingConstants.BYPASS_TO)) {
                scheduler.submit(() -> readValue(HeliosEasyControlsBindingConstants.BYPASS_TO_DAY));
                scheduler.submit(() -> readValue(HeliosEasyControlsBindingConstants.BYPASS_TO_MONTH));
            } else {
                scheduler.submit(() -> readValue(channelId));
            }
        } else { // write command
            String value = null;
            if (command instanceof OnOffType) {
                value = command == OnOffType.ON ? "1" : "0";
            } else if (command instanceof DateTimeType) {
                try {
                    ZonedDateTime d = ((DateTimeType) command).getZonedDateTime();
                    if (channelId.equals(HeliosEasyControlsBindingConstants.SYS_DATE)) {
                        setSysDateTime(d);
                    } else if (channelId.equals(HeliosEasyControlsBindingConstants.BYPASS_FROM)) {
                        this.setBypass(true, d.getDayOfMonth(), d.getMonthValue());
                    } else if (channelId.equals(HeliosEasyControlsBindingConstants.BYPASS_TO)) {
                        this.setBypass(false, d.getDayOfMonth(), d.getMonthValue());
                    } else {
                        value = formatDate(channelId, ((DateTimeType) command).getZonedDateTime());
                    }
                } catch (InterruptedException e) {
                    logger.debug(
                            "{} encountered Exception when trying to lock Semaphore for writing variable {} to the device: {}",
                            HeliosEasyControlsHandler.class.getSimpleName(), channelId, e.getMessage());
                }
            } else if ((command instanceof DecimalType) || (command instanceof StringType)) {
                value = command.toString();
            } else if (command instanceof QuantityType<?>) {
                // convert item's unit to the Helios device's unit
                Map<String, HeliosVariable> variableMap = this.variableMap;
                if (variableMap != null) {
                    HeliosVariable v = variableMap.get(channelId);
                    if (v != null) {
                        String unit = v.getUnit();
                        QuantityType<?> val = (QuantityType<?>) command;
                        if (unit != null) {
                            switch (unit) {
                                case HeliosVariable.UNIT_DAY:
                                    val = val.toUnit(Units.DAY);
                                    break;
                                case HeliosVariable.UNIT_HOUR:
                                    val = val.toUnit(Units.HOUR);
                                    break;
                                case HeliosVariable.UNIT_MIN:
                                    val = val.toUnit(Units.MINUTE);
                                    break;
                                case HeliosVariable.UNIT_SEC:
                                    val = val.toUnit(Units.SECOND);
                                    break;
                                case HeliosVariable.UNIT_VOLT:
                                    val = val.toUnit(Units.VOLT);
                                    break;
                                case HeliosVariable.UNIT_PERCENT:
                                    val = val.toUnit(Units.PERCENT);
                                    break;
                                case HeliosVariable.UNIT_PPM:
                                    val = val.toUnit(Units.PARTS_PER_MILLION);
                                    break;
                                case HeliosVariable.UNIT_TEMP:
                                    val = val.toUnit(SIUnits.CELSIUS);
                                    break;
                            }
                            value = val != null ? String.valueOf(val.doubleValue()) : null; // ignore the UoM
                        }
                    }
                }
            }
            if (value != null) {
                final String v = value;
                scheduler.submit(() -> {
                    try {
                        writeValue(channelId, v);
                        if (variableMap != null) {
                            HeliosVariable variable = variableMap.get(channelId);
                            if (variable != null) {
                                updateState(variable, v);
                                updateStatus(ThingStatus.ONLINE);
                            }
                        }
                    } catch (HeliosException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Writing value " + v + "to channel " + channelId + " failed: " + e.getMessage());
                    } catch (InterruptedException e) {
                        logger.debug(
                                "{} encountered Exception when trying to lock Semaphore for writing variable {} to the device: {}",
                                HeliosEasyControlsHandler.class.getSimpleName(), channelId, e.getMessage());

                    }
                });
            }
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(HeliosEasyControlsActions.class);
    }

    /**
     * Checks if the provided variable name is a property
     *
     * @param variableName The variable's name
     * @return true if the variable is a property
     */
    private boolean isProperty(String variableName) {
        return HeliosEasyControlsBindingConstants.PROPERTY_NAMES.contains(variableName);
    }

    /**
     * Writes a variable value to the Helios device
     *
     * @param variableName The variable name
     * @param value The new value
     * @return The value if the transaction succeeded, <tt>null</tt> otherwise
     * @throws HeliosException Thrown if the variable is read-only or the provided value is out of range
     */
    public void writeValue(String variableName, String value) throws HeliosException, InterruptedException {
        if (this.variableMap == null) {
            this.handleError("Variable definition is unavailable.", ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        } else {
            Map<String, HeliosVariable> variableMap = this.variableMap;
            if (variableMap != null) {
                HeliosVariable v = variableMap.get(variableName);

                if (v != null) {
                    if (!v.hasWriteAccess()) {
                        throw new HeliosException("Variable " + variableName + " is read-only");
                    } else if (!v.isInAllowedRange(value)) {
                        throw new HeliosException(
                                "Value " + value + " is outside of allowed range of variable " + variableName);
                    } else if (this.comms != null) {
                        // write to device
                        String payload = v.getVariableString() + "=" + value;
                        ModbusCommunicationInterface comms = this.comms;
                        if (comms != null) {
                            final Semaphore lock = transactionLocks.get(comms.getEndpoint());
                            if (lock != null) {
                                lock.acquire();
                                comms.submitOneTimeWrite(new ModbusWriteRegisterRequestBlueprint(
                                        HeliosEasyControlsBindingConstants.UNIT_ID,
                                        HeliosEasyControlsBindingConstants.START_ADDRESS, preparePayload(payload), true,
                                        HeliosEasyControlsBindingConstants.MAX_TRIES), result -> {
                                            lock.release();
                                            updateStatus(ThingStatus.ONLINE);
                                        }, failureInfo -> {
                                            lock.release();
                                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                                    "Error writing to device: " + failureInfo.getCause().getMessage());
                                        });
                            }
                        }
                    } else { // comms is null
                        this.handleError("Modbus communication interface is null",
                                ThingStatusDetail.COMMUNICATION_ERROR);
                    }
                }
            }
        }
    }

    /**
     * Read a variable from the Helios device
     *
     * @param variableName The variable name
     * @return The value
     */
    public void readValue(String variableName) {
        Map<String, HeliosVariable> variableMap = this.variableMap;
        ModbusCommunicationInterface comms = this.comms;
        if ((comms != null) && (variableMap != null)) {
            final Semaphore lock = transactionLocks.get(comms.getEndpoint());
            HeliosVariable v = variableMap.get(variableName);
            if ((v != null) && v.hasReadAccess() && (lock != null)) {
                try {
                    lock.acquire(); // will block until lock is available
                } catch (InterruptedException e) {
                    logger.warn("{} encountered Exception when trying to read variable {} from the device: {}",
                            HeliosEasyControlsHandler.class.getSimpleName(), variableName, e.getMessage());
                    return;
                }
                // write variable name to register
                String payload = v.getVariableString();
                comms.submitOneTimeWrite(new ModbusWriteRegisterRequestBlueprint(
                        HeliosEasyControlsBindingConstants.UNIT_ID, HeliosEasyControlsBindingConstants.START_ADDRESS,
                        preparePayload(payload), true, HeliosEasyControlsBindingConstants.MAX_TRIES), result -> {
                            comms.submitOneTimePoll(
                                    new ModbusReadRequestBlueprint(HeliosEasyControlsBindingConstants.UNIT_ID,
                                            ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                                            HeliosEasyControlsBindingConstants.START_ADDRESS, v.getCount(),
                                            HeliosEasyControlsBindingConstants.MAX_TRIES),
                                    pollResult -> {
                                        lock.release();
                                        Optional<ModbusRegisterArray> registers = pollResult.getRegisters();
                                        if (registers.isPresent()) {
                                            processResponse(v, registers.get());
                                        }
                                    }, failureInfo -> {
                                        lock.release();
                                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                                "Error reading from device: " + failureInfo.getCause().getMessage());
                                    });
                        }, failureInfo -> {
                            lock.release();
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Error writing to device: " + failureInfo.getCause().getMessage());

                        });
            }

        } else {
            if (this.comms == null) {
                this.handleError("Modbus communication interface is unavailable",
                        ThingStatusDetail.COMMUNICATION_ERROR);
            }
            if (variableMap == null) {
                this.handleError("Variable definition is unavailable", ThingStatusDetail.CONFIGURATION_ERROR);
            }
        }
    }

    private void updateSysDate(DateTimeType dateTime) {
        this.updateSysDateTime(dateTime.getZonedDateTime(), true, sysDate.getOffset().getTotalSeconds() / 60 / 60);
    }

    private void updateSysTime(DateTimeType dateTime) {
        this.updateSysDateTime(dateTime.getZonedDateTime(), false, sysDate.getOffset().getTotalSeconds() / 60 / 60);
    }

    private void updateUtcOffset(int utcOffset) {
        this.updateSysDateTime(this.sysDate, true, sysDate.getOffset().getTotalSeconds() / 60 / 60);
    }

    private void updateSysDateTime(ZonedDateTime dateTime, boolean updateDate, int utcOffset) {
        ZonedDateTime sysDate = this.sysDate;
        sysDate = ZonedDateTime.of(updateDate ? dateTime.getYear() : sysDate.getYear(),
                updateDate ? dateTime.getMonthValue() : sysDate.getMonthValue(),
                updateDate ? dateTime.getDayOfMonth() : sysDate.getDayOfMonth(),
                updateDate ? sysDate.getHour() : dateTime.getHour(),
                updateDate ? sysDate.getMinute() : dateTime.getMinute(),
                updateDate ? sysDate.getSecond() : dateTime.getSecond(), 0,
                ZoneId.of("UTC" + (utcOffset >= 0 ? "+" : "") + String.format("%02d", utcOffset) + ":00"));
        updateState("general#" + HeliosEasyControlsBindingConstants.SYS_DATE, new DateTimeType(sysDate));
        this.sysDate = sysDate;
    }

    private void setSysDateTime(ZonedDateTime date) throws InterruptedException {
        try {
            this.writeValue(HeliosEasyControlsBindingConstants.DATE,
                    this.formatDate(HeliosEasyControlsBindingConstants.DATE, date));
            this.writeValue(HeliosEasyControlsBindingConstants.TIME,
                    date.getHour() + ":" + date.getMinute() + ":" + date.getSecond());
            this.writeValue(HeliosEasyControlsBindingConstants.TIME_ZONE_DIFFERENCE_TO_GMT,
                    Integer.toString(date.getOffset().getTotalSeconds() / 60 / 60));
        } catch (HeliosException e) {
            logger.warn("{} encountered Exception when trying to set system date: {}",
                    HeliosEasyControlsHandler.class.getSimpleName(), e.getMessage());
        }
    }

    protected void setSysDateTime() throws InterruptedException {
        this.setSysDateTime(ZonedDateTime.now());
    }

    private void updateBypass(boolean from, boolean month, int val) {
        BypassDate bypassDate = from ? this.bypassFrom : this.bypassTo;
        if (bypassDate == null) {
            bypassDate = new BypassDate();
        }
        if (month) {
            bypassDate.setMonth(val);

        } else {
            bypassDate.setDay(val);
        }
        updateState("unitConfig#" + (from ? HeliosEasyControlsBindingConstants.BYPASS_FROM
                : HeliosEasyControlsBindingConstants.BYPASS_TO), bypassDate.toDateTimeType());
        if (from) {
            this.bypassFrom = bypassDate;

        } else {
            this.bypassTo = bypassDate;
        }
    }

    protected void setBypass(boolean from, int day, int month) throws InterruptedException {
        BypassDate bypassDate = new BypassDate(day, month);
        try {
            this.writeValue(from ? HeliosEasyControlsBindingConstants.BYPASS_FROM_DAY
                    : HeliosEasyControlsBindingConstants.BYPASS_TO_DAY, Integer.toString(bypassDate.getDay()));
            this.writeValue(
                    from ? HeliosEasyControlsBindingConstants.BYPASS_FROM_MONTH
                            : HeliosEasyControlsBindingConstants.BYPASS_TO_MONTH,
                    Integer.toString(bypassDate.getMonth()));
        } catch (HeliosException e) {
            logger.warn("{} encountered Exception when trying to set bypass period: {}",
                    HeliosEasyControlsHandler.class.getSimpleName(), e.getMessage());
        }
    }

    /**
     * Formats the provided date to a string in the device's configured date format
     *
     * @param variableName the variable name
     * @param date the date to be formatted
     * @return a string in the device's configured date format
     */
    public String formatDate(String variableName, ZonedDateTime date) {
        String y = Integer.toString(date.getYear());
        String m = Integer.toString(date.getMonthValue());
        if (m.length() == 1) {
            m = "0" + m;
        }
        String d = Integer.toString(date.getDayOfMonth());
        if (d.length() == 1) {
            d = "0" + d;
        }
        if (variableName.equals(HeliosEasyControlsBindingConstants.DATE)) { // fixed format for writing the system date
            return d + "." + m + "." + y;
        } else {
            switch (this.dateFormat) {
                case 0: // dd.mm.yyyy
                    return d + "." + m + "." + y;
                case 1: // mm.dd.yyyy
                    return m + "." + d + "." + y;
                case 2: // yyyy.mm.dd
                    return y + "." + m + "." + d;
                default:
                    return d + "." + m + "." + y;
            }
        }
    }

    private List<String> getMessages(long bitMask, int bits, String prefix) {
        ArrayList<String> msg = new ArrayList<String>();
        long mask = 1;
        for (int i = 0; i < bits; i++) {
            if ((bitMask & mask) != 0) {
                msg.add(translationProvider.getText(prefix + i));
            }
            mask <<= 1;
        }
        return msg;
    }

    /**
     * Transforms the errors provided by the device into a human readable form (the basis for the
     * corresponding action)
     *
     * @return an <code>List</code> of messages indicated by the error flags sent by the device
     */
    protected List<String> getErrorMessages() {
        return this.getMessages(this.errors, HeliosEasyControlsBindingConstants.BITS_ERROR_MSG,
                HeliosEasyControlsBindingConstants.PREFIX_ERROR_MSG);
    }

    /**
     * Transforms the warnings provided by the device into a human readable form (the basis for the
     * corresponding action)
     *
     * @return an <code>List</code> of messages indicated by the warning flags sent by the device
     */
    protected List<String> getWarningMessages() {
        return this.getMessages(this.warnings, HeliosEasyControlsBindingConstants.BITS_WARNING_MSG,
                HeliosEasyControlsBindingConstants.PREFIX_WARNING_MSG);
    }

    /**
     * Transforms the infos provided by the device into a human readable form (the basis for the
     * corresponding action)
     *
     * @return an <code>List</code> of messages indicated by the info flags sent by the device
     */
    protected List<String> getInfoMessages() {
        return this.getMessages(this.infos, HeliosEasyControlsBindingConstants.BITS_INFO_MSG,
                HeliosEasyControlsBindingConstants.PREFIX_INFO_MSG);
    }

    /**
     * Transforms the status flags provided by the device into a human readable form (the basis for the
     * corresponding action)
     *
     * @return an <code>List</code> of messages indicated by the status flags sent by the device
     */
    protected List<String> getStatusMessages() {
        ArrayList<String> msg = new ArrayList<String>();
        if (this.statusFlags.length() == HeliosEasyControlsBindingConstants.BITS_STATUS_MSG) {
            for (int i = 0; i < HeliosEasyControlsBindingConstants.BITS_STATUS_MSG; i++) {
                String key = HeliosEasyControlsBindingConstants.PREFIX_STATUS_MSG + i + "."
                        + (this.statusFlags.substring(HeliosEasyControlsBindingConstants.BITS_STATUS_MSG - i - 1,
                                HeliosEasyControlsBindingConstants.BITS_STATUS_MSG - i));
                String text = translationProvider.getText(key);
                if (!text.equals(key)) { // there is a text in the properties file (no text => flag is irrelevant)
                    msg.add(text);
                }
            }
        } else {
            msg.add("Status messages have not yet been read from the device");
        }
        return msg;
    }

    /**
     * Returns a DateTimeType object based on the provided String and the device's configured date format
     *
     * @param date The date string read from the device
     * @return A DateTimeType object representing the date or time specified
     */
    private DateTimeType toDateTime(String date) {
        String[] dateTimeParts = null;
        String dateTime = date;
        dateTimeParts = date.split("\\."); // try to split date components
        if (dateTimeParts.length == 1) { // time
            return DateTimeType.valueOf(date);
        } else if (dateTimeParts.length == 3) { // date - we'll try the device's date format
            switch (this.dateFormat) {
                case 0: // dd.mm.yyyy
                    dateTime = dateTimeParts[2] + "-" + dateTimeParts[1] + "-" + dateTimeParts[0];
                    break;
                case 1: // mm.dd.yyyy
                    dateTime = dateTimeParts[2] + "-" + dateTimeParts[0] + "-" + dateTimeParts[1];
                    break;
                case 2: // yyyy.mm.dd
                    dateTime = dateTimeParts[0] + "-" + dateTimeParts[1] + "-" + dateTimeParts[2];
                    break;
                default:
                    dateTime = dateTimeParts[2] + "-" + dateTimeParts[1] + "-" + dateTimeParts[0];
                    break;
            }
            return DateTimeType.valueOf(dateTime);
        }
        // falling back to default date format (apparently using the configured format has failed)
        dateTime = dateTimeParts[2] + "-" + dateTimeParts[1] + "-" + dateTimeParts[0];
        return DateTimeType.valueOf(dateTime);
    }

    private @Nullable QuantityType<?> toQuantityType(String value, @Nullable String unit) {
        if (unit == null) {
            return null;
        } else if (unit.equals(HeliosVariable.UNIT_DAY)) {
            return new QuantityType<>(Integer.parseInt(value), Units.DAY);
        } else if (unit.equals(HeliosVariable.UNIT_HOUR)) {
            return new QuantityType<>(Integer.parseInt(value), Units.HOUR);
        } else if (unit.equals(HeliosVariable.UNIT_MIN)) {
            return new QuantityType<>(Integer.parseInt(value), Units.MINUTE);
        } else if (unit.equals(HeliosVariable.UNIT_SEC)) {
            return new QuantityType<>(Integer.parseInt(value), Units.SECOND);
        } else if (unit.equals(HeliosVariable.UNIT_VOLT)) {
            return new QuantityType<>(Float.parseFloat(value), Units.VOLT);
        } else if (unit.equals(HeliosVariable.UNIT_PERCENT)) {
            return new QuantityType<>(Float.parseFloat(value), Units.PERCENT);
        } else if (unit.equals(HeliosVariable.UNIT_PPM)) {
            return new QuantityType<>(Float.parseFloat(value), Units.PARTS_PER_MILLION);
        } else if (unit.equals(HeliosVariable.UNIT_TEMP)) {
            return new QuantityType<>(Float.parseFloat(value), SIUnits.CELSIUS);
        } else {
            return null;
        }
    }

    /**
     * Prepares the payload for the request
     *
     * @param payload The String representation of the payload
     * @return The Register representation of the payload
     */
    private static ModbusRegisterArray preparePayload(String payload) {
        // determine number of registers
        byte[] asciiBytes = payload.getBytes(StandardCharsets.US_ASCII);
        int bufferLength = asciiBytes.length // ascii characters
                + 1 // NUL byte
                + ((asciiBytes.length % 2 == 0) ? 1 : 0); // to have even number of bytes
        assert bufferLength % 2 == 0; // Invariant, ensured above

        byte[] buffer = new byte[bufferLength];
        System.arraycopy(asciiBytes, 0, buffer, 0, asciiBytes.length);
        // Fill in rest of bytes with NUL bytes
        for (int i = asciiBytes.length; i < buffer.length; i++) {
            buffer[i] = '\0';
        }
        return new ModbusRegisterArray(buffer);
    }

    /**
     * Decodes the Helios device' response and updates the channel with the actual value of the variable
     *
     * @param response The registers received from the Helios device
     * @return The value or <tt>null</tt> if an error occurred
     */
    private void processResponse(HeliosVariable v, ModbusRegisterArray registers) {
        String r = ModbusBitUtilities.extractStringFromRegisters(registers, 0, registers.size() * 2,
                StandardCharsets.US_ASCII);
        String[] parts = r.split("=", 2); // remove the part "vXXXX=" from the string
        // making sure we have a proper response and the response matches the requested variable
        if ((parts.length == 2) && (v.getVariableString().equals(parts[0]))) {
            if (this.isProperty(v.getName())) {
                try {
                    updateProperty(
                            translationProvider
                                    .getText(HeliosEasyControlsBindingConstants.PROPERTIES_PREFIX + v.getName()),
                            v.formatPropertyValue(parts[1], translationProvider));
                } catch (HeliosException e) {
                    logger.warn("{} encountered Exception when trying to update property: {}",
                            HeliosEasyControlsHandler.class.getSimpleName(), e.getMessage());
                }
            } else {
                this.updateState(v, parts[1]);
            }
        } else { // another variable was read
            logger.warn("{} tried to read value from variable {} and the result provided by the device was {}",
                    HeliosEasyControlsHandler.class.getSimpleName(), v.getName(), r);
        }
    }

    private void updateState(HeliosVariable v, String value) {
        String variableType = v.getType();
        // System date and time
        if (v.getName().equals(HeliosEasyControlsBindingConstants.DATE)) {
            this.updateSysDate(this.toDateTime(value));
        } else if (v.getName().equals(HeliosEasyControlsBindingConstants.TIME)) {
            this.updateSysTime(this.toDateTime(value));
        } else if (v.getName().equals(HeliosEasyControlsBindingConstants.TIME_ZONE_DIFFERENCE_TO_GMT)) {
            this.updateUtcOffset(Integer.parseInt(value));
            // Bypass
        } else if (v.getName().equals(HeliosEasyControlsBindingConstants.BYPASS_FROM_DAY)) {
            this.updateBypass(true, false, Integer.parseInt(value));
        } else if (v.getName().equals(HeliosEasyControlsBindingConstants.BYPASS_FROM_MONTH)) {
            this.updateBypass(true, true, Integer.parseInt(value));
        } else if (v.getName().equals(HeliosEasyControlsBindingConstants.BYPASS_TO_DAY)) {
            this.updateBypass(false, false, Integer.parseInt(value));
        } else if (v.getName().equals(HeliosEasyControlsBindingConstants.BYPASS_TO_MONTH)) {
            this.updateBypass(false, true, Integer.parseInt(value));
        } else {
            Channel channel = getThing().getChannel(v.getGroupAndName());
            String itemType;
            if (channel != null) {
                itemType = channel.getAcceptedItemType();
                if (itemType != null) {
                    if (itemType.startsWith("Number:")) {
                        itemType = "Number";
                    }
                    switch (itemType) {
                        case "Number":
                            if (((HeliosVariable.TYPE_INTEGER.equals(variableType))
                                    || (HeliosVariable.TYPE_FLOAT.equals(variableType))) && (!value.equals("-"))) {
                                State state = null;
                                if (v.getUnit() == null) {
                                    state = DecimalType.valueOf(value);
                                } else { // QuantityType
                                    state = this.toQuantityType(value, v.getUnit());
                                }
                                if (state != null) {
                                    updateState(v.getGroupAndName(), state);
                                    updateStatus(ThingStatus.ONLINE);
                                    // update date format and messages upon read
                                    if (v.getName().equals(HeliosEasyControlsBindingConstants.DATE_FORMAT)) {
                                        this.dateFormat = Integer.parseInt(value);
                                    } else if (v.getName().equals(HeliosEasyControlsBindingConstants.ERRORS)) {
                                        this.errors = Long.parseLong(value);
                                    } else if (v.getName().equals(HeliosEasyControlsBindingConstants.WARNINGS)) {
                                        this.warnings = Integer.parseInt(value);
                                    } else if (v.getName().equals(HeliosEasyControlsBindingConstants.INFOS)) {
                                        this.infos = Integer.parseInt(value);
                                    }
                                }
                            }
                            break;
                        case "Switch":
                            if (variableType.equals(HeliosVariable.TYPE_INTEGER)) {
                                updateState(v.getGroupAndName(), value.equals("1") ? OnOffType.ON : OnOffType.OFF);
                            }
                            break;
                        case "String":
                            if (variableType.equals(HeliosVariable.TYPE_STRING)) {
                                updateState(v.getGroupAndName(), StringType.valueOf(value));
                                if (v.getName().equals(HeliosEasyControlsBindingConstants.STATUS_FLAGS)) {
                                    this.statusFlags = value;
                                }
                            }
                            break;
                        case "DateTime":
                            if (variableType.equals(HeliosVariable.TYPE_STRING)) {
                                updateState(v.getGroupAndName(), toDateTime(value));
                            }
                            break;
                    }
                } else { // itemType was null
                    logger.warn("{} couldn't determine item type of variable {}",
                            HeliosEasyControlsHandler.class.getSimpleName(), v.getName());
                }
            } else { // channel was null
                logger.warn("{} couldn't find channel for variable {}", HeliosEasyControlsHandler.class.getSimpleName(),
                        v.getName());
            }
        }
    }

    /**
     * Logs an error (as a warning entry) and updates the thing status
     *
     * @param errorMsg The error message to be logged and provided with the Thing's status update
     * @param status The Thing's new status
     */
    private void handleError(String errorMsg, ThingStatusDetail status) {
        updateStatus(ThingStatus.OFFLINE, status, errorMsg);
    }
}
