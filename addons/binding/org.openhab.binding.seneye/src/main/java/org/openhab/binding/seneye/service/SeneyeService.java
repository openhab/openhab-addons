/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.seneye.service;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SeneyeService} handles the connection to the Seneye API
 *
 * @author Niko Tanghe
 */

public class SeneyeService {

    private static Logger logger = LoggerFactory.getLogger(SeneyeService.class);

    private long refreshInterval;
    private int retry;

    private String aquariumname;
    private String userName;
    private String password;
    private String seneyeId;

    private boolean isInitialized;

    private final Gson gson;

    public SeneyeService() {
        this("", "", "", 120000);
    }

    public SeneyeService(String aquariumname, String userName, String password, long refreshInterval) {
        this.aquariumname = aquariumname;
        this.userName = userName;
        this.password = password;
        this.refreshInterval = refreshInterval;
        this.retry = 1;

        this.gson = new Gson();

        this.isInitialized = false;
    }

    public void startAutomaticRefresh(ScheduledExecutorService scheduledExecutorService,
            final ReadingsUpdate readingsUpdate) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    readingsUpdate.newState(getDeviceReadings());
                } catch (InvalidConfigurationException e) {
                    readingsUpdate.invalidConfig();
                }
            }
        };

        scheduledJob = scheduledExecutorService.scheduleAtFixedRate(runnable, 0, this.refreshInterval,
                TimeUnit.MILLISECONDS);
    }

    ScheduledFuture<?> scheduledJob;

    public void stopAutomaticRefresh() {
        scheduledJob.cancel(true);
    }

    public SeneyeDeviceReading getDeviceReadings() throws InvalidConfigurationException {
        int currentTry = 0;
        do {
            try {
                String responseState = GetData("/" + seneyeId + "/state?" + GetCredentials());
                String responseReadings = GetData("/" + seneyeId + "/exps?" + GetCredentials());

                SeneyeDeviceReading readings = gson.fromJson(responseReadings, SeneyeDeviceReading.class);
                readings.status = gson.fromJson(responseState, SeneyeStatus.class);

                logger.debug("seneye '{}' read", new Object[] { this.seneyeId });

                return readings;

            } catch (Exception se) {
                logger.debug("failed to read seneye '{}'", new Object[] { se.getMessage() });
            }
        } while (currentTry++ < this.retry);

        return null;

    }

    public boolean initialize() {
        String response = GetData("?" + GetCredentials());

        if (response.isEmpty()) {
            return false;
        }

        Seneye[] seneyeDevices = gson.fromJson(response, Seneye[].class);

        if (seneyeDevices == null) {
            return false;
        }

        for (Seneye seneye : seneyeDevices) {
            if (seneye.description.equals(this.aquariumname)) {
                seneyeId = Integer.toString(seneye.id);
                isInitialized = true;

                return true;
            }
        }
        return false;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    private String GetData(String request) {
        // get devices
        // https://api.seneye.com/v1/devices?user=emailaddress&pwd=xxx

        // get devicestatus
        // https://api.seneye.com/v1/devices/23142/state?user=emailaddress&pwd=xxx

        // get readings
        // https://api.seneye.com/v1/devices/23142/exps?user=emailaddress&pwd=xxx

        // get advices
        // https://api.seneye.com/v1/devices/23142/advices/<id>?user=emailaddress&pwd=xxx

        String url = "https://api.seneye.com/v1/devices" + request;

        HttpClient getClient = new HttpClient();

        GetMethod getMethod = new GetMethod(url);
        getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        getMethod.getParams().setParameter(HttpMethodParams.USER_AGENT,
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; .NET CLR 1.0.3705;)");
        getMethod.addRequestHeader("Accept", "application/json");

        try {
            int statusCode = getClient.executeMethod(getMethod);
            if (statusCode != HttpStatus.SC_OK) {
                logger.error("Method failed: {}", getMethod.getStatusLine());
                return "";
            }

            return IOUtils.toString(getMethod.getResponseBodyAsStream());
        } catch (HttpException e) {
            logger.error("Fatal protocol violation: {}", e.toString());
            return "";
        } catch (IOException e) {
            logger.error("Fatal transport error: {}", e.toString());
            return "";
        } finally {
            getMethod.releaseConnection();
        }
    }

    private String GetCredentials() {
        return "user=" + userName + "&pwd=" + password;
    }
}
