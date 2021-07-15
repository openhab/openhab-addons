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

import static org.openhab.binding.threema.internal.ThreemaBindingConstants.CHID_CREDITS;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.UNINITIALIZED;
import static org.openhab.core.thing.ThingStatusDetail.COMMUNICATION_ERROR;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.core.types.UnDefType.UNDEF;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpStatus.Code;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.threema.apitool.APIConnector;

/**
 * The {@link ThreemaBasicHandler} provides actions (services) to send text messages in basic mode.
 *
 * @author Kai K. - Initial contribution
 */
@NonNullByDefault
public class ThreemaBasicHandler extends BaseThingHandler {
    private static final Pattern HTTP_RESPONSE_CODE_PATTERN = Pattern.compile("HTTP response code: (\\d{3})");

    private final Logger logger = LoggerFactory.getLogger(ThreemaBasicHandler.class);

    private @Nullable ThreemaBasicConfiguration config;
    private @Nullable APIConnector apiConnector;
    private Map<String, byte[]> publicKeys = new ConcurrentHashMap<>();

    public ThreemaBasicHandler(Thing thing) {
        super(thing);
    }

    public ThreemaBasicHandler(Thing thing, APIConnector apiConnector) {
        super(thing);
        this.apiConnector = Objects.requireNonNull(apiConnector);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH.equals(command)) {
            lookupCredits();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(ThreemaBasicConfiguration.class);
        Optional<String> gatewayId = getGatewayId();
        if (!gatewayId.isPresent()) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Threema gateway ID not configured.");
            return;
        }

        Optional<String> secret = getGatewaySecret();
        if (!secret.isPresent()) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Threema gateway secret not configured.");
            return;
        }

        logger.info("Initialising Threema connector for {}", gatewayId.get());
        logger.info("Configured recipients: {}", getDefaultRecipients());

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
        publicKeys.clear();
        apiConnector = null;
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(ThreemaBasicActions.class);
    }

    public Integer lookupCredits() {
        final ChannelUID channelUid = composeChannelId(CHID_CREDITS);
        return getApiConnector().map(api -> {
            try {
                Integer credits = api.lookupCredits();
                updateState(channelUid, new DecimalType(credits));
                return credits;
            } catch (IOException e) {
                logger.error("Failed to query credits! {}", e.getMessage());
                handleException(e);
                updateState(channelUid, UNDEF);
                return -1;
            }
        }).orElseGet(() -> {
            updateStatus(UNINITIALIZED);
            updateState(channelUid, UNDEF);
            return -1;
        });
    }

    public boolean sendTextMessageSimple(String threemaId, String textMessage) {
        return getApiConnector().map(api -> {
            try {
                if (recipientExists(threemaId)) {
                    api.sendTextMessageSimple(threemaId, textMessage);
                    return true;
                } else {
                    logger.warn("Could not send text message to {}: Threema ID probably does not exist", threemaId);
                    return false;
                }
            } catch (IOException e) {
                logger.error("Failed to send text message to {}: {}", threemaId, e.getMessage());
                handleException(e);
                return false;
            }
        }).orElseGet(() -> {
            updateStatus(UNINITIALIZED);
            return false;
        });
    }

    private boolean recipientExists(String threemaId) {
        return publicKeys.computeIfAbsent(threemaId, this::lookupKey) != null;
    }

    private Optional<APIConnector> getApiConnector() {
        return Optional.ofNullable(this.apiConnector);
    }

    public boolean sendTextMessageSimple(String message) {
        return getDefaultRecipients().parallelStream().map(threemaId -> sendTextMessageSimple(threemaId, message))
                .allMatch(Boolean.TRUE::equals);
    }

    private void handleException(IOException e) {
        Optional.ofNullable(e.getMessage()).map(HTTP_RESPONSE_CODE_PATTERN::matcher).filter(Matcher::find)
                .map(match -> match.group(1)).map(Integer::valueOf).map(HttpStatus::getCode)
                .ifPresentOrElse(code -> handleHttpStatusCode(code),
                        () -> updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getLocalizedMessage()));
    }

    private void handleHttpStatusCode(Code code) {
        updateStatus(OFFLINE, CONFIGURATION_ERROR, code.getMessage());
    }

    private Optional<String> getGatewaySecret() {
        return getThreemaConfig().map(ThreemaBasicConfiguration::getSecret).filter(Objects::nonNull)
                .filter(str -> !str.isBlank());
    }

    private Optional<String> getGatewayId() {
        return getThreemaConfig().map(ThreemaBasicConfiguration::getGatewayId).filter(Objects::nonNull)
                .filter(str -> !str.isBlank());
    }

    private Optional<ThreemaBasicConfiguration> getThreemaConfig() {
        return Optional.ofNullable(config);
    }

    private List<String> getDefaultRecipients() {
        return getThreemaConfig().map(ThreemaBasicConfiguration::getRecipientIds).orElseGet(Collections::emptyList);
    }

    private ChannelUID composeChannelId(String channelName) {
        return new ChannelUID(getThing().getUID(), channelName);
    }

    private byte[] lookupKey(String threemaId) {
        return getApiConnector().map(api -> {
            try {
                return api.lookupKey(threemaId);
            } catch (IOException e) {
                logger.error("Failed to lookup public key for {}", threemaId);
                handleException(e);
                return null;
            }
        }).orElse(null);
    }
}
