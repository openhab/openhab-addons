/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.api1;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.MessageObserverAdapter;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionNumberRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotDescrBlk;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotDescrSen;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotDevDescrTypeAdapter;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotDevDescription;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotGenericSensorList;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotSensor;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.CoIotSensorTypeAdapter;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyColorUtils;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link Shelly1CoapHandler} handles the CoIoT/CoAP registration and events.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly1CoapHandler implements Shelly1CoapListener {
    private static final byte[] EMPTY_BYTE = new byte[0];

    private final Logger logger = LoggerFactory.getLogger(Shelly1CoapHandler.class);
    private final ShellyThingInterface thingHandler;
    private ShellyThingConfiguration config = new ShellyThingConfiguration();
    private final GsonBuilder gsonBuilder = new GsonBuilder();
    private final Gson gson;
    private String thingName;

    private boolean coiotBound = false;
    private Shelly1CoIoTInterface coiot;
    private int coiotVers = -1;

    private final Shelly1CoapServer coapServer;
    private @Nullable CoapClient statusClient;
    private Request reqDescription = new Request(Code.GET, Type.CON);
    private Request reqStatus = new Request(Code.GET, Type.CON);
    private boolean updatesRequested = false;
    private int coiotPort = COIOT_PORT;

    private int lastSerial = -1;
    private String lastPayload = "";
    private Map<String, CoIotDescrBlk> blkMap = new LinkedHashMap<>();
    private Map<String, CoIotDescrSen> sensorMap = new LinkedHashMap<>();
    private ShellyDeviceProfile profile;
    private ShellyApiInterface api;

    public Shelly1CoapHandler(ShellyThingInterface thingHandler, Shelly1CoapServer coapServer) {
        this.thingHandler = thingHandler;
        this.thingName = thingHandler.getThingName();
        this.profile = thingHandler.getProfile();
        this.api = thingHandler.getApi();
        this.coapServer = coapServer;
        this.coiot = new Shelly1CoIoTVersion2(thingName, thingHandler, blkMap, sensorMap); // Default: V2

        gsonBuilder.registerTypeAdapter(CoIotDevDescription.class, new CoIotDevDescrTypeAdapter());
        gsonBuilder.registerTypeAdapter(CoIotGenericSensorList.class, new CoIotSensorTypeAdapter());
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
        try {
            this.thingName = thingName;
            this.config = config;
            this.profile = thingHandler.getProfile();
            if (isStarted()) {
                logger.trace("{}: CoAP Listener was already started", thingName);
                stop();
            }

            logger.debug("{}: Starting CoAP Listener", thingName);
            if (!profile.coiotEndpoint.isEmpty() && profile.coiotEndpoint.contains(":")) {
                String ps = substringAfter(profile.coiotEndpoint, ":");
                coiotPort = Integer.parseInt(ps);
            }
            coapServer.start(config.localIp, coiotPort, this);
            statusClient = new CoapClient(completeUrl(config.deviceIp, coiotPort, COLOIT_URI_DEVSTATUS))
                    .setTimeout((long) SHELLY_API_TIMEOUT_MS).useNONs().setEndpoint(coapServer.getEndpoint());
            @Nullable
            Endpoint endpoint = null;
            CoapClient client = statusClient;
            if (client != null) {
                endpoint = client.getEndpoint();
            }
            if ((endpoint == null) || !endpoint.isStarted()) {
                logger.warn("{}: Unable to initialize CoAP access (network error)", thingName);
                throw new ShellyApiException("Network initialization failed");
            }

            discover();
        } catch (SocketException e) {
            logger.warn("{}: Unable to initialize CoAP access (socket exception) - {}", thingName, e.getMessage());
            throw new ShellyApiException("Network error", e);
        } catch (UnknownHostException e) {
            logger.info("{}: CoAP Exception (Unknown Host)", thingName, e);
            throw new ShellyApiException("Unknown Host: " + config.deviceIp, e);
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
            thingHandler.incProtErrors();
            return; // other device instance
        }
        ResponseCode code = response.getCode();
        if (code != ResponseCode.CONTENT) {
            // error handling
            logger.debug("{}: Unknown Response Code {} received, payload={}", thingName, code,
                    response.getPayloadString());
            thingHandler.incProtErrors();
            return;
        }

        List<Option> options = response.getOptions().asSortedList();
        String ip = response.getSourceContext().getPeerAddress().toString();
        boolean match = ip.contains(config.deviceIp);
        if (!match) {
            // We can't identify device by IP, so we need to check the CoAP header's Global Device ID
            for (Option opt : options) {
                if (opt.getNumber() == COIOT_OPTION_GLOBAL_DEVID) {
                    String devid = opt.getStringValue();
                    if (devid.contains("#")) {
                        // Format: <device type>#<mac address>#<coap version>
                        String macid = substringBetween(devid, "#", "#");
                        if (profile.mac.toUpperCase().contains(macid.toUpperCase())) {
                            match = true;
                            break;
                        }
                    }
                }
            }
        }
        if (!match) {
            // other instance
            return;
        }

        String payload = "";
        String devId = "";
        String uri = "";
        int serial = -1;
        try {
            thingHandler.incProtMessages();
            if (logger.isDebugEnabled()) {
                logger.debug("{}: CoIoT Message from {} (MID={}): {}", thingName,
                        response.getSourceContext().getPeerAddress(), response.getMID(), response.getPayloadString());
            }
            if (response.isCanceled() || response.isDuplicate() || response.isRejected()) {
                logger.debug("{} ({}): Packet was canceled, rejected or is a duplicate -> discard", thingName, devId);
                thingHandler.incProtErrors();
                return;
            }

            payload = response.getPayloadString();
            for (Option opt : options) {
                switch (opt.getNumber()) {
                    case OptionNumberRegistry.URI_PATH:
                        uri = COLOIT_URI_BASE + opt.getStringValue();
                        break;
                    case OptionNumberRegistry.URI_HOST: // ignore
                        break;
                    case OptionNumberRegistry.CONTENT_FORMAT: // ignore
                        break;
                    case COIOT_OPTION_GLOBAL_DEVID:
                        devId = opt.getStringValue();
                        String sVersion = substringAfterLast(devId, "#");
                        int iVersion = Integer.parseInt(sVersion);
                        if (coiotBound && (coiotVers != iVersion)) {
                            logger.debug("{}: CoIoT versopm has changed from {} to {}, maybe the firmware was upgraded",
                                    thingName, coiotVers, iVersion);
                            thingHandler.reinitializeThing();
                            coiotBound = false;
                        }
                        if (!coiotBound) {
                            thingHandler.updateProperties(PROPERTY_COAP_VERSION, sVersion);
                            logger.debug("{}: CoIoT Version {} detected", thingName, iVersion);
                            if (iVersion == COIOT_VERSION_1) {
                                coiot = new Shelly1CoIoTVersion1(thingName, thingHandler, blkMap, sensorMap);
                            } else if (iVersion == COIOT_VERSION_2) {
                                coiot = new Shelly1CoIoTVersion2(thingName, thingHandler, blkMap, sensorMap);
                            } else {
                                logger.warn("{}: Unsupported CoAP version detected: {}", thingName, sVersion);
                                return;
                            }
                            coiotVers = iVersion;
                            coiotBound = true;
                        }
                        break;
                    case COIOT_OPTION_STATUS_VALIDITY:
                        break;
                    case COIOT_OPTION_STATUS_SERIAL:
                        serial = opt.getIntegerValue();
                        break;
                    default:
                        logger.debug("{} ({}): COAP option {} with value {} skipped", thingName, devId, opt.getNumber(),
                                opt.getValue());
                }
            }

            // If we received a CoAP message successful the thing must be online
            thingHandler.setThingOnline();

            // The device changes the serial on every update, receiving a message with the same serial is a
            // duplicate, excep for battery devices! Those reset the serial every time when they wake-up
            if ((serial == lastSerial) && payload.equals(lastPayload) && (!profile.hasBattery
                    || coiot.getLastWakeup().equalsIgnoreCase("ext_power") || ((serial & 0xFF) != 0))) {
                logger.debug("{}: Serial {} was already processed, ignore update", thingName, serial);
                return;
            }

            // fixed malformed JSON :-(
            payload = fixJSON(payload);

            try {
                if (uri.equalsIgnoreCase(COLOIT_URI_DEVDESC) || (uri.isEmpty() && payload.contains(COIOT_TAG_BLK))) {
                    handleDeviceDescription(devId, payload);
                } else if (uri.equalsIgnoreCase(COLOIT_URI_DEVSTATUS)
                        || (uri.isEmpty() && payload.contains(COIOT_TAG_GENERIC))) {
                    handleStatusUpdate(devId, payload, serial);
                }
            } catch (ShellyApiException e) {
                logger.debug("{}: Unable to process CoIoT message: {}", thingName, e.toString());
                thingHandler.incProtErrors();
            }

            if (!updatesRequested) {
                // Observe Status Updates
                reqStatus = sendRequest(reqStatus, config.deviceIp, COLOIT_URI_DEVSTATUS, Type.NON);
                updatesRequested = true;
            }
        } catch (JsonSyntaxException | IllegalArgumentException | NullPointerException e) {
            logger.debug("{}: Unable to process CoIoT Message for payload={}", thingName, payload, e);
            resetSerial();
            thingHandler.incProtErrors();
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
    private void handleDeviceDescription(String devId, String payload) throws ShellyApiException {
        logger.debug("{}: CoIoT Device Description for {}: {}", thingName, devId, payload);

        try {
            boolean valid = true;

            // Decode Json
            CoIotDevDescription descr = fromJson(gson, payload, CoIotDevDescription.class);
            for (int i = 0; i < descr.blk.size(); i++) {
                CoIotDescrBlk blk = descr.blk.get(i);
                logger.debug("{}:    id={}: {}", thingName, blk.id, blk.desc);
                if (!blkMap.containsKey(blk.id)) {
                    blkMap.put(blk.id, blk);
                } else {
                    blkMap.replace(blk.id, blk);
                }
                if ((blk.type != null) && !blk.type.isEmpty()) {
                    // in fact it is a sen entry - that's vioaling the Spec
                    logger.trace("{}:    fix: auto-create sensor definition for id {}/{}!", thingName, blk.id,
                            blk.desc);
                    CoIotDescrSen sen = new CoIotDescrSen();
                    sen.id = blk.id;
                    sen.desc = blk.desc;
                    sen.type = blk.type;
                    sen.range = blk.range;
                    sen.links = blk.links;
                    valid &= addSensor(sen);
                }
            }

            // Save to thing properties
            thingHandler.updateProperties(PROPERTY_COAP_DESCR, payload);

            logger.debug("{}: Adding {} sensor definitions", thingName, descr.sen.size());
            if (descr.sen != null) {
                for (int i = 0; i < descr.sen.size(); i++) {
                    valid &= addSensor(descr.sen.get(i));
                }
            }
            coiot.completeMissingSensorDefinition(sensorMap);

            if (!valid) {
                logger.debug(
                        "{}: Incompatible device description detected for CoIoT version {} (id length mismatch), discarding!",
                        thingName, coiot.getVersion());

                discover();
                return;
            }
        } catch (JsonSyntaxException e) {
            logger.warn("{}: Unable to parse CoAP Device Description! JSON={}", thingName, payload);
        } catch (NullPointerException | IllegalArgumentException e) {
            logger.warn("{}: Unable to parse CoAP Device Description! JSON={}", thingName, payload, e);
        }
    }

    /**
     * Add a new sensor to the sensor table
     *
     * @param sen CoIotDescrSen of the sensor
     */
    private synchronized boolean addSensor(CoIotDescrSen sen) {
        logger.debug("{}:    id {}: {}, Type={}, Range={}, Links={}", thingName, sen.id, sen.desc, sen.type, sen.range,
                sen.links);
        // CoIoT version 2 changes from 3 digit IDs to 4 digit IDs
        // We need to make sure that the persisted device description matches,
        // otherwise the stored one is discarded and a new discovery is triggered
        // This happens on firmware up/downgrades (version 1.8 brings CoIoT v2 with 4 digit IDs)
        int vers = coiot.getVersion();
        if (((vers == COIOT_VERSION_1) && (sen.id.length() > 3))
                || ((vers >= COIOT_VERSION_2) && (sen.id.length() < 4))) {
            logger.debug("{}: Invalid format for sensor defition detected, id={}", thingName, sen.id);
            return false;
        }

        try {
            CoIotDescrSen fixed = coiot.fixDescription(sen, blkMap);
            if (!sensorMap.containsKey(fixed.id)) {
                sensorMap.put(sen.id, fixed);
            } else {
                sensorMap.replace(sen.id, fixed);
            }
        } catch (NullPointerException | IllegalArgumentException e) { // depending on firmware release the CoAP device
                                                                      // description is buggy
            logger.debug("{}: Unable to decode sensor definition -> skip", thingName, e);
        }

        return true;
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
     * @throws ShellyApiException
     */
    private void handleStatusUpdate(String devId, String payload, int serial) throws ShellyApiException {
        logger.debug("{}: CoIoT Sensor data {} (serial={})", thingName, payload, serial);
        if (blkMap.isEmpty()) {
            // send discovery packet
            resetSerial();
            discover();

            // try to uses description from last initialization
            String savedDescr = thingHandler.getProperty(PROPERTY_COAP_DESCR);
            if (savedDescr.isEmpty()) {
                logger.debug("{}: Device description not yet received, trigger auto-initialization", thingName);
                return;
            }

            // simulate received device description to create element table
            logger.debug("{}: Device description for {} restored: {}", thingName, devId, savedDescr);
            handleDeviceDescription(devId, savedDescr);
        }

        // Parse Json,
        CoIotGenericSensorList list = fromJson(gson, fixJSON(payload), CoIotGenericSensorList.class);
        if (list.generic == null) {
            logger.debug("{}: Sensor list has invalid format! Payload: {}", devId, payload);
            return;
        }

        List<CoIotSensor> sensorUpdates = list.generic;
        Map<String, State> updates = new TreeMap<String, State>();
        logger.debug("{}: {} CoAP sensor updates received", thingName, sensorUpdates.size());
        int failed = 0;
        ShellyColorUtils col = new ShellyColorUtils();
        for (int i = 0; i < sensorUpdates.size(); i++) {
            try {
                CoIotSensor s = sensorUpdates.get(i);
                CoIotDescrSen sen = sensorMap.get(s.id);
                if (sen == null) {
                    logger.debug("{}: Unable to sensor definition for id={}, payload={}", thingName, s.id, payload);
                    continue;
                }
                // find matching sensor definition from device description, use the Link ID as index
                CoIotDescrBlk element = null;
                sen = coiot.fixDescription(sen, blkMap);
                element = blkMap.get(sen.links);
                if (element == null) {
                    logger.debug("{}: Unable to find BLK for link {} from sen.id={}, payload={}", thingName, sen.links,
                            sen.id, payload);
                    continue;
                }
                logger.trace("{}:  Sensor value[{}]: id={}, Value={} ({}, Type={}, Range={}, Link={}: {})", thingName,
                        i, s.id, getString(s.valueStr).isEmpty() ? s.value : s.valueStr, sen.desc, sen.type, sen.range,
                        sen.links, element.desc);

                if (!coiot.handleStatusUpdate(sensorUpdates, sen, serial, s, updates, col)) {
                    logger.debug("{}: CoIoT data for id {}, type {}/{} not processed, value={}; payload={}", thingName,
                            sen.id, sen.type, sen.desc, s.value, payload);
                }
            } catch (NullPointerException | IllegalArgumentException e) {
                // even the processing of one value failed we continue with the next one (sometimes this is caused by
                // buggy formats provided by the device
                logger.debug("{}: Unable to process data from sensor[{}], devId={}, payload={}", thingName, i, devId,
                        payload, e);
            }
        }

        if (!updates.isEmpty()) {
            int updated = 0;
            for (Map.Entry<String, State> u : updates.entrySet()) {
                String key = u.getKey();
                updated += thingHandler.updateChannel(key, u.getValue(), false) ? 1 : 0;
            }
            if (updated > 0) {
                logger.debug("{}: {} channels updated from CoIoT status, serial={}", thingName, updated, serial);
                if (profile.isSensor || profile.isRoller) {
                    // CoAP is currently lacking the lastUpdate info, so we use host timestamp
                    thingHandler.updateChannel(profile.getControlGroup(0), CHANNEL_LAST_UPDATE, getTimestamp());
                }
            }

            if (profile.isLight && profile.inColor && col.isRgbValid()) {
                // Update color picker from single values
                if (col.isRgbValid()) {
                    thingHandler.updateChannel(mkChannelId(CHANNEL_GROUP_COLOR_CONTROL, CHANNEL_COLOR_PICKER),
                            col.toHSB(), false);
                }
            }

            if ((profile.isRGBW2 && !profile.inColor) || profile.isRoller) {
                // Aggregate Meter Data from different Coap updates
                int i = 1;
                double totalCurrent = 0.0;
                @SuppressWarnings("unused")
                double totalKWH = 0.0;
                boolean updateMeter = false;
                while (i <= thingHandler.getProfile().numMeters) {
                    String meter = CHANNEL_GROUP_METER + i;
                    double current = thingHandler.getChannelDouble(meter, CHANNEL_METER_CURRENTWATTS);
                    double total = thingHandler.getChannelDouble(meter, CHANNEL_METER_TOTALKWH);
                    totalCurrent += current >= 0 ? current : 0;
                    totalKWH += total >= 0 ? total : 0;
                    updateMeter |= current >= 0 | total >= 0;
                    i++;
                }
                if (updateMeter) {
                    thingHandler.updateChannel(CHANNEL_GROUP_METER, CHANNEL_METER_CURRENTWATTS,
                            toQuantityType(totalCurrent, DIGITS_WATT, Units.WATT));
                    thingHandler.updateChannel(CHANNEL_GROUP_METER, CHANNEL_LAST_UPDATE, getTimestamp());
                }
            }

            // Old firmware release are lacking various status values, which are not updated using CoIoT.
            // In this case we keep a refresh so it gets polled using REST. Beginning with Firmware 1.6 most
            // of the values are available
            thingHandler.triggerUpdateFromCoap();
        } else {
            if (failed == sensorUpdates.size()) {
                logger.debug("{}: Device description problem detected, re-discover", thingName);
                coiotBound = false;
                discover();
            }
        }

        // Remember serial, new packets with same serial will be ignored
        lastSerial = serial;
        lastPayload = payload;
    }

    private void discover() {
        if (coiot.getVersion() >= 2) {
            {
                try {
                    // Try to device description using http request (FW 1.10+)
                    String payload = api.getCoIoTDescription();
                    if (!payload.isEmpty()) {
                        logger.debug("{}: Using CoAP device description from successful HTTP /cit/d", thingName);
                        handleDeviceDescription(thingName, payload);
                        return;
                    }
                } catch (ShellyApiException e) {
                    // ignore if not supported by device
                }
            }
        }
        reqDescription = sendRequest(reqDescription, config.deviceIp, COLOIT_URI_DEVDESC, Type.CON);
    }

    /**
     * Fix malformed JSON - stupid, but the devices sometimes return malformed JSON with then causes a
     * JsonSyntaxException
     *
     * @param json to be checked/fixed
     */
    private static String fixJSON(String payload) {
        String json = payload;
        json = json.replace("}{", "},{");
        json = json.replace("][", "],[");
        json = json.replace("],,[", "],[");
        return json;
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
        return newRequest(ipAddress, coiotPort, uri, con).send();
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

    private Request newRequest(String ipAddress, int port, String uri, Type con) {
        // We need to build our own Request to set an empty Token
        Request request = new Request(Code.GET, con);
        request.setURI(completeUrl(ipAddress, port, uri));
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

    public int getVersion() {
        return coiotVers;
    }

    /**
     * Cancel pending requests and shutdown the client
     */
    public synchronized void stop() {
        if (isStarted()) {
            logger.debug("{}: Stopping CoAP Listener", thingName);
            coapServer.stop(this);
            CoapClient cclient = statusClient;
            if (cclient != null) {
                cclient.shutdown();
                statusClient = null;
            }
            Request request = reqDescription;
            if (!request.isCanceled()) {
                request.cancel();
            }
            request = reqStatus;
            if (!request.isCanceled()) {
                request.cancel();
            }
        }
        resetSerial();
        coiotBound = false;
    }

    public void dispose() {
        stop();
    }

    private static String completeUrl(String ipAddress, int port, String uri) {
        return "coap://" + ipAddress + ":" + port + uri;
    }
}
