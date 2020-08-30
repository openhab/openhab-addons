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
package org.openhab.binding.modbus.helioseasycontrols.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusRegister;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
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

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(HeliosEasyControlsHandler.class);

    /**
     * Configuration instance
     */
    private @Nullable HeliosEasyControlsConfiguration config;

    /**
     * Used to poll the variables' status based on the configured refresh interval
     */
    private @Nullable ScheduledFuture<?> pollingJob;

    /**
     * The map holding the variable meta info by variable name
     */
    private @Nullable Map<String, HeliosVariable> vMap;

    /**
     * This flag is used to ensure read requests (consisting of a write and subsequent read) are not influenced by
     * another transaction
     */
    private static ConcurrentHashMap<ModbusSlaveEndpoint, Semaphore> transactionLocks = new ConcurrentHashMap<ModbusSlaveEndpoint, Semaphore>();

    /**
     * Communication interface to the endpoint
     */
    private @Nullable ModbusCommunicationInterface comms;

    /**
     * The date format used by the device
     */
    private int dateFormat = -1;

    /**
     * The UTC offset of the device
     */
    private int utcOffset = 0;

    /**
     * Constructor
     *
     * @param thing
     * @param managerRef
     */
    public HeliosEasyControlsHandler(Thing thing) {
        super(thing);
    }

    /**
     * Reads variable definitions from JSON file and store them in vMap
     */
    private void readVariableDefinition() {
        Gson gson = new Gson();
        Type vMapType = new TypeToken<Map<String, HeliosVariable>>() {
        }.getType();
        this.vMap = gson.fromJson(
                new BufferedReader(new InputStreamReader(
                        getClass().getResourceAsStream(HeliosEasyControlsBindingConstants.VARIABLES_DEFINITION_FILE))),
                vMapType);
        if (vMap != null) {
            // add the name to the variable itself
            for (Map.Entry<String, HeliosVariable> entry : this.vMap.entrySet()) {
                entry.getValue().setName(entry.getKey()); // workaround to set the variable name inside the
                                                          // HeliosVariable object
                if (!entry.getValue().isOk()) {
                    this.handleError("Variables definition file contains inconsistent data.",
                            ThingStatusDetail.CONFIGURATION_ERROR);
                }
            }
        } else {
            this.handleError("Variables definition file not found or of illegal format.",
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
        if ((this.comms != null) && (this.vMap != null) && (this.config != null)) {
            HeliosEasyControlsHandler.transactionLocks.putIfAbsent(this.comms.getEndpoint(), new Semaphore(1, true));
            updateStatus(ThingStatus.UNKNOWN);

            // background initialization
            scheduler.execute(() -> {
                readValue(HeliosEasyControlsBindingConstants.DATE_FORMAT);
                // status will be updated to ONLINE by the read callback function (via processResponse)
            });

            // poll for status updates regularly
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    if (vMap != null) {
                        for (Map.Entry<String, HeliosVariable> entry : vMap.entrySet()) {
                            if (isLinked(entry.getKey())
                                    || (entry.getKey().equals(HeliosEasyControlsBindingConstants.DATE_FORMAT))
                                    || (entry.getKey()
                                            .equals(HeliosEasyControlsBindingConstants.TIME_ZONE_DIFFERENCE_TO_GMT))) {
                                readValue(entry.getKey());
                            }
                        }

                    } else {
                        handleError("Variable definition is null", ThingStatusDetail.CONFIGURATION_ERROR);
                    }
                }
            };
            this.pollingJob = scheduler.scheduleAtFixedRate(runnable, this.config.getRefreshInterval(),
                    this.config.getRefreshInterval(), TimeUnit.MILLISECONDS);

        } else { // at least one null assertion has failed, let's log the problem and update the thing status
            if (this.comms == null) {
                this.handleError("Modbus communication interface is unavailable.",
                        ThingStatusDetail.COMMUNICATION_ERROR);
            }
            if (this.vMap == null) {
                this.handleError("Variable definition is unavailable.", ThingStatusDetail.CONFIGURATION_ERROR);
            }
            if (this.config == null) {
                this.handleError("Binding configuration is unavailable.", ThingStatusDetail.CONFIGURATION_ERROR);
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
            scheduler.submit(new Runnable() {
                @Override
                public void run() {
                    readValue(channelId);
                }
            });
        } else { // write command
            String value = null;
            if (command instanceof OnOffType) {
                value = command == OnOffType.ON ? "1" : "0";
            } else if (command instanceof DateTimeType) {
                value = formatDate(channelId, ((DateTimeType) command).getZonedDateTime());
            } else if ((command instanceof DecimalType) || (command instanceof StringType)) {
                value = command.toString();
            } else if (command instanceof QuantityType<?>) {
                // convert item's unit to the Helios device's unit
                String unit = this.vMap.get(channelId).getUnit();
                QuantityType<?> val = (QuantityType<?>) command;
                if (unit != null) {
                    if (unit.equals(HeliosVariable.UNIT_DAY)) {
                        val = val.toUnit(SmartHomeUnits.DAY);
                    } else if (unit.equals(HeliosVariable.UNIT_HOUR)) {
                        val = val.toUnit(SmartHomeUnits.HOUR);
                    } else if (unit.equals(HeliosVariable.UNIT_MIN)) {
                        val = val.toUnit(SmartHomeUnits.MINUTE);
                    } else if (unit.equals(HeliosVariable.UNIT_SEC)) {
                        val = val.toUnit(SmartHomeUnits.SECOND);
                    } else if (unit.equals(HeliosVariable.UNIT_VOLT)) {
                        val = val.toUnit(SmartHomeUnits.VOLT);
                    } else if (unit.equals(HeliosVariable.UNIT_PERCENT)) {
                        val = val.toUnit(SmartHomeUnits.PERCENT);
                    } else if (unit.equals(HeliosVariable.UNIT_PPM)) {
                        val = val.toUnit(SmartHomeUnits.PARTS_PER_MILLION);
                    } else if (unit.equals(HeliosVariable.UNIT_TEMP)) {
                        val = val.toUnit(SIUnits.CELSIUS);
                    }
                    value = val != null ? String.valueOf(val.doubleValue()) : null; // ignore the UoM
                }
            }
            if (value != null) {
                final String v = value;
                scheduler.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            writeValue(channelId, v);
                            updateStatus(ThingStatus.ONLINE);
                        } catch (HeliosException e) {
                            logger.warn("Writing value {} to channel {} failed: {}", v, channelId, e.getMessage());
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Writing to device failed.");
                        }
                    }
                });
            }
        }
    }

    /**
     * Writes a variable value to the Helios device
     *
     * @param variableName The variable name
     * @param value The new value
     * @return The value if the transaction succeeded, <tt>null</tt> otherwise
     * @throws HeliosException Thrown if the variable is read-only or the provided value is out of range
     */
    public void writeValue(String variableName, String value) throws HeliosException {
        if (this.vMap == null) {
            this.handleError("Variable definition is unavailable.", ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        } else {
            HeliosVariable v = this.vMap.get(variableName);
            if (!v.hasWriteAccess()) {
                throw new HeliosException("Variable " + variableName + " is read-only");
            } else if (!v.isInAllowedRange(value)) {
                throw new HeliosException(
                        "Value " + value + " is outside of allowed range of variable " + variableName);
            } else if (this.comms != null) {
                // write to device
                String payload = v.getVariableString() + "=" + value;
                final Semaphore lock = transactionLocks.get(this.comms.getEndpoint());
                try {
                    lock.acquire();
                    this.comms.submitOneTimeWrite(
                            new ModbusWriteRegisterRequestBlueprint(HeliosEasyControlsBindingConstants.UNIT_ID,
                                    HeliosEasyControlsBindingConstants.START_ADDRESS,
                                    new ModbusRegisterArray(preparePayload(payload)), true,
                                    HeliosEasyControlsBindingConstants.MAX_TRIES),
                            result -> {
                                lock.release();
                                updateStatus(ThingStatus.ONLINE);
                            }, failureInfo -> {
                                String errorMsg = failureInfo.getCause().getMessage();
                                logger.warn("{} encountered error writing to device: {}",
                                        HeliosEasyControlsHandler.class.getSimpleName(), errorMsg);
                                lock.release();
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMsg);
                            });
                    // ensure the openHAB item is updated with the device's actual value
                    scheduler.schedule(new Runnable() {
                        @Override
                        public void run() {
                            readValue(variableName);
                        }
                    }, 1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    logger.warn(
                            "{} encountered Exception when trying to lock Semaphore for writing variable {} to the device: {}",
                            HeliosEasyControlsHandler.class.getSimpleName(), variableName, e.getMessage());
                }
            } else { // comms is null
                this.handleError("Modbus communication interface is null.", ThingStatusDetail.COMMUNICATION_ERROR);
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
        if ((this.comms != null) && (this.vMap != null)) {
            final Semaphore lock = transactionLocks.get(this.comms.getEndpoint());
            HeliosVariable v = this.vMap.get(variableName);
            if (v.hasReadAccess()) {
                try {
                    lock.acquire(); // will block until lock is available
                } catch (InterruptedException e) {
                    logger.warn("{} encountered Exception when trying to read variable {} from the device: {}",
                            HeliosEasyControlsHandler.class.getSimpleName(), variableName, e.getMessage());
                    return;
                }
                // write variable name to register
                String payload = v.getVariableString();
                this.comms.submitOneTimeWrite(new ModbusWriteRegisterRequestBlueprint(
                        HeliosEasyControlsBindingConstants.UNIT_ID, HeliosEasyControlsBindingConstants.START_ADDRESS,
                        new ModbusRegisterArray(preparePayload(payload)), true,
                        HeliosEasyControlsBindingConstants.MAX_TRIES), result -> {
                            this.comms.submitOneTimePoll(
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
                                        String errorMsg = failureInfo.getCause().getMessage();
                                        logger.warn("{} encountered error reading from device: {}",
                                                HeliosEasyControlsHandler.class.getSimpleName(), errorMsg);
                                        lock.release();
                                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                                errorMsg);
                                    });
                        }, failureInfo -> {
                            String errorMsg = failureInfo.getCause().getMessage();
                            logger.warn("{} encountered error writing to device: {}",
                                    HeliosEasyControlsHandler.class.getSimpleName(), errorMsg);
                            lock.release();
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMsg);

                        });
            }
        } else {
            if (this.comms == null) {
                this.handleError("Modbus communication interface is unavailable.",
                        ThingStatusDetail.COMMUNICATION_ERROR);
            }
            if (this.vMap == null) {
                this.handleError("Variable definition is unavailable.", ThingStatusDetail.CONFIGURATION_ERROR);
            }
        }
    }

    // TODO: set time incl. timezone, update time channels based on timezone read from device

    /**
     * Formats the provided date to a string in the device's configured date format
     *
     * @param variableName the variable name
     * @param date the date to be formatted
     * @return a string in the device's configured date format
     */
    private String formatDate(String variableName, ZonedDateTime date) {
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

    /**
     * Returns a DateTimeType object based on the provided String and the device's configured date format
     *
     * @param date The date string read from the device
     * @return A DateTimeType object representing the date or time specified
     */
    private DateTimeType toDateTime(String date) {
        String[] dateTimeParts = null;
        String dateTime = date;
        try { // date
            dateTimeParts = date.split("\\."); // try to split date components
            if (dateTimeParts.length == 1) { // time
                return DateTimeType.valueOf(date);
            } else { // date - we'll try the device's date format
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
        } catch (Exception e) {
            // falling back to default date format (apparently using the configured format has failed)
            if (dateTimeParts != null) {
                dateTime = dateTimeParts[2] + "-" + dateTimeParts[1] + "-" + dateTimeParts[0];
                return DateTimeType.valueOf(dateTime);
            } else {
                logger.warn("{} couldn't parse the provided date string: {}",
                        HeliosEasyControlsHandler.class.getSimpleName(), date);
                return new DateTimeType(); // return at least a valid DateTimeType object
            }

        }
    }

    private @Nullable QuantityType<?> toQuantityType(String value, @Nullable String unit) {
        if (unit == null) {
            return null;
        } else if (unit.equals(HeliosVariable.UNIT_DAY)) {
            return new QuantityType<>(Integer.parseInt(value), SmartHomeUnits.DAY);
        } else if (unit.equals(HeliosVariable.UNIT_HOUR)) {
            return new QuantityType<>(Integer.parseInt(value), SmartHomeUnits.HOUR);
        } else if (unit.equals(HeliosVariable.UNIT_MIN)) {
            return new QuantityType<>(Integer.parseInt(value), SmartHomeUnits.MINUTE);
        } else if (unit.equals(HeliosVariable.UNIT_SEC)) {
            return new QuantityType<>(Integer.parseInt(value), SmartHomeUnits.SECOND);
        } else if (unit.equals(HeliosVariable.UNIT_VOLT)) {
            return new QuantityType<>(Float.parseFloat(value), SmartHomeUnits.VOLT);
        } else if (unit.equals(HeliosVariable.UNIT_PERCENT)) {
            return new QuantityType<>(Float.parseFloat(value), SmartHomeUnits.PERCENT);
        } else if (unit.equals(HeliosVariable.UNIT_PPM)) {
            return new QuantityType<>(Float.parseFloat(value), SmartHomeUnits.PARTS_PER_MILLION);
        } else if (unit.equals(HeliosVariable.UNIT_TEMP)) {
            return new QuantityType<>(Float.parseFloat(value), SIUnits.CELSIUS);
        } else {
            return null;
        }
    }

    // TODO: Set date, time and UTC delta together => also additional channel required

    /**
     * Prepares the payload for the request
     *
     * @param payload The String representation of the payload
     * @return The Register representation of the payload
     */
    private ModbusRegister[] preparePayload(String payload) {

        // determine number of registers
        int l = (payload.length() + 1) / 2; // +1 because we need to include at least one termination symbol 0x00
        if ((payload.length() + 1) % 2 != 0) {
            l++;
        }

        ModbusRegister reg[] = new ModbusRegister[l];
        byte[] b = payload.getBytes();
        int ch = 0;
        for (int i = 0; i < reg.length; i++) {
            byte b1 = ch < b.length ? b[ch] : (byte) 0x00; // terminate with 0x00 if at the end of the payload
            ch++;
            byte b2 = ch < b.length ? b[ch] : (byte) 0x00;
            ch++;
            reg[i] = new ModbusRegister(b1, b2);
        }
        return reg;
    }

    /**
     * Decodes the Helios device' response and updates the channel with the actual value of the variable
     *
     * @param response The registers received from the Helios device
     * @return The value or <tt>null</tt> if an error occurred
     */
    private void processResponse(HeliosVariable v, ModbusRegisterArray registers) {
        // decode response
        byte[] b = new byte[registers.size() * 2];
        int actSize = 0; // track the actual size of the usable array (excluding any 0x00
                         // characters)
        for (int i = 0; i < registers.size(); i++) {
            byte[] reg = registers.getRegister(i).getBytes();
            if (reg.length == 2) { // only add to the array if it's a usable character
                if (reg[0] != 0x00) {
                    b[actSize++] = reg[0];
                }
                if (reg[1] != 0x00) {
                    b[actSize++] = reg[1];
                }
            }
        }
        b = Arrays.copyOf(b, actSize); // before creating a string of it the array needs to be
                                       // truncated
        String r = new String(b, StandardCharsets.US_ASCII);
        String[] parts = r.split("=", 2); // remove the part "vXXXX=" from the string
        // making sure we have a proper response and the response matches the requested variable
        if ((parts.length == 2) && (v.getVariableString().equals(parts[0]))) {
            String variableType = v.getType();
            Channel channel = getThing().getChannel(v.getGroupAndName());
            String itemType;
            if (channel != null) {
                itemType = channel.getAcceptedItemType();
                if (itemType != null) {
                    switch (itemType) {
                        case "Number":
                            if (((variableType.equals(HeliosVariable.TYPE_INTEGER))
                                    || (variableType == HeliosVariable.TYPE_FLOAT)) && (!parts[1].equals("-"))) {
                                State state = null;
                                if (v.getUnit() == null) {
                                    state = DecimalType.valueOf(parts[1]);
                                } else { // QuantityType
                                    state = this.toQuantityType(parts[1], v.getUnit());
                                }
                                if (state != null) {
                                    updateState(v.getGroupAndName(), state);
                                    updateStatus(ThingStatus.ONLINE);
                                    // update date format and UTC offset upon read
                                    if (v.getName().equals(HeliosEasyControlsBindingConstants.DATE_FORMAT)) {
                                        this.dateFormat = Integer.parseInt(parts[1]);
                                    } else if (v.getName()
                                            .equals(HeliosEasyControlsBindingConstants.TIME_ZONE_DIFFERENCE_TO_GMT)) {
                                        this.utcOffset = Integer.parseInt(parts[1]);
                                    }
                                }
                            }
                            break;
                        case "Switch":
                            if (variableType.equals(HeliosVariable.TYPE_INTEGER)) {
                                updateState(v.getGroupAndName(), parts[1].equals("1") ? OnOffType.ON : OnOffType.OFF);
                            }
                            break;
                        case "String":
                            if (variableType.equals(HeliosVariable.TYPE_STRING)) {
                                updateState(v.getGroupAndName(), StringType.valueOf(parts[1]));
                            }
                            break;
                        case "DateTime":
                            if (variableType.equals(HeliosVariable.TYPE_STRING)) {
                                updateState(v.getGroupAndName(), toDateTime(parts[1]));
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
        } else { // another variable was read
            logger.warn("{} tried to read value from variable {} and the result provided by the device was {}",
                    HeliosEasyControlsHandler.class.getSimpleName(), v.getName(), r);
        }
    }

    /**
     * Logs an error (as a warning entry) and updates the thing status
     *
     * @param errorMsg The error message to be logged and provided with the Thing's status update
     * @param status The Thing's new status
     */
    private void handleError(String errorMsg, ThingStatusDetail status) {
        logger.warn("{}", errorMsg);
        updateStatus(ThingStatus.OFFLINE, status, errorMsg);
    }
}