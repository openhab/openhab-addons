/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.enera.internal.handler;

import static org.openhab.binding.enera.internal.EneraBindingConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.core.Response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.enera.internal.model.AuthenticationHeaderValue;
import org.openhab.binding.enera.internal.model.EneraAccount;
import org.openhab.binding.enera.internal.util.AuthenticationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cognitoidentity.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link EneraAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Oliver Rahner - Initial contribution
 */
@NonNullByDefault
public class EneraAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EneraAccountHandler.class);

    private Gson gson = new GsonBuilder().create();

    private AuthenticationResultType tokens = new AuthenticationResultType();
    private String liveUri = new String();
    /**
     * @return the liveUri
     */
    public String getLiveUri() {
        return liveUri;
    }

    

    public EneraAccountHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            signin();
            // needed to get Live URI
            getAccountData();
        });
    }

    public AuthenticationResultType signin() {
        String username = getThing().getConfiguration().get(CONFIG_USERNAME).toString();
        String password = getThing().getConfiguration().get(CONFIG_PASSWORD).toString();

        logger.trace(String.format("Trying to authenticate user '%s'", username));
        try {
            logger.trace("Creating AuthenticationHelper");
            AuthenticationHelper authHelper = new AuthenticationHelper(COGNITO_USER_POOL_ID, COGNITO_CLIENT_ID,
                    COGNITO_REGION);
            logger.trace("Performing SRP Authentication...");
            AuthenticationResultType authResult = authHelper.PerformSRPAuthentication(username, password);
            logger.trace("... authentication returned");

            if (authResult.getAccessToken() != null && !authResult.getAccessToken().isEmpty()) {
                tokens = authResult;
                // no setting ONLINE here, because we _need_ the live URL first!
                //updateStatus(ThingStatus.ONLINE);
                return tokens;
            } else {
                tokens = new AuthenticationResultType();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error during login! (Wrong credentials?)");
            }
        } catch (NotAuthorizedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("Invalid credentials. Server Message: %s", e.getMessage()));
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Error during login! " + e.getMessage());
        }
        tokens = new AuthenticationResultType();
        return new AuthenticationResultType();
    }

    public AuthenticationHeaderValue getAuthorizationHeader() {
        if (!this.isSignedIn()) {
            signin();
            if (!this.isSignedIn()) {
                return new AuthenticationHeaderValue();
            }
        }

        // check if Access Token expired and log in again if necessary
        // (Note: in a usual OAuth/OpenID flow we would use Refresh Tokens instead,
        // this is not necessary here because we are in posession of the user's credentials)
        DecodedJWT jwt = JWT.decode(tokens.getIdToken());
        if (jwt == null) {
            String message = "Error while decoding JWT token! This should not happen.";
            logger.warn(message);
            throw new IllegalStateException(message);
        }

        Date expirationDate = jwt.getExpiresAt();
        if (expirationDate == null) {
            String message = "Expiration date of JWT token is null! This should not happen.";
            logger.warn(message);
            throw new IllegalStateException(message);
        }

        if (expirationDate.before(new Date())) {
            signin();
        }

        return new AuthenticationHeaderValue(tokens.getTokenType(), tokens.getIdToken());
    }

    public EneraAccount getAccountData() {
        logger.trace("Retrieving Authorization token to get account data");
        if (!this.isSignedIn()) {
            logger.trace("I am not signed in. Bailing out.");
            return new EneraAccount();
        }
        AuthenticationHeaderValue auth = getAuthorizationHeader();
        HttpsURLConnection conn;
        logger.trace("Creating HTTPS connection to '{}'", ENERA_ACCOUNT_URL);
        try {
            conn = (HttpsURLConnection) new URL(ENERA_ACCOUNT_URL).openConnection();
        } catch (IOException ex) {
            logger.warn("Exception while creating HttpsURLConnection");
            logger.warn(ex.getMessage());
            return new EneraAccount();
        }

        String jsonResult = "";
        int responseCode;
        
        conn.setRequestProperty("Authorization", String.format("%s %s", auth.getScheme(), auth.getParameter()));
        conn.setRequestProperty("X-Mandant", ENERA_MANDANT);

        logger.trace("Connecting to '{}'", conn.getURL());
        try (InputStream in = conn.getInputStream(); ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            logger.trace("Read {} bytes", result.size());
            jsonResult = result.toString(StandardCharsets.UTF_8.name());
            responseCode = conn.getResponseCode();
            logger.trace("Response code is {}", responseCode);
        } catch (IOException ex) {
            logger.debug("Could not connect.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            return new EneraAccount();
        } finally {
            conn.disconnect();
        }

        if (responseCode != Response.Status.OK.getStatusCode()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.warn("Got non-OK HTTP status code '{}'", responseCode);
            return new EneraAccount();
        }
        /*
        Type listType = new TypeToken<ArrayList<EneraDevice>>() {
        }.getType();
        
        ArrayList<EneraDevice> deviceList = gson.fromJson(jsonResult, listType);
        */
        
        EneraAccount account = gson.fromJson(jsonResult, EneraAccount.class);
        this.liveUri = account.getLiveURI();
        if (this.liveUri != null && !this.liveUri.equals("")) {
            updateStatus(ThingStatus.ONLINE);
        }

        return account;
    }


    private boolean isSignedIn() {
        return this.tokens.getIdToken() != null && !this.tokens.getIdToken().equals("");
    }

}
