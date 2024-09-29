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
package org.openhab.binding.mercedesme.internal.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.config.AccountConfiguration;
import org.openhab.binding.mercedesme.internal.discovery.MercedesMeDiscoveryService;
import org.openhab.binding.mercedesme.internal.server.AuthServer;
import org.openhab.binding.mercedesme.internal.server.AuthService;
import org.openhab.binding.mercedesme.internal.server.MBWebsocket;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.mbcarkit.proto.Client.ClientMessage;
import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdate;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinCommandStatusUpdatesByPID;

/**
 * The {@link AccountHandler} acts as Bridge between MercedesMe Account and the associated vehicles
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler implements AccessTokenRefreshListener {
    private static final String FEATURE_APPENDIX = "-features";
    private static final String COMMAND_APPENDIX = "-commands";

    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private final NetworkAddressService networkService;
    private final MercedesMeDiscoveryService discoveryService;
    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;
    private final Storage<String> storage;
    private final Map<String, VehicleHandler> activeVehicleHandlerMap = new HashMap<>();
    private final Map<String, VEPUpdate> vepUpdateMap = new HashMap<>();
    private final Map<String, Map<String, Object>> capabilitiesMap = new HashMap<>();

    private Optional<AuthServer> server = Optional.empty();
    private Optional<AuthService> authService = Optional.empty();
    private Optional<ScheduledFuture<?>> scheduledFuture = Optional.empty();

    private String capabilitiesEndpoint = "/v1/vehicle/%s/capabilities";
    private String commandCapabilitiesEndpoint = "/v1/vehicle/%s/capabilities/commands";
    private String poiEndpoint = "/v1/vehicle/%s/route";

    final MBWebsocket ws;
    Optional<AccountConfiguration> config = Optional.empty();
    @Nullable
    ClientMessage message;

    public AccountHandler(Bridge bridge, MercedesMeDiscoveryService mmds, HttpClient hc, LocaleProvider lp,
            StorageService store, NetworkAddressService nas) {
        super(bridge);
        discoveryService = mmds;
        networkService = nas;
        ws = new MBWebsocket(this);
        httpClient = hc;
        localeProvider = lp;
        storage = store.getStorage(Constants.BINDING_ID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        autodetectCallback();
        String configValidReason = configValid();
        if (!configValidReason.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configValidReason);
        } else {
            String callbackUrl = Utils.getCallbackAddress(config.get().callbackIP, config.get().callbackPort);
            thing.setProperty("callbackUrl", callbackUrl);
            server = Optional.of(new AuthServer(httpClient, config.get(), callbackUrl));
            authService = Optional
                    .of(new AuthService(this, httpClient, config.get(), localeProvider.getLocale(), storage));
            if (!server.get().start()) {
                String textKey = Constants.STATUS_TEXT_PREFIX + thing.getThingTypeUID().getId()
                        + Constants.STATUS_SERVER_RESTART;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        textKey + " [\"" + thing.getProperties().get("callbackUrl") + "\"]");
            } else {
                scheduledFuture = Optional.of(scheduler.scheduleWithFixedDelay(this::update, 0,
                        config.get().refreshInterval, TimeUnit.MINUTES));
            }
        }
    }

    public void update() {
        if (server.isPresent()) {
            if (!Constants.NOT_SET.equals(authService.get().getToken())) {
                ws.run();
            } else {
                // all failed - start manual authorization
                String textKey = Constants.STATUS_TEXT_PREFIX + thing.getThingTypeUID().getId()
                        + Constants.STATUS_AUTH_NEEDED;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        textKey + " [\"" + thing.getProperties().get("callbackUrl") + "\"]");
            }
        } else {
            // server not running - fix first
            String textKey = Constants.STATUS_TEXT_PREFIX + thing.getThingTypeUID().getId()
                    + Constants.STATUS_SERVER_RESTART;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, textKey);
        }
    }

    private void autodetectCallback() {
        // if Callback IP and Callback Port are not set => autodetect these values
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        Configuration updateConfig = super.editConfiguration();
        if (!updateConfig.containsKey("callbackPort")) {
            updateConfig.put("callbackPort", Utils.getFreePort());
        } else {
            Utils.addPort(config.get().callbackPort);
        }
        if (!updateConfig.containsKey("callbackIP")) {
            String ip = networkService.getPrimaryIpv4HostAddress();
            if (ip != null) {
                updateConfig.put("callbackIP", ip);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "@text/mercedesme.account.status.ip-autodetect-failure");
            }
        }
        super.updateConfiguration(updateConfig);
        // get new config after update
        config = Optional.of(getConfigAs(AccountConfiguration.class));
    }

    private String configValid() {
        config = Optional.of(getConfigAs(AccountConfiguration.class));
        String textKey = Constants.STATUS_TEXT_PREFIX + thing.getThingTypeUID().getId();
        if (Constants.NOT_SET.equals(config.get().callbackIP)) {
            return textKey + Constants.STATUS_IP_MISSING;
        } else if (config.get().callbackPort == -1) {
            return textKey + Constants.STATUS_PORT_MISSING;
        } else if (Constants.NOT_SET.equals(config.get().email)) {
            return textKey + Constants.STATUS_EMAIL_MISSING;
        } else if (Constants.NOT_SET.equals(config.get().region)) {
            return textKey + Constants.STATUS_REGION_MISSING;
        } else if (config.get().refreshInterval <= 01) {
            return textKey + Constants.STATUS_REFRESH_INVALID;
        } else {
            return Constants.EMPTY;
        }
    }

    @Override
    public void dispose() {
        if (server.isPresent()) {
            AuthServer authServer = server.get();
            authServer.stop();
            authServer.dispose();
            server = Optional.empty();
            Utils.removePort(config.get().callbackPort);
        }
        ws.interrupt();
        scheduledFuture.ifPresent(schedule -> {
            if (!schedule.isCancelled()) {
                schedule.cancel(true);
            }
        });
    }

    /**
     * https://next.openhab.org/javadoc/latest/org/openhab/core/auth/client/oauth2/package-summary.html
     */
    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
        if (!Constants.NOT_SET.equals(tokenResponse.getAccessToken())) {
            scheduler.schedule(this::update, 2, TimeUnit.SECONDS);
        } else if (server.isEmpty()) {
            // server not running - fix first
            String textKey = Constants.STATUS_TEXT_PREFIX + thing.getThingTypeUID().getId()
                    + Constants.STATUS_SERVER_RESTART;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, textKey);
        } else {
            // all failed - start manual authorization
            String textKey = Constants.STATUS_TEXT_PREFIX + thing.getThingTypeUID().getId()
                    + Constants.STATUS_AUTH_NEEDED;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    textKey + " [\"" + thing.getProperties().get("callbackUrl") + "\"]");
        }
    }

    @Override
    public String toString() {
        return Integer.toString(config.get().callbackPort);
    }

    public String getWSUri() {
        return Utils.getWebsocketServer(config.get().region);
    }

    public ClientUpgradeRequest getClientUpgradeRequest() {
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        request.setHeader("Authorization", authService.get().getToken());
        request.setHeader("X-SessionId", UUID.randomUUID().toString());
        request.setHeader("X-TrackingId", UUID.randomUUID().toString());
        request.setHeader("Ris-Os-Name", Constants.RIS_OS_NAME);
        request.setHeader("Ris-Os-Version", Constants.RIS_OS_VERSION);
        request.setHeader("Ris-Sdk-Version", Utils.getRisSDKVersion(config.get().region));
        request.setHeader("X-Locale",
                localeProvider.getLocale().getLanguage() + "-" + localeProvider.getLocale().getCountry()); // de-DE
        request.setHeader("User-Agent", Utils.getApplication(config.get().region));
        request.setHeader("X-Applicationname", Utils.getUserAgent(config.get().region));
        request.setHeader("Ris-Application-Version", Utils.getRisApplicationVersion(config.get().region));
        return request;
    }

    public void registerVin(String vin, VehicleHandler handler) {
        discoveryService.vehicleRemove(this, vin, handler.getThing().getThingTypeUID().getId());
        activeVehicleHandlerMap.put(vin, handler);
        VEPUpdate updateForVin = vepUpdateMap.get(vin);
        if (updateForVin != null) {
            handler.distributeContent(updateForVin);
        }
    }

    public void unregisterVin(String vin) {
        activeVehicleHandlerMap.remove(vin);
    }

    @SuppressWarnings("null")
    public void getVehicleCapabilities(String vin) {
        if (storage.containsKey(vin + FEATURE_APPENDIX)) {
            if (activeVehicleHandlerMap.containsKey(vin)) {
                activeVehicleHandlerMap.get(vin).setFeatureCapabilities(storage.get(vin + FEATURE_APPENDIX));
            }
        }
        if (storage.containsKey(vin + COMMAND_APPENDIX)) {
            if (activeVehicleHandlerMap.containsKey(vin)) {
                activeVehicleHandlerMap.get(vin).setCommandCapabilities(storage.get(vin + COMMAND_APPENDIX));
            }
        }
    }

    public boolean distributeVepUpdates(Map<String, VEPUpdate> map) {
        List<String> notFoundList = new ArrayList<>();
        map.forEach((key, value) -> {
            VehicleHandler h = activeVehicleHandlerMap.get(key);
            if (h != null) {
                h.distributeContent(value);
            } else {
                if (value.getFullUpdate()) {
                    vepUpdateMap.put(key, value);
                }
                notFoundList.add(key);
            }
        });
        notFoundList.forEach(vin -> {
            logger.trace("No VehicleHandler available for VIN {}", vin);
        });
        return notFoundList.isEmpty();
    }

    public void commandStatusUpdate(Map<String, AppTwinCommandStatusUpdatesByPID> updatesByVinMap) {
        updatesByVinMap.forEach((key, value) -> {
            VehicleHandler h = activeVehicleHandlerMap.get(key);
            if (h != null) {
                h.distributeCommandStatus(value);
            } else {
                logger.trace("No VehicleHandler available for VIN {}", key);
            }
        });
    }

    @SuppressWarnings("null")
    public void discovery(String vin) {
        if (activeVehicleHandlerMap.containsKey(vin)) {
            VehicleHandler vh = activeVehicleHandlerMap.get(vin);
            if (vh.getThing().getProperties().isEmpty()) {
                vh.getThing().setProperties(getStringCapabilities(vin));
            }
        } else {
            if (!capabilitiesMap.containsKey(vin)) {
                // only report new discovery if capabilities aren't discovered yet
                discoveryService.vehicleDiscovered(this, vin, getCapabilities(vin));
            }
        }
    }

    private Map<String, String> getStringCapabilities(String vin) {
        Map<String, Object> props = getCapabilities(vin);
        Map<String, String> stringProps = new HashMap<>();
        props.forEach((key, value) -> {
            stringProps.put(key, value.toString());
        });
        return stringProps;
    }

    private Map<String, Object> getCapabilities(String vin) {
        // check cache before hammering API
        Map<String, Object> m = capabilitiesMap.get(vin);
        if (m != null) {
            return m;
        }
        Map<String, Object> featureMap = new HashMap<>();
        try {
            // add vehicle capabilities
            String capabilitiesUrl = Utils.getRestAPIServer(config.get().region)
                    + String.format(capabilitiesEndpoint, vin);
            Request capabilitiesRequest = httpClient.newRequest(capabilitiesUrl);
            authService.get().addBasicHeaders(capabilitiesRequest);
            capabilitiesRequest.header("X-SessionId", UUID.randomUUID().toString());
            capabilitiesRequest.header("X-TrackingId", UUID.randomUUID().toString());
            capabilitiesRequest.header("Authorization", authService.get().getToken());

            ContentResponse capabilitiesResponse = capabilitiesRequest
                    .timeout(Constants.REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();

            String featureCapabilitiesJsonString = capabilitiesResponse.getContentAsString();
            if (!storage.containsKey(vin + FEATURE_APPENDIX)) {
                storage.put(vin + FEATURE_APPENDIX, featureCapabilitiesJsonString);
            }

            JSONObject jsonResponse = new JSONObject(featureCapabilitiesJsonString);
            JSONObject features = jsonResponse.getJSONObject("features");
            features.keySet().forEach(key -> {
                String value = features.get(key).toString();
                String newKey = Character.toUpperCase(key.charAt(0)) + key.substring(1);
                newKey = "feature" + newKey;
                featureMap.put(newKey, value);
            });

            // get vehicle type
            JSONObject vehicle = jsonResponse.getJSONObject("vehicle");
            JSONArray fuelTypes = vehicle.getJSONArray("fuelTypes");
            if (fuelTypes.length() > 1) {
                featureMap.put("vehicle", Constants.HYBRID);
            } else if ("ELECTRIC".equals(fuelTypes.get(0))) {
                featureMap.put("vehicle", Constants.BEV);
            } else {
                featureMap.put("vehicle", Constants.COMBUSTION);
            }

            // add command capabilities
            String commandCapabilitiesUrl = Utils.getRestAPIServer(config.get().region)
                    + String.format(commandCapabilitiesEndpoint, vin);
            Request commandCapabilitiesRequest = httpClient.newRequest(commandCapabilitiesUrl);
            authService.get().addBasicHeaders(commandCapabilitiesRequest);
            commandCapabilitiesRequest.header("X-SessionId", UUID.randomUUID().toString());
            commandCapabilitiesRequest.header("X-TrackingId", UUID.randomUUID().toString());
            commandCapabilitiesRequest.header("Authorization", authService.get().getToken());
            ContentResponse commandCapabilitiesResponse = commandCapabilitiesRequest
                    .timeout(Constants.REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();

            String commandCapabilitiesJsonString = commandCapabilitiesResponse.getContentAsString();
            if (!storage.containsKey(vin + COMMAND_APPENDIX)) {
                storage.put(vin + COMMAND_APPENDIX, commandCapabilitiesJsonString);
            }
            JSONObject commands = new JSONObject(commandCapabilitiesJsonString);
            JSONArray commandArray = commands.getJSONArray("commands");
            commandArray.forEach(object -> {
                String commandName = ((JSONObject) object).get("commandName").toString();
                String[] words = commandName.split("[\\W_]+");
                StringBuilder builder = new StringBuilder();
                builder.append("command");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    word = word.isEmpty() ? word
                            : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
                    builder.append(word);
                }
                String value = ((JSONObject) object).get("isAvailable").toString();
                featureMap.put(builder.toString(), value);
            });
            // store in cache
            capabilitiesMap.put(vin, featureMap);
            return featureMap;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.trace("Error retrieving capabilities: {}", e.getMessage());
            featureMap.clear();
        }
        return featureMap;
    }

    public void sendCommand(@Nullable ClientMessage cm) {
        if (cm != null) {
            ws.setCommand(cm);
        }
        scheduler.schedule(this::update, 2, TimeUnit.SECONDS);
    }

    public void keepAlive(boolean b) {
        ws.keepAlive(b);
    }

    @Override
    public void updateStatus(ThingStatus ts) {
        super.updateStatus(ts);
    }

    @Override
    public void updateStatus(ThingStatus ts, ThingStatusDetail tsd, @Nullable String tsdt) {
        super.updateStatus(ts, tsd, tsdt);
    }

    /**
     * Vehicle Actions
     *
     * @param poi
     */

    public void sendPoi(String vin, JSONObject poi) {
        String poiUrl = Utils.getRestAPIServer(config.get().region) + String.format(poiEndpoint, vin);
        Request poiRequest = httpClient.POST(poiUrl);
        authService.get().addBasicHeaders(poiRequest);
        poiRequest.header("X-SessionId", UUID.randomUUID().toString());
        poiRequest.header("X-TrackingId", UUID.randomUUID().toString());
        poiRequest.header("Authorization", authService.get().getToken());
        poiRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
        poiRequest.content(new StringContentProvider(poi.toString(), "utf-8"));

        try {
            ContentResponse cr = poiRequest.timeout(Constants.REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
            logger.trace("Send POI Response {} : {}", cr.getStatus(), cr.getContentAsString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.trace("Error Sending POI {}", e.getMessage());
        }
    }
}
