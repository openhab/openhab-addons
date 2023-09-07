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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.config.VehicleConfiguration;
import org.openhab.binding.mercedesme.internal.proto.Client.ClientMessage;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.BatteryMaxSocConfigure;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.CommandRequest;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.DoorsLock;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.DoorsUnlock;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.SigPosStart;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.SigPosStart.HornType;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.SigPosStart.LightType;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.SigPosStart.SigposType;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.SunroofClose;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.SunroofLift;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.SunroofOpen;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.TemperatureConfigure;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.TemperatureConfigure.TemperaturePoint;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.TemperatureConfigure.TemperaturePoint.Zone;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.WindowsClose;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.WindowsOpen;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.WindowsVentilate;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.ZEVPreconditioningStart;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.ZEVPreconditioningStop;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.ZEVPreconditioningType;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.ChargeProgramParameters;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.ChargeProgramsValue;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.TemperaturePointsValue;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.VEPUpdate;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.VehicleAttributeStatus;
import org.openhab.binding.mercedesme.internal.utils.ChannelStateMap;
import org.openhab.binding.mercedesme.internal.utils.Mapper;
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
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);
    private Optional<AccountHandler> accountHandler = Optional.empty();
    private Optional<QuantityType<?>> rangeElectric = Optional.empty();
    private Optional<VehicleConfiguration> config = Optional.empty();
    private Optional<QuantityType<?>> rangeFuel = Optional.empty();

    public VehicleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Received command {} for {}", command, channelUID);
        if (command instanceof RefreshType) {
            // todo

        } else if (Constants.GROUP_HVAC.equals(channelUID.getGroupId())) {
            /**
             * Commands for HVAC
             */
            String pin = accountHandler.get().config.get().pin;
            if ("active".equals(channelUID.getIdWithoutGroup())) {
                String supported = thing.getProperties().get("commandZevPreconditionConfigure");
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Air Conditioning Temperature Setting supported? {}", supported);
                } else {
                    logger.info("Received Air Condition Temperature change {}", command.getClass());
                    logger.info("Received DecimalType {}", ((QuantityType) command).doubleValue());
                    TemperatureConfigure tc = TemperatureConfigure.newBuilder()
                            .addTemperaturePoints(TemperaturePoint.newBuilder().setZone(Zone.FRONT_CENTER)
                                    .setTemperatureInCelsius(((QuantityType) command).doubleValue()).build())
                            .build();
                    CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                            .setRequestId(UUID.randomUUID().toString()).setTemperatureConfigure(tc).build();
                    ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                    accountHandler.get().sendCommand(cm);
                }
            } else if ("temperature".equals(channelUID.getIdWithoutGroup())) {
                String supported = thing.getProperties().get("commandZevPreconditioningStart");
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Air Conditioning supported? {}", supported);
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
            }
        } else if (Constants.GROUP_POSITION.equals(channelUID.getGroupId())) {
            /**
             * Commands for Positioning
             */
            if ("signal".equals(channelUID.getIdWithoutGroup())) {
                String supported = thing.getProperties().get("commandSigposStart");
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Signal Position supported? {}", supported);
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
            if ("max-soc".equals(channelUID.getIdWithoutGroup())) {
                String supported = thing.getProperties().get("commandBatteryMaxSocConfigure");
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Max SoC configuration supported? {}", supported);
                } else {
                    BatteryMaxSocConfigure batteryMax = BatteryMaxSocConfigure.newBuilder()
                            .setMaxSoc(((QuantityType) command).intValue()).build();
                    CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                            .setRequestId(UUID.randomUUID().toString()).setBatteryMaxSoc(batteryMax).build();
                    ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                    accountHandler.get().sendCommand(cm);
                }
            }
        } else if (Constants.GROUP_LOCK.equals(channelUID.getGroupId())) {
            /**
             * Commands for Locks
             */
            if ("door-lock".equals(channelUID.getIdWithoutGroup())) {
                String pin = accountHandler.get().config.get().pin;
                String supported = thing.getProperties().get("commandDoorsLock");
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Door Lock supported? {}", supported);
                } else {
                    if (OnOffType.ON.equals(command)) {
                        DoorsLock dl = DoorsLock.newBuilder().build();
                        CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                .setRequestId(UUID.randomUUID().toString()).setDoorsLock(dl).build();
                        ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                        accountHandler.get().sendCommand(cm);
                    } else {
                        if (Constants.NOT_SET.equals(pin)) {
                            logger.info("Security PIN? {}", pin);
                        } else {
                            DoorsUnlock du = DoorsUnlock.newBuilder().setPin(pin).build();
                            CommandRequest cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                    .setRequestId(UUID.randomUUID().toString()).setDoorsUnlock(du).build();
                            ClientMessage cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                            accountHandler.get().sendCommand(cm);
                        }
                    }
                }
            }
        } else if (Constants.GROUP_WINDOWS.equals(channelUID.getGroupId())) {
            /**
             * Commands for Windows
             */
            if ("control".equals(channelUID.getIdWithoutGroup())
                    && Constants.GROUP_WINDOWS.equals(channelUID.getGroupId())) {
                String supported = thing.getProperties().get("commandWindowsOpen");
                String pin = accountHandler.get().config.get().pin;
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Windows supported? {}", supported);
                } else {
                    CommandRequest cr;
                    ClientMessage cm;
                    switch (((DecimalType) command).intValue()) {
                        case 0:
                            WindowsClose wc = WindowsClose.newBuilder().build();
                            cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                    .setRequestId(UUID.randomUUID().toString()).setWindowsClose(wc).build();
                            cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                            accountHandler.get().sendCommand(cm);
                            break;
                        case 1:
                            if (Constants.NOT_SET.equals(pin)) {
                                logger.info("Security PIN? {}", pin);
                            } else {
                                WindowsOpen wo = WindowsOpen.newBuilder().setPin(pin).build();
                                cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                        .setRequestId(UUID.randomUUID().toString()).setWindowsOpen(wo).build();
                                cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                                accountHandler.get().sendCommand(cm);
                            }
                            break;
                        case 2:
                            if (Constants.NOT_SET.equals(pin)) {
                                logger.info("Security PIN? {}", pin);
                            } else {
                                WindowsVentilate wv = WindowsVentilate.newBuilder().setPin(pin).build();
                                cr = CommandRequest.newBuilder().setVin(config.get().vin)
                                        .setRequestId(UUID.randomUUID().toString()).setWindowsVentilate(wv).build();
                                cm = ClientMessage.newBuilder().setCommandRequest(cr).build();
                                accountHandler.get().sendCommand(cm);
                            }
                            break;
                        default:
                            logger.info("No Windows movement known for {}", command);
                    }
                }
            }
        } else if (Constants.GROUP_WINDOWS.equals(channelUID.getGroupId())) {
            /**
             * Commands for Windows
             */
            if ("sunroof-control".equals(channelUID.getIdWithoutGroup())) {
                String supported = thing.getProperties().get("commandSunroofOpen");
                String pin = accountHandler.get().config.get().pin;
                if (Boolean.FALSE.toString().equals(supported)) {
                    logger.info("Sunroof supported? {}", supported);
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
                                logger.info("Security PIN? {}", pin);
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
                                logger.info("Security PIN? {}", pin);
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
            } else {
                logger.info("Command {} with value {} not known", channelUID, command);
            }
        }
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
    }

    public void distributeContent(VEPUpdate data) {
        updateStatus(ThingStatus.ONLINE);
        Map<String, VehicleAttributeStatus> atts = data.getAttributesMap();

        /**
         * handle GPS
         */
        boolean latitude = atts.containsKey("positionLat");
        boolean longitude = atts.containsKey("positionLong");
        if (latitude && longitude) {
            boolean latitudeNil = Mapper.isNil(atts.get("positionLat"));
            boolean longitudeNil = Mapper.isNil(atts.get("positionLong"));
            if (!latitudeNil && !longitudeNil) {
                String gps = atts.get("positionLat").getDoubleValue() + "," + atts.get("positionLong").getDoubleValue();
                PointType pt = new PointType(gps);
                updateChannel(new ChannelStateMap("gps", Constants.GROUP_POSITION, pt));
            } else {
                logger.info("Either Latitude {} or Longitude {} attribute nil", latitudeNil, longitudeNil);
                updateChannel(new ChannelStateMap("gps", Constants.GROUP_POSITION, UnDefType.UNDEF));
            }
        } else {
            logger.info("Either Latitude {} or Longitude {} attribute missing", latitude, longitude);
            updateChannel(new ChannelStateMap("gps", Constants.GROUP_POSITION, UnDefType.UNDEF));
        }

        /**
         * handle temperature point
         */
        boolean hvacTemperaturePointAvailable = atts.containsKey("temperaturePoints");
        if (hvacTemperaturePointAvailable) {
            VehicleAttributeStatus hvacTemperaturePointAttribute = atts.get("temperaturePoints");
            if (hvacTemperaturePointAttribute.hasTemperaturePointsValue()) {
                TemperaturePointsValue tpValue = hvacTemperaturePointAttribute.getTemperaturePointsValue();
                VehicleEvents.TemperaturePoint tp = null;
                if (tpValue.getTemperaturePointsCount() == 1) {
                    tp = tpValue.getTemperaturePointsList().get(0);
                } else if (tpValue.getTemperaturePointsCount() > 1) {
                    tp = tpValue.getTemperaturePointsList().get(0);
                    logger.info("{} TemperaturePoints found - take first one");
                } else {
                    logger.info("No TemperaturePoint found");
                }
                if (tp != null) {
                    ChannelStateMap zoneMap = new ChannelStateMap("zone", Constants.GROUP_HVAC,
                            StringType.valueOf(tp.getZone()));
                    updateChannel(zoneMap);
                    QuantityType<Temperature> tempState = QuantityType.valueOf(tp.getTemperature(), SIUnits.CELSIUS);
                    ChannelStateMap tempMap = new ChannelStateMap("temperature", Constants.GROUP_HVAC, tempState);
                    updateChannel(zoneMap);
                }
            } else {
                logger.info("No TemperaturePoint Value found");
            }

        } else {
            logger.info("No TemperaturePoint Attribute found");
        }

        /**
         * handle Charge Program
         */
        if (Constants.BEV.equals(thing.getUID().getId()) || Constants.HYBRID.equals(thing.getUID().getId())) {
            boolean selectedProgram = atts.containsKey("selectedChargeProgram");
            boolean avaialablePrograms = atts.containsKey("chargePrograms");
            if (selectedProgram && avaialablePrograms) {
                int selected = (int) atts.get("selectedChargeProgram").getIntValue();
                ChargeProgramsValue cps = atts.get("chargePrograms").getChargeProgramsValue();
                ChargeProgramParameters cpp = cps.getChargeProgramParameters(selected);
                ChannelStateMap programMap = new ChannelStateMap("program", GROUP_CHARGE,
                        StringType.valueOf(cpp.getChargeProgram().name()));
                updateChannel(programMap);
                ChannelStateMap maxSocMap = new ChannelStateMap("max-soc", GROUP_CHARGE,
                        QuantityType.valueOf((double) cpp.getMaxSoc(), Units.PERCENT));
                updateChannel(maxSocMap);
            }
        }

        /**
         * handle "simple" values
         */
        atts.forEach((key, value) -> {
            ChannelStateMap csm = Mapper.getChannelStateMap(key, value);
            if (csm.isValid()) {
                updateChannel(csm);

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
            } else {
                // logger.trace("Unable to deliver state for {}", key);
            }
        });
        updateRadius();
    }

    private void updateRadius() {
        if (rangeElectric.isPresent()) {
            // update electric radius
            ChannelStateMap radiusElectric = new ChannelStateMap("radius-electric", GROUP_RANGE,
                    guessRangeRadius(rangeElectric.get()));
            updateChannel(radiusElectric);
            if (rangeFuel.isPresent()) {
                // update fuel & hybrid radius
                ChannelStateMap radiusFuel = new ChannelStateMap("radius-fuel", GROUP_RANGE,
                        guessRangeRadius(rangeFuel.get()));
                updateChannel(radiusFuel);
                int hybridKm = rangeElectric.get().intValue() + rangeFuel.get().intValue();
                QuantityType<Length> hybridRangeState = QuantityType.valueOf(hybridKm, KILOMETRE_UNIT);
                ChannelStateMap rangeHybrid = new ChannelStateMap("range-hybrid", GROUP_RANGE, hybridRangeState);
                updateChannel(rangeHybrid);
                ChannelStateMap radiusHybrid = new ChannelStateMap("radius-hybrid", GROUP_RANGE,
                        guessRangeRadius(hybridRangeState));
                updateChannel(radiusHybrid);
            }
        } else if (rangeFuel.isPresent()) {
            // update fuel & hybrid radius
            ChannelStateMap radiusFuel = new ChannelStateMap("radius-fuel", GROUP_RANGE,
                    guessRangeRadius(rangeFuel.get()));
            updateChannel(radiusFuel);
        }
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
    public static State guessRangeRadius(QuantityType<?> s) {
        double radius = s.intValue() * 0.8;
        return QuantityType.valueOf(Math.round(radius), KILOMETRE_UNIT);
    }

    protected void updateChannel(ChannelStateMap csm) {
        updateState(new ChannelUID(thing.getUID(), csm.getGroup(), csm.getChannel()), csm.getState());
    }

    @Override
    public void updateStatus(ThingStatus ts, ThingStatusDetail tsd, @Nullable String details) {
        super.updateStatus(ts, tsd, details);
    }
}
