/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mspa.internal.handler;

import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.json.JSONObject;
import org.openhab.binding.mspa.internal.MSpaConstants.ServiceRegion;
import org.openhab.binding.mspa.internal.MSpaUtils;
import org.openhab.binding.mspa.internal.config.MSpaOwnerAccountConfiguration;
import org.openhab.binding.mspa.internal.discovery.MSpaDiscoveryService;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MSpaOwnerAccount} connects your MSpa-Link account with email and password credentials.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MSpaOwnerAccount extends MSpaBaseAccount {

    private final Logger logger = LoggerFactory.getLogger(MSpaOwnerAccount.class);

    public MSpaOwnerAccount(Bridge bridge, HttpClient httpClient, MSpaDiscoveryService discovery,
            Storage<String> store) {
        super(bridge, httpClient, discovery, store);
    }

    @Override
    public void initialize() {
        MSpaOwnerAccountConfiguration config = getConfigAs(MSpaOwnerAccountConfiguration.class);
        if (config.email.isBlank() || config.password.isBlank() || config.region.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/status.mspa.owner-account.config-parameter-missing");
            return;
        }
        ownerConfig = Optional.of(config);
        // restore token from storage
        JSONObject storage = getStorage(config.email);
        if (storage.has(TOKEN)) {
            JSONObject tokenResponse = storage.getJSONObject(TOKEN);
            token = MSpaUtils.decodeStoredToken(tokenResponse);
        }
        updateStatus(ThingStatus.UNKNOWN);
        // token validation takes place in base class and can cause HTTP request
        scheduler.schedule(super::initialize, 0, TimeUnit.SECONDS);
    }

    @Override
    public void requestToken() {
        Request tokenRequest = getRequest(HttpMethod.POST, ENDPOINT_TOKEN);
        JSONObject body = new JSONObject();
        body.put("account", ownerConfig.get().email);
        body.put("password", MSpaUtils.getPasswordHash(ownerConfig.get().password));
        body.put("app_id", APP_IDS.get(ServiceRegion.valueOf(ownerConfig.get().region)));
        body.put("registration_id", EMPTY);
        body.put("push_type", "android");
        tokenRequest.content(new StringContentProvider(body.toString(), "utf-8"));
        String failReason = null;
        try {
            ContentResponse cr = tokenRequest.timeout(10000, TimeUnit.MILLISECONDS).send();
            int status = cr.getStatus();
            if (status == 200) {
                String response = cr.getContentAsString();
                token = MSpaUtils.decodeNewToken(response);
                if (MSpaUtils.isTokenValid(token)) {
                    JSONObject tokenStore = MSpaUtils.token2Json(token);
                    String persistenceId = ownerConfig.get().email;
                    JSONObject persistence = getStorage(persistenceId);
                    persistence.put(TOKEN, tokenStore);
                    persist(persistenceId, persistence);
                } else {
                    failReason = MSpaUtils.checkResponse(response);
                    logger.warn("Failed to get token - reason {}", failReason);
                }
            } else {
                failReason = cr.getReason();
                logger.warn("Failed to get token - reason {}", failReason);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            failReason = e.toString();
            logger.warn("Failed to get token - reason {}", failReason);
            handlePossibleInterrupt(e);
        }
        if (failReason != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/status.mspa.token-request-error [\"" + failReason + "\"]");
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }
}
