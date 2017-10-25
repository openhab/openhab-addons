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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.wso2iots.wso2iotsBindingConstants;
import org.openhab.binding.wso2iots.internal.wso2iotsConfiguration;
import org.openhab.binding.wso2iots.internal.jsonHumidity.wso2iotsResponseHumidity;
import org.openhab.binding.wso2iots.internal.jsonLight.wso2iotsResponseLight;
import org.openhab.binding.wso2iots.internal.jsonMotion.wso2iotsResponseMotion;
import org.openhab.binding.wso2iots.internal.jsonTemp.wso2iotsResponseTemp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link wso2iotsHandler} is responsible for handling commands, which are
 * sent to the channels.
 *
 * @author Ramesha Karunasena - Initial contribution
 */

public class wso2iotsHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(wso2iotsHandler.class);

    private static final int DEFAULT_REFRESH_PERIOD = 60;
    
    private static final int PERIOD = 200;

    private ScheduledFuture<?> refreshJob;

    private BigDecimal iotsResponseTemp;

    private BigDecimal iotsResponseLight;

    private BigDecimal iotsResponseMotion;

    private BigDecimal iotsResponseHumidity;

    private final Gson gson;

    public wso2iotsHandler(Thing thing) {
        super(thing);
        gson = new Gson();
    }

    @Override
    public void initialize() {
        
        logger.debug("Initializing wso2iots handler.");

        wso2iotsConfiguration config = getConfigAs(wso2iotsConfiguration.class);
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
        if (config.refresh != null && config.refresh < 2) {
            errorMsg = "Parameter 'refresh' must be at least 2 minutes";
            validConfig = false;
        }

        if (validConfig) {
            updateStatus(ThingStatus.OFFLINE);
            startAutomaticRefresh();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
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
        if (command instanceof RefreshType) {
            if (channelUID.getId().equals(wso2iotsBindingConstants.TEMPERATURE)) {
                updateChannel(channelUID.getId(), iotsResponseTemp);
            }
            if (channelUID.getId().equals(wso2iotsBindingConstants.LIGHT)) {
                updateChannel(channelUID.getId(), iotsResponseLight);
            }
            if (channelUID.getId().equals(wso2iotsBindingConstants.MOTION)) {
                updateChannel(channelUID.getId(), iotsResponseMotion);
            }
            if (channelUID.getId().equals(wso2iotsBindingConstants.HUMIDITY)) {
                updateChannel(channelUID.getId(), iotsResponseHumidity);
            }
        } else {
            logger.debug("Can not handle command {}", command);
        }
    }

    /**
     * Start the job refreshing the building monitor data
     */
    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        // Request new data
                        iotsResponseTemp = updateBuildingMonitorData(wso2iotsBindingConstants.TEMPERATURE);
                        iotsResponseLight = updateBuildingMonitorData(wso2iotsBindingConstants.LIGHT);
                        iotsResponseMotion = updateBuildingMonitorData(wso2iotsBindingConstants.MOTION);
                        iotsResponseHumidity = updateBuildingMonitorData(wso2iotsBindingConstants.HUMIDITY);

                        // Update all channels from the updated data
                        updateChannel(wso2iotsBindingConstants.TEMPERATURE, iotsResponseTemp);
                        updateChannel(wso2iotsBindingConstants.LIGHT, iotsResponseLight);
                        updateChannel(wso2iotsBindingConstants.MOTION, iotsResponseMotion);
                        updateChannel(wso2iotsBindingConstants.HUMIDITY, iotsResponseHumidity);

                    } catch (Exception e) {
                        logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                    }
                }
            };

            wso2iotsConfiguration config = getConfigAs(wso2iotsConfiguration.class);
            int delay = (config.refresh != null) ? config.refresh.intValue() : DEFAULT_REFRESH_PERIOD;
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
            if (iotsResponse == null) {
                state = UnDefType.UNDEF;
            } else if (iotsResponse instanceof BigDecimal) {
                state = new DecimalType(iotsResponse);

            } else {
                logger.warn("Update channel {}: Unsupported value type {}", channelId,
                        iotsResponse.getClass().getSimpleName());
            }
            logger.debug("Update channel {} with state {} ({})", channelId, (state == null) ? "null" : state.toString(),
                    (iotsResponse == null) ? "null" : iotsResponse.getClass().getSimpleName());

            // Update the channel
            if (state != null) {
                updateState(channelId, state);
            }
        }
    }

    private BigDecimal updateBuildingMonitorData(String sensorType) throws Exception {

        BigDecimal iotsResponse = null;
        String errormsg = null;
        String response1 = null;
        wso2iotsConfiguration config = getConfigAs(wso2iotsConfiguration.class);
        String Access_Token = config.apikey;
        String id = config.deviceId;
        long time = System.currentTimeMillis() / 1000;
        String fromTime = String.valueOf(time - PERIOD);
        String toTime = String.valueOf(time);

        String url = "https://localhost:8243/senseme/device/1.0.0/stats/";
        url = url + id + "?from=" + fromTime + "&to=" + toTime + "&sensorType=" + sensorType;

        URL obj;
        
        try {
        
            obj = this.validateCertificate(url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + Access_Token);

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

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

                if (sensorType == wso2iotsBindingConstants.TEMPERATURE) {

                    wso2iotsResponseTemp result = gson.fromJson(segment, wso2iotsResponseTemp.class);

                    iotsResponse = result.getvalues().getData();
                }

                else if (sensorType == wso2iotsBindingConstants.HUMIDITY) {

                    wso2iotsResponseHumidity result = gson.fromJson(segment, wso2iotsResponseHumidity.class);

                    iotsResponse = result.getvalues().getData();
                }

                else if (sensorType == wso2iotsBindingConstants.LIGHT) {

                    wso2iotsResponseLight result = gson.fromJson(segment, wso2iotsResponseLight.class);

                    iotsResponse = result.getvalues().getData();
                }

                else if (sensorType == wso2iotsBindingConstants.MOTION) {

                    wso2iotsResponseMotion result = gson.fromJson(segment, wso2iotsResponseMotion.class);

                    iotsResponse = result.getvalues().getData();
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

}
