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
package org.openhab.binding.openuv.internal.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openuv.internal.OpenUVException;
import org.openhab.binding.openuv.internal.config.BridgeConfiguration;
import org.openhab.binding.openuv.internal.discovery.OpenUVDiscoveryService;
import org.openhab.binding.openuv.internal.json.OpenUVResponse;
import org.openhab.binding.openuv.internal.json.OpenUVResult;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * {@link OpenUVBridgeHandler} is the handler for OpenUV API and connects it
 * to the webservice.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class OpenUVBridgeHandler extends BaseBridgeHandler {
    private static final String QUERY_URL = "https://api.openuv.io/api/v1/uv?lat=%.2f&lng=%.2f&alt=%.0f";
    private static final int RECONNECT_DELAY_MIN = 5;
    private static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(30);

    private final Logger logger = LoggerFactory.getLogger(OpenUVBridgeHandler.class);
    private final Properties header = new Properties();
    private final Gson gson;

    private Optional<ScheduledFuture<?>> reconnectJob = Optional.empty();
    private boolean keyVerified;

    public OpenUVBridgeHandler(Bridge bridge, Gson gson) {
        super(bridge);
        this.gson = gson;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OpenUV API bridge handler.");
        keyVerified = false;
        BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);
        if (config.apikey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-unknown-apikey");
            return;
        }
        header.put("x-access-token", config.apikey);
        initiateConnexion();
    }

    @Override
    public void dispose() {
        header.clear();
        freeReconnectJob();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            initiateConnexion();
        } else {
            logger.debug("The OpenUV bridge only handles Refresh command and not '{}'", command);
        }
    }

    private void initiateConnexion() {
        // Just checking if the provided api key is a valid one by making a fake call
        getUVData(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public @Nullable OpenUVResult getUVData(BigDecimal latitude, BigDecimal longitude, BigDecimal altitude) {
        String statusMessage = "";
        ThingStatusDetail statusDetail = ThingStatusDetail.COMMUNICATION_ERROR;
        String url = QUERY_URL.formatted(latitude, longitude, altitude);
        String jsonData = "";
        try {
            jsonData = HttpUtil.executeUrl("GET", url, header, null, null, REQUEST_TIMEOUT_MS);
            OpenUVResponse uvResponse = gson.fromJson(jsonData, OpenUVResponse.class);
            if (uvResponse != null) {
                String error = uvResponse.getError();
                if (error == null) {
                    updateStatus(ThingStatus.ONLINE);
                    keyVerified = true;
                    return uvResponse.getResult();
                }
                throw new OpenUVException(error);
            }
        } catch (JsonSyntaxException e) {
            if (jsonData.contains("MongoError")) {
                statusMessage = "@text/offline.comm-error-faultly-service [ \"%d\" ]".formatted(RECONNECT_DELAY_MIN);
                scheduleReconnectJob(RECONNECT_DELAY_MIN);
            } else {
                statusDetail = ThingStatusDetail.NONE;
                statusMessage = "@text/offline.invalid-json [ \"%s\" ]".formatted(url);
                logger.debug("{} : {}", statusMessage, jsonData);
            }
        } catch (IOException e) {
            statusMessage = "@text/offline.comm-error-ioexception [ \"%s\",\"%d\" ]".formatted(e.getMessage(),
                    RECONNECT_DELAY_MIN);
            scheduleReconnectJob(RECONNECT_DELAY_MIN);
        } catch (OpenUVException e) {
            if (e.isQuotaError()) {
                LocalDateTime nextMidnight = LocalDate.now().plusDays(1).atStartOfDay().plusMinutes(2);
                statusMessage = "@text/offline.comm-error-quota-exceeded [ \"%s\" ]".formatted(nextMidnight.toString());
                scheduleReconnectJob(Duration.between(LocalDateTime.now(), nextMidnight).toMinutes());
            } else if (e.isApiKeyError()) {
                if (keyVerified) {
                    statusMessage = "@text/offline.api-key-not-recognized [ \"%d\" ]".formatted(RECONNECT_DELAY_MIN);
                    scheduleReconnectJob(RECONNECT_DELAY_MIN);
                } else {
                    statusDetail = ThingStatusDetail.CONFIGURATION_ERROR;
                }
            }
        }
        updateStatus(ThingStatus.OFFLINE, statusDetail, statusMessage);
        return null;
    }

    private void scheduleReconnectJob(long delay) {
        freeReconnectJob();
        reconnectJob = Optional.of(scheduler.schedule(this::initiateConnexion, delay, TimeUnit.MINUTES));
    }

    private void freeReconnectJob() {
        reconnectJob.ifPresent(job -> job.cancel(true));
        reconnectJob = Optional.empty();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(OpenUVDiscoveryService.class);
    }
}
