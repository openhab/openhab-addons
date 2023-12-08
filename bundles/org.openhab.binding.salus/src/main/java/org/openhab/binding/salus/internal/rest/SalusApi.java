package org.openhab.binding.salus.internal.rest;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableSortedMap;
import static java.util.Objects.requireNonNull;

public class SalusApi  {
    private static final int MAX_TIMES = 3;
    private final Logger logger;
    private static final TypeToken<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeToken<>() {
    };
    private static final TypeToken<List<Object>> LIST_TYPE_REFERENCE = new TypeToken<>() {
    };
    private final String username;
    private final char[] password;
    private final String baseUrl;
    private final RestClient restClient;
    private final Gson mapper;
    private AuthToken authToken;
    private LocalDateTime authTokenExpireTime;

    public SalusApi(String username,
                    char[] password,
                    String baseUrl,
                    RestClient restClient,
                    Gson mapper) {
        this.username = requireNonNull(username, "username");
        this.password = requireNonNull(password, "password");
        this.baseUrl = removeTrailingSlash(requireNonNull(baseUrl, "baseUrl"));
        this.restClient = requireNonNull(restClient, "restClient can not be null!");
        this.mapper = requireNonNull(mapper, "mapper can not be null!");
        // thanks to this, logger will always inform for which rest client it's doing the job
        // it's helpful when more than one SalusApi exists
        logger = LoggerFactory.getLogger(SalusApi.class.getName() + "[" + username.replaceAll("\\.", "_") + "]");
    }

    private static String removeTrailingSlash(String str) {
        if (str != null && str.endsWith("/")) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    private void login(String username, char[] password) {
        login(username, password, 1);
    }
    private void login(String username, char[] password, int times) {
        logger.info("Login with username '{}', times={}", username, times);
        authToken = null;
        authTokenExpireTime = null;
        var finalUrl = url("/users/sign_in.json");
        var method = "POST";
        var inputBody = mapper.toJson(
                Map.of("user",
                        Map.of("email", username, "password", new String(password))));
        var response = restClient.post(
                finalUrl,
                new RestClient.Content(inputBody, "application/json"),
                new RestClient.Header("Accept", "application/json"));
        if (response.statusCode() == 401) {
            throw new HttpUnauthorizedException(method, finalUrl);
        }
        if (response.statusCode() == 403) {
            if (times <= MAX_TIMES) {
                login(username, password, times+1);
                return;
            }
            throw new HttpForbiddenException(method, finalUrl);
        }
        if (response.statusCode() / 100 == 4) {
            throw new HttpClientException(response.statusCode(), method, finalUrl);
        }
        if (response.statusCode() / 100 == 5) {
            throw new HttpServerException(response.statusCode(), method, finalUrl);
        }
        if (response.statusCode() != 200) {
            throw new HttpUnknownException(response.statusCode(), method, finalUrl);
        }
        authToken = mapper.fromJson(response.body(), AuthToken.class);
        authTokenExpireTime = LocalDateTime.now().plusSeconds(authToken.expiresIn());
        logger.info("Correctly logged in for user {}, role={}, expires at {} ({} secs)",
                username, authToken.role(),
                authTokenExpireTime, authToken.expiresIn());
    }

    private void refreshAccessToken() {
        if (this.authToken == null) {
            login(username, password);
        } else if (expiredToken()) {
            login(username, password);
        } else if (shouldRefreshTokenBeforeExpire()) {
            refreshBeforeExpire();
        } else {
            logger.debug("Refreshing token is not required");
        }
    }

    private boolean expiredToken() {
        return LocalDateTime.now().isAfter(authTokenExpireTime);
    }

    private boolean shouldRefreshTokenBeforeExpire() {
        return false;
    }

    private void refreshBeforeExpire() {
        logger.warn("Refreshing token before expire is not supported!");
    }

    private String url(String url) {
        return url(url, true);
    }

    private String url(String url, boolean addTimestamp) {
        if (addTimestamp) {
            return baseUrl + url + buildTimestampParam();
        }
        return baseUrl + url;
    }

    private String buildTimestampParam() {
        return "?timestamp=" + System.currentTimeMillis();
    }

    public ApiResponse<SortedSet<Device>> findDevices() {
        logger.debug("findDevices()");
        refreshAccessToken();
        var response = restClient.get(url("/apiv1/devices.json"), authHeader());
        if (response.statusCode() != 200) {
            // there was an error when querying endpoint
            logger.debug("findDevices()->ERROR {}", response.statusCode());
            return ApiResponse.error(parseError(response));
        }

        var devices = tryParseBody(response.body(), LIST_TYPE_REFERENCE, List.of())
                .stream()
                .map(this::parseDevice)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(TreeSet::new));
        logger.debug("findDevices()->OK");
        return ApiResponse.ok(devices);
    }

    private RestClient.Header authHeader() {
        return new RestClient.Header("Authorization", "auth_token " + authToken.accessToken());
    }

    private Optional<Device> parseDevice(Object obj) {
        if (!(obj instanceof Map<?, ?> firstLevelMap)) {
            logger.warn("Cannot parse device, because object is not type of map!\n", obj);
            return Optional.empty();
        }

        if (!firstLevelMap.containsKey("device")) {
            if (logger.isWarnEnabled()) {
                var str = firstLevelMap.entrySet()
                        .stream()
                        .map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("\n"));
                logger.warn("Cannot parse device, because firstLevelMap does not have [device] key!\n{}", str);
            }
            return Optional.empty();
        }
        var objLevel2 = firstLevelMap.get("device");


        if (!(objLevel2 instanceof Map<?, ?> map)) {
            logger.warn("Cannot parse device, because object is not type of map!\n", obj);
            return Optional.empty();
        }

        // parse `dns`
        if (!map.containsKey("dsn")) {
            if (logger.isWarnEnabled()) {
                var str = map.entrySet()
                        .stream()
                        .map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("\n"));
                logger.warn("Cannot parse device, because map does not have [dsn] key!\n{}", str);
            }
            return Optional.empty();
        }
        var dsn = (String) map.get("dsn");

        // parse `name`
        if (!map.containsKey("product_name")) {
            if (logger.isWarnEnabled()) {
                var str = map.entrySet()
                        .stream()
                        .map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("\n"));
                logger.warn("Cannot parse device, because map does not have [product_name] key!\n{}", str);
            }
            return Optional.empty();
        }
        var name = (String) map.get("product_name");

        // parse `properties`
        var list = map.entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> !entry.getKey().equals("dns"))
                .filter(entry -> !entry.getKey().equals("name"))
                .map(entry -> Pair.of(entry.getKey().toString(), (Object) entry.getValue()))
                .toList();
        // this weird thing need to be done,
        // because `Collectors.toMap` does not support value=null
        // and in our case, sometimes the values are null
        SortedMap<String, Object> properties = new TreeMap<>();
        for (var entry : list) {
            properties.put(entry.getKey(), entry.getValue());
        }
        properties = unmodifiableSortedMap(properties);

        return Optional.of(new Device(dsn.trim(), name.trim(), properties));
    }

    public ApiResponse<SortedSet<DeviceProperty<?>>> findDeviceProperties(String dsn) {
        logger.debug("findDeviceProperties({})", dsn);
        refreshAccessToken();
        var response = restClient.get(url("/apiv1/dsns/" + dsn + "/properties.json"), authHeader());
        if (response.statusCode() != 200) {
            // there was an error when querying endpoint
            logger.debug("findDeviceProperties()->ERROR {}", response.statusCode());
            return ApiResponse.error(parseError(response));
        }

        var deviceProperties = new TreeSet<DeviceProperty<?>>();
        var objects = tryParseBody(response.body(), LIST_TYPE_REFERENCE, List.of());
        for (var obj : objects) {
            parseDeviceProperty(obj).ifPresent(deviceProperties::add);
        }

        logger.debug("findDeviceProperties({})->OK", dsn);
        return ApiResponse.ok(deviceProperties);
    }

    private Optional<DeviceProperty<?>> parseDeviceProperty(Object obj) {
        if (!(obj instanceof Map<?, ?> firstLevelMap)) {
            logger.warn("Cannot parse device property, because object is not type of map!\n", obj);
            return Optional.empty();
        }

        if (!firstLevelMap.containsKey("property")) {
            if (logger.isWarnEnabled()) {
                var str = firstLevelMap.entrySet()
                        .stream()
                        .map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("\n"));
                logger.warn("Cannot parse device property, because firstLevelMap does not have [property] key!\n{}", str);
            }
            return Optional.empty();
        }

        var objLevel2 = firstLevelMap.get("property");
        if (!(objLevel2 instanceof Map<?, ?> map)) {
            logger.warn("Cannot parse device property, because object is not type of map!\n", obj);
            return Optional.empty();
        }

        // name
        if (!map.containsKey("name")) {
            if (logger.isWarnEnabled()) {
                var str = map.entrySet()
                        .stream()
                        .map(entry -> format("%s=%s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("\n"));
                logger.warn("Cannot parse device property, because map does not have [name] key!\n{}", str);
            }
            return Optional.empty();
        }
        var name = (String) map.get("name");

        // other meaningful properties
        var baseType = findOrNull(map, "base_type");
        var readOnly = findBoolOrNull(map, "read_only");
        var direction = findOrNull(map, "direction");
        var dataUpdatedAt = findOrNull(map, "data_updated_at");
        var productName = findOrNull(map, "product_name");
        var displayName = findOrNull(map, "display_name");
        var value = findObjectOrNull(map, "value");

        // parse `properties`
        var list = map.entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> !entry.getKey().equals("name"))
                .filter(entry -> !entry.getKey().equals("base_type"))
                .filter(entry -> !entry.getKey().equals("read_only"))
                .filter(entry -> !entry.getKey().equals("direction"))
                .filter(entry -> !entry.getKey().equals("data_updated_at"))
                .filter(entry -> !entry.getKey().equals("product_name"))
                .filter(entry -> !entry.getKey().equals("display_name"))
                .filter(entry -> !entry.getKey().equals("value"))
                .map(entry -> Pair.of(entry.getKey().toString(), (Object) entry.getValue()))
                .toList();
        // this weird thing need to be done,
        // because `Collectors.toMap` does not support value=null
        // and in our case, sometimes the values are null
        SortedMap<String, Object> properties = new TreeMap<>();
        for (var entry : list) {
            properties.put(entry.getKey(), entry.getValue());
        }
        properties = unmodifiableSortedMap(properties);

        return Optional.of(buildDeviceProperty(name, baseType, value, readOnly, direction, dataUpdatedAt, productName, displayName, properties));
    }

    private DeviceProperty<?> buildDeviceProperty(String name, String baseType, Object value, Boolean readOnly, String direction, String dataUpdatedAt, String productName, String displayName, SortedMap<String, Object> properties) {
        if ("boolean".equalsIgnoreCase(baseType)) {
            Boolean bool;
            if (value == null) {
                bool = null;
            } else if (value instanceof Boolean) {
                bool = (Boolean) value;
            } else if (value instanceof Number) {
                bool = ((Number) value).longValue() != 0;
            } else if (value instanceof String) {
                bool = parseBoolean((String) value);
            } else {
                logger.warn("Cannot parse boolean from [" + value + "]");
                bool = null;
            }
            return new DeviceProperty.BooleanDeviceProperty(name, readOnly, direction, dataUpdatedAt, productName, displayName, bool, properties);
        }
        if ("integer".equalsIgnoreCase(baseType)) {
            Long longValue;
            if (value == null) {
                longValue = null;
            } else if (value instanceof Long) {
                longValue = (Long) value;
            } else if (value instanceof Number) {
                longValue = ((Number) value).longValue();
            } else if (value instanceof String string) {
                try {
                    longValue = parseLong(string);
                } catch (NumberFormatException ex) {
                    logger.warn("Cannot parse long from [" + value + "]", ex);
                    longValue = null;
                }
            } else {
                logger.warn("Cannot parse long from [" + value + "]");
                longValue = null;
            }
            return new DeviceProperty.LongDeviceProperty(name, readOnly, direction, dataUpdatedAt, productName, displayName, longValue, properties);
        }
        var string = value != null ? value.toString() : null;
        return new DeviceProperty.StringDeviceProperty(name, readOnly, direction, dataUpdatedAt, productName, displayName, string, properties);
    }

    private String findOrNull(Map<?, ?> map, String name) {
        if (!map.containsKey(name)) {
            return null;
        }
        return (String) map.get(name);
    }

    @SuppressWarnings("SameParameterValue")
    private Boolean findBoolOrNull(Map<?, ?> map, String name) {
        if (!map.containsKey(name)) {
            return null;
        }
        var value = map.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String string) {
            return parseBoolean(string);
        }
        return null;
    }

    @SuppressWarnings("SameParameterValue")
    private Object findObjectOrNull(Map<?, ?> map, String name) {
        if (!map.containsKey(name)) {
            return null;
        }
        return map.get(name);
    }

    @SuppressWarnings("SameParameterValue")
    private <T> T tryParseBody(String body, TypeToken<T> typeToken, T defaultValue) {
        try {
            return mapper.fromJson(body, typeToken);
        } catch (JsonSyntaxException e) {
            logger.warn("Error when parsing body!\n{}", body);
            return defaultValue;
        }
    }

    private Error parseError(RestClient.Response<String> response) {
        var map = tryParseBody(response.body(), MAP_TYPE_REFERENCE, Map.of());
        if (!map.containsKey("error")) {
            return new Error(response.statusCode(), "ERROR");
        }
        var errorValue = (String) map.get("error");
        try {
            return mapper.fromJson(errorValue, Error.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Cannot parse Error from string:\n{}", errorValue);
            return new Error(response.statusCode(), "<ERROR>");
        }
    }
}
