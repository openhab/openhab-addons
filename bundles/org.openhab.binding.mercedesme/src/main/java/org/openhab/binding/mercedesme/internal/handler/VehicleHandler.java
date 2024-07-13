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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.openhab.binding.mercedesme.internal.MercedesMeStateOptionProvider;
import org.openhab.binding.mercedesme.internal.actions.VehicleActions;
import org.openhab.binding.mercedesme.internal.config.VehicleConfiguration;
import org.openhab.binding.mercedesme.internal.utils.ChannelStateMap;
import org.openhab.binding.mercedesme.internal.utils.Mapper;
import org.openhab.binding.mercedesme.internal.utils.UOMObserver;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
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

import com.daimler.mbcarkit.proto.Acp.ACP.CommandType;
import com.daimler.mbcarkit.proto.Acp.VehicleAPI.CommandState;
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
 * {@link VehicleHandler} transform data into state updates and handling of vehicle commands
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - Bugfix https://github.com/openhab/openhab-addons/issues/16932
 */
@NonNullByDefault
public class VehicleHandler extends BaseThingHandler {
    private static final List<String> HVAC_SEAT_LIST = Arrays
            .asList(new String[] { GROUP_HVAC + "#" + OH_CHANNEL_FRONT_LEFT, GROUP_HVAC + "#" + OH_CHANNEL_FRONT_RIGHT,
                    GROUP_HVAC + "#" + OH_CHANNEL_REAR_LEFT, GROUP_HVAC + "#" + OH_CHANNEL_REAR_RIGHT });

    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);
    private final LocationProvider locationProvider;
    private final MercedesMeCommandOptionProvider mmcop;
    private final MercedesMeStateOptionProvider mmsop;

    private Map<String, UOMObserver> unitStorage = new HashMap<>();
    private int ignitionState = -1;
    private boolean chargingState = false;
    private int selectedChargeProgram = -1;
    private int activeTemperaturePoint = -1;
    private Map<Integer, QuantityType<Temperature>> temperaturePointsStorage = new HashMap<>();
    private JSONObject chargeGroupValueStorage = new JSONObject();
    private Map<String, State> hvacGroupValueStorage = new HashMap<>();
    private String vehicleType = NOT_SET;

    Map<String, ChannelStateMap> eventStorage = new HashMap<>();
    Optional<AccountHandler> accountHandler = Optional.empty();
    Optional<VehicleConfiguration> config = Optional.empty();

    public VehicleHandler(Thing thing, LocationProvider lp, MercedesMeCommandOptionProvider cop,
            MercedesMeStateOptionProvider sop) {
        super(thing);
        vehicleType = thing.getThingTypeUID().getId();
        locationProvider = lp;
        mmcop = cop;
        mmsop = sop;
    }

    @Override
    public void initialize() {
        config = Optional.of(getConfigAs(VehicleConfiguration.class));
        Bridge bridge = getBridge();
        if (bridge != null) {
            updateStatus(ThingStatus.UNKNOWN);
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                setCommandStateOptions();
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

    private boolean supports(final String propertyName) {
        String supported = thing.getProperties().get(propertyName);
        return Boolean.TRUE.toString().equals(supported);
    }

    private ClientMessage createCM(CommandRequest cr) {
        return ClientMessage.newBuilder().setCommandRequest(cr).build();
    }

    @Override
    public void dispose() {
        accountHandler.get().unregisterVin(config.get().vin);
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /**
         * Commands shall be not that frequent so trace level for identifying problems should be feasible
         */
        logger.trace("Received command {} {} for {}", command.getClass(), command, channelUID);

        if (command instanceof RefreshType) {
            if (MB_KEY_FEATURE_CAPABILITIES.equals(channelUID.getIdWithoutGroup())
                    || MB_KEY_COMMAND_CAPABILITIES.equals(channelUID.getIdWithoutGroup())) {
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
            return;
        }
        var crBuilder = CommandRequest.newBuilder().setVin(config.get().vin).setRequestId(UUID.randomUUID().toString());
        String group = channelUID.getGroupId();
        String channel = channelUID.getIdWithoutGroup();
        String pin = accountHandler.get().config.get().pin;
        if (group == null) {
            logger.trace("No command {} found for {}", command, channel);
            return;
        }
        switch (group) {
            case GROUP_VEHICLE:
                switch (channel) {
                    case OH_CHANNEL_IGNITION:
                        if (!supports(MB_KEY_COMMAND_ENGINE_START)) {
                            logger.trace("Engine Start/Stop not supported");
                            return;
                        }
                        int commandValue = ((DecimalType) command).intValue();
                        if (commandValue == 4) {
                            if (Constants.NOT_SET.equals(pin)) {
                                logger.trace("Security PIN missing in Account bridge");
                                return;
                            }
                            EngineStart eStart = EngineStart.newBuilder().setPin(pin).build();
                            CommandRequest cr = crBuilder.setEngineStart(eStart).build();
                            accountHandler.get().sendCommand(createCM(cr));
                        } else if (commandValue == 0) {
                            EngineStop eStop = EngineStop.newBuilder().build();
                            CommandRequest cr = crBuilder.setEngineStop(eStop).build();
                            accountHandler.get().sendCommand(createCM(cr));
                        }
                        break;
                    case OH_CHANNEL_WINDOWS:
                        if (!supports(MB_KEY_COMMAND_WINDOWS_OPEN)) {
                            logger.trace("Windows control not supported");
                            return;
                        }
                        CommandRequest cr;
                        switch (((DecimalType) command).intValue()) {
                            case 0:
                                if (Constants.NOT_SET.equals(pin)) {
                                    logger.trace("Security PIN missing in Account bridge");
                                    return;
                                }
                                WindowsVentilate wv = WindowsVentilate.newBuilder().setPin(pin).build();
                                cr = crBuilder.setWindowsVentilate(wv).build();
                                accountHandler.get().sendCommand(createCM(cr));
                                break;
                            case 1:
                                WindowsClose wc = WindowsClose.newBuilder().build();
                                cr = crBuilder.setWindowsClose(wc).build();
                                accountHandler.get().sendCommand(createCM(cr));
                                break;
                            case 2:
                                if (Constants.NOT_SET.equals(pin)) {
                                    logger.trace("Security PIN missing in Account bridge");
                                    return;
                                }
                                WindowsOpen wo = WindowsOpen.newBuilder().setPin(pin).build();
                                cr = crBuilder.setWindowsOpen(wo).build();
                                accountHandler.get().sendCommand(createCM(cr));
                                break;
                            default:
                                logger.trace("No Windows movement known for {}", command);
                                break;
                        }
                        break;
                    case OH_CHANNEL_LOCK:
                        if (!supports(MB_KEY_COMMAND_DOORS_LOCK)) {
                            logger.trace("Door Lock not supported");
                            return;
                        }
                        switch (((DecimalType) command).intValue()) {
                            case 0:
                                DoorsLock dl = DoorsLock.newBuilder().build();
                                cr = crBuilder.setDoorsLock(dl).build();
                                accountHandler.get().sendCommand(createCM(cr));
                                break;
                            case 1:
                                if (Constants.NOT_SET.equals(pin)) {
                                    logger.trace("Security PIN missing in Account bridge");
                                    return;
                                }
                                DoorsUnlock du = DoorsUnlock.newBuilder().setPin(pin).build();
                                cr = crBuilder.setDoorsUnlock(du).build();
                                accountHandler.get().sendCommand(createCM(cr));
                                break;
                            default:
                                logger.trace("No lock command mapped to {}", command);
                                break;
                        }
                        break;
                    default:
                        logger.trace("No command {} found for {}#{}", command, group, channel);
                        break;
                }
                break; // vehicle group
            case GROUP_HVAC:
                switch (channel) {
                    case OH_CHANNEL_TEMPERATURE:
                        if (!supports(MB_KEY_COMMAND_ZEV_PRECONDITION_CONFIGURE)) {
                            logger.trace("Air Conditioning Temperature Setting not supported");
                            return;
                        }
                        if (command instanceof QuantityType<?> quantityTypeCommand) {
                            QuantityType<?> targetTempCelsius = quantityTypeCommand.toInvertibleUnit(SIUnits.CELSIUS);
                            if (targetTempCelsius == null) {
                                logger.trace("Cannot handle temperature command {}", quantityTypeCommand);
                                return;
                            }
                            TemperatureConfigure tc = TemperatureConfigure.newBuilder()
                                    .addTemperaturePoints(
                                            TemperaturePoint.newBuilder().setZoneValue(activeTemperaturePoint)
                                                    .setTemperatureInCelsius(targetTempCelsius.intValue()).build())
                                    .build();
                            CommandRequest cr = crBuilder.setTemperatureConfigure(tc).build();
                            accountHandler.get().sendCommand(createCM(cr));
                        } else {
                            logger.trace("Temperature {} shall be QuantityType with degree Celsius or Fahrenheit",
                                    command);
                        }
                        break;
                    case OH_CHANNEL_ACTIVE:
                        if (!supports(MB_KEY_COMMAND_ZEV_PRECONDITIONING_START)) {
                            logger.trace("Air Conditioning not supported");
                            return;
                        }
                        if (OnOffType.ON.equals(command)) {
                            ZEVPreconditioningStart precondStart = ZEVPreconditioningStart.newBuilder()
                                    .setType(ZEVPreconditioningType.NOW).build();
                            CommandRequest cr = crBuilder.setZevPreconditioningStart(precondStart).build();
                            accountHandler.get().sendCommand(createCM(cr));
                        } else {
                            ZEVPreconditioningStop precondStop = ZEVPreconditioningStop.newBuilder()
                                    .setType(ZEVPreconditioningType.NOW).build();
                            CommandRequest cr = crBuilder.setZevPreconditioningStop(precondStop).build();
                            accountHandler.get().sendCommand(createCM(cr));
                        }
                        break;
                    case OH_CHANNEL_FRONT_LEFT:
                    case OH_CHANNEL_FRONT_RIGHT:
                    case OH_CHANNEL_REAR_LEFT:
                    case OH_CHANNEL_REAR_RIGHT:
                        configureSeats(channelUID, (State) command);
                        break;
                    case OH_CHANNEL_AUX_HEAT:
                        if (!supports(MB_KEY_FEATURE_AUX_HEAT)) {
                            logger.trace("Auxiliary Heating not supported");
                            return;
                        }
                        if (OnOffType.ON.equals(command)) {
                            AuxheatStart auxHeatStart = AuxheatStart.newBuilder().build();
                            CommandRequest cr = crBuilder.setAuxheatStart(auxHeatStart).build();
                            accountHandler.get().sendCommand(createCM(cr));
                        } else {
                            AuxheatStop auxHeatStop = AuxheatStop.newBuilder().build();
                            CommandRequest cr = crBuilder.setAuxheatStop(auxHeatStop).build();
                            accountHandler.get().sendCommand(createCM(cr));
                        }
                        break;
                    case OH_CHANNEL_ZONE:
                        int zone = ((DecimalType) command).intValue();
                        if (!temperaturePointsStorage.containsKey(zone)) {
                            logger.trace("No Temperature Zone found for {}", command);
                            return;
                        }
                        ChannelStateMap zoneMap = new ChannelStateMap(OH_CHANNEL_ZONE, GROUP_HVAC,
                                (DecimalType) command);
                        updateChannel(zoneMap);
                        QuantityType<Temperature> selectedTemp = temperaturePointsStorage.get(zone);
                        if (selectedTemp != null) {
                            ChannelStateMap tempCSM = new ChannelStateMap(OH_CHANNEL_TEMPERATURE, GROUP_HVAC,
                                    selectedTemp);
                            updateChannel(tempCSM);
                        }
                    default:
                        logger.trace("No command {} found for {}#{}", command, group, channel);
                        break;
                }
                break; // hvac group
            case GROUP_POSITION:
                switch (channel) {
                    case OH_CHANNEL_SIGNAL:
                        if (!supports(MB_KEY_COMMAND_SIGPOS_START)) {
                            logger.trace("Signal Position not supported");
                            return;
                        }
                        SigPosStart sps;
                        CommandRequest cr;
                        switch (((DecimalType) command).intValue()) {
                            case 0: // light
                                sps = SigPosStart.newBuilder().setSigposType(SigposType.LIGHT_ONLY)
                                        .setLightType(LightType.DIPPED_HEAD_LIGHT).setSigposDuration(10).build();
                                cr = crBuilder.setSigposStart(sps).build();
                                accountHandler.get().sendCommand(createCM(cr));
                                break;
                            case 1: // horn
                                sps = SigPosStart.newBuilder().setSigposType(SigposType.HORN_ONLY).setHornRepeat(3)
                                        .setHornType(HornType.HORN_LOW_VOLUME).build();
                                cr = crBuilder.setSigposStart(sps).build();
                                accountHandler.get().sendCommand(createCM(cr));
                                break;
                            default:
                                logger.trace("No Positioning known for {}", command);
                        }
                        break;
                    default:
                        logger.trace("No command {} found for {}#{}", command, group, channel);
                        break;
                }
                break; // position group
            case GROUP_CHARGE:
                if (!supports(MB_KEY_COMMAND_CHARGE_PROGRAM_CONFIGURE)) {
                    logger.trace("Charge Program Configure not supported");
                    return;
                }
                int maxSocToSelect = 80;
                boolean autoUnlockToSelect = false;
                boolean sendCommand = false;

                synchronized (chargeGroupValueStorage) {
                    switch (channel) {
                        case OH_CHANNEL_PROGRAM:
                            selectedChargeProgram = ((DecimalType) command).intValue();
                            if (chargeGroupValueStorage.has(Integer.toString(selectedChargeProgram))) {
                                maxSocToSelect = chargeGroupValueStorage
                                        .getJSONObject(Integer.toString(selectedChargeProgram))
                                        .getInt(Constants.MAX_SOC_KEY);
                                autoUnlockToSelect = chargeGroupValueStorage
                                        .getJSONObject(Integer.toString(selectedChargeProgram))
                                        .getBoolean(Constants.AUTO_UNLOCK_KEY);
                                updateChannel(new ChannelStateMap(OH_CHANNEL_MAX_SOC, GROUP_CHARGE,
                                        QuantityType.valueOf(maxSocToSelect, Units.PERCENT)));
                                updateChannel(new ChannelStateMap(OH_CHANNEL_AUTO_UNLOCK, GROUP_CHARGE,
                                        OnOffType.from(autoUnlockToSelect)));
                                sendCommand = true;
                            } else {
                                logger.trace("No charge program found for {}", selectedChargeProgram);
                            }
                            break;
                        case OH_CHANNEL_AUTO_UNLOCK:
                            autoUnlockToSelect = OnOffType.ON.equals(command);
                            sendCommand = true;
                            break;
                        case OH_CHANNEL_MAX_SOC:
                            maxSocToSelect = ((QuantityType<?>) command).intValue();
                            sendCommand = true;
                            break;
                        default:
                            logger.trace("No command {} found for {}#{}", command, group, channel);
                            break;
                    }
                    if (sendCommand) {
                        Int32Value maxSocValue = Int32Value.newBuilder().setValue(maxSocToSelect).build();
                        BoolValue autoUnlockValue = BoolValue.newBuilder().setValue(autoUnlockToSelect).build();
                        ChargeProgramConfigure cpc = ChargeProgramConfigure.newBuilder()
                                .setChargeProgramValue(selectedChargeProgram).setMaxSoc(maxSocValue)
                                .setAutoUnlock(autoUnlockValue).build();
                        CommandRequest cr = crBuilder.setChargeProgramConfigure(cpc).build();
                        accountHandler.get().sendCommand(createCM(cr));
                    }
                }
                break; // charge group
            case GROUP_DOORS:
                switch (channel) {
                    case OH_CHANNEL_SUNROOF:
                        if (!supports(MB_KEY_COMMAND_SUNROOF_OPEN)) {
                            logger.trace("Sunroof control not supported");
                            return;
                        }
                        CommandRequest cr;
                        switch (((DecimalType) command).intValue()) {
                            case 0:
                                SunroofClose sc = SunroofClose.newBuilder().build();
                                cr = crBuilder.setSunroofClose(sc).build();
                                accountHandler.get().sendCommand(createCM(cr));
                                break;
                            case 1:
                                if (Constants.NOT_SET.equals(pin)) {
                                    logger.trace("Security PIN missing in Account bridge");
                                    return;
                                }
                                SunroofOpen so = SunroofOpen.newBuilder().setPin(pin).build();
                                cr = crBuilder.setSunroofOpen(so).build();
                                accountHandler.get().sendCommand(createCM(cr));
                                break;
                            case 2:
                                if (Constants.NOT_SET.equals(pin)) {
                                    logger.trace("Security PIN missing in Account bridge");
                                }
                                SunroofLift sl = SunroofLift.newBuilder().setPin(pin).build();
                                cr = crBuilder.setSunroofLift(sl).build();
                                accountHandler.get().sendCommand(createCM(cr));
                                break;
                            default:
                                logger.trace("No Sunroof movement known for {}", command);
                        }
                    default:
                        logger.trace("No command {} found for {}#{}", command, group, channel);
                        break;
                }
                break; // doors group
            default: // no group matching
                logger.trace("No command {} found for {}#{}", command, group, channel);
                break;
        }
    }

    private void configureSeats(ChannelUID channelUID, State command) {
        String supported = thing.getProperties().get(MB_KEY_COMMAND_ZEV_PRECONDITION_CONFIGURE_SEATS);
        if (Boolean.FALSE.toString().equals(supported)) {
            logger.trace("Seat Conditioning not supported");
        } else {
            com.daimler.mbcarkit.proto.VehicleCommands.ZEVPreconditioningConfigureSeats.Builder builder = ZEVPreconditioningConfigureSeats
                    .newBuilder();

            HVAC_SEAT_LIST.forEach(seat -> {
                ChannelStateMap csm = eventStorage.get(seat);
                if (csm != null) {
                    if (csm.getState() != UnDefType.UNDEF && !seat.equals(channelUID.getId())) {
                        OnOffType oot = (OnOffType) csm.getState();
                        switch (seat) {
                            case GROUP_HVAC + "#" + OH_CHANNEL_FRONT_LEFT:
                                builder.setFrontLeft(OnOffType.ON.equals(oot));
                                break;
                            case GROUP_HVAC + "#" + OH_CHANNEL_FRONT_RIGHT:
                                builder.setFrontRight(OnOffType.ON.equals(oot));
                                break;
                            case GROUP_HVAC + "#" + OH_CHANNEL_REAR_LEFT:
                                builder.setRearLeft(OnOffType.ON.equals(oot));
                                break;
                            case GROUP_HVAC + "#" + OH_CHANNEL_REAR_RIGHT:
                                builder.setRearRight(OnOffType.ON.equals(oot));
                                break;
                        }
                    }
                }
            });
            ZEVPreconditioningConfigureSeats seats = builder.build();
            CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                    .setRequestId(UUID.randomUUID().toString()).setZevPreconditionConfigureSeats(seats).build();
            ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
            accountHandler.get().sendCommand(cm);
        }
    }

    public void distributeCommandStatus(AppTwinCommandStatusUpdatesByPID cmdUpdates) {
        Map<Long, AppTwinCommandStatus> updates = cmdUpdates.getUpdatesByPidMap();
        updates.forEach((key, value) -> {
            try {
                // getting type and state may throw Exception
                int commandType = value.getType().getNumber();
                int commandState = value.getState().getNumber();
                // Command name
                ChannelStateMap csmCommand = new ChannelStateMap(OH_CHANNEL_CMD_NAME, GROUP_COMMAND,
                        new DecimalType(commandType));
                updateChannel(csmCommand);
                // Command State
                ChannelStateMap csmState = new ChannelStateMap(OH_CHANNEL_CMD_STATE, GROUP_COMMAND,
                        new DecimalType(commandState));
                updateChannel(csmState);
                // Command Time
                DateTimeType dtt = Utils.getDateTimeType(value.getTimestampInMs());
                UOMObserver observer = null;
                if (Locale.US.getCountry().equals(Utils.getCountry())) {
                    observer = new UOMObserver(UOMObserver.TIME_US);
                } else {
                    observer = new UOMObserver(UOMObserver.TIME_ROW);
                }
                ChannelStateMap csmUpdated = new ChannelStateMap(OH_CHANNEL_CMD_LAST_UPDATE, GROUP_COMMAND, dtt,
                        observer);
                updateChannel(csmUpdated);
            } catch (IllegalArgumentException iae) {
                logger.trace("Cannot decode command {} {}", value.getAllFields().toString(), iae.getMessage());
                // silent ignore update
            }
        });
    }

    public void distributeContent(VEPUpdate data) {
        updateStatus(ThingStatus.ONLINE);
        boolean fullUpdate = data.getFullUpdate();
        /**
         * Deliver proto update
         */
        String newProto = Utils.proto2Json(data, thing.getThingTypeUID());
        String combinedProto = newProto;
        ChannelUID protoUpdateChannelUID = new ChannelUID(thing.getUID(), GROUP_VEHICLE, OH_CHANNEL_PROTO_UPDATE);
        ChannelStateMap oldProtoMap = eventStorage.get(protoUpdateChannelUID.getId());
        if (oldProtoMap != null) {
            String oldProto = ((StringType) oldProtoMap.getState()).toFullString();
            Map<?, ?> combinedMap = Utils.combineMaps(new JSONObject(oldProto).toMap(),
                    new JSONObject(newProto).toMap());
            combinedProto = (new JSONObject(combinedMap)).toString();
        }
        // proto updates causing large printouts in openhab.log
        // update channel in case of user connected this channel with an item
        ChannelStateMap dataUpdateMap = new ChannelStateMap(OH_CHANNEL_PROTO_UPDATE, GROUP_VEHICLE,
                StringType.valueOf(combinedProto));
        updateChannel(dataUpdateMap);

        Map<String, VehicleAttributeStatus> atts = data.getAttributesMap();
        /**
         * handle "simple" values
         */
        atts.forEach((key, value) -> {
            ChannelStateMap csm = Mapper.getChannelStateMap(key, value);
            if (csm.isValid()) {
                /**
                 * Store some values and UOM Observer
                 */
                if (GROUP_HVAC.equals(csm.getGroup())) {
                    hvacGroupValueStorage.put(csm.getChannel(), csm.getState());
                }

                /**
                 * handle some specific channels
                 */
                String channel = csm.getChannel();
                // handle range channels very specific regarding to vehicle type
                boolean block = false;
                switch (channel) {
                    case OH_CHANNEL_RANGE_ELECTRIC:
                        if (!Constants.COMBUSTION.equals(vehicleType)) {
                            ChannelStateMap radiusElectric = new ChannelStateMap(OH_CHANNEL_RADIUS_ELECTRIC,
                                    GROUP_RANGE, guessRangeRadius(csm.getState()), csm.getUomObserver());
                            updateChannel(radiusElectric);
                        } else {
                            block = true;
                        }
                        break;
                    case OH_CHANNEL_RANGE_FUEL:
                        if (!Constants.BEV.equals(vehicleType)) {
                            ChannelStateMap radiusFuel = new ChannelStateMap(OH_CHANNEL_RADIUS_FUEL, GROUP_RANGE,
                                    guessRangeRadius(csm.getState()), csm.getUomObserver());
                            updateChannel(radiusFuel);
                        } else {
                            block = true;
                        }
                        break;
                    case OH_CHANNEL_RANGE_HYBRID:
                        if (Constants.HYBRID.equals(vehicleType)) {
                            ChannelStateMap radiusHybrid = new ChannelStateMap(OH_CHANNEL_RADIUS_HYBRID, GROUP_RANGE,
                                    guessRangeRadius(csm.getState()), csm.getUomObserver());
                            updateChannel(radiusHybrid);
                        } else {
                            block = true;
                        }
                        break;
                    case OH_CHANNEL_SOC:
                        if (!Constants.COMBUSTION.equals(vehicleType)) {
                            if (config.get().batteryCapacity > 0) {
                                float socValue = ((QuantityType<?>) csm.getState()).floatValue();
                                float batteryCapacity = config.get().batteryCapacity;
                                float chargedValue = Math.round(socValue * 1000 * batteryCapacity / 1000) / (float) 100;
                                ChannelStateMap charged = new ChannelStateMap(OH_CHANNEL_CHARGED, GROUP_RANGE,
                                        QuantityType.valueOf(chargedValue, Units.KILOWATT_HOUR));
                                updateChannel(charged);
                                float unchargedValue = Math.round((100 - socValue) * 1000 * batteryCapacity / 1000)
                                        / (float) 100;
                                ChannelStateMap uncharged = new ChannelStateMap(OH_CHANNEL_UNCHARGED, GROUP_RANGE,
                                        QuantityType.valueOf(unchargedValue, Units.KILOWATT_HOUR));
                                updateChannel(uncharged);
                            } else {
                                ChannelStateMap charged = new ChannelStateMap(OH_CHANNEL_CHARGED, GROUP_RANGE,
                                        QuantityType.valueOf(0, Units.KILOWATT_HOUR));
                                updateChannel(charged);
                                ChannelStateMap uncharged = new ChannelStateMap(OH_CHANNEL_UNCHARGED, GROUP_RANGE,
                                        QuantityType.valueOf(0, Units.KILOWATT_HOUR));
                                updateChannel(uncharged);
                            }
                        } else {
                            block = true;
                        }
                        break;
                    case OH_CHANNEL_FUEL_LEVEL:
                        if (!Constants.BEV.equals(vehicleType)) {
                            if (config.get().fuelCapacity > 0) {
                                float fuelLevelValue = ((QuantityType<?>) csm.getState()).floatValue();
                                float fuelCapacity = config.get().fuelCapacity;
                                float litersInTank = Math.round(fuelLevelValue * 1000 * fuelCapacity / 1000)
                                        / (float) 100;
                                ChannelStateMap tankFilled = new ChannelStateMap(OH_CHANNEL_TANK_REMAIN, GROUP_RANGE,
                                        QuantityType.valueOf(litersInTank, Mapper.defaultVolumeUnit));
                                updateChannel(tankFilled);
                                float litersFree = Math.round((100 - fuelLevelValue) * 1000 * fuelCapacity / 1000)
                                        / (float) 100;
                                ChannelStateMap tankOpen = new ChannelStateMap(OH_CHANNEL_TANK_OPEN, GROUP_RANGE,
                                        QuantityType.valueOf(litersFree, Mapper.defaultVolumeUnit));
                                updateChannel(tankOpen);
                            } else {
                                ChannelStateMap tankFilled = new ChannelStateMap(OH_CHANNEL_TANK_REMAIN, GROUP_RANGE,
                                        QuantityType.valueOf(0, Mapper.defaultVolumeUnit));
                                updateChannel(tankFilled);
                                ChannelStateMap tankOpen = new ChannelStateMap(OH_CHANNEL_TANK_OPEN, GROUP_RANGE,
                                        QuantityType.valueOf(0, Mapper.defaultVolumeUnit));
                                updateChannel(tankOpen);
                            }
                        } else {
                            block = true;
                        }
                        break;
                    case OH_CHANNEL_COOLANT_FLUID:
                    case OH_CHANNEL_ENGINE:
                    case OH_CHANNEL_GAS_FLAP:
                        if (Constants.BEV.equals(vehicleType)) {
                            block = true;
                        }
                        break;
                }
                if (!block) {
                    updateChannel(csm);
                }
            }
        });
        /**
         * handle GPS
         */
        if (atts.containsKey(MB_KEY_POSITION_LAT) && atts.containsKey(MB_KEY_POSITION_LONG)) {
            double lat = Utils.getDouble(atts.get(MB_KEY_POSITION_LAT));
            double lon = Utils.getDouble(atts.get(MB_KEY_POSITION_LONG));
            if (lat != -1 && lon != -1) {
                PointType pt = new PointType(lat + "," + lon);
                updateChannel(new ChannelStateMap(OH_CHANNEL_GPS, Constants.GROUP_POSITION, pt));

                // calculate distance to home
                PointType homePoint = locationProvider.getLocation();
                Unit<Length> lengthUnit = KILOMETRE_UNIT;
                if (homePoint != null) {
                    double distance = Utils.distance(homePoint.getLatitude().doubleValue(), lat,
                            homePoint.getLongitude().doubleValue(), lon, 0.0, 0.0);
                    UOMObserver observer = new UOMObserver(UOMObserver.LENGTH_KM_UNIT);
                    if (Locale.US.getCountry().equals(Utils.getCountry())) {
                        observer = new UOMObserver(UOMObserver.LENGTH_MILES_UNIT);
                        lengthUnit = ImperialUnits.MILE;
                    }
                    updateChannel(new ChannelStateMap(OH_CHANNEL_HOME_DISTANCE, Constants.GROUP_RANGE,
                            QuantityType.valueOf(distance / 1000, lengthUnit), observer));
                } else {
                    logger.trace("No home location found");
                }

            } else {
                if (fullUpdate) {
                    logger.trace("Either Latitude {} or Longitude {} attribute nil", lat, lon);
                    updateChannel(new ChannelStateMap(OH_CHANNEL_GPS, Constants.GROUP_POSITION, UnDefType.UNDEF));
                }
            }
        }

        /**
         * handle temperature point
         */
        if (atts.containsKey(MB_KEY_TEMPERATURE_POINTS)) {
            VehicleAttributeStatus hvacTemperaturePointAttribute = atts.get(MB_KEY_TEMPERATURE_POINTS);
            if (hvacTemperaturePointAttribute != null) {
                if (hvacTemperaturePointAttribute.hasTemperaturePointsValue()) {
                    TemperaturePointsValue tpValue = hvacTemperaturePointAttribute.getTemperaturePointsValue();
                    if (tpValue.getTemperaturePointsCount() > 0) {
                        List<VehicleEvents.TemperaturePoint> tPointList = tpValue.getTemperaturePointsList();
                        List<CommandOption> commandOptions = new ArrayList<>();
                        List<StateOption> stateOptions = new ArrayList<>();
                        tPointList.forEach(point -> {
                            String zoneName = point.getZone();
                            int zoneNumber = Utils.getZoneNumber(zoneName);
                            Unit<Temperature> temperatureUnit = Mapper.defaultTemperatureUnit;
                            UOMObserver observer = null;
                            if (hvacTemperaturePointAttribute.hasTemperatureUnit()) {
                                observer = new UOMObserver(
                                        hvacTemperaturePointAttribute.getTemperatureUnit().toString());
                                Unit<?> observerUnit = observer.getUnit();
                                if (observerUnit != null) {
                                    temperatureUnit = observerUnit.asType(Temperature.class);
                                }
                            }
                            ChannelUID cuid = new ChannelUID(thing.getUID(), GROUP_HVAC, OH_CHANNEL_TEMPERATURE);
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
                                            logger.trace("Cannot transform Temperature Display Value {} into Double",
                                                    point.getTemperatureDisplayValue());
                                        }
                                    }
                                }
                                QuantityType<Temperature> temperatureState = QuantityType.valueOf(temperature,
                                        temperatureUnit);
                                temperaturePointsStorage.put(zoneNumber, temperatureState);
                                if (activeTemperaturePoint == zoneNumber) {
                                    ChannelStateMap zoneCSM = new ChannelStateMap(OH_CHANNEL_ZONE, Constants.GROUP_HVAC,
                                            new DecimalType(activeTemperaturePoint));
                                    updateChannel(zoneCSM);
                                    ChannelStateMap tempCSM = new ChannelStateMap(OH_CHANNEL_TEMPERATURE,
                                            Constants.GROUP_HVAC, temperatureState, observer);
                                    updateChannel(tempCSM);
                                }
                            } else {
                                logger.trace("No Integer mapping found for Temperature Zone {}", zoneName);
                            }
                            commandOptions.add(new CommandOption(Integer.toString(zoneNumber), zoneName));
                            stateOptions.add(new StateOption(Integer.toString(zoneNumber), zoneName));
                        });
                        ChannelUID cuid = new ChannelUID(thing.getUID(), GROUP_HVAC, OH_CHANNEL_ZONE);
                        mmcop.setCommandOptions(cuid, commandOptions);
                        mmsop.setStateOptions(cuid, stateOptions);
                    } else {
                        // don't set to undef - maybe partial update
                        logger.trace("No TemperaturePoints found - list empty");
                    }
                } else {
                    // don't set to undef - maybe partial update
                    logger.trace("No TemperaturePointsValue found");
                }
            } else {
                // don't set to undef - maybe partial update
                logger.trace("No TemperaturePoints found");
            }
        } else {
            // full update acknowledged - set to undef
            if (fullUpdate) {
                ChannelStateMap zoneMap = new ChannelStateMap(OH_CHANNEL_ZONE, Constants.GROUP_HVAC, UnDefType.UNDEF);
                updateChannel(zoneMap);
                QuantityType<Temperature> tempState = QuantityType.valueOf(-1, Mapper.defaultTemperatureUnit);
                ChannelStateMap tempMap = new ChannelStateMap(OH_CHANNEL_TEMPERATURE, Constants.GROUP_HVAC, tempState);
                updateChannel(tempMap);
            }
        }

        /**
         * handle Charge Program
         */
        if (Constants.BEV.equals(thing.getThingTypeUID().getId())
                || Constants.HYBRID.equals(thing.getThingTypeUID().getId())) {
            VehicleAttributeStatus vas = atts.get(MB_KEY_CHARGE_PROGRAMS);
            if (vas != null) {
                ChargeProgramsValue cpv = vas.getChargeProgramsValue();
                if (cpv.getChargeProgramParametersCount() > 0) {
                    List<ChargeProgramParameters> chargeProgramParameters = cpv.getChargeProgramParametersList();
                    List<CommandOption> commandOptions = new ArrayList<>();
                    List<StateOption> stateOptions = new ArrayList<>();
                    synchronized (chargeGroupValueStorage) {
                        chargeGroupValueStorage.clear();
                        chargeProgramParameters.forEach(program -> {
                            String programName = program.getChargeProgram().name();
                            int number = Utils.getChargeProgramNumber(programName);
                            if (number >= 0) {
                                JSONObject programValuesJson = new JSONObject();
                                programValuesJson.put(Constants.MAX_SOC_KEY, program.getMaxSoc());
                                programValuesJson.put(Constants.AUTO_UNLOCK_KEY, program.getAutoUnlock());
                                chargeGroupValueStorage.put(Integer.toString(number), programValuesJson);
                                commandOptions.add(new CommandOption(Integer.toString(number), programName));
                                stateOptions.add(new StateOption(Integer.toString(number), programName));

                            }
                        });
                    }
                    ChannelUID cuid = new ChannelUID(thing.getUID(), GROUP_CHARGE, OH_CHANNEL_PROGRAM);
                    mmcop.setCommandOptions(cuid, commandOptions);
                    mmsop.setStateOptions(cuid, stateOptions);
                    vas = atts.get(MB_KEY_SELECTED_CHARGE_PROGRAM);
                    if (vas != null) {
                        selectedChargeProgram = (int) vas.getIntValue();
                        ChargeProgramParameters cpp = cpv.getChargeProgramParameters(selectedChargeProgram);
                        ChannelStateMap programMap = new ChannelStateMap(OH_CHANNEL_PROGRAM, GROUP_CHARGE,
                                DecimalType.valueOf(Integer.toString(selectedChargeProgram)));
                        updateChannel(programMap);
                        ChannelStateMap maxSocMap = new ChannelStateMap(OH_CHANNEL_MAX_SOC, GROUP_CHARGE,
                                QuantityType.valueOf((double) cpp.getMaxSoc(), Units.PERCENT));
                        updateChannel(maxSocMap);
                        ChannelStateMap autoUnlockMap = new ChannelStateMap(OH_CHANNEL_AUTO_UNLOCK, GROUP_CHARGE,
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
         * handle day of charge end
         */
        ChannelStateMap chargeTimeEndCSM = eventStorage.get(GROUP_CHARGE + "#" + OH_CHANNEL_END_TIME);
        if (chargeTimeEndCSM != null) {
            State entTimeState = chargeTimeEndCSM.getState();
            if (entTimeState instanceof DateTimeType endDateTimeType) {
                // we've a valid charged end time
                VehicleAttributeStatus vas = atts.get(MB_KEY_ENDOFCHARGEDAY);
                if (vas != null && !Utils.isNil(vas)) {
                    // proto weekday starts with MONDAY=0, java ZonedDateTime starts with MONDAY=1
                    long estimatedWeekday = Utils.getInt(vas) + 1;
                    ZonedDateTime storedZdt = endDateTimeType.getZonedDateTime();
                    long storedWeekday = storedZdt.getDayOfWeek().getValue();
                    // check if estimated weekday is smaller than stored
                    // estimation Monday=1 vs. stored Saturday=6 => (7+1)-6=2 days ahead
                    if (estimatedWeekday < storedWeekday) {
                        estimatedWeekday += 7;
                    }
                    if (estimatedWeekday != storedWeekday) {
                        DateTimeType adjustedDtt = new DateTimeType(
                                storedZdt.plusDays(estimatedWeekday - storedWeekday));
                        ChannelStateMap adjustedCsm = new ChannelStateMap(OH_CHANNEL_END_TIME, GROUP_CHARGE,
                                adjustedDtt);
                        updateChannel(adjustedCsm);
                    }
                }
            }
        }

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
    public static State guessRangeRadius(State state) {
        if (state instanceof QuantityType<?> qt) {
            double radius = qt.intValue() * 0.8;
            return QuantityType.valueOf(Math.round(radius), qt.getUnit());
        }
        return QuantityType.valueOf(-1, Units.ONE);
    }

    protected void updateChannel(ChannelStateMap csm) {
        String channel = csm.getChannel();
        ChannelUID cuid = new ChannelUID(thing.getUID(), csm.getGroup(), channel);
        eventStorage.put(cuid.getId(), csm);

        /**
         * proto updates causing large printouts in openhab.log
         * only log in case of channel is connected to an item
         */
        if (OH_CHANNEL_PROTO_UPDATE.equals(csm.getChannel())) {
            ChannelUID protoUpdateChannelUID = new ChannelUID(thing.getUID(), GROUP_VEHICLE, OH_CHANNEL_PROTO_UPDATE);
            if (!isLinked(protoUpdateChannelUID)) {
                eventStorage.put(protoUpdateChannelUID.getId(), csm);
                return;
            }
        }

        /**
         * Check correct channel patterns
         */
        if (csm.hasUomObserver()) {
            UOMObserver deliveredObserver = csm.getUomObserver();
            UOMObserver storedObserver = unitStorage.get(channel);
            boolean change = true;
            if (storedObserver != null) {
                change = !storedObserver.equals(deliveredObserver);
            }
            // Channel adaptions for items with configurable units
            String pattern = deliveredObserver.getPattern(csm.getGroup(), csm.getChannel());
            if (pattern != null) {
                if (pattern.startsWith("%") && change) {
                    mmsop.setStatePattern(cuid, pattern);
                } else {
                    handleComplexTripPattern(channel, pattern);
                }
            }
            unitStorage.put(channel, deliveredObserver);
        }

        /**
         * Check if Websocket shall be kept alive during charging or driving
         */
        if (!UnDefType.UNDEF.equals(csm.getState())) {
            if (GROUP_VEHICLE.equals(csm.getGroup()) && OH_CHANNEL_IGNITION.equals(csm.getChannel())) {
                ignitionState = ((DecimalType) csm.getState()).intValue();
            } else if (GROUP_CHARGE.equals(csm.getGroup()) && OH_CHANNEL_ACTIVE.equals(csm.getChannel())) {
                chargingState = OnOffType.ON.equals((csm.getState()));
            }
        }

        if (OH_CHANNEL_ZONE.equals(channel) && !UnDefType.UNDEF.equals(csm.getState())) {
            activeTemperaturePoint = ((DecimalType) csm.getState()).intValue();
        }

        updateState(cuid, csm.getState());
    }

    private void handleComplexTripPattern(String channel, String pattern) {
        switch (channel) {
            case OH_CHANNEL_CONS_EV:
            case OH_CHANNEL_CONS_EV_RESET:
                StringType consumptionUnitEv = StringType.valueOf(pattern);
                ChannelStateMap csmEv = new ChannelStateMap(OH_CHANNEL_CONS_EV_UNIT, GROUP_TRIP, consumptionUnitEv);
                updateChannel(csmEv);
                break;
            case OH_CHANNEL_CONS_CONV:
            case OH_CHANNEL_CONS_CONV_RESET:
                StringType consumptionUnitFuel = StringType.valueOf(pattern);
                ChannelStateMap csmFuel = new ChannelStateMap(OH_CHANNEL_CONS_CONV_UNIT, GROUP_TRIP,
                        consumptionUnitFuel);
                updateChannel(csmFuel);
                break;
        }
    }

    @Override
    public void updateStatus(ThingStatus ts, ThingStatusDetail tsd, @Nullable String details) {
        super.updateStatus(ts, tsd, details);
    }

    @Override
    public void updateStatus(ThingStatus ts) {
        if (ThingStatus.ONLINE.equals(ts) && !ThingStatus.ONLINE.equals(thing.getStatus())) {
            if (accountHandler.isPresent()) {
                accountHandler.get().getVehicleCapabilities(config.get().vin);
            }
        }
        super.updateStatus(ts);
    }

    public void setFeatureCapabilities(@Nullable String capabilities) {
        if (capabilities != null) {
            ChannelStateMap csm = new ChannelStateMap(MB_KEY_FEATURE_CAPABILITIES, GROUP_VEHICLE,
                    StringType.valueOf(capabilities));
            updateChannel(csm);
        }
    }

    public void setCommandCapabilities(@Nullable String capabilities) {
        if (capabilities != null) {
            ChannelStateMap csm = new ChannelStateMap(MB_KEY_COMMAND_CAPABILITIES, GROUP_VEHICLE,
                    StringType.valueOf(capabilities));
            updateChannel(csm);
        }
    }

    private void setCommandStateOptions() {
        List<StateOption> commandTypeOptions = new ArrayList<>();
        CommandType[] ctValues = CommandType.values();
        for (int i = 0; i < ctValues.length; i++) {
            if (!UNRECOGNIZED.equals(ctValues[i].toString())) {
                StateOption co = new StateOption(Integer.toString(ctValues[i].getNumber()), ctValues[i].toString());
                commandTypeOptions.add(co);
            }
        }
        mmsop.setStateOptions(new ChannelUID(thing.getUID(), GROUP_COMMAND, OH_CHANNEL_CMD_NAME), commandTypeOptions);
        List<StateOption> commandStateOptions = new ArrayList<>();
        CommandState[] csValues = CommandState.values();
        for (int j = 0; j < csValues.length; j++) {
            if (!UNRECOGNIZED.equals(csValues[j].toString())) {
                StateOption so = new StateOption(Integer.toString(csValues[j].getNumber()), csValues[j].toString());
                commandStateOptions.add(so);
            }
        }
        mmsop.setStateOptions(new ChannelUID(thing.getUID(), GROUP_COMMAND, OH_CHANNEL_CMD_STATE), commandStateOptions);
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
