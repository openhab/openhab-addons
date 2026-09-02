/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sunsynk.internal.api.AccountController;
import org.openhab.binding.sunsynk.internal.api.dto.Inverter;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkAuthenticateException;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkClientAuthenticateException;
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
    private static final long EXPIRY_SECONDS = 100L; // 100 seconds before expiry
    private AccountController sunAccount = new AccountController();
    private @Nullable ScheduledFuture<?> discoverApiKeyJob;
    private @Nullable SunSynkAccountConfig accountConfig;

    public SunSynkAccountHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        accountConfig = getConfigAs(SunSynkAccountConfig.class);
        updateStatus(ThingStatus.UNKNOWN);
        logger.debug("SunSynk Handler initialised attempting to retrieve configuration");
        discoverApiKeyJob = scheduler.schedule(this::configAccount, 0, TimeUnit.SECONDS); // calls account config
                                                                                          // asynchronously
    }

    public void setBridgeOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void setBridgeOffline() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Check credentials and re-enable account");
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
        ScheduledFuture<?> discoverApiKeyJob = this.discoverApiKeyJob;
        if (discoverApiKeyJob != null) {
            discoverApiKeyJob.cancel(true);
            this.discoverApiKeyJob = null;
        }
    }

    private boolean configAccount() {
        SunSynkAccountConfig accountConfig = this.accountConfig;
        if (accountConfig == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No account config provided.");
            return false;
        }
        if (accountConfig.getEmail().isBlank() | accountConfig.getPassword().isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "E-mail address or Password missing in account configuration");
            return false;
        }
        try {
            this.sunAccount.clientAuthenticate(accountConfig.getEmail(), accountConfig.getPassword());
        } catch (SunSynkClientAuthenticateException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error attempting to authenticate client: {}.", e.getMessage());
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to authenticate binding with SunSynk");
            return false;
        } catch (SunSynkAuthenticateException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error attempting to authenticate user: {}.", e.getMessage());
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to authenticate user credentials");
            return false;
        }
        updateStatus(ThingStatus.ONLINE);
        return true;
    }

    /**
     * Checks if the bearer token is near expiry and if it is refreshes by logging in.
     * 
     * @throws SunSynkAuthenticateException
     */
    public synchronized void refreshAccount() throws SunSynkAuthenticateException {
        try {
            SunSynkAccountConfig accountConfig = this.accountConfig;
            if (accountConfig == null) {
                throw new SunSynkTokenException("No account config");
            }
            long expiresFromNow = this.sunAccount.checkExpireTime();
            if (expiresFromNow < EXPIRY_SECONDS) {
                logger.debug("Account configuration token about to expire - logging in.");
                if (!configAccount()) {
                    throw new SunSynkTokenException("failed to config account");
                }
            } else {
                Duration d = Duration.ofSeconds(expiresFromNow);
                logger.debug(
                        "Account configuration token not expired, valid for: {} days, {} hours, {} minutes, {} seconds",
                        d.toDays(), d.toHoursPart(), d.toMinutesPart(), d.toSecondsPart());
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (SunSynkTokenException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to refresh account: " + e.getMessage());
            throw new SunSynkAuthenticateException("" + e.getMessage());
        }
    }
}
