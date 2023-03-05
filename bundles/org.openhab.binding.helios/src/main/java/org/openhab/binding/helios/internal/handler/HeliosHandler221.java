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
package org.openhab.binding.helios.internal.handler;

import static org.openhab.binding.helios.internal.HeliosBindingConstants.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.openhab.binding.helios.internal.ws.rest.RESTError;
import org.openhab.binding.helios.internal.ws.rest.RESTEvent;
import org.openhab.binding.helios.internal.ws.rest.RESTPort;
import org.openhab.binding.helios.internal.ws.rest.RESTSubscribeResponse;
import org.openhab.binding.helios.internal.ws.rest.RESTSwitch;
import org.openhab.binding.helios.internal.ws.rest.RESTSystemInfo;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link HeliosHandler221} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */

public class HeliosHandler221 extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HeliosHandler221.class);

    // List of Configuration constants
    public static final String IP_ADDRESS = "ipAddress";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    // List of all REST API URI, commands, and JSON constants
    public static final String BASE_URI = "https://{ip}/api/";
    public static final String SYSTEM_PATH = "system/{cmd}";
    public static final String FIRMWARE_PATH = "firmware/{cmd}";
    public static final String LOG_PATH = "log/{cmd}";
    public static final String SWITCH_PATH = "switch/{cmd}";
    public static final String PORT_PATH = "io/{cmd}";

    public static final String INFO = "info";
    public static final String STATUS = "status";

    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";
    public static final String PULL = "pull";
    public static final String CAPABILITIES = "caps";
    public static final String CONTROL = "ctrl";

    public static final String DEVICESTATE = "DeviceState";
    public static final String AUDIOLOOPTEST = "AudioLoopTest";
    public static final String MOTIONDETECTED = "MotionDetected";
    public static final String NOISEDETECTED = "NoiseDetected";
    public static final String KEYPRESSED = "KeyPressed";
    public static final String KEYRELEASED = "KeyReleased";
    public static final String CODEENTERED = "CodeEntered";
    public static final String CARDENTERED = "CardEntered";
    public static final String INPUTCHANGED = "InputChanged";
    public static final String OUTPUTCHANGED = "OutputChanged";
    public static final String CALLSTATECHANGED = "CallStateChanged";
    public static final String REGISTRATIONSTATECHANGED = "RegistrationStateChanged";
    public static final String SWITCHSTATECHANGED = "SwitchStateChanged";

    // REST Client API variables
    private Client heliosClient;
    private WebTarget baseTarget;
    private WebTarget systemTarget;
    private WebTarget logTarget;
    private WebTarget switchTarget;
    private WebTarget portTarget;
    private String ipAddress;

    // JSON variables
    private Gson gson = new Gson();

    private ScheduledFuture<?> logJob;
    private static final long RESET_INTERVAL = 15;
    private static final long HELIOS_DURATION = 120;
    private static final long HELIOS_PULL_DURATION = 10;

    private long logSubscriptionID = 0;

    public HeliosHandler221(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing the Helios IP Vario handler for '{}'.", getThing().getUID().toString());

        ipAddress = (String) getConfig().get(IP_ADDRESS);
        String username = (String) getConfig().get(USERNAME);
        String password = (String) getConfig().get(PASSWORD);

        if (ipAddress != null && !ipAddress.isEmpty() && username != null && !username.isEmpty() && password != null
                && !password.isEmpty()) {
            SecureRestClientTrustManager secureRestClientTrustManager = new SecureRestClientTrustManager();
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("SSL");
            } catch (NoSuchAlgorithmException e1) {
                logger.error("An exception occurred while requesting the SSL encryption algorithm : '{}'",
                        e1.getMessage(), e1);
            }
            try {
                if (sslContext != null) {
                    sslContext.init(null, new javax.net.ssl.TrustManager[] { secureRestClientTrustManager }, null);
                }
            } catch (KeyManagementException e1) {
                logger.error("An exception occurred while initialising the SSL context : '{}'", e1.getMessage(), e1);
            }

            heliosClient = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                    return true;
                }
            }).build();
            heliosClient.register(new Authenticator(username, password));

            baseTarget = heliosClient.target(BASE_URI);
            systemTarget = baseTarget.path(SYSTEM_PATH);
            logTarget = baseTarget.path(LOG_PATH);
            switchTarget = baseTarget.path(SWITCH_PATH);

            Response response = null;
            try {
                response = systemTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", INFO)
                        .request(MediaType.APPLICATION_JSON_TYPE).get();
            } catch (NullPointerException e) {
                logger.debug("An exception occurred while fetching system info of the Helios IP Vario '{}' : '{}'",
                        getThing().getUID().toString(), e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return;
            }

            if (response == null) {
                logger.debug("There is a configuration problem for the Helios IP Vario '{}'",
                        getThing().getUID().toString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return;
            }

            JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();

            if (logger.isTraceEnabled()) {
                logger.trace("initialize() Request : {}", systemTarget.resolveTemplate("ip", ipAddress)
                        .resolveTemplate("cmd", INFO).getUri().toASCIIString());
                if (jsonObject.get("success").toString().equals("true")) {
                    logger.trace("initialize() Response: {}", jsonObject.get("result"));
                }
                if (jsonObject.get("success").toString().equals("false")) {
                    logger.trace("initialize() Response: {}", jsonObject.get("error"));
                }
            }

            if (jsonObject.get("success").toString().equals("false")) {
                RESTError error = gson.fromJson(jsonObject.get("error").toString(), RESTError.class);
                logger.debug(
                        "An error occurred while communicating with the Helios IP Vario '{}': code '{}', param '{}' : '{}'",
                        new Object[] { getThing().getUID().toString(), error.code, error.param, error.description });
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        error.code + ":" + error.param + ":" + error.description);
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return;
            }

            if (jsonObject.get("success").toString().equals("true")) {
                if (logJob == null || logJob.isCancelled()) {
                    logJob = scheduler.scheduleWithFixedDelay(logRunnable, 0, 1, TimeUnit.SECONDS);
                }

                updateStatus(ThingStatus.ONLINE);

                scheduler.schedule(configureRunnable, 0, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Helios IP Vario handler for '{}'.", getThing().getUID().toString());

        if (logSubscriptionID != 0) {
            unsubscribe();
        }

        if (logJob != null && !logJob.isCancelled()) {
            logJob.cancel(true);
            logJob = null;
        }

        if (heliosClient != null) {
            heliosClient.close();
            heliosClient = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            ChannelTypeUID triggerUID = new ChannelTypeUID(BINDING_ID, SWITCH_TRIGGER);
            ChannelTypeUID enablerUID = new ChannelTypeUID(BINDING_ID, SWITCH_ENABLER);
            Channel theChannel = getThing().getChannel(channelUID.getId());

            if (theChannel != null) {
                ChannelTypeUID channelType = theChannel.getChannelTypeUID();
                if (channelType.equals(triggerUID)) {
                    String switchID = channelUID.getId().substring(6);
                    triggerSwitch(switchID);
                }

                if (channelType.equals(enablerUID)) {
                    String switchID = channelUID.getId().substring(6, channelUID.getId().lastIndexOf("active"));
                    if (command instanceof OnOffType && command == OnOffType.OFF) {
                        enableSwitch(switchID, false);
                    } else if (command instanceof OnOffType && command == OnOffType.ON) {
                        enableSwitch(switchID, true);
                    }
                }
            }
        }
    }

    private long subscribe() {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            logTarget = baseTarget.path(LOG_PATH);

            Response response = null;
            try {
                response = logTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", SUBSCRIBE)
                        .queryParam("include", "new").queryParam("duration", HELIOS_DURATION)
                        .request(MediaType.APPLICATION_JSON_TYPE).get();
            } catch (NullPointerException e) {
                logger.debug(
                        "An exception occurred while subscribing to the log entries of the Helios IP Vario '{}' : '{}'",
                        getThing().getUID().toString(), e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return 0;
            }

            if (response != null) {
                JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();

                if (logger.isTraceEnabled()) {
                    logger.trace("subscribe() Request : {}",
                            logTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", SUBSCRIBE)
                                    .queryParam("include", "new").queryParam("duration", HELIOS_DURATION).getUri()
                                    .toASCIIString());
                    if (jsonObject.get("success").toString().equals("true")) {
                        logger.trace("subscribe() Response: {}", jsonObject.get("result"));
                    }
                    if (jsonObject.get("success").toString().equals("false")) {
                        logger.trace("subscribe() Response: {}", jsonObject.get("error"));
                    }
                }

                if (jsonObject.get("success").toString().equals("true")) {
                    RESTSubscribeResponse subscribeResponse = gson.fromJson(jsonObject.get("result").toString(),
                            RESTSubscribeResponse.class);
                    logger.debug("The subscription id to pull logs from the Helios IP Vario '{}' is '{}'",
                            getThing().getUID().toString(), subscribeResponse.id);
                    return subscribeResponse.id;
                } else {
                    RESTError error = gson.fromJson(jsonObject.get("error").toString(), RESTError.class);
                    logger.debug(
                            "An error occurred while communicating with the Helios IP Vario '{}': code '{}', param '{}' : '{}'",
                            getThing().getUID().toString(), error.code, error.param, error.description);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            error.code + ":" + error.param + ":" + error.description);
                    scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                    return 0;
                }
            } else {
                logger.debug("An error occurred while subscribing to the log entries of the Helios IP Vario '{}'",
                        getThing().getUID().toString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return 0;
            }
        }

        return 0;
    }

    private void unsubscribe() {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            logTarget = baseTarget.path(LOG_PATH);

            Response response = null;
            try {
                response = logTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", UNSUBSCRIBE)
                        .queryParam("id", logSubscriptionID).request(MediaType.APPLICATION_JSON_TYPE).get();
            } catch (Exception e) {
                logger.debug(
                        "An exception occurred while unsubscribing from the log entries of the Helios IP Vario '{}' : {}",
                        getThing().getUID().toString(), e.getMessage(), e);
                logSubscriptionID = 0;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return;
            }

            if (response != null) {
                JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();

                if (logger.isTraceEnabled()) {
                    logger.trace("unsubscribe() Request : {}",
                            logTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", UNSUBSCRIBE)
                                    .queryParam("id", logSubscriptionID).getUri().toASCIIString());
                    if (jsonObject.get("success").toString().equals("true")) {
                        logger.trace("unsubscribe() Response: {}", jsonObject.get("result"));
                    }
                    if (jsonObject.get("success").toString().equals("false")) {
                        logger.trace("unsubscribe() Response: {}", jsonObject.get("error"));
                    }
                }

                if (jsonObject.get("success").toString().equals("true")) {
                    logger.debug("Successfully unsubscribed from the log entries of the Helios IP Vario '{}'",
                            getThing().getUID().toString());
                } else {
                    RESTError error = gson.fromJson(jsonObject.get("error").toString(), RESTError.class);
                    logger.debug(
                            "An error occurred while communicating with the Helios IP Vario '{}' : code '{}', param '{}' : '{}'",
                            getThing().getUID().toString(), error.code, error.param, error.description);
                    logSubscriptionID = 0;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            error.code + ":" + error.param + ":" + error.description);
                    scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                    return;
                }
            } else {
                logger.debug("An error occurred while unsubscribing from the log entries of the Helios IP Vario '{}'",
                        getThing().getUID().toString());
                logSubscriptionID = 0;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return;
            }
        }
    }

    private List<RESTEvent> pullLog(long logSubscriptionID) {
        if (getThing().getStatus() == ThingStatus.ONLINE && heliosClient != null) {
            logTarget = baseTarget.path(LOG_PATH);

            Response response = null;
            try {
                long now = System.currentTimeMillis();
                response = logTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", PULL)
                        .queryParam("id", logSubscriptionID).queryParam("timeout", HELIOS_PULL_DURATION)
                        .request(MediaType.APPLICATION_JSON_TYPE).get();
                logger.trace("Pulled logs in {} millseconds from {}", System.currentTimeMillis() - now,
                        getThing().getUID());
            } catch (NullPointerException e) {
                logger.debug("An exception occurred while pulling log entries from the Helios IP Vario '{}' : '{}'",
                        getThing().getUID().toString(), e.getMessage(), e);
                this.logSubscriptionID = 0;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return null;
            }

            if (response != null) {
                JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();

                if (logger.isTraceEnabled()) {
                    logger.trace("pullLog() Request : {}",
                            logTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", PULL)
                                    .queryParam("id", logSubscriptionID).queryParam("timeout", HELIOS_PULL_DURATION)
                                    .getUri().toASCIIString());
                    if (jsonObject.get("success").toString().equals("true")) {
                        logger.trace("pullLog() Response: {}", jsonObject.get("result"));
                    }
                    if (jsonObject.get("success").toString().equals("false")) {
                        logger.trace("pullLog() Response: {}", jsonObject.get("error"));
                    }
                }

                if (jsonObject.get("success").toString().equals("true")) {
                    logger.trace("Successfully pulled log entries from the Helios IP Vario '{}'",
                            getThing().getUID().toString());
                    JsonObject js = (JsonObject) jsonObject.get("result");
                    RESTEvent[] eventArray = gson.fromJson(js.getAsJsonArray("events"), RESTEvent[].class);
                    return Arrays.asList(eventArray);
                } else {
                    RESTError error = gson.fromJson(jsonObject.get("error").toString(), RESTError.class);
                    logger.debug(
                            "An error occurred while communicating with the Helios IP Vario '{}' : code '{}', param '{}' : '{}'",
                            getThing().getUID().toString(), error.code, error.param, error.description);
                    this.logSubscriptionID = 0;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            error.code + ":" + error.param + ":" + error.description);
                    scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                    return null;
                }
            } else {
                logger.debug("An error occurred while polling log entries from the Helios IP Vario '{}'",
                        getThing().getUID().toString());
                this.logSubscriptionID = 0;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return null;
            }
        }

        return null;
    }

    private List<RESTSwitch> getSwitches() {
        switchTarget = baseTarget.path(SWITCH_PATH);

        Response response = null;
        try {
            response = switchTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", CAPABILITIES)
                    .request(MediaType.APPLICATION_JSON_TYPE).get();
        } catch (NullPointerException e) {
            logger.debug(
                    "An exception occurred while requesting switch capabilities from the Helios IP Vario '{}' : '{}'",
                    getThing().getUID().toString(), e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
            return null;
        }

        if (response != null) {
            JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();

            if (logger.isTraceEnabled()) {
                logger.trace("getSwitches() Request : {}", switchTarget.resolveTemplate("ip", ipAddress)
                        .resolveTemplate("cmd", CAPABILITIES).getUri().toASCIIString());
                if (jsonObject.get("success").toString().equals("true")) {
                    logger.trace("getSwitches() Response: {}", jsonObject.get("result"));
                }
                if (jsonObject.get("success").toString().equals("false")) {
                    logger.trace("getSwitches() Response: {}", jsonObject.get("error"));
                }
            }

            if (jsonObject.get("success").toString().equals("true")) {
                logger.debug("Successfully requested switch capabilities from the Helios IP Vario '{}'",
                        getThing().getUID().toString());
                String result = jsonObject.get("result").toString();
                result = result.replace("switch", "id");
                JsonObject js = JsonParser.parseString(result).getAsJsonObject();
                RESTSwitch[] switchArray = gson.fromJson(js.getAsJsonArray("ides"), RESTSwitch[].class);
                if (switchArray != null) {
                    return Arrays.asList(switchArray);
                }
            } else {
                RESTError error = gson.fromJson(jsonObject.get("error").toString(), RESTError.class);
                logger.debug(
                        "An error occurred while communicating with the Helios IP Vario '{}' : code '{}', param '{}' : '{}'",
                        getThing().getUID().toString(), error.code, error.param, error.description);
                if ("8".equals(error.code)) {
                    logger.debug(
                            "The API is not supported by the Helios hardware or current license, or the Authentication method is not set to Basic");
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            error.code + ":" + error.param + ":" + error.description);
                    scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                }
                return null;
            }
        } else {
            logger.debug("An error occurred while requesting switch capabilities from the Helios IP Vario '{}'",
                    getThing().getUID().toString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
        }

        return null;
    }

    private void triggerSwitch(String id) {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            switchTarget = baseTarget.path(SWITCH_PATH);

            Response response = null;
            try {
                response = switchTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", CONTROL)
                        .queryParam("switch", id).queryParam("action", "trigger")
                        .request(MediaType.APPLICATION_JSON_TYPE).get();
            } catch (NullPointerException e) {
                logger.debug("An exception occurred while triggering a switch  on the Helios IP Vario '{}' : '{}'",
                        getThing().getUID().toString(), e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return;
            }

            if (response != null) {
                JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();

                if (logger.isTraceEnabled()) {
                    logger.trace("triggerSwitch() Request : {}",
                            switchTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", CONTROL)
                                    .queryParam("switch", id).queryParam("action", "trigger").getUri().toASCIIString());
                    if (jsonObject.get("success").toString().equals("true")) {
                        logger.trace("triggerSwitch() Response: {}", jsonObject.get("result"));
                    }
                    if (jsonObject.get("success").toString().equals("false")) {
                        logger.trace("triggerSwitch() Response: {}", jsonObject.get("error"));
                    }
                }

                if (jsonObject.get("success").toString().equals("true")) {
                    logger.debug("Successfully triggered a switch on the Helios IP Vario '{}'",
                            getThing().getUID().toString());
                } else {
                    RESTError error = gson.fromJson(jsonObject.get("error").toString(), RESTError.class);
                    logger.error(
                            "An error occurred while communicating with the Helios IP Vario '{}' : code '{}', param '{}' : '{}'",
                            getThing().getUID().toString(), error.code, error.param, error.description);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            error.code + ":" + error.param + ":" + error.description);
                    scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                    return;
                }
            } else {
                logger.warn("An error occurred while triggering a switch on the Helios IP Vario '{}'",
                        getThing().getUID().toString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return;
            }
        }
    }

    private void enableSwitch(String id, boolean flag) {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            switchTarget = baseTarget.path(SWITCH_PATH);

            Response response = null;
            try {
                response = switchTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", CONTROL)
                        .queryParam("switch", id).queryParam("action", flag ? "on" : "off")
                        .request(MediaType.APPLICATION_JSON_TYPE).get();
            } catch (NullPointerException e) {
                logger.error("An exception occurred while dis/enabling a switch  on the Helios IP Vario '{}' : '{}'",
                        getThing().getUID().toString(), e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return;
            }

            if (response != null) {
                JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();

                if (logger.isTraceEnabled()) {
                    logger.trace("enableSwitch() Request : {}",
                            switchTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", CONTROL)
                                    .queryParam("switch", id).queryParam("action", flag ? "on" : "off").getUri()
                                    .toASCIIString());
                    if (jsonObject.get("success").toString().equals("true")) {
                        logger.trace("enableSwitch() Response: {}", jsonObject.get("result"));
                    }
                    if (jsonObject.get("success").toString().equals("false")) {
                        logger.trace("enableSwitch() Response: {}", jsonObject.get("error"));
                    }
                }

                if (jsonObject.get("success").toString().equals("true")) {
                    logger.debug("Successfully dis/enabled a  switch on the Helios IP Vario '{}'",
                            getThing().getUID().toString());
                } else {
                    RESTError error = gson.fromJson(jsonObject.get("error").toString(), RESTError.class);
                    logger.error(
                            "An error occurred while communicating with the Helios IP Vario '{}': code '{}', param '{}' : '{}'",
                            getThing().getUID().toString(), error.code, error.param, error.description);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            error.code + ":" + error.param + ":" + error.description);
                    scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                    return;
                }
            } else {
                logger.warn("An error occurred while dis/enabling a switch on the Helios IP Vario '{}'",
                        getThing().getUID().toString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
                scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                return;
            }
        }
    }

    private List<RESTPort> getPorts() {
        portTarget = baseTarget.path(PORT_PATH);

        Response response = null;
        try {
            response = portTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", CAPABILITIES)
                    .request(MediaType.APPLICATION_JSON_TYPE).get();
        } catch (NullPointerException e) {
            logger.error(
                    "An exception occurred while requesting port capabilities from the Helios IP Vario '{}' : '{}'",
                    getThing().getUID().toString(), e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
            return null;
        }

        if (response != null) {
            JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();

            if (logger.isTraceEnabled()) {
                logger.trace("getPorts() Request : {}", portTarget.resolveTemplate("ip", ipAddress)
                        .resolveTemplate("cmd", CAPABILITIES).getUri().toASCIIString());
                if (jsonObject.get("success").toString().equals("true")) {
                    logger.trace("getPorts() Response: {}", jsonObject.get("result"));
                }
                if (jsonObject.get("success").toString().equals("false")) {
                    logger.trace("getPorts() Response: {}", jsonObject.get("error"));
                }
            }

            if (jsonObject.get("success").toString().equals("true")) {
                logger.debug("Successfully requested port capabilities from the Helios IP Vario '{}'",
                        getThing().getUID().toString());
                JsonObject js = (JsonObject) jsonObject.get("result");
                RESTPort[] portArray = gson.fromJson(js.getAsJsonArray("ports"), RESTPort[].class);
                if (portArray != null) {
                    return Arrays.asList(portArray);
                }
            } else {
                RESTError error = gson.fromJson(jsonObject.get("error").toString(), RESTError.class);
                logger.error(
                        "An error occurred while communicating with the Helios IP Vario '{}': code '{}', param '{}' : '{}'",
                        getThing().getUID().toString(), error.code, error.param, error.description);
                if ("8".equals(error.code)) {
                    logger.debug(
                            "The API is not supported by the Helios hardware or current license, or the Authentication method is not set to Basic");
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            error.code + ":" + error.param + ":" + error.description);
                    scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
                }
                return null;
            }
        } else {
            logger.warn("An error occurred while requesting port capabilities from the Helios IP Vario '{}'",
                    getThing().getUID().toString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
        }

        return null;
    }

    protected Runnable resetRunnable = () -> {
        logger.debug("Resetting the Helios IP Vario handler for '{}'", getThing().getUID());
        dispose();
        initialize();
    };

    protected Runnable configureRunnable = () -> {
        logger.debug("Fetching the configuration of the Helios IP Vario '{}' ", getThing().getUID().toString());

        Response response = null;
        try {
            response = systemTarget.resolveTemplate("ip", ipAddress).resolveTemplate("cmd", INFO)
                    .request(MediaType.APPLICATION_JSON_TYPE).get();
        } catch (NullPointerException e) {
            logger.error("An exception occurred while fetching system info of the Helios IP Vario '{}' : '{}'",
                    getThing().getUID().toString(), e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            scheduler.schedule(resetRunnable, RESET_INTERVAL, TimeUnit.SECONDS);
            return;
        }

        if (response != null) {
            JsonObject jsonObject = JsonParser.parseString(response.readEntity(String.class)).getAsJsonObject();

            if (logger.isTraceEnabled()) {
                logger.trace("configureRunnable Request : {}", systemTarget.resolveTemplate("ip", ipAddress)
                        .resolveTemplate("cmd", INFO).getUri().toASCIIString());
                if (jsonObject.get("success").toString().equals("true")) {
                    logger.trace("configureRunnable Response: {}", jsonObject.get("result"));
                }
                if (jsonObject.get("success").toString().equals("false")) {
                    logger.trace("configureRunnable Response: {}", jsonObject.get("error"));
                }
            }

            RESTSystemInfo systemInfo = gson.fromJson(jsonObject.get("result").toString(), RESTSystemInfo.class);

            Map<String, String> properties = editProperties();
            properties.put(VARIANT, systemInfo.variant);
            properties.put(SERIAL_NUMBER, systemInfo.serialNumber);
            properties.put(HW_VERSION, systemInfo.hwVersion);
            properties.put(SW_VERSION, systemInfo.swVersion);
            properties.put(BUILD_TYPE, systemInfo.buildType);
            properties.put(DEVICE_NAME, systemInfo.deviceName);
            updateProperties(properties);
        }

        List<RESTSwitch> switches = getSwitches();

        if (switches != null) {
            for (RESTSwitch aSwitch : switches) {
                if (aSwitch.enabled.equals("true")) {
                    logger.debug("Adding a channel to the Helios IP Vario '{}' for the switch with id '{}'",
                            getThing().getUID().toString(), aSwitch.id);
                    ThingBuilder thingBuilder = editThing();
                    ChannelTypeUID enablerUID = new ChannelTypeUID(BINDING_ID, SWITCH_ENABLER);
                    ChannelTypeUID triggerUID = new ChannelTypeUID(BINDING_ID, SWITCH_TRIGGER);

                    Channel channel = ChannelBuilder
                            .create(new ChannelUID(getThing().getUID(), "switch" + aSwitch.id + "active"), "Switch")
                            .withType(enablerUID).build();
                    thingBuilder.withChannel(channel);
                    channel = ChannelBuilder
                            .create(new ChannelUID(getThing().getUID(), "switch" + aSwitch.id), "Switch")
                            .withType(triggerUID).build();
                    thingBuilder.withChannel(channel);
                    updateThing(thingBuilder.build());
                }
            }
        }

        List<RESTPort> ports = getPorts();

        if (ports != null) {
            for (RESTPort aPort : ports) {
                logger.debug("Adding a channel to the Helios IP Vario '{}' for the IO port with id '{}'",
                        getThing().getUID().toString(), aPort.port);
                ThingBuilder thingBuilder = editThing();
                ChannelTypeUID triggerUID = new ChannelTypeUID(BINDING_ID, IO_TRIGGER);

                Map<String, String> channelProperties = new HashMap<>();
                channelProperties.put("type", aPort.type);

                Channel channel = ChannelBuilder
                        .create(new ChannelUID(getThing().getUID(), "io" + aPort.port), "Switch").withType(triggerUID)
                        .withProperties(channelProperties).build();
                thingBuilder.withChannel(channel);
                updateThing(thingBuilder.build());
            }
        }
    };

    protected Runnable logRunnable = () -> {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            if (logSubscriptionID == 0) {
                logSubscriptionID = subscribe();
            }

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

            while (logSubscriptionID != 0) {
                try {
                    List<RESTEvent> events = pullLog(logSubscriptionID);

                    if (events != null) {
                        for (RESTEvent event : events) {
                            Date date = new Date(Long.valueOf(event.utcTime));
                            DateTimeType stampType = new DateTimeType(dateFormatter.format(date));

                            logger.debug("Received the event for Helios IP Vario '{}' with ID '{}' of type '{}' on {}",
                                    getThing().getUID().toString(), event.id, event.event, dateFormatter.format(date));

                            switch (event.event) {
                                case DEVICESTATE: {
                                    StringType valueType = new StringType(event.params.get("state").getAsString());
                                    updateState(DEVICE_STATE, valueType);
                                    updateState(DEVICE_STATE_STAMP, stampType);
                                    break;
                                }
                                case AUDIOLOOPTEST: {
                                    if (event.params.get("result").getAsString().equals("passed")) {
                                        updateState(AUDIO_LOOP_TEST, OnOffType.ON);
                                    } else if (event.params.get("result").getAsString().equals("failed")) {
                                        updateState(AUDIO_LOOP_TEST, OnOffType.OFF);
                                    } else {
                                        updateState(AUDIO_LOOP_TEST, UnDefType.UNDEF);
                                    }

                                    updateState(AUDIO_LOOP_TEST_STAMP, stampType);
                                    break;
                                }
                                case MOTIONDETECTED: {
                                    if (event.params.get("state").getAsString().equals("in")) {
                                        updateState(MOTION, OnOffType.ON);
                                    } else if (event.params.get("state").getAsString().equals("out")) {
                                        updateState(MOTION, OnOffType.OFF);
                                    } else {
                                        updateState(MOTION, UnDefType.UNDEF);
                                    }

                                    updateState(MOTION_STAMP, stampType);
                                    break;
                                }
                                case NOISEDETECTED: {
                                    if (event.params.get("state").getAsString().equals("in")) {
                                        updateState(NOISE, OnOffType.ON);
                                    } else if (event.params.get("state").getAsString().equals("out")) {
                                        updateState(NOISE, OnOffType.OFF);
                                    } else {
                                        updateState(NOISE, UnDefType.UNDEF);
                                    }

                                    updateState(NOISE_STAMP, stampType);
                                    break;
                                }
                                case KEYPRESSED: {
                                    triggerChannel(KEY_PRESSED, event.params.get("key").getAsString());

                                    updateState(KEY_PRESSED_STAMP, stampType);
                                    break;
                                }
                                case KEYRELEASED: {
                                    triggerChannel(KEY_RELEASED, event.params.get("key").getAsString());

                                    updateState(KEY_RELEASED_STAMP, stampType);
                                    break;
                                }
                                case CODEENTERED: {
                                    triggerChannel(CODE, event.params.get("code").getAsString());

                                    if (event.params.get("valid").getAsString().equals("true")) {
                                        updateState(CODE_VALID, OnOffType.ON);
                                    } else if (event.params.get("valid").getAsString().equals("false")) {
                                        updateState(CODE_VALID, OnOffType.OFF);
                                    } else {
                                        updateState(CODE_VALID, UnDefType.UNDEF);
                                    }

                                    updateState(CODE_STAMP, stampType);
                                    break;
                                }
                                case CARDENTERED: {
                                    triggerChannel(CARD, event.params.get("uid").getAsString());

                                    if (event.params.get("valid").getAsString().equals("true")) {
                                        updateState(CARD_VALID, OnOffType.ON);
                                    } else if (event.params.get("valid").getAsString().equals("false")) {
                                        updateState(CARD_VALID, OnOffType.OFF);
                                    } else {
                                        updateState(CARD_VALID, UnDefType.UNDEF);
                                    }

                                    updateState(CARD_STAMP, stampType);
                                    break;
                                }
                                case INPUTCHANGED: {
                                    ChannelUID inputChannel = new ChannelUID(getThing().getUID(),
                                            "io" + event.params.get("port").getAsString());

                                    if (event.params.get("state").getAsString().equals("true")) {
                                        updateState(inputChannel, OnOffType.ON);
                                    } else if (event.params.get("state").getAsString().equals("false")) {
                                        updateState(inputChannel, OnOffType.OFF);
                                    } else {
                                        updateState(inputChannel, UnDefType.UNDEF);
                                    }
                                    break;
                                }
                                case OUTPUTCHANGED: {
                                    ChannelUID inputChannel = new ChannelUID(getThing().getUID(),
                                            "io" + event.params.get("port").getAsString());

                                    if (event.params.get("state").getAsString().equals("true")) {
                                        updateState(inputChannel, OnOffType.ON);
                                    } else if (event.params.get("state").getAsString().equals("false")) {
                                        updateState(inputChannel, OnOffType.OFF);
                                    } else {
                                        updateState(inputChannel, UnDefType.UNDEF);
                                    }
                                    break;
                                }
                                case CALLSTATECHANGED: {
                                    StringType valueType = new StringType(event.params.get("state").getAsString());
                                    updateState(CALL_STATE, valueType);

                                    valueType = new StringType(event.params.get("direction").getAsString());
                                    updateState(CALL_DIRECTION, valueType);

                                    updateState(CALL_STATE_STAMP, stampType);
                                    break;
                                }
                                case REGISTRATIONSTATECHANGED: {
                                    break;
                                }
                                case SWITCHSTATECHANGED: {
                                    if (event.params.get("state").getAsString().equals("true")) {
                                        updateState(SWITCH_STATE, OnOffType.ON);
                                    } else if (event.params.get("state").getAsString().equals("false")) {
                                        updateState(SWITCH_STATE, OnOffType.OFF);
                                    } else {
                                        updateState(SWITCH_STATE, UnDefType.UNDEF);
                                    }

                                    if (event.params.get("originator") != null) {
                                        StringType originatorType = new StringType(
                                                event.params.get("originator").getAsString());
                                        updateState(SWITCH_STATE_ORIGINATOR, originatorType);
                                    }

                                    DecimalType switchType = new DecimalType(event.params.get("switch").getAsString());
                                    updateState(SWITCH_STATE_SWITCH, switchType);

                                    updateState(SWITCH_STATE_STAMP, stampType);
                                    break;
                                }
                                default: {
                                    logger.debug("Unrecognised event type : '{}'", event.event);
                                    Set<Map.Entry<String, JsonElement>> entrySet = event.params.entrySet();
                                    for (Map.Entry<String, JsonElement> entry : entrySet) {
                                        logger.debug("Key '{}', Value '{}'", entry.getKey(),
                                                event.params.get(entry.getKey()).getAsString().replace("\"", ""));
                                    }
                                }
                            }
                        }
                    } else {
                        if (logger.isTraceEnabled()) {
                            logger.trace("No events were retrieved");
                        }
                    }
                } catch (Exception e) {
                    logger.error("An exception occurred while processing an event : '{}'", e.getMessage(), e);
                }
            }
        }
    };

    protected class Authenticator implements ClientRequestFilter {

        private final String user;
        private final String password;

        public Authenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            MultivaluedMap<String, Object> headers = requestContext.getHeaders();
            final String basicAuthentication = getBasicAuthentication();
            headers.add("Authorization", basicAuthentication);
        }

        private String getBasicAuthentication() {
            String token = this.user + ":" + this.password;
            return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        }
    }

    public class SecureRestClientTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        public boolean isClientTrusted(X509Certificate[] arg0) {
            return true;
        }

        public boolean isServerTrusted(X509Certificate[] arg0) {
            return true;
        }
    }
}
