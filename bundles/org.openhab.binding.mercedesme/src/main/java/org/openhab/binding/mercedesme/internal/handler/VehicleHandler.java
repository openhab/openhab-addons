/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal.handler;

import static org.openhab.binding.mercedesme.internal.Constants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.MercedesMeCommandOptionProvider;
import org.openhab.binding.mercedesme.internal.MercedesMeDynamicStateDescriptionProvider;
import org.openhab.binding.mercedesme.internal.MercedesMeStateOptionProvider;
import org.openhab.binding.mercedesme.internal.actions.VehicleActions;
import org.openhab.binding.mercedesme.internal.config.VehicleConfiguration;
import org.openhab.binding.mercedesme.internal.utils.ChannelStateMap;
import org.openhab.binding.mercedesme.internal.utils.Mapper;
import org.openhab.binding.mercedesme.internal.utils.UOMObserver;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.mbcarkit.proto.Client.ClientMessage;
import com.daimler.mbcarkit.proto.VehicleCommands.AuxheatStart;
import com.daimler.mbcarkit.proto.VehicleCommands.AuxheatStop;
import com.daimler.mbcarkit.proto.VehicleCommands.ChargeProgramConfigure;
import com.daimler.mbcarkit.proto.VehicleCommands.CommandRequest;
import com.daimler.mbcarkit.proto.VehicleCommands.DoorsLock;
import com.daimler.mbcarkit.proto.VehicleCommands.DoorsUnlock;
import com.daimler.mbcarkit.proto.VehicleCommands.EngineStart;
import com.daimler.mbcarkit.proto.VehicleCommands.EngineStop;
import com.daimler.mbcarkit.proto.VehicleCommands.SigPosStart;
import com.daimler.mbcarkit.proto.VehicleCommands.SigPosStart.HornType;
import com.daimler.mbcarkit.proto.VehicleCommands.SigPosStart.LightType;
import com.daimler.mbcarkit.proto.VehicleCommands.SigPosStart.SigposType;
import com.daimler.mbcarkit.proto.VehicleCommands.SunroofClose;
import com.daimler.mbcarkit.proto.VehicleCommands.SunroofLift;
import com.daimler.mbcarkit.proto.VehicleCommands.SunroofOpen;
import com.daimler.mbcarkit.proto.VehicleCommands.TemperatureConfigure;
import com.daimler.mbcarkit.proto.VehicleCommands.TemperatureConfigure.TemperaturePoint;
import com.daimler.mbcarkit.proto.VehicleCommands.WindowsClose;
import com.daimler.mbcarkit.proto.VehicleCommands.WindowsOpen;
import com.daimler.mbcarkit.proto.VehicleCommands.WindowsVentilate;
import com.daimler.mbcarkit.proto.VehicleCommands.ZEVPreconditioningConfigureSeats;
import com.daimler.mbcarkit.proto.VehicleCommands.ZEVPreconditioningStart;
import com.daimler.mbcarkit.proto.VehicleCommands.ZEVPreconditioningStop;
import com.daimler.mbcarkit.proto.VehicleCommands.ZEVPreconditioningType;
import com.daimler.mbcarkit.proto.VehicleEvents;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramParameters;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePointsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdate;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinCommandStatus;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinCommandStatusUpdatesByPID;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;

/**
 * The {@link VehicleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);
    private final MercedesMeCommandOptionProvider mmcop;
    private final MercedesMeStateOptionProvider mmsop;
    private final MercedesMeDynamicStateDescriptionProvider mmdsdp;
    private Map<String, UOMObserver> unitStorage = new HashMap<String, UOMObserver>();
    Map<String, ChannelStateMap> eventStorage = new HashMap<String, ChannelStateMap>();

    private int ignitionState = -1;
    private boolean chargingState = false;
    private int selectedChargeProgram = -1;
    private int activeTemperaturePoint = -1;
    private Map<Integer, QuantityType<Temperature>> temperaturePointsStorage = new HashMap<Integer, QuantityType<Temperature>>();
    private JSONObject chargeGroupValueStorage = new JSONObject();
    private Map<String, State> hvacGroupValueStorage = new HashMap<String, State>();
    private String vehicleType = NOT_SET;
    Optional<AccountHandler> accountHandler = Optional.empty();
    Optional<VehicleConfiguration> config = Optional.empty();

    public VehicleHandler(Thing thing, MercedesMeCommandOptionProvider cop, MercedesMeStateOptionProvider sop,
            MercedesMeDynamicStateDescriptionProvider dsdp) {
        super(thing);
        vehicleType = thing.getThingTypeUID().getId();
        mmcop = cop;
        mmsop = sop;
        mmdsdp = dsdp;
    }

    @Override
    public void initialize() {
        config = Optional.of(getConfigAs(VehicleConfiguration.class));
        Bridge bridge = getBridge();
        if (bridge != null) {
            updateStatus(ThingStatus.UNKNOWN);
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                accountHandler = Optional.of((AccountHandler) handler);
                accountHandler.get().registerVin(config.get().vin, this);
            } else {
                throw new IllegalStateException("BridgeHandler is null");
            }
        } else {
            String textKey = Constants.STATUS_TEXT_PREFIX + "vehicle" + Constants.STATUS_BRIDGE_MISSING;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, textKey);
        }
    }

    @Override
    public void dispose() {
        accountHandler.get().unregisterVin(config.get().vin);
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Received command {} {} for {}", command.getClass(), command, channelUID);
        if (command instanceof RefreshType) {
            if ("feature-capabilities".equals(channelUID.getIdWithoutGroup())
                    || "command-capabilities".equals(channelUID.getIdWithoutGroup())) {
                accountHandler.ifPresent(ah -> {
                    ah.getVehicleCapabilities(config.get().vin);
                });
            } else {
                // deliver from event storage
                ChannelStateMap csm = eventStorage.get(channelUID.getId());
                if (csm != null) {
                    updateChannel(csm);
                }
            }
            // ensure unit update
            unitStorage.remove(channelUID.getIdWithoutGroup());
        } else if (Constants.GROUP_VEHICLE.equals(channelUID.getGroupId())) {
            /**
             * Commands for Vehicle
             */
            if ("ignition".equals(channelUID.getIdWithoutGroup())) {
                String supported = thing.getProperties().get("commandEngineStart");
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Engine Start/Stop not supported {}", supported);
                } else {
                    int commandValue = ((DecimalType) command).intValue();
                    if (commandValue == 4) {
                        String pin = accountHandler.get().config.get().pin;
                        if (Constants.NOT_SET.equals(pin)) {
                            logger.info("Security PIN missing {}", pin);
                        } else {
                            EngineStart eStart = EngineStart.newBuilder().setPin(supported).build();
                            CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                    .setRequestId(UUID.randomUUID().toString()).setEngineStart(eStart).build();
                            ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                            accountHandler.get().sendCommand(cm);
                        }
                    } else if (commandValue == 0) {
                        EngineStop eStop = EngineStop.newBuilder().build();
                        CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                .setRequestId(UUID.randomUUID().toString()).setEngineStop(eStop).build();
                        ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                        accountHandler.get().sendCommand(cm);
                    }
                }
            } else if ("windows".equals(channelUID.getIdWithoutGroup())) {
                String supported = thing.getProperties().get("commandWindowsOpen");
                String pin = accountHandler.get().config.get().pin;
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Windows control not supported {}", supported);
                } else {
                    CommandRequest cr;
                    ClientMessage cm;
                    switch (((DecimalType) command).intValue()) {
                        case 0:
                            if (Constants.NOT_SET.equals(pin)) {
                                logger.info("Security PIN missing {}", pin);
                            } else {
                                WindowsVentilate wv = WindowsVentilate.newBuilder().setPin(pin).build();
                                cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                        .setRequestId(UUID.randomUUID().toString()).setWindowsVentilate(wv).build();
                                cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                                accountHandler.get().sendCommand(cm);
                            }
                            break;
                        case 1:
                            WindowsClose wc = WindowsClose.newBuilder().build();
                            cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                    .setRequestId(UUID.randomUUID().toString()).setWindowsClose(wc).build();
                            cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                            accountHandler.get().sendCommand(cm);
                            break;
                        case 2:
                            if (Constants.NOT_SET.equals(pin)) {
                                logger.info("Security PIN missing {}", pin);
                            } else {
                                WindowsOpen wo = WindowsOpen.newBuilder().setPin(pin).build();
                                cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                        .setRequestId(UUID.randomUUID().toString()).setWindowsOpen(wo).build();
                                cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                                accountHandler.get().sendCommand(cm);
                            }
                            break;
                        default:
                            logger.info("No Windows movement known for {}", command);
                            break;
                    }
                }
            } else if ("lock".equals(channelUID.getIdWithoutGroup())) {
                String pin = accountHandler.get().config.get().pin;
                String supported = thing.getProperties().get("commandDoorsLock");
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Door Lock not supported {}", supported);
                } else {
                    switch (((DecimalType) command).intValue()) {
                        case 0:
                            DoorsLock dl = DoorsLock.newBuilder().build();
                            CommandRequest lockCr = CommandRequest.newBuilder().setVin(config.get().vin)
                                    .setRequestId(UUID.randomUUID().toString()).setDoorsLock(dl).build();
                            ClientMessage lockCm = ClientMessage.newBuilder().setCommandRequest(lockCr).build();
                            accountHandler.get().sendCommand(lockCm);
                            break;
                        case 1:

                            if (Constants.NOT_SET.equals(pin)) {
                                logger.info("Security PIN missing! {}", pin);
                            } else {
                                DoorsUnlock du = DoorsUnlock.newBuilder().setPin(pin).build();
                                CommandRequest unlockCr = CommandRequest.newBuilder().setVin(config.get().vin)
                                        .setRequestId(UUID.randomUUID().toString()).setDoorsUnlock(du).build();
                                ClientMessage unlockCm = ClientMessage.newBuilder().setCommandRequest(unlockCr).build();
                                accountHandler.get().sendCommand(unlockCm);
                            }
                            break;
                        default:
                            logger.info("No lock command mapped to {}", command);
                            break;
                    }
                }
            }
        } else if (Constants.GROUP_HVAC.equals(channelUID.getGroupId())) {
            /**
             * Commands for HVAC
             */
            if (CHANNEL_TEMPERATURE.equals(channelUID.getIdWithoutGroup())) {
                String supported = thing.getProperties().get("commandZevPreconditionConfigure");
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Air Conditioning Temperature Setting not supported {}", supported);
                } else {
                    logger.info("Received Air Condition Temperature change {}", command.getClass());
                    logger.info("Received DecimalType {}", ((QuantityType) command).doubleValue());
                    QuantityType<Temperature> tempToSet = ((QuantityType<Temperature>) command);
                    TemperatureConfigure tc = TemperatureConfigure.newBuilder()
                            .addTemperaturePoints(TemperaturePoint.newBuilder().setZoneValue(activeTemperaturePoint)
                                    .setTemperatureInCelsius(tempToSet.toInvertibleUnit(SIUnits.CELSIUS).intValue())
                                    .build())
                            .build();
                    CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                            .setRequestId(UUID.randomUUID().toString()).setTemperatureConfigure(tc).build();
                    ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                    accountHandler.get().sendCommand(cm);
                }
            } else if ("active".equals(channelUID.getIdWithoutGroup())) {
                String supported = thing.getProperties().get("commandZevPreconditioningStart");
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Air Conditioning not supported {}", supported);
                } else {
                    if (OnOffType.ON.equals(command)) {
                        ZEVPreconditioningStart precondStart = ZEVPreconditioningStart.newBuilder()
                                .setType(ZEVPreconditioningType.NOW).build();
                        CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                .setRequestId(UUID.randomUUID().toString()).setZevPreconditioningStart(precondStart)
                                .build();
                        ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                        accountHandler.get().sendCommand(cm);
                    } else {
                        ZEVPreconditioningStop precondStop = ZEVPreconditioningStop.newBuilder()
                                .setType(ZEVPreconditioningType.NOW).build();
                        CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                .setRequestId(UUID.randomUUID().toString()).setZevPreconditioningStop(precondStop)
                                .build();
                        ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                        accountHandler.get().sendCommand(cm);
                    }
                }
            } else if ("front-left".equals(channelUID.getIdWithoutGroup())) {
                hvacGroupValueStorage.put("front-left", (State) command);
                configureSeats();
            } else if ("front-right".equals(channelUID.getIdWithoutGroup())) {
                hvacGroupValueStorage.put("front-right", (State) command);
                configureSeats();
            } else if ("rear-left".equals(channelUID.getIdWithoutGroup())) {
                hvacGroupValueStorage.put("rear-left", (State) command);
                configureSeats();
            } else if ("rear-right".equals(channelUID.getIdWithoutGroup())) {
                hvacGroupValueStorage.put("rear-right", (State) command);
                configureSeats();
            } else if ("aux-heat".equals(channelUID.getIdWithoutGroup())) {
                String supported = thing.getProperties().get("featureAuxHeat");
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Auxiliray Heating not supported {}", supported);
                } else {
                    if (OnOffType.ON.equals(command)) {
                        AuxheatStart auxHeatStart = AuxheatStart.newBuilder().build();
                        CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                .setRequestId(UUID.randomUUID().toString()).setAuxheatStart(auxHeatStart).build();
                        ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                        accountHandler.get().sendCommand(cm);
                    } else {
                        AuxheatStop auxHeatStop = AuxheatStop.newBuilder().build();
                        CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                .setRequestId(UUID.randomUUID().toString()).setAuxheatStop(auxHeatStop).build();
                        ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                        accountHandler.get().sendCommand(cm);
                    }
                }
            } else if ("zone".equals(channelUID.getIdWithoutGroup())) {
                int zone = ((DecimalType) command).intValue();
                ChannelStateMap zoneMap = new ChannelStateMap("zone", GROUP_HVAC, (DecimalType) command);
                updateChannel(zoneMap);
                QuantityType<Temperature> selectedTemp = temperaturePointsStorage.get(zone);
                if (selectedTemp != null) {
                    ChannelStateMap tempCSM = new ChannelStateMap("temperature", GROUP_HVAC, selectedTemp);
                    updateChannel(tempCSM);
                }
            }
        } else if (Constants.GROUP_POSITION.equals(channelUID.getGroupId())) {
            /**
             * Commands for Positioning
             */
            if ("signal".equals(channelUID.getIdWithoutGroup())) {
                String supported = thing.getProperties().get("commandSigposStart");
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Signal Position not supported {}", supported);
                } else {
                    SigPosStart sps;
                    CommandRequest cr;
                    ClientMessage cm;
                    switch (((DecimalType) command).intValue()) {
                        case 0: // light
                            sps = SigPosStart.newBuilder().setSigposType(SigposType.LIGHT_ONLY)
                                    .setLightType(LightType.DIPPED_HEAD_LIGHT).setSigposDuration(10).build();
                            cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                    .setRequestId(UUID.randomUUID().toString()).setSigposStart(sps).build();
                            cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                            accountHandler.get().sendCommand(cm);
                            break;
                        case 1: // horn
                            sps = SigPosStart.newBuilder().setSigposType(SigposType.HORN_ONLY).setHornRepeat(3)
                                    .setHornType(HornType.HORN_LOW_VOLUME).build();
                            cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                    .setRequestId(UUID.randomUUID().toString()).setSigposStart(sps).build();
                            cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                            accountHandler.get().sendCommand(cm);
                            break;
                        default:
                            logger.info("No Positioning known for {}", command);
                    }
                }
            }
        } else if (Constants.GROUP_CHARGE.equals(channelUID.getGroupId())) {
            /**
             * Commands for Charging
             */
            synchronized (chargeGroupValueStorage) {
                int maxSocToSelect = 80;
                boolean autoUnlockToSelect = false;
                String supported = thing.getProperties().get("commandChargeProgramConfigure");
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Charge Program Cosnfigure not supported");
                } else {
                    boolean sendCommand = false;
                    if ("program".equals(channelUID.getIdWithoutGroup())) {
                        selectedChargeProgram = ((DecimalType) command).intValue();
                        if (chargeGroupValueStorage.has(Integer.toString(selectedChargeProgram))) {
                            maxSocToSelect = chargeGroupValueStorage
                                    .getJSONObject(Integer.toString(selectedChargeProgram))
                                    .getInt(Constants.MAX_SOC_KEY);
                            autoUnlockToSelect = chargeGroupValueStorage
                                    .getJSONObject(Integer.toString(selectedChargeProgram))
                                    .getBoolean(Constants.AUTOUNLOCK_KEY);
                            // updateChannel(new ChannelStateMap("max-soc", GROUP_CHARGE,
                            // QuantityType.valueOf(maxSocToSelect, Units.PERCENT)));
                            // updateChannel(new ChannelStateMap("auto-unlock", GROUP_CHARGE,
                            // OnOffType.from(autoUnlockToSelect)));
                            sendCommand = true;
                        } else {
                            logger.info("No charge program found for {}", selectedChargeProgram);
                        }
                    }
                    if ("auto-unlock".equals(channelUID.getIdWithoutGroup())) {
                        autoUnlockToSelect = ((OnOffType) command).equals(OnOffType.ON);
                        sendCommand = true;
                    } else if ("max-soc".equals(channelUID.getIdWithoutGroup())) {
                        maxSocToSelect = ((QuantityType) command).intValue();
                        sendCommand = true;
                    } else {
                        // nothing to be send
                    }
                    if (sendCommand) {
                        Int32Value maxSocValue = Int32Value.newBuilder().setValue(maxSocToSelect).build();
                        BoolValue autoUnlockValue = BoolValue.newBuilder().setValue(autoUnlockToSelect).build();
                        ChargeProgramConfigure cpc = ChargeProgramConfigure.newBuilder()
                                .setChargeProgramValue(selectedChargeProgram).setMaxSoc(maxSocValue)
                                .setAutoUnlock(autoUnlockValue).build();
                        CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                .setRequestId(UUID.randomUUID().toString()).setChargeProgramConfigure(cpc).build();
                        ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                        accountHandler.get().sendCommand(cm);
                    }
                }
            }
        } else if (Constants.GROUP_DOORS.equals(channelUID.getGroupId())) {
            /**
             * Commands for Windows
             */
            if ("sunroof".equals(channelUID.getIdWithoutGroup())) {
                String supported = thing.getProperties().get("commandSunroofOpen");
                String pin = accountHandler.get().config.get().pin;
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Sunroof control not supported {}", supported);
                } else {
                    CommandRequest cr;
                    ClientMessage cm;
                    switch (((DecimalType) command).intValue()) {
                        case 0:
                            SunroofClose sc = SunroofClose.newBuilder().build();
                            cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                    .setRequestId(UUID.randomUUID().toString()).setSunroofClose(sc).build();
                            cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                            accountHandler.get().sendCommand(cm);
                            break;
                        case 1:
                            if (Constants.NOT_SET.equals(pin)) {
                                logger.info("Security PIN missing {}", pin);
                            } else {
                                SunroofOpen so = SunroofOpen.newBuilder().setPin(pin).build();
                                cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                        .setRequestId(UUID.randomUUID().toString()).setSunroofOpen(so).build();
                                cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                                accountHandler.get().sendCommand(cm);
                            }
                            break;
                        case 2:
                            if (Constants.NOT_SET.equals(pin)) {
                                logger.info("Security PIN? missing", pin);
                            } else {
                                SunroofLift sl = SunroofLift.newBuilder().setPin(pin).build();
                                cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                        .setRequestId(UUID.randomUUID().toString()).setSunroofLift(sl).build();
                                cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                                accountHandler.get().sendCommand(cm);
                            }
                            break;
                        default:
                            logger.info("No Sunroof movement known for {}", command);
                    }
                }
            }
        }
    }

    private void configureSeats() {
        String supported = thing.getProperties().get("commandZevPreconditionConfigureSeats");
        if (Boolean.FALSE.toString().equals(supported)) {
            logger.info("Seat Conditioning not supported");
        } else {
            boolean frontLeft = Utils.boolFromState(hvacGroupValueStorage.get("front-left"));
            boolean frontRight = Utils.boolFromState(hvacGroupValueStorage.get("front-right"));
            boolean rearLeft = Utils.boolFromState(hvacGroupValueStorage.get("rear-left"));
            boolean rearRight = Utils.boolFromState(hvacGroupValueStorage.get("rear-right"));
            ZEVPreconditioningConfigureSeats seats = ZEVPreconditioningConfigureSeats.newBuilder()
                    .setFrontLeft(frontLeft).setFrontRight(frontRight).setRearLeft(rearLeft).setRearRight(rearRight)
                    .build();
            CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                    .setRequestId(UUID.randomUUID().toString()).setZevPreconditionConfigureSeats(seats).build();
            ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
            accountHandler.get().sendCommand(cm);
        }
    }

    @SuppressWarnings("rawtypes")
    public void distributeCommandStatus(AppTwinCommandStatusUpdatesByPID cmdUpadtes) {
        Map<Long, AppTwinCommandStatus> updates = cmdUpadtes.getUpdatesByPidMap();
        updates.forEach((key, value) -> {
            // Command name
            StringType command = StringType.valueOf(value.getType().toString());
            ChannelStateMap csmCommand = new ChannelStateMap("cmd-name", GROUP_COMMAND, command);
            updateChannel(csmCommand);
            // Command State
            StringType state = StringType.valueOf(value.getState().toString());
            ChannelStateMap csmState = new ChannelStateMap("cmd-state", GROUP_COMMAND, state);
            updateChannel(csmState);
            // Command Time
            DateTimeType dtt = Utils.getDateTimeType(value.getTimestampInMs());
            UOMObserver observer = null;
            if (Locale.US.getCountry().equals(Utils.getCountry())) {
                observer = new UOMObserver(UOMObserver.TIME_US);
            } else {
                observer = new UOMObserver(UOMObserver.TIME_ROW);
            }
            ChannelStateMap csmUpdated = new ChannelStateMap("cmd-last-update", GROUP_COMMAND, dtt, observer);
            updateChannel(csmUpdated);
        });
    }

    public void distributeContent(VEPUpdate data) {
        updateStatus(ThingStatus.ONLINE);
        boolean fullUpdate = data.getFullUpdate();
        /**
         * Deliver proto update
         */
        try {
            // logger.info("Received new proto data vep {}", data);
            String newProto = Utils.proto2Json(data, thing.getThingTypeUID());
            String combinedProto = newProto;
            ChannelUID protoUpdateChannelUID = new ChannelUID(thing.getUID(), GROUP_VEHICLE, "proto-update");
            ChannelStateMap oldProtoMap = eventStorage.get(protoUpdateChannelUID.getId());
            if (oldProtoMap != null) {
                try {
                    String oldProto = ((StringType) oldProtoMap.getState()).toFullString();
                    Map combinedMap = Utils.combineMaps(new JSONObject(oldProto).toMap(),
                            new JSONObject(newProto).toMap());
                    combinedProto = (new JSONObject(combinedMap)).toString();
                } catch (Throwable t) {
                    logger.trace("Exception when decoding old Proto: {}", t.getMessage());
                    StackTraceElement[] ste = t.getStackTrace();
                    for (int i = 0; i < ste.length; i++) {
                        logger.trace("{}", ste[i].toString());
                    }
                }
            }
            // proto updates causing large printouts in openhab.log
            // update channel in case of user connected this channel with an item
            ChannelStateMap dataUpdateMap = new ChannelStateMap("proto-update", GROUP_VEHICLE,
                    StringType.valueOf(combinedProto));
            updateChannel(dataUpdateMap);
        } catch (Throwable t) {
            logger.trace("Exception when combining Protos: {}", t.getMessage());
            StackTraceElement[] ste = t.getStackTrace();
            for (int i = 0; i < ste.length; i++) {
                logger.trace("{}", ste[i].toString());
            }
        }

        Map<String, VehicleAttributeStatus> atts = data.getAttributesMap();
        /**
         * handle GPS
         */
        if (atts.containsKey("positionLat") && atts.containsKey("positionLong")) {
            double lat = Utils.getDouble(atts.get("positionLat"));
            double lon = Utils.getDouble(atts.get("positionLong"));
            if (lat > 0 && lon > 0) {
                PointType pt = new PointType(lat + "," + lon);
                updateChannel(new ChannelStateMap("gps", Constants.GROUP_POSITION, pt));
            } else {
                if (fullUpdate) {
                    logger.info("Either Latitude {} or Longitude {} attribute nil", lat, lon);
                    updateChannel(new ChannelStateMap("gps", Constants.GROUP_POSITION, UnDefType.UNDEF));
                }
            }
        }

        /**
         * handle temperature point
         */
        if (atts.containsKey("temperaturePoints")) {
            VehicleAttributeStatus hvacTemperaturePointAttribute = atts.get("temperaturePoints");
            if (hvacTemperaturePointAttribute.hasTemperaturePointsValue()) {
                TemperaturePointsValue tpValue = hvacTemperaturePointAttribute.getTemperaturePointsValue();
                // List<VehicleEvents.TemperaturePoint> tpList = new ArrayList<VehicleEvents.TemperaturePoint>();
                if (tpValue.getTemperaturePointsCount() > 0) {
                    List<VehicleEvents.TemperaturePoint> tPointList = tpValue.getTemperaturePointsList();
                    List<CommandOption> commandOptions = new ArrayList<CommandOption>();
                    List<StateOption> stateOptions = new ArrayList<StateOption>();
                    tPointList.forEach(point -> {
                        String zoneName = point.getZone();
                        int zoneNumber = Utils.getZoneNumber(zoneName);
                        Unit<Temperature> temperatureUnit = Mapper.defaultTemperatureUnit;
                        UOMObserver observer = null;
                        if (hvacTemperaturePointAttribute.hasTemperatureUnit()) {
                            observer = new UOMObserver(hvacTemperaturePointAttribute.getTemperatureUnit().toString());
                            if (observer.getUnit().isEmpty()) {
                                logger.warn("No Unit found for temperature - take default ");
                            } else {
                                temperatureUnit = observer.getUnit().get();
                            }
                        }
                        System.out.println("Default " + temperatureUnit);
                        ChannelUID cuid = new ChannelUID(thing.getUID(), GROUP_HVAC, "temperature");
                        mmcop.setCommandOptions(cuid, Utils.getTemperatureOptions(temperatureUnit));
                        if (zoneNumber > 0) {
                            if (activeTemperaturePoint == -1) {
                                activeTemperaturePoint = zoneNumber;
                            }
                            double temperature = point.getTemperature();
                            if (point.getTemperatureDisplayValue() != null) {
                                if (point.getTemperatureDisplayValue().strip().length() > 0) {
                                    try {
                                        temperature = Double.valueOf(point.getTemperatureDisplayValue());
                                    } catch (NumberFormatException nfe) {
                                        logger.info("Cannot transform Temperature Display Value {} into Double",
                                                point.getTemperatureDisplayValue());
                                    }
                                }
                            }
                            QuantityType<Temperature> temperatureState = QuantityType.valueOf(temperature,
                                    temperatureUnit);
                            temperaturePointsStorage.put(zoneNumber, temperatureState);
                            if (activeTemperaturePoint == zoneNumber) {
                                ChannelStateMap zoneCSM = new ChannelStateMap("zone", Constants.GROUP_HVAC,
                                        new DecimalType(activeTemperaturePoint));
                                updateChannel(zoneCSM);
                                ChannelStateMap tempCSM = new ChannelStateMap(CHANNEL_TEMPERATURE, Constants.GROUP_HVAC,
                                        temperatureState, observer);
                                updateChannel(tempCSM);
                            }
                        } else {
                            logger.info("No Integer mapping found for Temperature Zone {}", zoneName);
                        }
                        commandOptions.add(new CommandOption(Integer.toString(zoneNumber), zoneName));
                        stateOptions.add(new StateOption(Integer.toString(zoneNumber), zoneName));
                    });
                    ChannelUID cuid = new ChannelUID(thing.getUID(), GROUP_HVAC, "zone");
                    mmcop.setCommandOptions(cuid, commandOptions);
                    mmsop.setStateOptions(cuid, stateOptions);
                } else {
                    logger.trace("No TemperaturePoints found - list empty");
                }
            } else {
                logger.trace("No TemperaturePointsValue found");
            }
        } else {
            if (fullUpdate) {
                logger.trace("No TemperaturePoint Key found");
                ChannelStateMap zoneMap = new ChannelStateMap("zone", Constants.GROUP_HVAC, UnDefType.UNDEF);
                updateChannel(zoneMap);
                QuantityType<Temperature> tempState = QuantityType.valueOf(-1, Mapper.defaultTemperatureUnit);
                ChannelStateMap tempMap = new ChannelStateMap(CHANNEL_TEMPERATURE, Constants.GROUP_HVAC, tempState);
                updateChannel(tempMap);
            }
        }

        /**
         * handle Charge Program
         */
        if (Constants.BEV.equals(thing.getThingTypeUID().getId())
                || Constants.HYBRID.equals(thing.getThingTypeUID().getId())) {
            if (atts.containsKey("chargePrograms")) {
                ChargeProgramsValue cpv = atts.get("chargePrograms").getChargeProgramsValue();
                if (cpv.getChargeProgramParametersCount() > 0) {
                    List<ChargeProgramParameters> chareProgeamParameters = cpv.getChargeProgramParametersList();
                    List<CommandOption> commandOptions = new ArrayList<CommandOption>();
                    List<StateOption> stateOptions = new ArrayList<StateOption>();
                    synchronized (chargeGroupValueStorage) {
                        chargeGroupValueStorage.clear();
                        chareProgeamParameters.forEach(program -> {
                            String programName = program.getChargeProgram().name();
                            int number = Utils.getChargeProgramNumber(programName);
                            if (number >= 0) {
                                JSONObject programValuesJson = new JSONObject();
                                programValuesJson.put(Constants.MAX_SOC_KEY, program.getMaxSoc());
                                programValuesJson.put(Constants.AUTOUNLOCK_KEY, program.getAutoUnlock());
                                chargeGroupValueStorage.put(Integer.toString(number), programValuesJson);
                                commandOptions.add(new CommandOption(Integer.toString(number), programName));
                                stateOptions.add(new StateOption(Integer.toString(number), programName));

                            }
                        });
                    }
                    ChannelUID cuid = new ChannelUID(thing.getUID(), GROUP_CHARGE, "program");
                    mmcop.setCommandOptions(cuid, commandOptions);
                    mmsop.setStateOptions(cuid, stateOptions);

                    if (atts.containsKey("selectedChargeProgram")) {
                        selectedChargeProgram = (int) atts.get("selectedChargeProgram").getIntValue();
                        ChargeProgramParameters cpp = cpv.getChargeProgramParameters(selectedChargeProgram);
                        ChannelStateMap programMap = new ChannelStateMap("program", GROUP_CHARGE,
                                DecimalType.valueOf(Integer.toString(selectedChargeProgram)));
                        updateChannel(programMap);
                        ChannelStateMap maxSocMap = new ChannelStateMap("max-soc", GROUP_CHARGE,
                                QuantityType.valueOf((double) cpp.getMaxSoc(), Units.PERCENT));
                        updateChannel(maxSocMap);
                        ChannelStateMap autoUnlockMap = new ChannelStateMap("auto-unlock", GROUP_CHARGE,
                                OnOffType.from(cpp.getAutoUnlock()));
                        updateChannel(autoUnlockMap);
                    }
                } else {
                    logger.trace("No Charge Program property available for {}", thing.getThingTypeUID());
                }
            } else {
                if (fullUpdate) {
                    logger.trace("No Charge Programs found");
                }
            }
        }
        /**
         * handle "simple" values
         */
        atts.forEach((key, value) -> {
            // logger.trace("Distribute {}", key);
            ChannelStateMap csm = Mapper.getChannelStateMap(key, value);
            if (csm.isValid()) {

                /**
                 * Store some values and UOM Observer
                 */
                if (GROUP_HVAC.equals(csm.getGroup())) {
                    hvacGroupValueStorage.put(csm.getChannel(), csm.getState());
                }

        // Mileage for all cars
        String odoUrl = String.format(ODO_URL, config.get().vin);
        if (accountConfigAvailable()) {
        } else {
            logger.trace("{} Account not properly configured", this.getThing().getLabel());
        }

        // Electric status for hybrid and electric
        if (uid.equals(BEV) || uid.equals(HYBRID)) {
            String evUrl = String.format(EV_URL, config.get().vin);
            if (accountConfigAvailable()) {
            } else {
                logger.trace("{} Account not properly configured", this.getThing().getLabel());
            }
        }

        // Fuel for hybrid and combustion
        if (uid.equals(COMBUSTION) || uid.equals(HYBRID)) {
            String fuelUrl = String.format(FUEL_URL, config.get().vin);
            if (accountConfigAvailable()) {
            } else {
                logger.trace("{} Account not properly configured", this.getThing().getLabel());
            }
        }

        // Status and Lock for all
        String statusUrl = String.format(STATUS_URL, config.get().vin);
        if (accountConfigAvailable()) {
        } else {
            logger.trace("{} Account not properly configured", this.getThing().getLabel());
        }
        String lockUrl = String.format(LOCK_URL, config.get().vin);
        if (accountConfigAvailable()) {
        } else {
            logger.trace("{} Account not properly configured", this.getThing().getLabel());
        }

        // Range radius for all types
        updateRadius();
    }

    private boolean accountConfigAvailable() {
        if (accountHandler.isPresent()) {
            if (accountHandler.get().config.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private void getImageResources() {
        if (accountHandler.get().getImageApiKey().equals(NOT_SET)) {
            logger.debug("Image API key not set");
            return;
        }
        // add config parameters
        MultiMap<String> parameterMap = new MultiMap<>();
        parameterMap.add("background", Boolean.toString(config.get().background));
        parameterMap.add("night", Boolean.toString(config.get().night));
        parameterMap.add("cropped", Boolean.toString(config.get().cropped));
        parameterMap.add("roofOpen", Boolean.toString(config.get().roofOpen));
        parameterMap.add("fileFormat", config.get().format);
        String params = UrlEncoded.encode(parameterMap, StandardCharsets.UTF_8, false);
        String url = String.format(IMAGE_EXTERIOR_RESOURCE_URL, config.get().vin) + "?" + params;
        logger.debug("Get Image resources {} {} ", accountHandler.get().getImageApiKey(), url);
        Request req = httpClient.newRequest(url).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        req.header("x-api-key", accountHandler.get().getImageApiKey());
        req.header(HttpHeader.ACCEPT, "application/json");
        try {
            ContentResponse cr = req.send();
            if (cr.getStatus() == 200) {
                imageStorage.get().put(EXT_IMG_RES + config.get().vin, cr.getContentAsString());
                setImageOtions();
            } else {
                logger.debug("Failed to get image resources {} {}", cr.getStatus(), cr.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Error getting image resources {}", e.getMessage());
        }
    }

    private void setImageOtions() {
        List<String> entries = new ArrayList<>();
        if (imageStorage.get().containsKey(EXT_IMG_RES + config.get().vin)) {
            String resources = imageStorage.get().get(EXT_IMG_RES + config.get().vin);
            JSONObject jo = new JSONObject(resources);
            jo.keySet().forEach(entry -> {
                entries.add(entry);
            });
        }
        Collections.sort(entries);
        List<CommandOption> commandOptions = new ArrayList<>();
        List<StateOption> stateOptions = new ArrayList<>();
        entries.forEach(entry -> {
            CommandOption co = new CommandOption(entry, null);
            commandOptions.add(co);
            StateOption so = new StateOption(entry, null);
            stateOptions.add(so);
        });
        if (commandOptions.isEmpty()) {
            commandOptions.add(new CommandOption("Initilaze", null));
            stateOptions.add(new StateOption("Initilaze", null));
        }
        ChannelUID cuid = new ChannelUID(thing.getUID(), GROUP_IMAGE, "image-view");
        mmcop.setCommandOptions(cuid, commandOptions);
        mmsop.setStateOptions(cuid, stateOptions);
    }

    private String getImage(String key) {
        if (accountHandler.get().getImageApiKey().equals(NOT_SET)) {
            logger.debug("Image API key not set");
            return EMPTY;
        }
        String imageId = EMPTY;
        if (imageStorage.get().containsKey(EXT_IMG_RES + config.get().vin)) {
            String resources = imageStorage.get().get(EXT_IMG_RES + config.get().vin);
            JSONObject jo = new JSONObject(resources);
            if (jo.has(key)) {
                imageId = jo.getString(key);
            }
        } else {
            getImageResources();
            return EMPTY;
        }

        String url = IMAGE_BASE_URL + "/images/" + imageId;
        Request req = httpClient.newRequest(url).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        req.header("x-api-key", accountHandler.get().getImageApiKey());
        req.header(HttpHeader.ACCEPT, "*/*");
        ContentResponse cr;
        try {
            cr = req.send();
            byte[] response = cr.getContent();
            return Base64.getEncoder().encodeToString(response);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Get Image {} error {}", url, e.getMessage());
        }
        return EMPTY;
    }

    private void call(String url) {
        String requestUrl = String.format(url, config.get().vin);
        // Calculate endpoint for debugging
        String[] endpoint = requestUrl.split("/");
        String finalEndpoint = endpoint[endpoint.length - 1];
        // debug prefix contains Thing label and call endpoint for propper debugging
        String debugPrefix = this.getThing().getLabel() + Constants.COLON + finalEndpoint;

        Request req = httpClient.newRequest(requestUrl).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        req.header(HttpHeader.AUTHORIZATION, "Bearer " + accountHandler.get().getToken());
        try {
            ContentResponse cr = req.send();
            logger.trace("{} Response {} {}", debugPrefix, cr.getStatus(), cr.getContentAsString());
            if (cr.getStatus() == 200) {
                distributeContent(cr.getContentAsString().trim());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.info("{} Error getting data {}", debugPrefix, e.getMessage());
            fallbackCall(requestUrl);
        }
    }

    /**
     * Fallback solution with Java11 classes
     * Performs try with Java11 HttpClient - https://zetcode.com/java/getpostrequest/ to identify Community problem
     * https://community.openhab.org/t/mercedes-me-binding/136852/21
     *
     * @param requestUrl
     */
    private void fallbackCall(String requestUrl) {
        // Calculate endpoint for debugging
        String[] endpoint = requestUrl.split("/");
        String finalEndpoint = endpoint[endpoint.length - 1];
        // debug prefix contains Thing label and call endpoint for propper debugging
        String debugPrefix = this.getThing().getLabel() + Constants.COLON + finalEndpoint;

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(requestUrl))
                .header(HttpHeader.AUTHORIZATION.toString(), "Bearer " + "abc").GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.debug("{} Fallback Response {} {}", debugPrefix, response.statusCode(), response.body());
            if (response.statusCode() == 200) {
                distributeContent(response.body().trim());
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("{} Error getting data via fallback {}", debugPrefix, e.getMessage());
        }
    }

    private void distributeContent(String json) {
        if (json.startsWith("[") && json.endsWith("]")) {
            JSONArray ja = new JSONArray(json);
            for (Iterator<Object> iterator = ja.iterator(); iterator.hasNext();) {
                JSONObject jo = (JSONObject) iterator.next();
                ChannelStateMap csm = Mapper.getChannelStateMap(jo);
                if (csm.isValid()) {
                    updateChannel(csm);

                    /**
                     * handle some specific channels
                     */
                    // store ChannelMap for range radius calculation
                    String channel = csm.getChannel();
                    if ("range-electric".equals(channel)) {
                        rangeElectric = Optional.of((QuantityType<?>) csm.getState());
                    } else if ("range-fuel".equals(channel)) {
                        rangeFuel = Optional.of((QuantityType<?>) csm.getState());
                    } else if ("soc".equals(channel)) {
                        if (config.get().batteryCapacity > 0) {
                            float socValue = ((QuantityType<?>) csm.getState()).floatValue();
                            float batteryCapacity = config.get().batteryCapacity;
                            float chargedValue = Math.round(socValue * 1000 * batteryCapacity / 1000) / (float) 100;
                            ChannelStateMap charged = new ChannelStateMap("charged", GROUP_RANGE,
                                    QuantityType.valueOf(chargedValue, Units.KILOWATT_HOUR), csm.getTimestamp());
                            updateChannel(charged);
                            float unchargedValue = Math.round((100 - socValue) * 1000 * batteryCapacity / 1000)
                                    / (float) 100;
                            ChannelStateMap uncharged = new ChannelStateMap("uncharged", GROUP_RANGE,
                                    QuantityType.valueOf(unchargedValue, Units.KILOWATT_HOUR), csm.getTimestamp());
                            updateChannel(uncharged);
                        } else {
                            logger.debug("No battery capacity given");
                        }
                    } else if ("fuel-level".equals(channel)) {
                        if (config.get().fuelCapacity > 0) {
                            float fuelLevelValue = ((QuantityType<?>) csm.getState()).floatValue();
                            float fuelCapacity = config.get().fuelCapacity;
                            float litersInTank = Math.round(fuelLevelValue * 1000 * fuelCapacity / 1000) / (float) 100;
                            ChannelStateMap tankFilled = new ChannelStateMap("tank-remain", GROUP_RANGE,
                                    QuantityType.valueOf(litersInTank, Units.LITRE), csm.getTimestamp());
                            updateChannel(tankFilled);
                            float litersFree = Math.round((100 - fuelLevelValue) * 1000 * fuelCapacity / 1000)
                                    / (float) 100;
                            ChannelStateMap tankOpen = new ChannelStateMap("tank-open", GROUP_RANGE,
                                    QuantityType.valueOf(litersFree, Units.LITRE), csm.getTimestamp());
                            updateChannel(tankOpen);
                        } else {
                            logger.debug("No fuel capacity given");
                        }
                    }
                }
            }
        });

        /**
         * Check if Websocket shall be kept alive
         */
        accountHandler.get().keepAlive(ignitionState == 4 || chargingState);
    }

    /**
     * Easy function but there's some measures behind:
     * Guessing the range of the Vehicle on Map. If you can drive x kilometers with your Vehicle it's not feasible to
     * project this x km Radius on Map. The roads to be taken are causing some overhead because they are not a straight
     * line from Location A to B.
     * I've taken some measurements to calculate the overhead factor based on Google Maps
     * Berlin - Dresden: Road Distance: 193 air-line Distance 167 = Factor 87%
     * Kassel - Frankfurt: Road Distance: 199 air-line Distance 143 = Factor 72%
     * After measuring more distances you'll find out that the outcome is between 70% and 90%. So
     *
     * This depends also on the roads of a concrete route but this is only a guess without any Route Navigation behind
     *
     * @param s
     * @return mapping from air-line distance to "real road" distance
     */
    public static State guessRangeRadius(QuantityType<Length> s) {
        double radius = s.intValue() * 0.8;
        return QuantityType.valueOf(Math.round(radius), s.getUnit());
    }

    protected void updateChannel(ChannelStateMap csm) {
        String channel = csm.getChannel();
        ChannelUID cuid = new ChannelUID(thing.getUID(), csm.getGroup(), channel);
        eventStorage.put(cuid.getId(), csm);

        /**
         * proto updates causing large printouts in openhab.log
         * only log in case of channel is connected to an item
         */
        if ("proto-update".equals(csm.getChannel())) {
            ChannelUID protoUpdateChannelUID = new ChannelUID(thing.getUID(), GROUP_VEHICLE, "proto-update");
            if (!isLinked(protoUpdateChannelUID)) {
                eventStorage.put(protoUpdateChannelUID.getId(), csm);
                return;
            }
        }

        /**
         * Check correct channel patterns
         */
        if (csm.hasUomObersever()) {
            UOMObserver deliveredObserver = csm.getUomObersever();
            UOMObserver storedObserver = unitStorage.get(channel);
            boolean change = true;
            if (storedObserver != null) {
                change = !storedObserver.equals(deliveredObserver);
            }
            // Channel adaptions for items with configurable units
            String pattern = deliveredObserver.getPattern(csm.getGroup());
            if (pattern.startsWith("%") && change) {
                logger.trace("Set Pattern for {}#{} to {}", channel, csm.getGroup(), pattern);
                mmdsdp.setStatePattern(cuid, pattern);
            } else {
                handleComplexTripPattern(channel, pattern);
            }
            unitStorage.put(channel, deliveredObserver);
        }

        /**
         * Check if Websocket shall be kept alive during charging or driving
         */
        if (!UnDefType.UNDEF.equals(csm.getState())) {
            if (GROUP_VEHICLE.equals(csm.getGroup()) && "ignition".equals(csm.getChannel())) {
                ignitionState = ((DecimalType) csm.getState()).intValue();
            } else if (GROUP_CHARGE.equals(csm.getGroup()) && "active".equals(csm.getChannel())) {
                chargingState = ((OnOffType) csm.getState()).equals(OnOffType.ON);
            }
        }

        if ("zone".equals(channel) && !UnDefType.UNDEF.equals(csm.getState())) {
            activeTemperaturePoint = ((DecimalType) csm.getState()).intValue();
        }

        updateState(cuid, csm.getState());
    }

    private void handleComplexTripPattern(String channel, String pattern) {
        // logger.info("Handle pattern {} for {}", pattern, channel);
        switch (channel) {
            case "cons-ev":
            case "cons-ev-reset":
                StringType consumptionUnitEv = StringType.valueOf(pattern);
                ChannelStateMap csmEv = new ChannelStateMap("cons-ev-unit", GROUP_TRIP, consumptionUnitEv);
                updateChannel(csmEv);
                break;
            case "cons-conv":
            case "cons-conv-reset":
                StringType consumptionUnitFuel = StringType.valueOf(pattern);
                ChannelStateMap csmFuel = new ChannelStateMap("cons-conv-unit", GROUP_TRIP, consumptionUnitFuel);
                updateChannel(csmFuel);
                break;
            default:
                // logger.info("No handling found: pattern {} for {}", pattern, channel);
        }
    }

    @Override
    public void updateStatus(ThingStatus ts, ThingStatusDetail tsd, @Nullable String details) {
        super.updateStatus(ts, tsd, details);
    }

    @Override
    public void updateStatus(ThingStatus ts) {
        if (ThingStatus.ONLINE.equals(ts) && !ThingStatus.ONLINE.equals(thing.getStatus())) {
            logger.info("Request capabilities");
            if (accountHandler.isPresent()) {
                accountHandler.get().getVehicleCapabilities(config.get().vin);
            }
        }
        super.updateStatus(ts);
    }

    public void setFeatureCapabilities(@Nullable String capa) {
        logger.trace("Received Capabilities Features {}", capa);
        if (capa != null) {
            ChannelStateMap csm = new ChannelStateMap("feature-capabilities", GROUP_VEHICLE, StringType.valueOf(capa));
            updateChannel(csm);
        }
    }

    public void setCommandCapabilities(@Nullable String capa) {
        logger.trace("Received Capabilities Commands{}", capa);
        if (capa != null) {
            ChannelStateMap csm = new ChannelStateMap("command-capabilities", GROUP_VEHICLE, StringType.valueOf(capa));
            updateChannel(csm);
        }
    }

    /**
     * Vehicle Actions
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(VehicleActions.class);
    }

    public void sendPoi(JSONObject poi) {
        accountHandler.get().sendPoi(config.get().vin, poi);
    }
}
