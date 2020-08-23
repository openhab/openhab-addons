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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.*;
import static org.openhab.binding.bmwconnecteddrive.internal.handler.HTTPConstants.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.discovery.ConnectedCarDiscovery;
import org.openhab.binding.bmwconnecteddrive.internal.dto.ConnectedDriveUserInfo;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ConnectedDriveBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConnectedDriveBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(ConnectedDriveBridgeHandler.class);
    private static final Gson GSON = new Gson();
    private HttpClient httpClient;
    private Token token = new Token();
    private BundleContext bundleContext;
    private ServiceRegistration<?> discoveryServiceRegstration;
    private ConnectedCarDiscovery discoveryService;
    private @Nullable ConnectedDriveConfiguration configuration;
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable String troubleshootFingerprint;
    private @Nullable String authUri;

    public ConnectedDriveBridgeHandler(Bridge bridge, HttpClient hc, BundleContext bc) {
        super(bridge);
        httpClient = hc;
        bundleContext = bc;
        discoveryService = new ConnectedCarDiscovery(this);
        discoveryServiceRegstration = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<>());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getIdWithoutGroup().equals(DISCOVERY_FINGERPRINT)) {
            if (command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {
                    if (troubleshootFingerprint != null) {
                        logger.warn("BMW ConnectedDrive Binding - Discovery Troubleshoot fingerprint - BEGIN");
                        logger.warn("{}", troubleshootFingerprint);
                        logger.warn("BMW ConnectedDrive Binding - Discovery Troubleshoot fingerprint - END");
                    } else {
                        logger.warn(
                                "BMW ConnectedDrive Binding - No Discovery Troubleshoot fingerprint available. Please check for valid username and password Settings for proper connection towards ConnectDrive");
                    }
                }
                // Switch back to off immediately
                updateState(channelUID, OnOffType.OFF);
            }
        } else if (channelUID.getIdWithoutGroup().equals(DISCOVERY_TRIGGER)) {
            if (command instanceof OnOffType) {
                if (command.equals(OnOffType.ON)) {
                    // trigger discovery again - helpful after the user performed some changes in the ConnectedDrive
                    // Portal and
                    // wants to refresh the changes
                    scheduler.schedule(this::getPortalData, 0, TimeUnit.SECONDS);
                }
                // Switch back to off immediately
                updateState(channelUID, OnOffType.OFF);
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        configuration = getConfigAs(ConnectedDriveConfiguration.class);
        if (configuration != null) {
            // generate URI for Authorization
            // https://customer.bmwgroup.com/one/app/oauth.js
            StringBuffer uri = new StringBuffer();
            uri.append("https://customer.bmwgroup.com");
            if (BimmerConstants.SERVER_NORTH_AMERICA.equals(configuration.region)) {
                uri.append("/gcdm/usa/oauth/authenticate");
            } else {
                uri.append("/gcdm/oauth/authenticate");
            }
            authUri = uri.toString();

            scheduler.schedule(this::getPortalData, 0, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null) {
            localRefreshJob.cancel(true);
        }
    }

    public void getPortalData() {
        getToken();
        String webAPIUrl = "https://" + BimmerConstants.SERVER_MAP.get(configuration.region)
                + "/webapi/v1/user/vehicles/";
        logger.info("Request {}", webAPIUrl);
        Request req = httpClient.newRequest(webAPIUrl);
        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_JSON);
        req.header(HttpHeader.AUTHORIZATION, token.getBearerToken());
        try {
            ContentResponse contentResponse = req.timeout(30, TimeUnit.SECONDS).send();
            logger.info("Status {}", contentResponse.getStatus());
            logger.info("Reason {}", contentResponse.getReason());
            logger.info("Info {}", contentResponse.getContentAsString());

            ConnectedDriveUserInfo userInfo = GSON.fromJson(contentResponse.getContentAsString(),
                    ConnectedDriveUserInfo.class);
            discoveryService.scan(userInfo);
            updateStatus(ThingStatus.ONLINE);
            if (userInfo.getVehicles() != null) {
                if (userInfo.getVehicles().isEmpty()) {
                    troubleshootFingerprint = "No Cars found in your ConnectedDrive Account";
                } else {
                    userInfo.getVehicles().forEach(entry -> {
                        entry.vin = "xxx";
                    });
                    troubleshootFingerprint = GSON.toJson(userInfo);
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Get Data Exception {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error during BMW ConnectedDrive Authorization " + e.getMessage());
        }
    }

    public String getRegionServer() {
        return BimmerConstants.SERVER_MAP.get(configuration.region);
    }

    public Token getToken() {
        if (token.isExpired() || !token.isValid()) {
            token = getNewToken();
        }
        return token;
    }

    /**
     * Authorize at BMW Connected Drive Portal and re
     *
     * @return
     */
    public Token getNewToken() {
        httpClient.setFollowRedirects(false);
        Request req = httpClient.POST(authUri);

        req.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED);
        req.header(HttpHeader.CONNECTION, KEEP_ALIVE);
        req.header(HttpHeader.HOST, BimmerConstants.SERVER_MAP.get(configuration.region));
        req.header(HttpHeader.AUTHORIZATION, BimmerConstants.AUTHORIZATION_VALUE);
        req.header(CREDENTIALS, BimmerConstants.CREDENTIAL_VALUES);

        MultiMap<String> dataMap = new MultiMap<String>();
        dataMap.add(CLIENT_ID, BimmerConstants.CLIENT_ID_VALUE);
        dataMap.add(RESPONSE_TYPE, TOKEN);
        dataMap.add(REDIRECT_URI, BimmerConstants.REDIRECT_URI_VALUE);
        dataMap.add(SCOPE, BimmerConstants.SCOPE_VALUES);
        dataMap.add(USERNAME, configuration.userName);
        dataMap.add(PASSWORD, configuration.password);
        String urlEncodedData = UrlEncoded.encode(dataMap, Charset.defaultCharset(), false);

        // logger.info("URL encoded data {}", urlEncodedData);
        // logger.info("Data size {} ", urlEncodedData.length());
        req.header("Content-Length", urlEncodedData.length() + "");
        req.content(new StringContentProvider(urlEncodedData));
        try {
            ContentResponse contentResponse = req.timeout(30, TimeUnit.SECONDS).send();
            logger.info("Status {} ", contentResponse.getStatus());
            // logger.info("Reason {} ", contentResponse.getReason());
            // logger.info("Encoding {} ", contentResponse.getEncoding());
            // logger.info("Content length {} ", contentResponse.getContent().length);
            // logger.info("Media Type {} ", contentResponse.getMediaType());
            HttpFields fields = contentResponse.getHeaders();
            HttpField field = fields.getField(HttpHeader.LOCATION);
            return getTokenFromUrl(field.getValue());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.warn("Auth Exception: {}", e.getMessage());
        }
        return new Token();
    }

    public Token getTokenFromUrl(String encodedUrl) {
        MultiMap<String> tokenMap = new MultiMap<String>();
        UrlEncoded.decodeTo(encodedUrl, tokenMap, StandardCharsets.US_ASCII);
        final Token token = new Token();
        tokenMap.forEach((key, value) -> {
            logger.info("Key {} Value {}", key, value);
            if (key.endsWith(ACCESS_TOKEN)) {
                token.setToken(value.get(0).toString());
            } else if (key.equals(EXPIRES_IN)) {
                logger.info("Expires {}", value.get(0).toString());
                token.setExpiration(Integer.parseInt(value.get(0).toString()));
            } else if (key.equals(TOKEN_TYPE)) {
                token.setType(value.get(0).toString());
            }
        });
        return token;
    }

    public void close() {
        discoveryServiceRegstration.unregister();
    }
}
