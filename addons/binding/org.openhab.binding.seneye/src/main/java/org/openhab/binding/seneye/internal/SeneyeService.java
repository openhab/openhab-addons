/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.seneye.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SeneyeService} handles the connection to the Seneye API
 *
 * @author Niko Tanghe - Initial contribution
 */

public class SeneyeService {

    private static Logger logger = LoggerFactory.getLogger(SeneyeService.class);

    private int retry;

    private SeneyeConfigurationParameters config;

    private String seneyeId;

    private boolean isInitialized;

    private final Gson gson;

    private static HttpClient httpClient = new HttpClient(new SslContextFactory());

    public SeneyeService(SeneyeConfigurationParameters config) throws CommunicationException {
        this.config = config;

        this.retry = 1;

        this.gson = new Gson();

        this.isInitialized = false;

        if (!httpClient.isStarted()) {
            try {
                httpClient.setFollowRedirects(false);
                httpClient.start();
            } catch (Exception e) {
                throw new CommunicationException("Cannot start HttpClient!", e);
            }
        }

    }

    public void startAutomaticRefresh(ScheduledExecutorService scheduledExecutorService,
            final ReadingsUpdate readingsUpdate) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                readingsUpdate.newState(getDeviceReadings());
            }
        };

        scheduledJob = scheduledExecutorService.scheduleWithFixedDelay(runnable, 0, config.poll_time, TimeUnit.MINUTES);
    }

    ScheduledFuture<?> scheduledJob;

    public void stopAutomaticRefresh() {
        if (scheduledJob != null) {
            scheduledJob.cancel(true);
        }
    }

    public SeneyeDeviceReading getDeviceReadings() {
        int currentTry = 0;
        do {
            try {
                String responseState = GetData("/" + seneyeId + "/state?" + GetCredentials());
                String responseReadings = GetData("/" + seneyeId + "/exps?" + GetCredentials());

                SeneyeDeviceReading readings = gson.fromJson(responseReadings, SeneyeDeviceReading.class);
                readings.status = gson.fromJson(responseState, SeneyeStatus.class);

                logger.debug("seneye '{}' read", this.seneyeId);

                return readings;

            } catch (Exception se) {
                // ok, this readout failed, swallow this error, this is a scheduled task and this is in a retry loop,
                // so it will be retried.
                logger.debug("failed to read seneye '{}'", se.getMessage());
            }
        } while (currentTry++ < this.retry);

        return null;

    }

    public void initialize() throws CommunicationException, InvalidConfigurationException {
        String response = GetData("?" + GetCredentials());

        Seneye[] seneyeDevices = gson.fromJson(response, Seneye[].class);

        for (Seneye seneye : seneyeDevices) {
            if (seneye.description.equals(config.aquarium_name)) {
                seneyeId = Integer.toString(seneye.id);
                isInitialized = true;
                return;
            }
        }
        throw new InvalidConfigurationException(
                "Could not find a seneye with aquarium name '" + config.aquarium_name + "'");
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    private String GetData(String request) throws CommunicationException {
        // get devices
        // https://api.seneye.com/v1/devices?user=emailaddress&pwd=xxx

        // get devicestatus
        // https://api.seneye.com/v1/devices/23142/state?user=emailaddress&pwd=xxx

        // get readings
        // https://api.seneye.com/v1/devices/23142/exps?user=emailaddress&pwd=xxx

        // get advices
        // https://api.seneye.com/v1/devices/23142/advices/<id>?user=emailaddress&pwd=xxx

        String url = "https://api.seneye.com/v1/devices" + request;

        Request getMethod = httpClient.newRequest(url);
        getMethod.accept("application/json");

        try {
            ContentResponse response = getMethod.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.debug("Get readings method failed: {}", response.getReason());
                return "";
            }

            return response.getContentAsString();
        } catch (InterruptedException e) {
            throw new CommunicationException("Request aborted", e);
        } catch (TimeoutException e) {
            throw new CommunicationException("Timeout error", e);
        } catch (ExecutionException e) {
            throw new CommunicationException("Communication error", e.getCause());
        }
    }

    private String GetCredentials() {
        return "user=" + config.username + "&pwd=" + config.password;
    }
}
