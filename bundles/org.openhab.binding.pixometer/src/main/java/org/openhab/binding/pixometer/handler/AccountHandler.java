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
package org.openhab.binding.pixometer.handler;

import static org.openhab.binding.pixometer.internal.PixometerBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pixometer.internal.config.PixometerAccountConfiguration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link AccountHandler} is responsible for handling the api connection and authorization (including token
 * refresh)
 *
 * @author Jerome Luckenbach - Initial contribution
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final int TOKEN_MIN_DIFF_MS = (int) TimeUnit.DAYS.toMillis(2);

    private @NonNullByDefault({}) String authToken;
    private int refreshInterval;
    private long tokenExpiryDate;

    public AccountHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to handle here currently
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Pixometer Accountservice");

        PixometerAccountConfiguration config = getConfigAs(PixometerAccountConfiguration.class);
        setRefreshInterval(config.refresh);
        String user = config.user;
        String password = config.password;
        String scope = "read"; // Prepared for config value

        // Check expiry date every Day and obtain new access token if difference is less then or equal to 2 days
        scheduler.scheduleWithFixedDelay(() -> {
            logger.debug("Checking if new access token is needed...");
            try {
                long difference = getTokenExpiryDate() - System.nanoTime();
                if (difference <= TOKEN_MIN_DIFF_MS) {
                    obtainAuthTokenAndExpiryDate(user, password, scope);
                }
            } catch (RuntimeException r) {
                logger.debug("Could not check token expiry date for Thing {}: {}", getThing().getUID(), r.getMessage(),
                        r);
            }
        }, 1, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);

        logger.debug("Refresh job scheduled to run every days. for '{}'", getThing().getUID());
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    /**
     * Request auth token with read or write access.
     * (Write access is prepared for a possible later usage for updating meters.)
     *
     * @param user The username to use
     * @param password The corresponding password
     * @param scope The granted scope on the api for the binding
     */
    private void obtainAuthTokenAndExpiryDate(String user, String password, String scope) {
        try {
            String url = API_BASE_URL + "v1/access-token/";
            Properties urlHeader = (Properties) new Properties().put("CONTENT-TYPE", "application/json");

            JsonObject httpBody = new JsonObject();
            httpBody.addProperty("username", user);
            httpBody.addProperty("password", password);
            httpBody.addProperty("scope", scope);

            InputStream content = new ByteArrayInputStream(httpBody.toString().getBytes(StandardCharsets.UTF_8));
            String urlResponse = HttpUtil.executeUrl("POST", url, urlHeader, content, "application/json", 2000);
            JsonObject responseJson = (JsonObject) JsonParser.parseString(urlResponse);

            if (responseJson.has(AUTH_TOKEN)) {
                // Store the expire date for automatic token refresh
                int expiresIn = Integer.parseInt(responseJson.get("expires_in").toString());
                setTokenExpiryDate(TimeUnit.SECONDS.toNanos(expiresIn));

                setAuthToken(responseJson.get(AUTH_TOKEN).toString().replace("\"", ""));

                updateStatus(ThingStatus.ONLINE);
                return;
            }

            String errorMsg = String.format("Invalid Api Response ( %s )", responseJson);

            throw new IOException(errorMsg);
        } catch (IOException e) {
            String errorMsg = String.format(
                    "Could not obtain auth token. Please check your configured account credentials. %s %s",
                    this.getThing().getUID(), e.getMessage());

            logger.debug(errorMsg, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMsg);
        }
    }

    /**
     * Getters and Setters
     */

    public String getAuthToken() {
        return authToken;
    }

    private void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    private void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public long getTokenExpiryDate() {
        return tokenExpiryDate;
    }

    private void setTokenExpiryDate(long expiresIn) {
        this.tokenExpiryDate = System.nanoTime() + expiresIn;
    }
}
