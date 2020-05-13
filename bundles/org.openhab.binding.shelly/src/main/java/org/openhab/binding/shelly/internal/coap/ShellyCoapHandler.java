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
package org.openhab.binding.shelly.internal.coap;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.MessageObserverAdapter;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiJsonDTO.ShellySettingsRelay;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotDescrBlk;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotDescrSen;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotDevDescription;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotGenericSensorList;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotSensor;
import org.openhab.binding.shelly.internal.coap.ShellyCoapJSonDTO.CoIotSensorTypeAdapter;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyBaseHandler;
import org.openhab.binding.shelly.internal.handler.ShellyColorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import tec.uom.se.unit.Units;

/**
 * The {@link ShellyCoapHandler} handles the CoIoT/CoAP registration and events.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyCoapHandler implements ShellyCoapListener {
    private final Logger logger = LoggerFactory.getLogger(ShellyCoapHandler.class);

    private final ShellyBaseHandler thingHandler;
    private ShellyThingConfiguration config = new ShellyThingConfiguration();
    private final GsonBuilder gsonBuilder = new GsonBuilder();
    private final Gson gson;
    private String thingName;
    private boolean discovering = false;

    private final ShellyCoapServer coapServer;
    private @Nullable CoapClient statusClient;
    private Request reqDescription = new Request(Code.GET, Type.CON);
    private Request reqStatus = new Request(Code.GET, Type.CON);

    private int lastSerial = -1;
    private String lastPayload = "";
    private Map<String, CoIotDescrBlk> blockMap = new LinkedHashMap<>();
    private LinkedHashMap<String, CoIotDescrSen> sensorMap = new LinkedHashMap<>();

    private static final byte[] EMPTY_BYTE = new byte[0];

    public ShellyCoapHandler(ShellyBaseHandler thingHandler, ShellyCoapServer coapServer) {
        this.thingHandler = thingHandler;
        this.coapServer = coapServer;
        this.thingName = thingHandler.thingName;

        gsonBuilder.registerTypeAdapter(CoIotGenericSensorList.class, new CoIotSensorTypeAdapter());
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
    }

    /**
     * Initialize CoAP access, send discovery packet and start Status server
     *
     * @parm thingName Thing name derived from Thing Type/hostname
     * @parm config ShellyThingConfiguration
     * @thows ShellyApiException
     */
    public synchronized void start(String thingName, ShellyThingConfiguration config) throws ShellyApiException {
        if (isStarted()) {
            logger.trace("{}: CoAP Listener was already started", thingName);
            return;
        }
        try {
            this.thingName = thingName;
            this.config = config;

            reqDescription = sendRequest(reqDescription, config.deviceIp, COLOIT_URI_DEVDESC, Type.CON);

            if (!isStarted()) {
                logger.debug("{}: Starting CoAP Listener", thingName);
                reqDescription = sendRequest(reqDescription, config.deviceIp, COLOIT_URI_DEVDESC, Type.CON);

                coapServer.start(config.localIp, this);
                statusClient = new CoapClient(completeUrl(config.deviceIp, COLOIT_URI_DEVSTATUS))
                        .setTimeout((long) SHELLY_API_TIMEOUT_MS).useNONs().setEndpoint(coapServer.getEndpoint());
            }
        } catch (UnknownHostException e) {
            ShellyApiException ea = new ShellyApiException(e);
            logger.debug("{}: CoAP Exception", thingName, e);
            throw ea;
        }
    }

    public boolean isStarted() {
        return statusClient != null;
    }

    /**
     * Process an inbound Response (or mapped Request): decode CoAP options. handle discovery result or status updates
     *
     * @param response The Response packet
     */
    @Override
    public void processResponse(@Nullable Response response) {
        if (response == null) {
            return; // other device instance
        }
        String ip = response.getSourceContext().getPeerAddress().toString();
        if (!ip.contains(config.deviceIp)) {
            return;
        }

        String payload = "";
        String devId = "";
        String uri = "";
        // int validity = 0;
        int serial = 0;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("{}: CoIoT Message from {} (MID={}): {}", thingName,
                        response.getSourceContext().getPeerAddress(), response.getMID(), response.getPayloadString());
            }
            if (response.isCanceled() || response.isDuplicate() || response.isRejected()) {
                logger.debug("{} ({}): Packet was canceled, rejected or is a duplicate -> discard", thingName, devId);
                return;
            }

            if (response.getCode() == ResponseCode.CONTENT) {
                payload = response.getPayloadString();
                List<Option> options = response.getOptions().asSortedList();
                int i = 0;
                while (i < options.size()) {
                    Option opt = options.get(i);
                    switch (opt.getNumber()) {
                        case OptionNumberRegistry.URI_PATH:
                            uri = COLOIT_URI_BASE + opt.getStringValue();
                            break;
                        case COIOT_OPTION_GLOBAL_DEVID:
                            devId = opt.getStringValue();
                            break;
                        case COIOT_OPTION_STATUS_VALIDITY:
                            // validity = o.getIntegerValue();
                            break;
                        case COIOT_OPTION_STATUS_SERIAL:
                            serial = opt.getIntegerValue();
                            if (serial == lastSerial) {
                                // As per specification the serial changes when any sensor data has changed. The App
                                // should ignore any updates with the same serial. However, as we have seen with the
                                // Shelly HT and Shelly 4 Pro this is not always the case. The device comes up with an
                                // status packet having the same serial, but new payload information.
                                // Work Around: Packet will only be ignored when Serial AND Payload are the same as last
                                // time
                                if (!lastPayload.isEmpty() && !lastPayload.equals(payload)) {
                                    logger.debug(
                                            "{}: Duplicate serial {} will be processed, because payload is different: {} vs. {}",
                                            thingName, serial, payload, lastPayload);
                                    break;
                                }
                                logger.trace("{}: Serial {} was already processed, ignore update", thingName, serial);
                                return;
                            }
                            break;
                        default:
                            logger.debug("{} ({}): COAP option {} with value {} skipped", thingName, devId,
                                    opt.getNumber(), opt.getValue());
                    }
                    i++;
                }

                // If we received a CoAP message successful the thing must be online
                thingHandler.setThingOnline();

                if (uri.equalsIgnoreCase(COLOIT_URI_DEVDESC) || (uri.isEmpty() && payload.contains(COIOT_TAG_BLK))) {
                    handleDeviceDescription(devId, payload);
                } else if (uri.equalsIgnoreCase(COLOIT_URI_DEVSTATUS)
                        || (uri.isEmpty() && payload.contains(COIOT_TAG_GENERIC))) {
                    handleStatusUpdate(devId, payload, serial);
                }
            } else {
                // error handling
                logger.debug("{}: Unknown Response Code {} received, payload={}", thingName, response.getCode(),
                        response.getPayloadString());
            }

            if (!discovering) {
                // Observe Status Updates
                reqStatus = sendRequest(reqStatus, config.deviceIp, COLOIT_URI_DEVSTATUS, Type.NON);
                discovering = true;
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.debug("{}: Unable to process CoIoT Message for payload={}", thingName, payload, e);
            resetSerial();
        }
    }

    /**
     * Process a CoIoT device description message. This includes definitions on device units (Relay0, Relay1, Sensors
     * etc.) as well as a definition of sensors and actors. This information needs to be stored allowing to map ids from
     * status updates to the device units and matching the correct thing channel.
     *
     * @param devId The device id reported in the CoIoT message.
     * @param payload Device desciption in JSon format, example:
     *            {"blk":[{"I":0,"D":"Relay0"}],"sen":[{"I":112,"T":"Switch","R":"0/1","L":0}],"act":[{"I":211,"D":"Switch","L":0,"P":[{"I":2011,"D":"ToState","R":"0/1"}]}]}
     */
    private void handleDeviceDescription(String devId, String payload) {
        // Device description: payload = StringUtils.substringBefore(payload, "}]}]}") + "}]}]}";
        logger.debug("{}: CoIoT Device Description for {}: {}", thingName, devId, payload);

        // Decode Json
        CoIotDevDescription descr = gson.fromJson(payload, CoIotDevDescription.class);

        int i;
        for (i = 0; i < descr.blk.size(); i++) {
            CoIotDescrBlk blk = descr.blk.get(i);
            logger.debug("{}:    id={}: {}", thingName, blk.id, blk.desc);
            if (!blockMap.containsKey(blk.id)) {
                blockMap.put(blk.id, blk);
            } else {
                blockMap.replace(blk.id, blk);
            }
            if ((blk.type != null) && !blk.type.isEmpty()) {
                // in fact it is a sen entry - that's vioaling the Spec
                logger.trace("{}:    fix: auto-create sensor definition for id {}/{}!", thingName, blk.id, blk.desc);
                CoIotDescrSen sen = new CoIotDescrSen();
                sen.id = blk.id;
                sen.desc = blk.desc;
                sen.type = blk.type;
                sen.range = blk.range;
                sen.links = blk.links;
                addSensor(sen);
            }
        }
        logger.debug("{}: Adding {} sensor definitions", thingName, descr.sen.size());
        if (descr.sen != null) {
            for (i = 0; i < descr.sen.size(); i++) {
                addSensor(descr.sen.get(i));
            }
        }

        // Save to thing properties
        thingHandler.updateProperties(PROPERTY_COAP_DESCR, payload);
    }

    /**
     * Add a new sensor to the sensor table
     *
     * @param sen CoIotDescrSen of the sensor
     */
    private synchronized void addSensor(CoIotDescrSen sen) {
        logger.debug("{}:    id {}: {}, Type={}, Range={}, Links={}", thingName, sen.id, sen.desc, sen.type, sen.range,
                sen.links);
        try {
            CoIotDescrSen fixed = fixDescription(sen);
            if (!sensorMap.containsKey(fixed.id)) {
                sensorMap.put(sen.id, fixed);
            } else {
                sensorMap.replace(sen.id, fixed);
            }
        } catch (NullPointerException e) { // depending on firmware release the CoAP device description is buggy
            logger.debug("{}: Unable to decode sensor definition -> skip", thingName, e);
        }
    }

    /**
     * Process CoIoT status update message. If a status update is received, but the device description has not been
     * received yet a GET is send to query device description.
     *
     * @param devId device id included in the status packet
     * @param payload CoAP payload (Json format), example: {"G":[[0,112,0]]}
     * @param serial Serial for this request. If this the the same as last serial
     *            the update was already sent and processed so this one gets
     *            ignored.
     */
    private void handleStatusUpdate(String devId, String payload, int serial) {
        logger.debug("{}: CoIoT Sensor data {}", thingName, payload);
        if (blockMap.isEmpty()) {
            // send discovery packet
            resetSerial();
            reqDescription = sendRequest(reqDescription, config.deviceIp, COLOIT_URI_DEVDESC, Type.CON);

            // try to uses description from last initialization
            String savedDescr = thingHandler.getProperty(PROPERTY_COAP_DESCR);
            if (savedDescr.isEmpty()) {
                logger.debug("{}: Device description not yet received, trigger auto-initialization", thingName);
                return;
            }

            // simulate received device description to create element table
            handleDeviceDescription(devId, savedDescr);
            logger.debug("{}: Device description for {} restored: {}", thingName, devId, savedDescr);
        }

        // Parse Json,
        CoIotGenericSensorList list = gson.fromJson(payload, CoIotGenericSensorList.class);
        if (list.generic == null) {
            logger.debug("{}: Sensor list has invalid format! Payload: {}", devId, payload);
            return;
        }
        List<CoIotSensor> sensorUpdates = list.generic;

        ShellyDeviceProfile profile = thingHandler.getProfile();
        Map<String, State> updates = new TreeMap<String, State>();
        logger.debug("{}: {} CoAP sensor updates received", thingName, sensorUpdates.size());
        for (int i = 0; i < sensorUpdates.size(); i++) {
            try {
                CoIotSensor s = sensorUpdates.get(i);
                if (!sensorMap.containsKey(s.id)) {
                    logger.debug("{}: Invalid index in sensor description: {}", thingName, i);
                    continue;
                }
                CoIotDescrSen sen = sensorMap.get(s.id);
                // find matching sensor definition from device description, use the Link ID as index
                sen = fixDescription(sen);
                if (!blockMap.containsKey(sen.links)) {
                    logger.debug("{}: Invalid CoAP description: sen.links({}", thingName, getString(sen.links));
                    continue;
                }

                if (!blockMap.containsKey(sen.links)) {
                    logger.debug("{}: Unable to find BLK for link {} from sen.id={}", thingName, sen.links, sen.id);
                    continue;
                }
                CoIotDescrBlk element = blockMap.get(sen.links);
                logger.trace("{}:  Sensor value[{}]: id={}, Value={} ({}, Type={}, Range={}, Link={}: {})", thingName,
                        i, s.id, s.value, sen.desc, sen.type, sen.range, sen.links, element.desc);

                // Process status information and convert into channel updates
                Integer rIndex = Integer.parseInt(sen.links) + 1;
                String rGroup = profile.numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL
                        : CHANNEL_GROUP_RELAY_CONTROL + rIndex;

                switch (sen.type.toLowerCase()) {
                    case "b" /* BatteryLevel */:
                        updateChannel(updates, CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LEVEL,
                                toQuantityType(s.value, DIGITS_PERCENT, SmartHomeUnits.PERCENT));
                        break;
                    case "t" /* Temperature */:
                        Double value = getDouble(s.value);
                        switch (sen.desc.toLowerCase()) {
                            case "temperature": // Sensor Temp
                                if (getString(profile.settings.temperatureUnits)
                                        .equalsIgnoreCase(SHELLY_TEMP_FAHRENHEIT)) {
                                    value = ImperialUnits.FAHRENHEIT.getConverterTo(Units.CELSIUS)
                                            .convert(getDouble(s.value)).doubleValue();
                                }
                                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TEMP,
                                        toQuantityType(value, DIGITS_TEMP, SIUnits.CELSIUS));
                                break;
                            case "temperature f": // Device Temp -> ignore (we use C only)
                                break;
                            case "temperature c": // Device Temü in C
                                // Device temperature
                                updateChannel(updates, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP,
                                        toQuantityType(value, DIGITS_NONE, SIUnits.CELSIUS));
                                break;
                            case "external temperature f": // Shelly 1/1PM external temp sensors
                                // ignore F, we use C only
                                break;
                            case "external temperature c": // Shelly 1/1PM external temp sensors
                            case "external_temperature":
                                int idx = getExtTempId(sen.id);
                                if (idx > 0) {
                                    updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TEMP + idx,
                                            toQuantityType(value, DIGITS_TEMP, SIUnits.CELSIUS));
                                } else {
                                    logger.debug("{}: Unable to get extSensorId {} from {}/{}, payload={}", thingName,
                                            sen.id, sen.type, sen.desc, payload);
                                }
                                break;
                            default:
                                logger.debug("{}: Unknown temperatur type: {}", thingName, sen.desc);
                        }
                        break;
                    case "h" /* Humidity */:
                        updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_HUM,
                                toQuantityType(s.value, DIGITS_PERCENT, SmartHomeUnits.PERCENT));
                        break;
                    case "m" /* Motion */:
                        updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_MOTION,
                                s.value == 1 ? OnOffType.ON : OnOffType.OFF);
                        break;
                    case "l" /* Luminosity */:
                        updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_LUX,
                                toQuantityType(s.value, DIGITS_LUX, SmartHomeUnits.LUX));
                        break;
                    case "p" /* Power/Watt */:
                        String mGroup = profile.numMeters == 1 ? CHANNEL_GROUP_METER : CHANNEL_GROUP_METER + rIndex;
                        updateChannel(updates, mGroup, CHANNEL_METER_CURRENTWATTS,
                                toQuantityType(s.value, DIGITS_WATT, SmartHomeUnits.WATT));
                        updateChannel(updates, mGroup, CHANNEL_LAST_UPDATE, getTimestamp());
                        break;
                    case "s" /* CatchAll */:
                        String senValue = sen.desc.toLowerCase();
                        switch (senValue) {
                            case "state":
                            case "output":
                                updatePower(profile, updates, rIndex, sen, s, sensorUpdates);
                                break;
                            case "brightness":
                                // already handled by state/output
                                break;
                            case "overtemp":
                                if (s.value == 1) {
                                    thingHandler.postEvent(ALARM_TYPE_OVERTEMP, true);
                                }
                                break;
                            case "energy counter 0 [w-min]":
                                updateChannel(updates, rGroup, CHANNEL_METER_LASTMIN1,
                                        toQuantityType(s.value, DIGITS_WATT, SmartHomeUnits.WATT));
                                break;
                            case "energy counter 1 [w-min]":
                                updateChannel(updates, rGroup, CHANNEL_METER_LASTMIN2,
                                        toQuantityType(s.value, DIGITS_WATT, SmartHomeUnits.WATT));
                                break;
                            case "energy counter 2 [w-min]":
                                updateChannel(updates, rGroup, CHANNEL_METER_LASTMIN3,
                                        toQuantityType(s.value, DIGITS_WATT, SmartHomeUnits.WATT));
                                break;
                            case "energy counter total [w-h]": // EM3 reports W/h
                            case "energy counter total [w-min]":
                                Double total = profile.isEMeter ? s.value / 1000 : s.value / 60 / 1000;
                                updateChannel(updates, rGroup, CHANNEL_METER_TOTALKWH,
                                        toQuantityType(total, DIGITS_KWH, SmartHomeUnits.KILOWATT_HOUR));
                                break;
                            case "voltage":
                                updateChannel(updates, rGroup, CHANNEL_EMETER_VOLTAGE,
                                        toQuantityType(getDouble(s.value), DIGITS_VOLT, SmartHomeUnits.VOLT));
                                break;
                            case "current":
                                updateChannel(updates, rGroup, CHANNEL_EMETER_CURRENT,
                                        toQuantityType(getDouble(s.value), DIGITS_VOLT, SmartHomeUnits.AMPERE));
                                break;
                            case "pf":
                                updateChannel(updates, rGroup, CHANNEL_EMETER_PFACTOR, getDecimal(s.value));
                                break;
                            case "position":
                                // work around: Roller reports 101% instead max 100
                                double pos = Math.max(SHELLY_MIN_ROLLER_POS, Math.min(s.value, SHELLY_MAX_ROLLER_POS));
                                updateChannel(updates, CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_CONTROL,
                                        toQuantityType(SHELLY_MAX_ROLLER_POS - pos, SmartHomeUnits.PERCENT));
                                updateChannel(updates, CHANNEL_GROUP_ROL_CONTROL, CHANNEL_ROL_CONTROL_POS,
                                        toQuantityType(pos, SmartHomeUnits.PERCENT));
                                break;
                            case "input":
                                handleInput(sen, s, rGroup, updates);
                                break;
                            case "flood":
                                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_FLOOD,
                                        s.value == 1 ? OnOffType.ON : OnOffType.OFF);
                                break;
                            case "tilt": // DW with FW1.6.5+
                                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_TILT,
                                        toQuantityType(s.value, DIGITS_NONE, SmartHomeUnits.DEGREE_ANGLE));
                                break;
                            case "vibration": // DW with FW1.6.5+
                                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_VIBRATION,
                                        s.value == 1 ? OnOffType.ON : OnOffType.OFF);
                                break;
                            case "charger": // Sense
                                updateChannel(updates, CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_CHARGER,
                                        s.value == 1 ? OnOffType.ON : OnOffType.OFF);
                                break;
                            // RGBW2/Bulb
                            case "red":
                                updateChannel(updates, CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_RED,
                                        ShellyColorUtils.toPercent((int) s.value));
                                break;
                            case "green":
                                updateChannel(updates, CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_GREEN,
                                        ShellyColorUtils.toPercent((int) s.value));
                                break;
                            case "blue":
                                updateChannel(updates, CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_BLUE,
                                        ShellyColorUtils.toPercent((int) s.value));
                                break;
                            case "white":
                                updateChannel(updates, CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_WHITE,
                                        ShellyColorUtils.toPercent((int) s.value));
                                break;
                            case "gain":
                                updateChannel(updates, CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_GAIN,
                                        ShellyColorUtils.toPercent((int) s.value, SHELLY_MIN_GAIN, SHELLY_MAX_GAIN));
                                break;
                            case "temp": // Shelly Bulb
                            case "colortemperature": // Shelly Duo
                                updateChannel(updates,
                                        profile.inColor ? CHANNEL_GROUP_COLOR_CONTROL : CHANNEL_GROUP_WHITE_CONTROL,
                                        CHANNEL_COLOR_TEMP,
                                        ShellyColorUtils.toPercent((int) s.value, profile.minTemp, profile.maxTemp));
                                break;
                            default:
                                logger.debug(
                                        "{}: Update for unknown sensor with id {}, type {}/{} received, value={}, payload={}",
                                        thingName, sen.id, sen.type, sen.desc, s.value, payload);
                        }
                        break;
                    default:
                        logger.debug("{}: Sensor data for id {}, type {}/{} not processed, value={}; payload={}",
                                thingName, sen.id, sen.type, sen.desc, s.value, payload);
                }
            } catch (IllegalArgumentException | NullPointerException | ArrayIndexOutOfBoundsException e) {
                // even the processing of one value failed we continue with the next one (sometimes this is caused by
                // buggy formats provided by the device
                logger.debug("{}: Unable to process data from sensor[{}], devId={}, payload={}", thingName, i, devId,
                        payload, e);
            }
        }

        if (!updates.isEmpty()) {
            if (profile.hasBattery || thingHandler.autoCoIoT) {
                // CoAP is currently lacking the lastUpdate info, so we use host timestamp
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_LAST_UPDATE, getTimestamp());
            }
            int updated = 0;
            for (Map.Entry<String, State> u : updates.entrySet()) {
                updated += thingHandler.updateChannel(u.getKey(), u.getValue(), false) ? 1 : 0;
            }
            if (updated > 0) {
                logger.debug("{}: {} channels updated from CoIoT status", thingName, updated);
            }

            // Old firmware release are lacking various status values, which are not updated using CoIoT.
            // In this case we keep a refresh so it gets polled using REST. Beginning with Firmware 1.6 most
            // of the values are available

            if ((!thingHandler.autoCoIoT && (thingHandler.scheduledUpdates <= 1))
                    || (thingHandler.autoCoIoT && !profile.isLight && !profile.isSensor)) {
                thingHandler.requestUpdates(1, false);
            }
        }

        // Remember serial, new packets with same serial will be ignored
        lastSerial = serial;
        lastPayload = payload;
    }

    private void handleInput(CoIotDescrSen sen, CoIotSensor s, String rGroup, Map<String, State> updates) {
        final ShellyDeviceProfile profile = thingHandler.getProfile();
        int idx = getSensorNumber("Input", sen.id);
        if (idx <= 0) {
            return;
        }

        int r = idx - 1;
        String iGroup = rGroup;
        String iChannel = CHANNEL_INPUT;
        if (profile.isRGBW2) {
            // RGBW2 has only one input, not one per channel
            iGroup = CHANNEL_GROUP_LIGHT_CONTROL;
        } else if (profile.isDimmer || profile.isRoller) {
            // Dimmer and Roller things have 2 inputs
            iChannel = CHANNEL_INPUT + String.valueOf(idx);
        } else {
            // Device has 1 input per relay: 0=off, 1+2 depend on switch mode
            iGroup = profile.numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL : CHANNEL_GROUP_RELAY_CONTROL + idx;
        }

        if ((profile.settings.relays != null) && (r >= 0) && (r < profile.settings.relays.size())) {
            ShellySettingsRelay relay = profile.settings.relays.get(r);
            logger.trace("{}: Coap update for button (type {})", thingName, relay.btnType);
            if ((s.value != 0) && (relay.btnType.equalsIgnoreCase(SHELLY_BTNT_MOMENTARY)
                    || relay.btnType.equalsIgnoreCase(SHELLY_BTNT_MOM_ON_RELEASE)
                    || relay.btnType.equalsIgnoreCase(SHELLY_BTNT_DETACHED))) {
                String trigger = "";
                switch ((int) s.value) {
                    case 0:
                        trigger = CommonTriggerEvents.RELEASED;
                        break;
                    case 1:
                        trigger = CommonTriggerEvents.SHORT_PRESSED;
                        break;
                    case 2:
                        trigger = CommonTriggerEvents.LONG_PRESSED;
                        break;
                }
                if (!trigger.isEmpty()) {
                    logger.debug("{}: Update button state with {}", thingName, trigger);
                    thingHandler.triggerChannel(iGroup, CHANNEL_BUTTON_TRIGGER, trigger);
                }
            }
        }
        updateChannel(updates, iGroup, iChannel, s.value == 0 ? OnOffType.OFF : OnOffType.ON);
    }

    /**
     *
     * Handles the combined updated of the brightness channel:
     * brightness$Switch is the OnOffType (power state)
     * brightness&Value is the brightness value
     *
     * @param profile Device profile, required to select the channel group and name
     * @param updates List of updates. updatePower will add brightness$Switch and brightness&Value if changed
     * @param id Sensor id from the update
     * @param sen Sensor description from the update
     * @param s New sensor value
     * @param allUpdatesList of updates. This is required, because we need to update both values at the same time
     */
    private void updatePower(ShellyDeviceProfile profile, Map<String, State> updates, Integer id, CoIotDescrSen sen,
            CoIotSensor s, List<CoIotSensor> allUpdates) {
        String group = "";
        String channel = CHANNEL_BRIGHTNESS;
        String checkL = ""; // RGBW-white uses 4 different Power, Brightness, VSwitch values
        if (profile.isLight || profile.isDimmer) {
            if (profile.isBulb || profile.inColor) {
                group = CHANNEL_GROUP_LIGHT_CONTROL;
                channel = CHANNEL_LIGHT_POWER;
            } else if (profile.isDuo) {
                group = CHANNEL_GROUP_WHITE_CONTROL;
            } else if (profile.isDimmer) {
                group = CHANNEL_GROUP_RELAY_CONTROL;
            } else if (profile.isRGBW2) {
                group = CHANNEL_GROUP_LIGHT_CHANNEL + id;
                checkL = String.valueOf(id.intValue() - 1); // id is 1-based, L is 0-based
                logger.trace("{}: updatePower() for L={}", thingName, checkL);
            }

            // We need to update brigthtess and on/off state at the same time to avoid "flipping brightness slider" in
            // the UI
            Double brightness = -1.0;
            Double power = -1.0;
            for (CoIotSensor update : allUpdates) {
                CoIotDescrSen d = fixDescription(sensorMap.get(update.id));
                if (!checkL.isEmpty() && !d.links.equals(checkL)) {
                    // continue until we find the correct one
                    continue;
                }
                if (d.desc.equalsIgnoreCase("brightness")) {
                    brightness = new Double(update.value);
                } else if (d.desc.equalsIgnoreCase("output") || d.desc.equalsIgnoreCase("state")) {
                    power = new Double(update.value);
                }
            }
            if (power != -1) {
                updateChannel(updates, group, channel + "$Switch", power == 1 ? OnOffType.ON : OnOffType.OFF);
            }
            if (brightness != -1) {
                updateChannel(updates, group, channel + "$Value",
                        toQuantityType(power == 1 ? brightness : 0, DIGITS_NONE, SmartHomeUnits.PERCENT));
            }
        } else if (profile.hasRelays) {
            group = profile.numRelays <= 1 ? CHANNEL_GROUP_RELAY_CONTROL : CHANNEL_GROUP_RELAY_CONTROL + id;
            updateChannel(updates, group, CHANNEL_OUTPUT, s.value == 1 ? OnOffType.ON : OnOffType.OFF);
        } else if (profile.isSensor) {
            // Sensor state
            if (profile.isDW) { // Door Window has item type Contact
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_STATE,
                        s.value != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            } else {
                updateChannel(updates, CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_STATE,
                        s.value == 1 ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }

    private boolean updateChannel(Map<String, State> updates, String group, String channel, State value) {
        updates.put(mkChannelId(group, channel), value);
        return true;
    }

    /**
     *
     * Depending on the device type and firmware release there are significant bugs or incosistencies in the CoIoT
     * Device Description returned by the discovery request. Shelly is even not following it's own speicifcation. All of
     * that has been reported to Shelly and acknowledged. Firmware 1.6 brought significant improvements. However, the
     * old mapping stays in to support older firmware releases.
     *
     * @param sen Sensor description received from device
     * @return fixed Sensor description (sen)
     */
    private CoIotDescrSen fixDescription(CoIotDescrSen sen) {
        // Shelly1: reports null descr+type "Switch" -> map to S
        // Shelly1PM: reports null descr+type "Overtemp" -> map to O
        // Shelly1PM: reports null descr+type "W" -> add description
        // Shelly1PM: reports temp senmsors without desc -> add description
        // Shelly Dimmer: sensors are reported without descriptions -> map to S
        // SHelly Sense: multiple issues: Description should not be lower case, invalid type for Motion and Battery
        // Shelly Sense: Battery is reported with Desc "battery", but type "H" instead of "B"
        // Shelly Sense: Motion is reported with Desc "battery", but type "H" instead of "B"
        // Shelly Bulb: Colors are coded with Type="Red" etc. rather than Type="S" and color as Descr
        // Shelly RGBW2 is reporting Brightness, Power, VSwitch for each channel, but all with L=0
        if (sen.desc == null) {
            sen.desc = "";
        }
        String desc = sen.desc.toLowerCase();

        // RGBW2 reports Power_0, Power_1, Power_2, Power_3; same for VSwitch and Brightness, all of them linkted to L:0
        // we break it up to Power with L:0, Power with L:1...
        if (desc.contains("_") && (desc.contains("power") || desc.contains("vswitch") || desc.contains("brightness"))) {
            String newDesc = StringUtils.substringBefore(sen.desc, "_");
            String newLink = StringUtils.substringAfter(sen.desc, "_");
            sen.desc = newDesc;
            sen.links = newLink;
            if (!blockMap.containsKey(sen.links)) {
                // auto-insert a matching blk entry
                CoIotDescrBlk blk = new CoIotDescrBlk();
                CoIotDescrBlk blk0 = blockMap.get("0"); // blk 0 is always there
                blk.id = sen.links;
                blk.desc = blk0.desc + "_" + blk.id;
                blockMap.put(blk.id, blk);
            }
        }

        switch (sen.type.toLowerCase()) {
            case "w": // old devices/firmware releases use "W", new ones "P"
                sen.type = "P";
                sen.desc = "Power";
                break;
            case "tc":
                sen.type = "T";
                sen.desc = "Temperature C";
                break;
            case "tf":
                sen.type = "T";
                sen.desc = "Temperature F";
                break;
            case "overtemp":
                sen.type = "S";
                sen.desc = "Overtemp";
                break;
            case "relay0":
            case "switch":
            case "vswitch":
                sen.type = "S";
                sen.desc = "State";
                break;
        }

        switch (sen.desc.toLowerCase()) {
            case "motion": // fix acc to spec it's T=M
                sen.type = "M";
                sen.desc = "Motion";
                break;
            case "battery": // fix: type is B not H
                sen.type = "B";
                sen.desc = "Battery";
                break;
            case "overtemp":
                sen.type = "S";
                sen.desc = "Overtemp";
                break;
            case "relay0":
            case "switch":
            case "vswitch":
                sen.type = "S";
                sen.desc = "State";
                break;
            case "e cnt 0 [w-min]": // 4 Pro
            case "e cnt 1 [w-min]":
            case "e cnt 2 [w-min]":
            case "e cnt total [w-min]": // 4 Pro
                sen.desc = sen.desc.toLowerCase().replace("e cnt", "energy counter");
                break;

        }

        if (sen.desc.isEmpty()) {
            switch (sen.type.toLowerCase()) {
                case "p":
                    sen.desc = "Power";
                    break;
                case "T":
                    sen.desc = "Temperature";
                    break;
                case "input":
                    sen.type = "S";
                    sen.desc = "Input";
                    break;
                case "output":
                    sen.type = "S";
                    sen.desc = "Output";
                    break;
                case "brightness":
                    sen.type = "S";
                    sen.desc = "Brightness";
                    break;
                case "red":
                case "green":
                case "blue":
                case "white":
                case "gain":
                case "temp": // Bulb: Color temperature
                    sen.desc = sen.type;
                    sen.type = "S";
                    break;
                case "vswitch":
                    // it seems that Shelly tends to break their own spec: T is the description and D is no longer
                    // included
                    // -> map D to sen.T and set CatchAll for T
                    sen.desc = sen.type;
                    sen.type = "S";
                    break;
                // Default: set no description
                // (there are no T values defined in the CoIoT spec)
                case "tostate":
                default:
                    sen.desc = "";
            }
        }
        return sen;
    }

    /**
     * Send a new request (Discovery to get Device Description). Before a pending
     * request will be canceled.
     *
     * @param request The current request (this will be canceled an a new one will
     *            be created)
     * @param ipAddress Device's IP address
     * @param uri The URI we are calling (CoIoT = /cit/d or /cit/s)
     * @param con true: send as CON, false: send as NON
     * @return new packet
     */
    private Request sendRequest(@Nullable Request request, String ipAddress, String uri, Type con) {
        if ((request != null) && !request.isCanceled()) {
            request.cancel();
        }

        resetSerial();
        return newRequest(ipAddress, uri, con).send();
    }

    /**
     * Allocate a new Request structure. A message observer will be added to get the
     * callback when a response has been received.
     *
     * @param ipAddress IP address of the device
     * @param uri URI to be addressed
     * @param uri The URI we are calling (CoIoT = /cit/d or /cit/s)
     * @param con true: send as CON, false: send as NON
     * @return new packet
     */

    private Request newRequest(String ipAddress, String uri, Type con) {
        // We need to build our own Request to set an empty Token
        Request request = new Request(Code.GET, con);
        request.setURI(completeUrl(ipAddress, uri));
        request.setToken(EMPTY_BYTE);
        request.addMessageObserver(new MessageObserverAdapter() {
            @Override
            public void onResponse(@Nullable Response response) {
                processResponse(response);
            }

            @Override
            public void onCancel() {
                logger.debug("{}: CoAP Request was canceled", thingName);
            }

            @Override
            public void onTimeout() {
                logger.debug("{}: CoAP Request timed out", thingName);
            }
        });
        return request;
    }

    /**
     * Reset serial and payload used to detect duplicate messages, which have to be ignored.
     * We can't rely that the device manages serials correctly all the time. There are firmware releases sending updated
     * sensor information with the serial from the last packet, which is wrong. We bypass this problem by comparing also
     * the payload.
     */
    private void resetSerial() {
        lastSerial = -1;
        lastPayload = "";
    }

    /**
     * Find index of Input id, which is required to map to channel name
     *
     * @parm sensorDesc D field from sensor update
     * @param sensorId The id from the sensor update
     * @return Index of found entry (+1 will be the suffix for the channel name) or null if sensorId is not found
     */
    private int getSensorNumber(String sensorDesc, String sensorId) {
        int idx = 0;
        for (Map.Entry<String, CoIotDescrSen> se : sensorMap.entrySet()) {
            CoIotDescrSen sen = se.getValue();
            if (sen.desc.equalsIgnoreCase(sensorDesc)) {
                idx++; // iterate from input1..2..n
            }
            if (sen.id.equalsIgnoreCase(sensorId) && blockMap.containsKey(sen.links)) {
                CoIotDescrBlk blk = blockMap.get(sen.links);
                if (StringUtils.substring(blk.desc, 5).equalsIgnoreCase("Relay")) {
                    idx = Integer.parseInt(StringUtils.substringAfter(blk.desc, "Relay"));
                }
                return idx;
            }
        }
        logger.debug("{}: sensorId {} not found in sensorMap!", thingName, sensorId);
        return -1;
    }

    /**
     *
     * Get matching sensorId for updates on "External Temperature" - there might be more than 1 sensor.
     *
     * @param sensorId sensorId to map into a channel index
     * @return Index of the corresponding channel (e.g. 0 build temperature1, 1->temperagture2...)
     */
    private int getExtTempId(String sensorId) {
        int idx = 0;
        for (Map.Entry<String, CoIotDescrSen> se : sensorMap.entrySet()) {
            CoIotDescrSen sen = se.getValue();
            if (sen.desc.equalsIgnoreCase("external_temperature")
                    || sen.desc.equalsIgnoreCase("external temperature c")) {
                idx++; // iterate from temperature1..2..n
            }
            if (sen.id.equalsIgnoreCase(sensorId)) {
                return idx;
            }
        }
        logger.debug("{}: sensorId {} not found in sensorMap!", thingName, sensorId);
        return -1;
    }

    /**
     * Cancel pending requests and shutdown the client
     */
    public synchronized void stop() {
        if (isStarted()) {
            logger.debug("{}: Stopping CoAP Listener", thingName);
            coapServer.stop(this);
            if (statusClient != null) {
                statusClient.shutdown();
                statusClient = null;
            }
            if (!reqDescription.isCanceled()) {
                reqDescription.cancel();
            }
            if (!reqStatus.isCanceled()) {
                reqStatus.cancel();
            }
        }
    }

    public void dispose() {
        stop();
    }

    private static String completeUrl(String ipAddress, String uri) {
        return "coap://" + ipAddress + ":" + COIOT_PORT + uri;
    }
}
