/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wso2iots.handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.wso2iots.Wso2iotsBindingConstants;
import org.openhab.binding.wso2iots.internal.config.BridgeConfiguration;
import org.openhab.binding.wso2iots.internal.config.BuildingMonitorConfiguration;
import org.openhab.binding.wso2iots.internal.jsonResponse.Wso2iotsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link BuildingMonitorHandler} is responsible for handling commands, which are
 * sent to the channels.
 *
 * @author Ramesha Karunasena - Initial contribution
 */
public class BuildingMonitorHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BuildingMonitorHandler.class);

    private static final int API_RESPONSE_INTERVAL = 200;

    private ScheduledFuture<?> refreshJob;

    private BigDecimal iotsResponseTemp;

    private BigDecimal iotsResponseLight;

    private BigDecimal iotsResponseMotion;

    private BigDecimal iotsResponseHumidity;

    private final Gson gson;

    protected BridgeHandler bridgeHandler;

    public BuildingMonitorHandler(Thing thing) {
        super(thing);
        gson = new Gson();
    }

    @Override
    public void initialize() {

        logger.debug("Initializing thing handler.");

        BuildingMonitorConfiguration config = getConfigAs(BuildingMonitorConfiguration.class);
        logger.debug("config apikey = (omitted from logging)");
        logger.debug("config deviceId = {}", config.deviceId);
        logger.debug("config refresh = {}", config.refresh);

        boolean validConfig = true;
        String errorMsg = null;

        if (StringUtils.trimToNull(config.apikey) == null) {
            errorMsg = "Parameter 'apikey' is mandatory and must be configured";
            validConfig = false;
        }
        if (StringUtils.trimToNull(config.deviceId) == null) {
            errorMsg = "Parameter 'deviceId' is mandatory and must be configured";
            validConfig = false;
        }
        if (config.refresh == null) {
            errorMsg = "Parameter 'refresh' must be configured";
            validConfig = false;
        } else if (config.refresh < 2) {
            errorMsg = "Parameter 'refresh' must be at least 2 minutes";
            validConfig = false;
        }
        bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null || !bridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Bridge handler not found or not ONLINE.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);

        } else {
            BridgeConfiguration configBridge = bridgeHandler.getBridgeConfiguration();
            if (validConfig) {
                updateStatus(ThingStatus.OFFLINE);
                startAutomaticRefresh(config, configBridge);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
            }
        }
    }

    protected synchronized BridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();

        if (bridge == null) {
            logger.debug("unable to get bridge");
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof BridgeHandler) {
            return (BridgeHandler) handler;
        } else {
            logger.debug("unable to get bridge handler");
            return null;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the wso2iots handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null || !bridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
            logger.debug("Bridge handler not found or not ONLINE.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);

        } else {
            if (command instanceof RefreshType) {
                switch (channelUID.getId()) {
                    case Wso2iotsBindingConstants.TEMPERATURE:
                        updateChannel(channelUID.getId(), iotsResponseTemp);
                        break;
                    case Wso2iotsBindingConstants.LIGHT:
                        updateChannel(channelUID.getId(), iotsResponseLight);
                        break;
                    case Wso2iotsBindingConstants.MOTION:
                        updateChannel(channelUID.getId(), iotsResponseMotion);
                        break;
                    case Wso2iotsBindingConstants.HUMIDITY:
                        updateChannel(channelUID.getId(), iotsResponseHumidity);
                        break;
                }

            } else {
                logger.debug("Can not handle command {}", command);
            }
        }
    }

    /**
     * Start the job refreshing the building monitor data
     */
    private void startAutomaticRefresh(BuildingMonitorConfiguration config, BridgeConfiguration configBridge) {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                bridgeHandler = getBridgeHandler();
                if (bridgeHandler == null || !bridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                    logger.debug("Bridge handler not found or not ONLINE.");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);

                } else {
                    try {
                        // Request new data
                        iotsResponseTemp = updateBuildingMonitorData(Wso2iotsBindingConstants.TEMPERATURE, config,
                                configBridge);
                        iotsResponseLight = updateBuildingMonitorData(Wso2iotsBindingConstants.LIGHT, config,
                                configBridge);
                        iotsResponseMotion = updateBuildingMonitorData(Wso2iotsBindingConstants.MOTION, config,
                                configBridge);
                        iotsResponseHumidity = updateBuildingMonitorData(Wso2iotsBindingConstants.HUMIDITY, config,
                                configBridge);

                        // Update all channels from the updated data
                        updateChannel(Wso2iotsBindingConstants.TEMPERATURE, iotsResponseTemp);
                        updateChannel(Wso2iotsBindingConstants.LIGHT, iotsResponseLight);
                        updateChannel(Wso2iotsBindingConstants.MOTION, iotsResponseMotion);
                        updateChannel(Wso2iotsBindingConstants.HUMIDITY, iotsResponseHumidity);

                    } catch (Exception e) {
                        logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                    }
                }

            };

            int delay = config.refresh.intValue();
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.MINUTES);
        }
    }

    /**
     * Update the channel from the last building monitor data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    private void updateChannel(String channelId, BigDecimal iotsResponse) {
        if (isLinked(channelId)) {
            State state = null;
            if (iotsResponse instanceof BigDecimal) {
                state = new DecimalType(iotsResponse);
            } else {
                state = UnDefType.UNDEF;
            }
            logger.debug("Update channel {} with state {} ({})", channelId, state.toString(),
                    (iotsResponse == null) ? "null" : iotsResponse.getClass().getSimpleName());

            // Update the channel
            if (state != null) {
                updateState(channelId, state);
            }
        }
    }

    private BigDecimal updateBuildingMonitorData(String sensorType, BuildingMonitorConfiguration config,
            BridgeConfiguration configBridge) throws Exception {

        BigDecimal iotsResponse = null;
        String errormsg = null;
        String response1 = null;
        String hostname = configBridge.getHostname();
        String port = configBridge.getPort().toString();
        String Access_Token = config.apikey;
        String id = config.deviceId;
        long time = System.currentTimeMillis() / 1000;
        String fromTime = String.valueOf(time - API_RESPONSE_INTERVAL);
        String toTime = String.valueOf(time);

        String url = "https://" + hostname + ":" + port + "/senseme/device/1.0.0/stats/";
        url = url + id + "?from=" + fromTime + "&to=" + toTime + "&sensorType=" + sensorType;

        URL obj;

        obj = this.validateCertificate(url);

        if (obj == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Certificate validation failed");
            return null;
        } else {

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + Access_Token);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {

                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                response1 = response.toString();

                if (response1.equals("[]")) {
                    errormsg = "empty response from wso2 iots";
                    logger.warn(errormsg);

                } else {
                    String segment = response1.substring(response1.lastIndexOf("{"), response1.lastIndexOf("}"));
                    segment = "{\"values\":" + segment + "}";

                    logger.debug(response1);
                    logger.debug(segment);
                    // Map the JSON response to an object

                    Wso2iotsResponse result = gson.fromJson(segment, Wso2iotsResponse.class);

                    if (sensorType == Wso2iotsBindingConstants.TEMPERATURE) {

                        iotsResponse = result.getvalues().getTemperature();
                    }

                    else if (sensorType == Wso2iotsBindingConstants.HUMIDITY) {

                        iotsResponse = result.getvalues().getHumidity();
                    }

                    else if (sensorType == Wso2iotsBindingConstants.LIGHT) {

                        iotsResponse = result.getvalues().getLight();
                    }

                    else if (sensorType == Wso2iotsBindingConstants.MOTION) {

                        iotsResponse = result.getvalues().getMotion();
                    }

                }
            } catch (Exception e) {
                errormsg = e.getMessage();
                logger.warn(errormsg);
            }

            if (errormsg == null) {
                updateStatus(ThingStatus.ONLINE);
                return iotsResponse;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errormsg);
                return null;
            }
        }
    }

    private URL validateCertificate(String url2) {

        URL url = null;

        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }

        // Now can access an https URL without having the certificate in the truststore
        try {
            url = new URL(url2);
        } catch (MalformedURLException e) {
            logger.warn("Constructed url is not valid: {}", e.getMessage());
        }

        return url;
    }

    // Changes the thing state depending on the state of the bridge
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (ThingStatus.OFFLINE.equals(bridgeStatusInfo.getStatus())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            this.dispose();
        } else if (ThingStatus.ONLINE.equals(bridgeStatusInfo.getStatus())
                && ThingStatus.OFFLINE.equals(getThing().getStatusInfo().getStatus())
                && ThingStatusDetail.BRIDGE_OFFLINE.equals(getThing().getStatusInfo().getStatusDetail())) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
            this.initialize();
        }
    }

}
