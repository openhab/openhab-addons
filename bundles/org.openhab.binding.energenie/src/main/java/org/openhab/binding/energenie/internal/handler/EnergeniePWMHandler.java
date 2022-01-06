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
package org.openhab.binding.energenie.internal.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energenie.internal.EnergeniePWMStateEnum;
import org.openhab.binding.energenie.internal.config.EnergenieConfiguration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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

    private static final int HTTP_TIMEOUT_MILLISECONDS = 6000;

    private String host = "";
    private String password = "";
    private int refreshInterval;

    private @Nullable ScheduledFuture<?> refreshJob;

    public EnergeniePWMHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::getState);
        }
    }

    @Override
    public void initialize() {
        EnergenieConfiguration config = getConfigAs(EnergenieConfiguration.class);

        if (!config.host.isEmpty() && !config.password.isEmpty()) {
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
        final ScheduledFuture<?> refreshJob = this.refreshJob;

        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }
    }

    public synchronized void getState() {
        String url = "http://" + host + "/login.html";
        String urlParameters = "pw=" + password;
        InputStream urlContent = new ByteArrayInputStream(urlParameters.getBytes(StandardCharsets.UTF_8));
        String loginResponseString = null;

        try {
            logger.trace("sending 'POST' request to URL : {}", url);
            loginResponseString = HttpUtil.executeUrl("POST", url, urlContent, "TEXT/PLAIN", HTTP_TIMEOUT_MILLISECONDS);

            if (loginResponseString != null) {
                updateState("voltage", EnergeniePWMStateEnum.VOLTAGE.readState(loginResponseString));
                updateState("current", EnergeniePWMStateEnum.CURRENT.readState(loginResponseString));
                updateState("power", EnergeniePWMStateEnum.POWER.readState(loginResponseString));
                updateState("energy", EnergeniePWMStateEnum.ENERGY.readState(loginResponseString));
                try {
                    HttpUtil.executeUrl("POST", url, HTTP_TIMEOUT_MILLISECONDS);
                    logger.trace("logout from ip {}", host);
                } catch (IOException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "failed to logout: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            logger.debug("energenie: failed to login to {} with ip {}", thing.getUID(), host, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private synchronized void onUpdate() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob == null || refreshJob.isCancelled()) {
            this.refreshJob = scheduler.scheduleWithFixedDelay(this::getState, 5, refreshInterval, TimeUnit.SECONDS);
        }
    }
}
