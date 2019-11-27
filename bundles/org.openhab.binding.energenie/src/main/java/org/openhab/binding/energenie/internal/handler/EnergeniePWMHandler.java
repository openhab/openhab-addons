/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.energenie.internal.handler;

import static org.openhab.binding.energenie.internal.EnergenieBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnergeniePWMHandler} is responsible for reading states and update PWM channels.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */

@NonNullByDefault
public class EnergeniePWMHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EnergeniePWMHandler.class);

    private String host = "";
    private String password = "";

    /** the timeout to use for connecting to a given host (defaults to 5000 milliseconds) */
    private int timeout = 6000;

    /**
     * The default refresh interval in Seconds.
     */
    private int DEFAULT_REFRESH_INTERVAL = 60;
    @Nullable
    private ScheduledFuture<?> refreshJob;
    private Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            getState();
        }
    };

    public EnergeniePWMHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // PWM devices don't support any commands, just value reading
    }

    @Override
    public void initialize() {

        Configuration config = getConfig();

        if ((config.get("host") != null) && (config.get("password") != null)) {
            host = (String) config.get("host");
            password = (String) config.get("password");
            logger.debug("Initializing EnergeniePWMHandler for Host '{}'", host);
            updateStatus(ThingStatus.ONLINE);
            onUpdate();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can not access device , IP-Address or password not set");
        }
    }

    @Override
    public void dispose() {
        logger.debug("EnergeniePWMHandler disposed.");
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    public void getState() {
        String url = "http://" + host + "/login.html";
        String urlParameters = "pw=" + password;
        InputStream urlContent = new ByteArrayInputStream(urlParameters.getBytes(Charset.forName("UTF-8")));
        String loginResponseString = null;

        try {
            logger.trace("sendlogin to {} with password {}", host, password);
            logger.trace("sending 'POST' request to URL : {}", url);
            loginResponseString = HttpUtil.executeUrl("POST", url, urlContent, "TEXT/PLAIN", timeout);

            if (loginResponseString != null) {
                readState(loginResponseString, "voltage");
                readState(loginResponseString, "current");
                readState(loginResponseString, "power");
                readState(loginResponseString, "energy");
                try {
                    HttpUtil.executeUrl("POST", url, timeout);
                    logger.trace("logout from ip {}", host);
                } catch (Exception e) {
                    logger.error("failed to logout from ip {}", host);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("energenie: failed to login to ip {}", host);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public void readState(String loginResponseString, String channel) {
        String stateResponseSearch = "";
        int start = 0, stop = 0, divisor = 1;
        double value = 0.0;

        switch (channel) {
            case VOLTAGE:
                stateResponseSearch = "var V  = ";
                start = 9;
                stop = 20;
                divisor = 10;

                break;
            case CURRENT:
                stateResponseSearch = "var I  = ";
                start = 9;
                stop = 20;
                divisor = 100;
                break;
            case POWER:
                stateResponseSearch = "var P=";
                start = 6;
                stop = 20;
                divisor = 466;
                break;
            case ENERGY:
                stateResponseSearch = "var E=";
                start = 6;
                stop = 20;
                divisor = 25600;
                break;
        }

        int findState = loginResponseString.lastIndexOf(stateResponseSearch);
        if (findState > 0) {
            logger.trace("searchstring {} found at position {}", stateResponseSearch, findState);
            String slicedResponseTmp = loginResponseString.substring(findState + start, findState + stop);
            logger.trace("transformed state response = {}", slicedResponseTmp);
            String[] slicedResponse = slicedResponseTmp.split(";");
            logger.trace("transformed state response = {} - {}", slicedResponse[0], slicedResponse[1]);
            if (Double.parseDouble(slicedResponse[0]) / 1 == Double.parseDouble(slicedResponse[0])) {
                value = Double.parseDouble(slicedResponse[0]) / divisor;
            } else {
                value = -1.0;
            }
            State valueState = new DecimalType(value);
            updateState(channel, valueState);

        } else {
            logger.trace("searchstring %s not found", stateResponseSearch);
        }

    }

    private synchronized void onUpdate() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Configuration config = getThing().getConfiguration();
            int refreshInterval = DEFAULT_REFRESH_INTERVAL;
            Object refreshConfig = config.get("refresh");
            if (refreshConfig != null) {
                refreshInterval = ((BigDecimal) refreshConfig).intValue();
            }
            refreshJob = scheduler.scheduleWithFixedDelay(refreshRunnable, 5, refreshInterval, TimeUnit.SECONDS);
        }
    }

}
