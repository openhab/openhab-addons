package org.openhab.binding.salusbinding.internal.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.util.Objects.requireNonNull;

public class ApacheHttpClient implements RestClient {
    private static final int MAX_TRY_TIMES = 3;
    private final Logger logger;
    private final String username;
    private final char[] password;
    private final String baseUrl;
    private final ObjectMapper mapper;

    private AuthToken authToken;
    private LocalDateTime authTokenExpireTime;

    public ApacheHttpClient(String username, char[] password, String baseUrl) {
        this(username, password, baseUrl, defaultMapper());
    }

    private static ObjectMapper defaultMapper() {
        var mapper = new ObjectMapper();
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    protected ApacheHttpClient(String username, char[] password, String baseUrl, ObjectMapper mapper) {
        this.username = requireNonNull(username, "username can not be null!");
        this.password = requireNonNull(password, "password can not be null!");
        this.baseUrl = removeTrailingSlash(requireNonNull(baseUrl, "baseUrl can not be null!"));
        this.mapper = mapper;
        logger = LoggerFactory.getLogger(ApacheHttpClient.class.getName() + "[" + username.replaceAll("\\.", "_") + "]");
    }

    private static String removeTrailingSlash(String str) {
        if (str != null && str.endsWith("/")) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    private void login(String username, char[] password) {
        logger.info("Login with username '{}'", username);
        var url = url("/users/sign_in.json");
        var method = "POST";
        try (var httpClient = HttpClientBuilder.create().build()) {
            var request = new HttpPost(url);
            var inputBody = mapper.writeValueAsString(
                    Map.of("user", Map.of("email", username, "password", new String(password)))
            );
            request.setEntity(new StringEntity(inputBody));
            var headers = Map.of("Accept", "application/json", "Content-Type", "application/json");
            headers.forEach(request::addHeader);
            var response = httpClient.execute(
                    request,
                    r -> {
                        var body = EntityUtils.toString(r.getEntity());
                        if (logger.isDebugEnabled()) {
                            // the first param in replaceAll is a regex
                            // so if there is '.' or something similar in password
                            // weird thing might happen
                            var safeInputBody = inputBody.replaceAll(new String(password), "***");
                            var reqHeaders = headers.entrySet()
                                    .stream()
                                    .map(entry -> String.format("%s: %s", entry.getKey(), entry.getValue()))
                                    .collect(Collectors.joining("\n"));
                            var respHeaders = Arrays.stream(r.getHeaders())
                                    .map(header -> String.format("%s: %s", header.getName(), header.getValue()))
                                    .collect(Collectors.joining("\n"));
                            logger.debug("""
                                                                        
                                    REQUEST:
                                    {} {}
                                    {}

                                    {}
                                                                        
                                    RESPONSE:
                                    Response code: {}
                                    {}
                                                                        
                                    {}
                                    """, method, url, reqHeaders, safeInputBody, r.getCode(), respHeaders, body);
                        }
                        return new Response<>(r.getCode(), body);
                    });
            if (response.statusCode() == 401) {
                throw new HttpUnauthorizedException(method, url);
            }
            if (response.statusCode() == 403) {
                throw new HttpForbiddenException(method, url);
            }
            if (response.statusCode() / 100 == 4) {
                throw new HttpClientException(response.statusCode(), method, url);
            }
            // Salus return 500 if you gonna pass wrong credentials 😑...
            if (response.statusCode() / 100 == 5) {
                throw new HttpServerException(response.statusCode(), method, url);
            }
            if (response.statusCode() != 200) {
                throw new HttpUnknownException(response.statusCode(), method, url);
            }
            authToken = mapper.readValue(response.body(), AuthToken.class);
            authTokenExpireTime = LocalDateTime.now().plusSeconds(authToken.expiresIn());
            logger.info("Correctly logged in for user {}, role={}, expires at {} ({} secs)",
                    username, authToken.role(),
                    authTokenExpireTime, authToken.expiresIn());
        } catch (IOException e) {
            throw new HttpIOException(method, url, e);
        }
    }

    @Override
    public Response<String> get(String url) {
        return get(url, 1);
    }

    private Response<String> get(String url, int time) {
        refreshAccessToken();
        var finalUrl = url(url, false);
        var method = "GET";
        try (var httpClient = HttpClientBuilder.create().build()) {
            var request = new HttpGet(finalUrl);
            var headers = Map.of(
                    "Accept", "application/json",
                    "Content-Type", "application/json",
                    "Authorization", "auth_token " + authToken.accessToken());
            headers.forEach(request::addHeader);
            var response = httpClient.execute(
                    request,
                    r -> {
                        var body = EntityUtils.toString(r.getEntity());
                        if (logger.isDebugEnabled()) {
                            var reqHeaders = headers.entrySet()
                                    .stream()
                                    .map(entry -> {
                                        if (entry.getKey().equals("Authorization")) {
                                            return String.format("%s: %s", entry.getKey(), "auth_token ***");
                                        }
                                        return String.format("%s: %s", entry.getKey(), entry.getValue());
                                    })
                                    .collect(Collectors.joining("\n"));
                            var respHeaders = Arrays.stream(r.getHeaders())
                                    .map(header -> String.format("\t%s: %s", header.getName(), header.getValue()))
                                    .collect(Collectors.joining("\n"));
                            var bodyOrNull = StringUtils.isBlank(body) ? "<EMPTY>" : body;
                            logger.debug("""
                                                                        
                                    >> REQUEST:
                                    {} {}
                                    {}

                                    << RESPONSE:
                                    Response code: {}
                                    {}
                                                                        
                                    {}
                                    """, method, finalUrl, reqHeaders, r.getCode(), respHeaders, bodyOrNull);
                        }
                        return new Response<>(r.getCode(), body);
                    });
            if ((response.statusCode() == 401 || response.statusCode() == 403) && time <= MAX_TRY_TIMES) {
                logger.warn("Got 401 when {} {}. Dropping OAuth tokens and trying one more time ({}/{})", method, finalUrl, time, MAX_TRY_TIMES);
                authToken = null;
                return get(url, time + 1);
            }
            return response;
        } catch (IOException e) {
            throw new HttpIOException(method, finalUrl, e);
        }
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

    @Override
    public String toString() {
        return "ApacheHttpClient{" + username + " at " + baseUrl + '}';
    }
}
