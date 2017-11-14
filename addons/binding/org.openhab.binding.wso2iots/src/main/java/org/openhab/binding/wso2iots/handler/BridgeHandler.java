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
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.wso2iots.internal.config.BridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BridgeHandler} is responsible for handling commands, which are
 * sent to the channels.
 *
 * @author Ramesha Karunasena - Initial contribution
 */
public class BridgeHandler extends BaseBridgeHandler {

    private ScheduledFuture<?> refreshJob;

    private final Logger logger = LoggerFactory.getLogger(BridgeHandler.class);

    public BridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {

        logger.debug("Initializing bridge handler.");

        BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);
        logger.debug("config hostname = {}", config.hostname);
        logger.debug("config port = {}", config.port);

        boolean validConfig = true;
        String errorMsg = null;

        if (StringUtils.trimToNull(config.hostname) == null) {
            errorMsg = "Parameter 'hostname' is mandatory and must be configured";
            validConfig = false;
        }
        if (config.port == null) {
            errorMsg = "Parameter 'port' must be configured";
            validConfig = false;
        }
        if (config.refresh == null) {
            errorMsg = "Parameter 'refresh' must be configured";
            validConfig = false;
        }

        if (validConfig) {
            updateStatus(ThingStatus.OFFLINE);
            startAutomaticRefresh(config);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the bridge handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No channels - nothing to do
    }

    /**
     * Start the job refreshing the handler
     */
    private void startAutomaticRefresh(BridgeConfiguration config) {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                try {

                    checkConnection();

                } catch (Exception e) {
                    logger.error("Exception occurred during execution: {}", e.getMessage(), e);
                }
            };

            int delay = config.refresh.intValue();
            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.MINUTES);
        }
    }

    protected BridgeConfiguration getBridgeConfiguration() {

        BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);
        return config;

    }

    private void checkConnection() throws Exception {

        String errormsg = null;
        String response1 = null;
        BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);
        String hostname = config.hostname;

        String url = "http://" + hostname + ":9763/services/Version.VersionHttpEndpoint";

        URL obj;

        obj = this.validateCertificate(url);

        if (obj == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Certificate validation failed");

        } else {

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            try {

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                response1 = response.toString();
                logger.debug(response1);

                if (response1.equals("[]")) {
                    errormsg = "empty response from wso2 iots";
                    logger.warn(errormsg);
                }
            } catch (Exception e) {
                errormsg = e.getMessage();
                logger.warn(errormsg);
            }

            if (errormsg == null) {
                updateStatus(ThingStatus.ONLINE);

            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errormsg);

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

}