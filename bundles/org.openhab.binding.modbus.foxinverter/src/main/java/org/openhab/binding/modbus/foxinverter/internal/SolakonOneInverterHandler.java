/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.foxinverter.internal;

import static org.openhab.binding.modbus.foxinverter.internal.ModbusFoxInverterBindingConstants.*;

import java.text.MessageFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.BaseModbusThingHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusConstants;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolakonOneInverterHandler} is responsible for reading the Modbus values of the
 * Solakon ONE inverter.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public class SolakonOneInverterHandler extends BaseModbusThingHandler {
    // used by Register definition to mark a new request must be created,
    // required if you are not allowed to read certain registers ranges
    public static final int ENFORCE_NEW_REQUEST = -1;

    private static final class ModbusRequest {

        private final Deque<SolakonOneInverterRegisters> registers;
        private final ModbusReadRequestBlueprint blueprint;

        public ModbusRequest(Deque<SolakonOneInverterRegisters> registers, int slaveId, int tries) {
            this.registers = registers;
            this.blueprint = initReadRequest(registers, slaveId, tries);
        }

        private ModbusReadRequestBlueprint initReadRequest(Deque<SolakonOneInverterRegisters> registers, int slaveId,
                int tries) {
            int firstRegister = Objects.requireNonNull(registers.getFirst()).getRegisterNumber();
            int lastRegister = Objects.requireNonNull(registers.getLast()).getRegisterNumber();
            int length = lastRegister - firstRegister + Objects.requireNonNull(registers.getLast()).getRegisterCount();
            assert length <= ModbusConstants.MAX_REGISTERS_READ_COUNT;

            return new ModbusReadRequestBlueprint(slaveId, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                    firstRegister, length, tries);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(SolakonOneInverterHandler.class);
    private @Nullable LocaleProvider localeProvider;
    private @Nullable TranslationProvider translationProvider;
    private final Bundle bundle;

    private List<ModbusRequest> modbusRequests = new ArrayList<>();

    private int[] alarm = new int[3]; // cache status of the 3 alarm registers
    private boolean alarmState = false; // previous alarm state
    private boolean statusFault = false; // cache status of fault bit

    public SolakonOneInverterHandler(Thing thing, final @Nullable TranslationProvider translationProvider,
            final @Nullable LocaleProvider localeProvider) {
        super(thing);
        this.localeProvider = localeProvider;
        this.translationProvider = translationProvider;
        bundle = FrameworkUtil.getBundle(this.getClass());
    }

    /**
     * Splits the Solakon ONE Inverter Registers into multiple ModbusRequest, to ensure the max request size.
     */
    private List<ModbusRequest> buildRequests(int tries) {
        final List<ModbusRequest> requests = new ArrayList<>();
        Deque<SolakonOneInverterRegisters> currentRequest = new ArrayDeque<>();
        int currentRequestFirstRegister = 0;

        for (SolakonOneInverterRegisters channel : SolakonOneInverterRegisters.values()) {
            logger.debug("Evaluating register {}", channel.name());

            if (currentRequest.isEmpty()) {
                currentRequest.add(channel);
                currentRequestFirstRegister = channel.getRegisterNumber();
            } else if (ENFORCE_NEW_REQUEST == channel.getRegisterNumber()) {
                // marker, start new request
                if (!currentRequest.isEmpty()) {
                    requests.add(new ModbusRequest(currentRequest, getSlaveId(), tries));
                }
                currentRequest = new ArrayDeque<>();
            } else {
                int sizeWithRegisterAdded = channel.getRegisterNumber() - currentRequestFirstRegister
                        + channel.getRegisterCount();
                if (sizeWithRegisterAdded > ModbusConstants.MAX_REGISTERS_READ_COUNT) {
                    requests.add(new ModbusRequest(currentRequest, getSlaveId(), tries));
                    currentRequest = new ArrayDeque<>();

                    currentRequest.add(channel);
                    currentRequestFirstRegister = channel.getRegisterNumber();

                    logger.debug("Starting new Modbus request template due to size limit, first register {} ({})",
                            channel.name(), currentRequestFirstRegister);
                } else {
                    currentRequest.add(channel);
                }
            }
        }

        if (!currentRequest.isEmpty()) {
            requests.add(new ModbusRequest(currentRequest, getSlaveId(), tries));
        }
        logger.debug("Created {} Modbus request templates.", requests.size());
        return requests;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Inverter {}, channel {} received command {}", getThing().getUID(), channelUID, command);

        if (command instanceof RefreshType && !this.modbusRequests.isEmpty()) {
            logger.info("REFRESH command received, submitting one-time polls for all registers.");

            readStaticData();

            for (ModbusRequest request : this.modbusRequests) {
                submitOneTimePoll(request.blueprint,
                        (AsyncModbusReadResult result) -> this.readSuccessful(request, result), this::readError);
            }
        }
    }

    @Override
    public void modbusInitialize() {
        final SolakonOneInverterConfiguration config = getConfigAs(SolakonOneInverterConfiguration.class);

        if (config.pollInterval <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    getTranslation("offline.config.poll_interval", config.pollInterval));
            return;
        }

        if (config.maxTries <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    getTranslation("offline.config.max_tries", config.maxTries));
            return;
        }

        this.updateStatus(ThingStatus.UNKNOWN);
        // read static data, store into properties
        getThing().setProperties(readStaticData());

        // setup regular polling
        this.modbusRequests = this.buildRequests(config.maxTries);

        for (ModbusRequest request : modbusRequests) {
            registerRegularPoll(request.blueprint, config.pollInterval, 0,
                    (AsyncModbusReadResult result) -> this.readSuccessful(request, result), this::readError);
        }
    }

    private Map<String, String> readStaticData() {
        Map<String, String> properties = new java.util.concurrent.ConcurrentHashMap<>();
        try {
            // Initial poll of static info
            // a) manufacturer, model, serial number
            submitOneTimePoll(new ModbusReadRequestBlueprint(getSlaveId(),
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 30000, 16 * 3, 3),
                    (AsyncModbusReadResult result) -> {
                        logger.trace("Initial poll successful {}", result);
                        byte[] res = result.getRegisters().get().getBytes();
                        String modelInfo = new String(res, 0, 16).trim();
                        String serialNo = new String(res, 16, 16).trim();
                        String manufacturerId = new String(res, 32, 16).trim();

                        logger.debug("Inverter Model: {}, S/N: {}, Manufacturer ID: {}", modelInfo, serialNo,
                                manufacturerId);
                        if (!modelInfo.isEmpty()) {
                            properties.put(PROPERTY_MODEL_NAME, modelInfo);
                        }
                        if (!serialNo.isEmpty()) {
                            properties.put(PROPERTY_SERIAL_NO, serialNo);
                        }
                        if (!manufacturerId.isEmpty()) {
                            properties.put(PROPERTY_MANUFACTURER_ID, manufacturerId);
                        }
                    }, (AsyncModbusFailure<ModbusReadRequestBlueprint> error) -> {
                        logger.warn("Initial poll failed", error.getCause());
                    });
            Thread.sleep(1000);
            // b) firmware versions
            submitOneTimePoll(new ModbusReadRequestBlueprint(getSlaveId(),
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 36001, 3, 3), (AsyncModbusReadResult result) -> {
                        byte[] res = result.getRegisters().get().getBytes();
                        properties.put(PROPERTY_FIRMWARE_WR, String.format("%d.%03d", res[0], res[1]));
                        // not sure, could also be 4,5
                        properties.put(PROPERTY_FIRMWARE_PV, String.format("%d.%03d", res[2], res[3]));
                    }, (AsyncModbusFailure<ModbusReadRequestBlueprint> error) -> {
                    });
            Thread.sleep(1000);
            // c) firmware versions
            submitOneTimePoll(new ModbusReadRequestBlueprint(getSlaveId(),
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 37003, 1, 3), (AsyncModbusReadResult result) -> {
                        byte[] res = result.getRegisters().get().getBytes();
                        properties.put(PROPERTY_FIRMWARE_BMS, String.format("%d.%03d", res[0], res[1]));
                    }, (AsyncModbusFailure<ModbusReadRequestBlueprint> error) -> {
                    });
            Thread.sleep(1000);
            // d) rated power, max active power
            submitOneTimePoll(new ModbusReadRequestBlueprint(getSlaveId(),
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 39053, 2 * 2, 3),
                    (AsyncModbusReadResult result) -> {
                        ModbusBitUtilities.extractStateFromRegisters(result.getRegisters().get(), 0,
                                ModbusConstants.ValueType.UINT32).ifPresent(v -> {
                                    properties.put(PROPERTY_RATED_POWER, Objects.toString(v.toBigDecimal()) + " W");
                                });
                        ModbusBitUtilities.extractStateFromRegisters(result.getRegisters().get(), 2,
                                ModbusConstants.ValueType.UINT32).ifPresent(v -> {
                                    properties.put(PROPERTY_MAX_ACTIVE_POWER,
                                            Objects.toString(v.toBigDecimal()) + " W");
                                });
                    }, (AsyncModbusFailure<ModbusReadRequestBlueprint> error) -> {
                    });
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return properties;
    }

    private void processAlarmState() {
        boolean currentAlarmState = (alarm[0] != 0) || (alarm[1] != 0) || (alarm[2] != 0) || statusFault;
        if (currentAlarmState != alarmState) {
            alarmState = currentAlarmState;
            if (alarmState) {
                logger.warn("Inverter {} is in Alarm State: Alarm1=0x{}, Alarm2=0x{}, Alarm3=0x{}, StatusFault={}",
                        getThing().getUID(), Integer.toHexString(alarm[0]), Integer.toHexString(alarm[1]),
                        Integer.toHexString(alarm[2]), statusFault);
                // indicate severe error also by setting the ThingStatus
                // (UNKNOWN seems more appropriate than OFFLINE, as the device still gets commands)
                updateStatus(ThingStatus.UNKNOWN);
            } else {
                logger.info("Inverter {}, alarm resolved", getThing().getUID());
                updateStatus(ThingStatus.ONLINE);
            }
        }
        logger.debug("{} {}", "STATUS_ALARM", OnOffType.from(alarmState));
        updateState(new ChannelUID(thing.getUID(), "fi-overview", "fi-status-alarm"), OnOffType.from(alarmState));
    }

    private void processHiddenChannel(SolakonOneInverterRegisters channel, org.openhab.core.types.State v) {
        // this block deals with channels which are not directly exposed, but
        // used to update other channels
        logger.trace("Update on internal channel {} to {}", channel.getChannelName(), v);
        DecimalType d = v.as(DecimalType.class);
        if (d == null) {
            logger.warn("Internal channel {} is not DecimalType, cannot process", channel.getChannelName());
        } else {
            int i = d.intValue();
            switch (channel.getChannelName()) {
                // active power reports -30000 W during startup, to be suppressed
                case "hidden-active-power":
                    if (i != -30000) {
                        logger.debug("{} {}", "ACTIVE_POWER", v);
                        updateState(
                                new ChannelUID(thing.getUID(), "fi-" + channel.getChannelGroup(), "fi-active-power"),
                                v);
                    }
                    break;
                // grid frequency is used to create STATUS_ON_GRID
                case "hidden-grid-frequency":
                    logger.debug("{} {}", "GRID_FREQUENCY", v);
                    updateState(new ChannelUID(thing.getUID(), "fi-" + channel.getChannelGroup(), "fi-grid-frequency"),
                            v);
                    OnOffType state = OnOffType.from(i >= 1);
                    logger.debug("{} {}", "STATUS_ON_GRID", state);
                    updateState(new ChannelUID(thing.getUID(), "fi-" + channel.getChannelGroup(), "fi-status-on-grid"),
                            state);
                    break;
                // status seems to use only 3 bits, export a separate channel for each
                case "hidden-status1":
                    boolean statusStandby = (i & 0x01) != 0;
                    boolean statusOperation = (i & 0x02) != 0;
                    logger.debug("{} {}", "STATUS_STANDBY", OnOffType.from(statusStandby));
                    updateState(new ChannelUID(thing.getUID(), "fi-" + channel.getChannelGroup(), "fi-status-standby"),
                            OnOffType.from(statusStandby));
                    logger.debug("{} {}", "STATUS_OPERATION", OnOffType.from(statusOperation));
                    updateState(
                            new ChannelUID(thing.getUID(), "fi-" + channel.getChannelGroup(), "fi-status-operation"),
                            OnOffType.from(statusOperation));
                    // this is a global variable, as the fault state is stored and evaluated for alarm output
                    statusFault = (i & 0x40) != 0;
                    processAlarmState();
                    break;
                // alarm states are currently
                case "hidden-alarm1":
                    alarm[0] = i;
                    processAlarmState();
                    break;
                case "hidden-alarm2":
                    alarm[1] = i;
                    processAlarmState();
                    break;
                case "hidden-alarm3":
                    alarm[2] = i;
                    processAlarmState();
                    break;
                // EPS output state is set to 0 or 2 by the app
                case "hidden-eps-output":
                    OnOffType epsState = OnOffType.OFF;
                    if (i == 2) {
                        epsState = OnOffType.ON;
                    }
                    logger.debug("{} {}", "EPS_OUTPUT", epsState);
                    updateState(new ChannelUID(thing.getUID(), "fi-" + channel.getChannelGroup(), "fi-eps-output"),
                            epsState);
                    break;
                default:
                    logger.warn("Unhandled internal channel {}", channel.getChannelName());
            }
        }
    }

    private void readSuccessful(ModbusRequest request, AsyncModbusReadResult result) {
        logger.trace("readSuccessful {}: {}", request, result);
        result.getRegisters().ifPresent(registers -> {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                // if alarm is set, do not set ONLINE as is would override the alarm indication
                if (!alarmState) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }

            int firstRegister = Objects.requireNonNull(request.registers.getFirst()).getRegisterNumber();

            for (SolakonOneInverterRegisters channel : request.registers) {
                int index = channel.getRegisterNumber() - firstRegister;
                logger.debug("{} {}", channel.toString(), ModbusBitUtilities
                        .extractStateFromRegisters(registers, index, channel.getType()).map(channel::createState));
                ModbusBitUtilities.extractStateFromRegisters(registers, index, channel.getType())
                        .map(channel::createState).ifPresentOrElse(v -> {
                            if (!channel.getChannelName().startsWith("hidden-")) {
                                updateState(createChannelUid(channel), v);
                            } else {
                                processHiddenChannel(channel, v);
                            }
                        }, () -> {
                            logger.warn("Could not extract state for channel {}", channel.getChannelName());
                        });
            }
        });
    }

    private void readError(AsyncModbusFailure<ModbusReadRequestBlueprint> error) {
        // TODO improve error handling, stop regular polling on repeated errors, sporadic polling only
        this.logger.debug("Failed to get Modbus data", error.getCause());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                getTranslation("offline.communication_error", error.getCause().getMessage()));
    }

    private ChannelUID createChannelUid(SolakonOneInverterRegisters register) {
        return new ChannelUID(thing.getUID(), "fi-" + register.getChannelGroup(), "fi-" + register.getChannelName());
    }

    /**
     * get translated text
     *
     * @param text text to be translated, may contain placeholders \{n\} for the n-th optional argument of this function
     * @param arguments any optional arguments, will be inserted
     * @return translated text with substitutions if translationProvider is set and provides a translation, otherwise
     *         returns original text with substitutions
     */
    public String getTranslation(final String text, @Nullable Object @Nullable... arguments) {
        if (translationProvider != null) {
            // localeProvider might be null, but if not, getLocale will return NonNull Locale;
            // locale cannot be cached, as getLocale() will return different result once locale is changed by user
            final Locale locale = (localeProvider != null) ? localeProvider.getLocale() : Locale.getDefault();
            final String res = translationProvider.getText(bundle, text, text, locale, arguments);
            if (res != null) {
                return res;
            }
        }
        // translating not possible, we still have the original text without any substitutions
        if (arguments == null || arguments.length == 0) {
            return text;
        }
        // else execute pattern substitution in untranslated text
        return MessageFormat.format(text, arguments);
    }
}
