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
package org.openhab.binding.energenie.internal.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.energenie.internal.config.EnergenieConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnergeniePWMHandler} is responsible for reading states and update PWM channels.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */

@NonNullByDefault
public class EnergeniePWMHandler extends BaseThingHandler {

    public enum PWMState {
        VOLTAGE("var V  = ", 9, 20, 10, SmartHomeUnits.VOLT),
        CURRENT("var V  = ", 9, 20, 100, SmartHomeUnits.AMPERE),
        POWER("var P=", 6, 20, 466, SmartHomeUnits.WATT),
        ENERGY("var E=", 6, 20, 25600, SmartHomeUnits.WATT_HOUR);

        private final Logger logger = LoggerFactory.getLogger(PWMState.class);

        private final String stateResponseSearch;
        private final int start;
        private final int stop;
        private final int divisor;
        private final Unit<?> unit;

        private PWMState(final String stateResponseSearch, final int start, final int stop, final int divisor,
                final Unit<?> unit) {
            this.stateResponseSearch = stateResponseSearch;
            this.start = start;
            this.stop = stop;
            this.divisor = divisor;
            this.unit = unit;
        }

        public String getStateResponseSearch() {
            return stateResponseSearch;
        }

        public int getStart() {
            return start;
        }

        public int getStop() {
            return stop;
        }

        public int getDivisor() {
            return divisor;
        }

        public State readState(final String loginResponseString) {
            final int findState = loginResponseString.lastIndexOf(stateResponseSearch);

            if (findState > 0) {
                logger.trace("searchstring {} found at position {}", stateResponseSearch, findState);
                final String slicedResponseTmp = loginResponseString.substring(findState + start, findState + stop);
                logger.trace("transformed state response = {}", slicedResponseTmp);
                final String[] slicedResponse = slicedResponseTmp.split(";");
                logger.trace("transformed state response = {} - {}", slicedResponse[0], slicedResponse[1]);
                final double value;

                if (Double.parseDouble(slicedResponse[0]) / 1 == Double.parseDouble(slicedResponse[0])) {
                    value = Double.parseDouble(slicedResponse[0]) / divisor;
                } else {
                    value = -1.0;
                }
                return QuantityType.valueOf(value, unit);
            } else {
                logger.trace("searchstring '{} not found", stateResponseSearch);
                return UnDefType.UNDEF;
            }
        }
    }

    private final Logger logger = LoggerFactory.getLogger(EnergeniePWMHandler.class);

    private static final int HTTP_TIMEOUT_MILLISECONDS = 6000;

    private @Nullable EnergenieConfiguration config;

    private String host = "";
    private String password = "";
    private int refreshInterval;

    @Nullable
    private ScheduledFuture<?> refreshJob;

    public EnergeniePWMHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // PWM devices don't support any commands, just value reading
    }

    @Override
    public void initialize() {
        EnergenieConfiguration config = getConfigAs(EnergenieConfiguration.class);

        this.config = config;

        if (config.host != null && config.password != null) {
            host = config.host;
            password = config.password;
            refreshInterval = EnergenieConfiguration.DEFAULT_REFRESH_INTERVAL;
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
        InputStream urlContent = new ByteArrayInputStream(urlParameters.getBytes(StandardCharsets.UTF_8));
        String loginResponseString = null;

        try {
            logger.trace("sendlogin to {} with password {}", host, password);
            logger.trace("sending 'POST' request to URL : {}", url);
            loginResponseString = HttpUtil.executeUrl("POST", url, urlContent, "TEXT/PLAIN", HTTP_TIMEOUT_MILLISECONDS);

            if (loginResponseString != null) {
                updateState("voltage", PWMState.VOLTAGE.readState(loginResponseString));
                updateState("current", PWMState.CURRENT.readState(loginResponseString));
                updateState("power", PWMState.POWER.readState(loginResponseString));
                updateState("energy", PWMState.ENERGY.readState(loginResponseString));
                try {
                    HttpUtil.executeUrl("POST", url, HTTP_TIMEOUT_MILLISECONDS);
                    logger.trace("logout from ip {}", host);
                } catch (IOException e) {
                    logger.debug("failed to logout from {} with ip {}", thing.getUID(), host, e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }

        } catch (IOException e) {
            logger.debug("energenie: failed to login to {} with ip {}", thing.getUID(), host, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private synchronized void onUpdate() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            refreshJob = scheduler.scheduleWithFixedDelay(this::getState, 5, refreshInterval, TimeUnit.SECONDS);
        }
    }

}
