/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sunsynk.internal.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sunsynk.internal.api.AccountController;
import org.openhab.binding.sunsynk.internal.api.dto.Inverter;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkAuthenticateException;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkInverterDiscoveryException;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkTokenException;
import org.openhab.binding.sunsynk.internal.config.SunSynkAccountConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SunSynkAccountHandler} is responsible for handling the SunSynk Account Bridge
 *
 *
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class SunSynkAccountHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SunSynkAccountHandler.class);
    private AccountController sunAccount = new AccountController();
    private @Nullable ScheduledFuture<?> discoverApiKeyJob;

    public SunSynkAccountHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("SunSynk Handler Intialised attempting to retrieve configuration");
        startDiscoverApiKeyJob();
    }

    private void startDiscoverApiKeyJob() {
        if (discoverApiKeyJob == null || discoverApiKeyJob.isCancelled()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    logger.debug("Starting account discovery job.");
                    configAccount();
                    logger.debug("Done account discovery job.");
                }
            };
            discoverApiKeyJob = scheduler.schedule(runnable, 1, TimeUnit.SECONDS);
        }
    }

    public void setBridgeOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void setBridgeOffline() {
        updateStatus(ThingStatus.OFFLINE);
    }

    public List<Inverter> getInvertersFromSunSynk() {
        logger.debug("Attempting to find inverters tied to account");
        ArrayList<Inverter> inverters = new ArrayList<>();
        try {
            inverters = sunAccount.getDetails();
        } catch (SunSynkInverterDiscoveryException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to find inverters registered to account");
        }
        return inverters;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing sunsynk bridge handler.");
        if (discoverApiKeyJob != null && !discoverApiKeyJob.isCancelled()) {
            discoverApiKeyJob.cancel(true);
            discoverApiKeyJob = null;
        }
    }

    public void configAccount() {
        SunSynkAccountConfig accountConfig = getConfigAs(SunSynkAccountConfig.class);
        try {
            this.sunAccount.authenticate(accountConfig.getEmail(), accountConfig.getPassword());
        } catch (SunSynkAuthenticateException | SunSynkTokenException e) {
            logger.debug("Error attempting to autheticate account Msg = {} Cause = {}", e.getMessage().toString(),
                    e.getCause().toString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to authenticate account");
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        logger.debug("Account configuration updated : {}", this.sunAccount.toString());
    }

    public boolean refreshAccount() throws SunSynkAuthenticateException {
        SunSynkAccountConfig accountConfig = getConfigAs(SunSynkAccountConfig.class);
        try {
            this.sunAccount.refreshAccount(accountConfig.getEmail());
        } catch (SunSynkTokenException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to refresh token");
            return false;
        }
        return true;
    }
}
