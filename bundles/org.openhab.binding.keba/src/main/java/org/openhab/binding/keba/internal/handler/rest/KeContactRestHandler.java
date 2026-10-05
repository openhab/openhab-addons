/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.keba.internal.handler.rest;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.keba.internal.handler.KeContactCombinedConfiguration;
import org.openhab.binding.keba.internal.handler.KeContactProtocolHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handler for KEBA's authenticated REST API.
 *
 * @author Michael Weger - Initial contribution
 * @author Michael Weger - Supplemental polling and lifecycle handling
 */
@NonNullByDefault
public class KeContactRestHandler extends KeContactProtocolHandler {

    private static final String API_PREFIX = "/v2";
    private static final int CONNECT_TIMEOUT_SECONDS = 5;

    private final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(KeContactRestHandler.class));
    private KeContactCombinedConfiguration config = new KeContactCombinedConfiguration();
    private @Nullable String serialNumber;
    private @Nullable HttpClient httpClient;
    private @Nullable String accessToken;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable Long lastWallboxPoll;
    private long nextSupplementalPoll;
    private final boolean supplemental;
    private final Object lifecycleLock = new Object();
    private final Object requestLock = new Object();
    private final AtomicInteger generation = new AtomicInteger();
    private boolean initializing;
    private boolean disposed;

    public KeContactRestHandler(Thing thing) {
        this(thing, null, null);
    }

    public KeContactRestHandler(Thing thing, @Nullable Configuration configuration, @Nullable Listener listener) {
        super(thing, configuration, listener);
        this.supplemental = listener != null;
    }

    @Override
    public void initialize() {
        KeContactCombinedConfiguration configuration = getConfigAs(KeContactCombinedConfiguration.class);
        config = configuration;
        String username = configuration.username;
        @Nullable
        String configuredPassword = configuration.password;
        boolean verifyCertificate = configuration.verifyCertificate;
        if (!configuration.restEnabled || configuration.ipAddress.isBlank() || configuration.restPort < 1
                || configuration.restPort > 65535 || configuredPassword == null || configuredPassword.isBlank()
                || configuration.refreshInterval < 10 || configuration.refreshIntervalSlow < 10) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.config-error-rest");
            return;
        }
        try {
            configuration.getRestBaseUrl();
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.config-error-rest");
            return;
        }
        String password = configuredPassword;
        int expectedGeneration;
        synchronized (lifecycleLock) {
            if (initializing || pollingJob != null) {
                return;
            }
            disposed = false;
            initializing = true;
            expectedGeneration = generation.incrementAndGet();
        }
        scheduler.execute(() -> {
            HttpClient client = createHttpClient(verifyCertificate);
            try {
                client.start();
                disableAuthenticationProtocolHandler(client);
                synchronized (lifecycleLock) {
                    if (expectedGeneration != generation.get()) {
                        client.stop();
                        return;
                    }
                    httpClient = client;
                }
                synchronized (requestLock) {
                    login(username, password);
                    String resolvedSerialNumber = discoverSerialNumber();
                    synchronized (lifecycleLock) {
                        if (expectedGeneration != generation.get()) {
                            return;
                        }
                        serialNumber = resolvedSerialNumber;
                        lastWallboxPoll = null;
                        nextSupplementalPoll = 0;
                        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                            if (expectedGeneration == generation.get()) {
                                synchronized (requestLock) {
                                    if (expectedGeneration == generation.get()) {
                                        poll();
                                    }
                                }
                            }
                        }, 0, 1, TimeUnit.SECONDS);
                        initializing = false;
                    }
                }
            } catch (Exception e) {
                logger.debug("REST initialization failed: {}", e.getMessage());
                synchronized (lifecycleLock) {
                    if (expectedGeneration == generation.get()) {
                        initializing = false;
                        httpClient = null;
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/offline.comm-error-rest [\"" + messageOf(e) + "\"]");
                    }
                }
                try {
                    client.stop();
                } catch (Exception stopError) {
                    logger.debug("Failed to stop REST client: {}", stopError.getMessage());
                }
            }
        });
    }

    public void retryInitialization() {
        synchronized (lifecycleLock) {
            if (disposed || initializing || pollingJob != null) {
                return;
            }
            initialize();
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        disposeCommunication();
        initialize();
    }

    @Override
    public void dispose() {
        disposeCommunication();
        super.dispose();
    }

    private void disposeCommunication() {
        HttpClient client;
        synchronized (lifecycleLock) {
            generation.incrementAndGet();
            disposed = true;
            initializing = false;
            ScheduledFuture<?> task = pollingJob;
            if (task != null) {
                task.cancel(false);
                pollingJob = null;
            }
            accessToken = null;
            serialNumber = null;
            client = httpClient;
            httpClient = null;
        }
        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.debug("Failed to stop REST client: {}", e.getMessage());
            }
        }
    }

    private void login() throws Exception {
        String username = config.username;
        @Nullable
        String password = config.password;
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("REST password is not configured");
        }
        login(username, password);
    }

    private void login(String username, String password) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);
        JsonObject response = request("/jwt/login", "POST", body.toString(), false);
        JsonElement token = response.get("accessToken");
        if (token == null || token.isJsonNull() || token.getAsString().isBlank()) {
            throw new IllegalStateException("REST login did not return an access token");
        }
        accessToken = token.getAsString();
    }

    private void poll() {
        long now = pollingTime();
        int interval = isPrimary() ? Math.min(config.refreshInterval, config.refreshIntervalSlow)
                : config.refreshIntervalSlow;
        @Nullable
        Long lastPoll = lastWallboxPoll;
        boolean wallboxDue = lastPoll == null || now - lastPoll >= TimeUnit.SECONDS.toNanos(interval);
        boolean supplementalDue = now >= nextSupplementalPoll;
        if (!wallboxDue && !supplementalDue) {
            return;
        }
        if (supplementalDue) {
            nextSupplementalPoll = now + TimeUnit.SECONDS.toNanos(config.refreshIntervalSlow);
        }
        try {
            if (wallboxDue) {
                lastWallboxPoll = now;
                JsonObject wallbox = request("/wallboxes/" + serialNumber(), "GET", null, true);
                updateStatus(ThingStatus.ONLINE);
                updateWallbox(wallbox);
            }
            if (supplementalDue && (!supplemental || isLinked("dipswitchinterpretation"))) {
                updateDipSwitchInterpretation();
            }
            if (supplementalDue && (!supplemental || isLinked("phaseswitchsource"))) {
                updatePhaseSwitchSource();
            }
            if (supplementalDue && (!supplemental || isLinked("sessionstart") || isLinked("sessionduration")
                    || isLinked("sessionconsumption"))) {
                try {
                    updateSession();
                } catch (Exception e) {
                    if (!supplemental) {
                        throw e;
                    }
                    logger.debug("Failed to read supplemental REST session: {}", e.getMessage());
                }
            }
            if (wallboxDue) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (Exception e) {
            logger.debug("REST polling failed: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-rest [\"" + messageOf(e) + "\"]");
        }
    }

    protected long pollingTime() {
        return System.nanoTime();
    }

    private void updateWallbox(JsonObject wallbox) {
        updateOptionalState("state", wallbox, "state", value -> new StringType(value));
        updateOptionalState("vehicle", wallbox, "vehiclePlugged", value -> OnOffType.from(Boolean.parseBoolean(value)));
        updateOptionalState("session", wallbox, "sessionActive", value -> OnOffType.from(Boolean.parseBoolean(value)));
        updateOptionalState("error", wallbox, "errorCode", StringType::new);
        updateOptionalState("reserved", wallbox, "reserved", value -> OnOffType.from(Boolean.parseBoolean(value)));
        updateOptionalState("input", wallbox, "x2active", value -> OnOffType.from(Boolean.parseBoolean(value)));
        updateOptionalState("authon", wallbox, "authorizationEnabled",
                value -> OnOffType.from(Boolean.parseBoolean(value)));
        updateOptionalState("externalmeter", wallbox, "hasExternalMeter",
                value -> OnOffType.from(Boolean.parseBoolean(value)));
        updateOptionalState("permanentlylocked", wallbox, "permanentlyLocked",
                value -> OnOffType.from(Boolean.parseBoolean(value)));
        updateOptionalState("maxphases", wallbox, "maxPhases", DecimalType::new);
        updateOptionalState("maxsupportedcurrent", wallbox, "maxCurrent",
                value -> new QuantityType<>(Double.parseDouble(value), Units.AMPERE));
        updateOptionalState("phaseconfiguration", wallbox, "phaseUsed", StringType::new);
        updateOptionalState("phaseswitchstate", wallbox, "phaseUsed",
                value -> new DecimalType(value.contains("_") ? 3 : 1));
        JsonElement dipSwitchSettings = wallbox.get("dipSwitchSettings");
        if (dipSwitchSettings != null && dipSwitchSettings.isJsonArray()) {
            updateState("dipswitchsettings", new StringType(dipSwitchSettings.toString()));
        }
        updateOptionalState("enableduser", wallbox, "state", value -> OnOffType.from(!"UNAVAILABLE".equals(value)));

        Map<String, String> properties = new HashMap<>();
        addOptionalProperty(properties, wallbox, "serialNumber", "serial");
        addOptionalProperty(properties, wallbox, "model", "model");
        addOptionalProperty(properties, wallbox, "firmwareVersion", "firmware");
        addOptionalProperty(properties, wallbox, "alias", "alias");
        addOptionalProperty(properties, wallbox, "ipAddress", "reportedIpAddress");
        addOptionalProperty(properties, wallbox, "macAddress", "macAddress");
        String connectionAddress = URI.create(normalizedBaseUrl()).getHost();
        if (connectionAddress != null) {
            properties.put("ipAddress", connectionAddress);
        }
        if (!properties.isEmpty()) {
            updateProperties(properties);
        }

        JsonObject meter = object(wallbox, "meter");
        if (meter == null) {
            return;
        }
        updateOptionalState("power", meter, "totalActivePower",
                value -> new QuantityType<>(Double.parseDouble(value) / 1000.0, Units.WATT));
        updateOptionalState("energy", meter, "meterValue",
                value -> new QuantityType<>(Double.parseDouble(value) / 1000.0, Units.WATT_HOUR));
        updateOptionalState("current", meter, "currentOffered",
                value -> new QuantityType<>(Double.parseDouble(value) / 1000.0, Units.AMPERE));
        updateOptionalState("powerfactor", meter, "totalPowerFactor",
                value -> new QuantityType<>(Double.parseDouble(value) / 10.0, Units.PERCENT));
        updateOptionalState("temperature", meter, "temperature",
                value -> new QuantityType<>(Double.parseDouble(value) / 100.0, SIUnits.CELSIUS));

        JsonArray lines = meter.getAsJsonArray("lines");
        if (lines == null) {
            return;
        }
        for (int index = 0; index < Math.min(lines.size(), 3); index++) {
            JsonObject line = lines.get(index).getAsJsonObject();
            String suffix = Integer.toString(index + 1);
            if (line.has("current")) {
                updateState("I" + suffix, new QuantityType<>(line.get("current").getAsDouble() / 1000.0, Units.AMPERE));
            }
            if (line.has("voltage")) {
                updateState("U" + suffix, new QuantityType<>(line.get("voltage").getAsDouble(), Units.VOLT));
            }
        }
    }

    private void updateSession() throws Exception {
        JsonObject response = request(
                "/sessions?limit=1&orderField=SESSION_START_DATE&orderDir=DESC&filters=SOCKET_SERIAL_NUMBER="
                        + serialNumber(),
                "GET", null, true);
        JsonArray sessions = response.getAsJsonArray("sessions");
        JsonObject session = sessions == null || sessions.isEmpty() ? null : sessions.get(0).getAsJsonObject();
        if (session == null) {
            return;
        }
        if (session.has("startDate")) {
            updateState("sessionstart", new DateTimeType(
                    Objects.requireNonNull(Instant.ofEpochMilli(session.get("startDate").getAsLong()))));
        }
        if (session.has("duration")) {
            updateState("sessionduration",
                    new QuantityType<>(session.get("duration").getAsDouble() / 1000.0, Units.SECOND));
        }
        if (session.has("energyConsumed")) {
            updateState("sessionconsumption",
                    new QuantityType<>(session.get("energyConsumed").getAsDouble() / 1000.0, Units.WATT_HOUR));
        }
    }

    private void handleAction(ChannelUID channelUID, OnOffType command) throws Exception {
        String channel = channelUID.getIdWithoutGroup();
        String wallboxPath = "/wallboxes/" + serialNumber();
        if ("enableduser".equals(channel)) {
            request(wallboxPath + "/change-availability", "POST", "{\"available\":" + (command == OnOffType.ON) + "}",
                    true);
            updateState(channel, command);
        } else if ("permanentlylocked".equals(channel)) {
            request(wallboxPath + "/permanently-lock", command == OnOffType.ON ? "POST" : "DELETE", null, true);
            updateState(channel, command);
        } else if (command == OnOffType.ON) {
            String operation = switch (channel) {
                case "unlock" -> wallboxPath + "/unlock";
                case "start" -> wallboxPath + "/start-charging";
                case "stop" -> wallboxPath + "/stop-charging";
                case "togglephaseswitch" -> wallboxPath + "/phase-toggle";
                case "reboot" -> wallboxPath + "/reboot";
                default -> null;
            };
            if (operation != null) {
                request(operation, "POST", null, true);
                updateState(channel, OnOffType.OFF);
            }
        }
    }

    private void updatePhaseSwitchSource() {
        try {
            JsonObject response = request("/configs/lmgmt/", "GET", null, true);
            JsonElement enabled = configurationValue(response, "connector_phase_enable");
            JsonElement source = configurationValue(response, "connector_phase_source");
            Integer channelValue = enabled != null && !enabled.isJsonNull() && !enabled.getAsBoolean()
                    ? Integer.valueOf(0)
                    : phaseSourceChannelValue(source);
            if (channelValue != null) {
                updateState("phaseswitchsource", new DecimalType(channelValue));
            }
        } catch (Exception e) {
            logger.debug("Failed to read REST phase switching source: {}", e.getMessage());
        }
    }

    private void updateDipSwitchInterpretation() {
        try {
            JsonObject dipSwitches = request("/wallboxes/dipswitch/" + serialNumber(), "GET", null, true);
            if (!dipSwitches.isEmpty()) {
                updateState("dipswitchinterpretation", new StringType(dipSwitches.toString()));
            }
        } catch (Exception e) {
            logger.debug("Failed to read REST DIP switch interpretation: {}", e.getMessage());
        }
    }

    private void setPhaseSwitchSource(int channelValue) throws Exception {
        JsonObject body = new JsonObject();
        JsonArray configs = new JsonArray();
        JsonObject enableConfig = new JsonObject();
        enableConfig.addProperty("key", "connector_phase_enable");
        enableConfig.addProperty("value", channelValue != 0);
        configs.add(enableConfig);
        if (channelValue != 0) {
            String source = switch (channelValue) {
                case 1 -> "CPM_PROFILES";
                case 2 -> "CPM_DIR_CTRL";
                case 3 -> "MODBUS";
                case 4 -> "UDP";
                default -> throw new IllegalArgumentException("Unsupported phase switching source: " + channelValue);
            };
            JsonObject sourceConfig = new JsonObject();
            sourceConfig.addProperty("key", "connector_phase_source");
            sourceConfig.addProperty("value", source);
            configs.add(sourceConfig);
        }
        body.add("configs", configs);
        request("/configs/lmgmt/", "PUT", body.toString(), true);
        updateState("phaseswitchsource", new DecimalType(channelValue));
    }

    private static @Nullable JsonElement configurationValue(JsonObject response, String key) {
        JsonElement configsElement = response.get("configs");
        if (configsElement != null && configsElement.isJsonArray()) {
            for (JsonElement element : configsElement.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    JsonObject configEntry = element.getAsJsonObject();
                    JsonElement configKey = configEntry.get("key");
                    if (configKey != null && key.equals(configKey.getAsString())) {
                        return configEntry.get("value");
                    }
                }
            }
        }
        JsonElement directValue = response.get(key);
        if (directValue != null) {
            return directValue;
        }
        JsonElement wrappedValue = response.get("value");
        return wrappedValue != null && wrappedValue.isJsonObject() ? wrappedValue.getAsJsonObject().get(key) : null;
    }

    private static @Nullable Integer phaseSourceChannelValue(@Nullable JsonElement source) {
        if (source == null || source.isJsonNull()) {
            return null;
        }
        return switch (source.getAsString().toUpperCase()) {
            case "CPM_PROFILES" -> 1;
            case "CPM_DIR_CTRL" -> 2;
            case "MODBUS" -> 3;
            case "UDP" -> 4;
            case "NONE", "DISABLED", "0" -> 0;
            default -> null;
        };
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::poll);
            return;
        }
        if (command instanceof DecimalType decimalCommand && "phaseswitchsource".equals(channelUID.getId())) {
            int source = decimalCommand.intValue();
            if (source < 0 || source > 4) {
                return;
            }
            try {
                setPhaseSwitchSource(source);
            } catch (Exception e) {
                logger.debug("REST phase switching source update failed: {}", e.getMessage());
            }
            return;
        }
        if (command instanceof OnOffType onOffCommand) {
            try {
                handleAction(channelUID, onOffCommand);
            } catch (Exception e) {
                logger.debug("REST command failed: {}", e.getMessage());
            }
        }
    }

    protected JsonObject request(String path, String method, @Nullable String body, boolean authenticated)
            throws Exception {
        return request(path, method, body, authenticated, true);
    }

    private JsonObject request(String path, String method, @Nullable String body, boolean authenticated,
            boolean retryAuthentication) throws Exception {
        HttpClient client = httpClient;
        if (client == null) {
            throw new IllegalStateException("REST client is not initialized");
        }
        String apiPath = "/serialnumber".equals(path) ? path : API_PREFIX + path;
        Request request = client.newRequest(URI.create(normalizedBaseUrl() + apiPath))
                .timeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS).header(HttpHeader.ACCEPT, "application/json");
        if (authenticated && accessToken != null) {
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken);
        }
        if ("POST".equals(method)) {
            request.method(HttpMethod.POST).content(new StringContentProvider(body == null ? "{}" : body),
                    "application/json");
        } else if ("PUT".equals(method)) {
            request.method(HttpMethod.PUT).content(new StringContentProvider(body == null ? "{}" : body),
                    "application/json");
        } else if ("DELETE".equals(method)) {
            request.method(HttpMethod.DELETE);
        } else {
            request.method(HttpMethod.GET);
        }
        ContentResponse response = request.send();
        if (response.getStatus() == 401 && authenticated && retryAuthentication) {
            login();
            return request(path, method, body, true, false);
        }
        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw new IllegalStateException("REST request returned HTTP " + response.getStatus());
        }
        String responseBody = response.getContentAsString();
        if (responseBody.isBlank()) {
            return new JsonObject();
        }
        JsonElement parsed = JsonParser.parseString(responseBody);
        if (parsed.isJsonObject()) {
            return Objects.requireNonNull(parsed.getAsJsonObject());
        }
        JsonObject wrapper = new JsonObject();
        wrapper.add("value", parsed);
        return wrapper;
    }

    private String discoverSerialNumber() throws Exception {
        JsonElement value = request("/serialnumber", "GET", null, false).get("value");
        if (value == null || value.isJsonNull() || value.getAsString().isBlank()) {
            throw new IllegalStateException("REST API did not return a serial number");
        }
        return Objects.requireNonNull(value.getAsString());
    }

    private String serialNumber() {
        String value = serialNumber;
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("REST serial number is not initialized");
        }
        return value;
    }

    private String normalizedBaseUrl() {
        return config.getRestBaseUrl();
    }

    private static HttpClient createHttpClient(boolean verifyCertificate) {
        return new HttpClient(new SslContextFactory.Client(!verifyCertificate));
    }

    private void disableAuthenticationProtocolHandler(HttpClient client) {
        client.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
    }

    private static String messageOf(Exception exception) {
        String message = exception.getMessage();
        return message == null ? Objects.requireNonNull(exception.getClass().getSimpleName()) : message;
    }

    private static @Nullable JsonObject object(JsonObject parent, String name) {
        JsonElement value = parent.get(name);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : null;
    }

    private static void addOptionalProperty(Map<String, String> properties, JsonObject object, String source,
            String target) {
        JsonElement value = object.get(source);
        if (value != null && !value.isJsonNull()) {
            properties.put(target, Objects.requireNonNull(value.getAsString()));
        }
    }

    private void updateOptionalState(String channel, JsonObject object, String property,
            java.util.function.Function<String, org.openhab.core.types.State> converter) {
        if (object.has(property) && !object.get(property).isJsonNull()) {
            updateState(channel, converter.apply(Objects.requireNonNull(object.get(property).getAsString())));
        }
    }
}
