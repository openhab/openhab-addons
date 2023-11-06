/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.siemensrds.internal;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link RdsCloudHandler} is the handler for Siemens RDS cloud account (
 * also known as the Climatix IC server account )
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class RdsCloudHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(RdsCloudHandler.class);

    private @Nullable RdsCloudConfiguration config = null;

    private @Nullable RdsAccessToken accessToken = null;

    public RdsCloudHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // there is nothing to do
    }

    @Override
    public void initialize() {
        RdsCloudConfiguration config = this.config = getConfigAs(RdsCloudConfiguration.class);

        if (config.userEmail.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "missing email address");
            return;
        }

        if (config.userPassword.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "missing password");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("polling interval={}", config.pollingInterval);
        }

        if (config.pollingInterval < FAST_POLL_INTERVAL || config.pollingInterval > LAZY_POLL_INTERVAL) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("polling interval out of range [%d..%d]", FAST_POLL_INTERVAL, LAZY_POLL_INTERVAL));
            return;
        }
    }

    @Override
    public void dispose() {
        // there is nothing to do
    }

    /*
     * public method: used by RDS smart thermostat handlers return the polling
     * interval (seconds)
     */
    public int getPollInterval() throws RdsCloudException {
        RdsCloudConfiguration config = this.config;
        if (config != null) {
            return config.pollingInterval;
        }
        throw new RdsCloudException("missing polling interval");
    }

    /*
     * private method: check if the current token is valid, and renew it if
     * necessary
     */
    private synchronized void refreshToken() {
        RdsCloudConfiguration config = this.config;
        RdsAccessToken accessToken = this.accessToken;

        if (accessToken == null || accessToken.isExpired()) {
            try {
                if (config == null) {
                    throw new RdsCloudException("missing configuration");
                }

                String url = URL_TOKEN;
                String payload = String.format(TOKEN_REQUEST, config.userEmail, config.userPassword);

                logger.debug(LOG_HTTP_COMMAND, HTTP_POST, url.length());
                logger.debug(LOG_PAYLOAD_FMT, LOG_SENDING_MARK, url);
                logger.debug(LOG_PAYLOAD_FMT, LOG_SENDING_MARK, payload);

                String json = RdsAccessToken.httpGetTokenJson(config.apiKey, payload);

                if (logger.isTraceEnabled()) {
                    logger.trace(LOG_CONTENT_LENGTH, LOG_RECEIVED_MSG, json.length());
                    logger.trace(LOG_PAYLOAD_FMT, LOG_RECEIVED_MARK, json);
                } else if (logger.isDebugEnabled()) {
                    logger.debug(LOG_CONTENT_LENGTH_ABR, LOG_RECEIVED_MSG, json.length());
                    logger.debug(LOG_PAYLOAD_FMT_ABR, LOG_RECEIVED_MARK,
                            json.substring(0, Math.min(json.length(), 30)));
                }

                accessToken = this.accessToken = RdsAccessToken.createFromJson(json);
            } catch (RdsCloudException e) {
                logger.warn(LOG_SYSTEM_EXCEPTION, "refreshToken()", e.getClass().getName(), e.getMessage());
            } catch (JsonParseException | IOException e) {
                logger.warn(LOG_RUNTIME_EXCEPTION, "refreshToken()", e.getClass().getName(), e.getMessage());
            }
        }

        if (accessToken != null) {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "cloud server responded");
            }
        } else {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "cloud server authentication error");
            }
        }
    }

    /*
     * public method: used by RDS smart thermostat handlers to fetch the current
     * token
     */
    public synchronized String getToken() throws RdsCloudException {
        refreshToken();
        RdsAccessToken accessToken = this.accessToken;
        if (accessToken != null) {
            return accessToken.getToken();
        }
        throw new RdsCloudException("no access token");
    }

    public String getApiKey() throws RdsCloudException {
        RdsCloudConfiguration config = this.config;
        if (config != null) {
            return config.apiKey;
        }
        throw new RdsCloudException("no api key");
    }
}
