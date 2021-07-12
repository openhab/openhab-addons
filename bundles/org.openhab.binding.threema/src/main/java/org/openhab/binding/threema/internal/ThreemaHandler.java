/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.threema.internal;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.threema.apitool.APIConnector;

/**
 * The {@link ThreemaHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai K. - Initial contribution
 */
@NonNullByDefault
public class ThreemaHandler extends BaseThingHandler {
    private static final Pattern HTTP_RESPONSE_CODE_PATTERN = Pattern.compile("HTTP response code: (\\d{3})");

    private final Logger logger = LoggerFactory.getLogger(ThreemaHandler.class);

    private @Nullable ThreemaConfiguration config;
    private @Nullable APIConnector apiConnector;

    public ThreemaHandler(Thing thing) {
        super(thing);
    }

    public ThreemaHandler(Thing thing, APIConnector apiConnector) {
        super(thing);
        this.apiConnector = Objects.requireNonNull(apiConnector);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands to handle
    }

    @Override
    public void initialize() {
        config = getConfigAs(ThreemaConfiguration.class);
        Optional<String> gatewayId = Optional.ofNullable(config).map(ThreemaConfiguration::getGatewayId)
                .filter(Objects::nonNull).filter(str -> !str.isBlank());

        if (!gatewayId.isPresent()) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Threema gateway ID not configured.");
            return;
        }

        Optional<String> secret = Optional.ofNullable(config).map(ThreemaConfiguration::getSecret)
                .filter(Objects::nonNull).filter(str -> !str.isBlank());

        if (!secret.isPresent()) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Threema gateway secret not configured.");
            return;
        }

        logger.info("Initialising Threema connector for {}", gatewayId);
        logger.info("Configured recipients: {}", config.getRecipientIds());

        apiConnector = Optional.ofNullable(apiConnector)
                .orElseGet(() -> new APIConnector(gatewayId.get(), secret.get(), new ThreemaKeyStore()));

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            int credits = lookupCredits();
            if (credits > 0) {
                logger.info("{} Threema.Gateway credits available.", credits);
                updateStatus(ThingStatus.ONLINE);
            }
        });
    }

    @Override
    public void dispose() {
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        apiConnector = null;
        super.dispose();
    }

    public Integer lookupCredits() {
        try {
            return apiConnector.lookupCredits();
        } catch (IOException e) {
            logger.error("Failed to query credits! {}", e.getMessage());
            handleException(e);
            return -1;
        }
    }

    public boolean sendTextMessageSimple(String threemaId, String textMessage) {
        try {
            apiConnector.sendTextMessageSimple(threemaId, textMessage);
            return true;
        } catch (IOException e) {
            logger.error("Failed to send text message in basic mode to {}: {}", threemaId, e.getMessage());
            handleException(e);
            return false;
        }
    }

    public boolean sendTextMessageSimple(String message) {
        return config.getRecipientIds().parallelStream().map(threemaId -> sendTextMessageSimple(threemaId, message))
                .allMatch(Boolean.TRUE::equals);
    }

    private void handleException(IOException e) {
        Optional.ofNullable(e.getMessage()).map(HTTP_RESPONSE_CODE_PATTERN::matcher).filter(Matcher::find)
                .map(match -> match.group(1)).map(Integer::valueOf).map(HttpStatus::getCode)
                .ifPresentOrElse(code -> handleHttpStatusCode(code), () -> updateStatus(ThingStatus.OFFLINE,
                        ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage()));
    }

    private void handleHttpStatusCode(Code code) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, code.getMessage());
    }
}
