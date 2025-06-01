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
package org.openhab.binding.mspa.internal.handler;

import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.json.JSONObject;
import org.openhab.binding.mspa.internal.MSpaUtils;
import org.openhab.binding.mspa.internal.config.MSpaVisitorAccountConfiguration;
import org.openhab.binding.mspa.internal.discovery.MSpaDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MSpaVisitorAccount} connects your MSpa-Link account with a visitor id and QR grant code
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MSpaVisitorAccount extends MSpaBaseAccount {

    private final Logger logger = LoggerFactory.getLogger(MSpaVisitorAccount.class);

    public MSpaVisitorAccount(Bridge bridge, HttpClient httpClient, MSpaDiscoveryService discovery,
            Storage<String> store) {
        super(bridge, httpClient, discovery, store);
    }

    @Override
    public void initialize() {
        MSpaVisitorAccountConfiguration config = getConfigAs(MSpaVisitorAccountConfiguration.class);
        // generate visitor id if necessary
        if (UNKNOWN.equals(config.visitorId)) {
            Configuration updateConfig = editConfiguration();
            String visitorId = MSpaUtils.getPasswordHash(UUID.randomUUID().toString()).substring(0, 16);
            updateConfig.put("visitorId", visitorId);
            super.updateConfiguration(updateConfig);
        }
        // check for configuration errors
        if (UNKNOWN.equals(config.grantCode) || UNKNOWN.equals(config.region)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@test/status.mspa.visitor-account.config-parameter-missing");
            return;
        }
        visitorConfig = Optional.of(config);
        // request token if necessary
        JSONObject storage = getStorage(config.visitorId);
        if (storage.has(TOKEN)) {
            JSONObject tokenResponse = storage.getJSONObject(TOKEN);
            token = MSpaUtils.decodeStoredToken(tokenResponse);
            if (!MSpaUtils.isTokenValid(token)) {
                requestToken();
            }
        } else {
            requestToken();
        }
        grantDevices();
        super.initialize();
    }

    private void grantDevices() {
        String persistenceId = visitorConfig.get().visitorId;
        JSONObject persistence = getStorage(persistenceId);
        JSONObject storedGrants = new JSONObject();
        if (persistence.has(GRANTS)) {
            storedGrants = persistence.getJSONObject(GRANTS);
        }
        String[] configuredGrants = visitorConfig.get().grantCode.split(",");
        JSONObject validGrants = new JSONObject();

        for (int i = 0; i < configuredGrants.length; i++) {
            if (storedGrants.has(configuredGrants[i])) {
                // already granted - continue loop
                validGrants.put(configuredGrants[i], storedGrants.get(configuredGrants[i]));
            } else {
                // not granted yet
                Request grantRequest = getRequest(POST, GRAND_DEVICE_ENDPOINT);
                JSONObject body = new JSONObject();
                body.put("push_type", "android");
                body.put("registration_id", "");
                body.put("vercode", configuredGrants[i]);
                body.put("binding_type", "home");
                grantRequest.content(new StringContentProvider(body.toString(), "utf-8"));
                try {
                    ContentResponse cr = grantRequest.timeout(10000, TimeUnit.MILLISECONDS).send();
                    int status = cr.getStatus();
                    String response = cr.getContentAsString();
                    if (status == 200) {
                        logger.info("Device granted {}", response);
                        validGrants.put(configuredGrants[i], Instant.now().toString());
                    } else {
                        logger.warn("Device grant failed {} : {}", cr.getReason(), response);
                    }
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    logger.warn("Device grant failed - reason {}", e.getMessage());
                }

            }
        }
        persistence.put(GRANTS, validGrants);
        persist(persistenceId, persistence);
    }

    @Override
    public void requestToken() {
        logger.info("Request guest token");
        Request tokenRequest = getRequest(POST, VISITOR_ENDPOINT);
        JSONObject body = new JSONObject();
        body.put("visitor_id", visitorConfig.get().visitorId);
        body.put("app_id", APP_IDS.get(visitorConfig.get().region));
        body.put("registration_id", "");
        body.put("push_type", "android");
        body.put("lan_code", "EN");
        tokenRequest.content(new StringContentProvider(body.toString(), "utf-8"));
        try {
            ContentResponse cr = tokenRequest.timeout(10000, TimeUnit.MILLISECONDS).send();
            int status = cr.getStatus();
            if (status == 200) {
                String response = cr.getContentAsString();
                token = MSpaUtils.decodeNewToken(response);
                if (MSpaUtils.isTokenValid(token)) {
                    JSONObject tokenStore = MSpaUtils.token2Json(token);
                    String persistenceId = visitorConfig.get().visitorId;
                    JSONObject persistence = getStorage(persistenceId);
                    persistence.put(TOKEN, tokenStore);
                    persist(persistenceId, persistence);
                } else {
                    logger.warn("Failed to get token - reason {}", response);
                }
            } else {
                logger.warn("Failed to get token - reason {}", cr.getReason());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Failed to get token - reason {}", e.getMessage());
        }
    }
}
