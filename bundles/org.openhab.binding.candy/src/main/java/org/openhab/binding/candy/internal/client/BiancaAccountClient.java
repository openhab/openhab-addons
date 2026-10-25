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
package org.openhab.binding.candy.internal.client;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.FutureResponseListener;
import org.eclipse.jetty.util.Fields;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.openhab.binding.candy.internal.BiancaConfiguration;
import org.openhab.binding.candy.internal.protocol.BiancaProgramCatalog;
import org.openhab.binding.candy.internal.protocol.BiancaProgramCatalog.Program;
import org.openhab.binding.candy.internal.protocol.CandyApplianceSelectionException;
import org.openhab.binding.candy.internal.protocol.CandyAuthenticationException;
import org.openhab.binding.candy.internal.protocol.CandyCodec;
import org.openhab.binding.candy.internal.protocol.CandyProtocolException;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;

/**
 * Retrieves appliance keys and program catalogs through simply-Fi's Salesforce login, without enrolling or modifying
 * appliances.
 * Each handler session owns an isolated cookie jar; blocking bootstrap I/O runs on an openHAB worker pool.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public class BiancaAccountClient {
    private static final URI AUTH = URI.create("https://account.candy-home.com/CandyApp");
    private static final URI API = URI.create("https://simply-fi.herokuapp.com");
    private static final String CLIENT_ID = "3MVG9QDx8IX8nP5T2Ha8ofvlmjKuido4mcuSVCv4GwStG0Lf84ccYQylvDYy9d_ZLtnyAPzJt4khJoNYn_QVB";
    private static final String CALLBACK = "candy://mobilesdk/detect/oauth/done";
    private static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_8 like Mac OS X) "
            + "AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148";
    private static final Set<String> LOGIN_HOSTS = Set.of("account.candy-home.com", "haiereurope.my.site.com");
    private static final Pattern CALLBACK_URL = Pattern.compile("candy://mobilesdk/detect/oauth/done[^\\s\"'<>]*");
    private static final Pattern SCRIPT_REDIRECT = Pattern
            .compile("window\\.location(?:\\.replace\\s*\\(\\s*|\\.href\\s*=\\s*)['\"]([^'\"]+)['\"]");

    private final Logger logger = LoggerFactory.getLogger(BiancaAccountClient.class);
    private final Gson gson = new GsonBuilder().setStrictness(Strictness.STRICT).create();
    private final HttpClient httpClient;
    private final URI auth;
    private final URI api;

    /** Creates an account session using an owned, unstarted client, never the shared HTTP client. */
    public BiancaAccountClient(HttpClient httpClient) {
        this(httpClient, AUTH, API);
    }

    BiancaAccountClient(HttpClient httpClient, URI auth, URI api) {
        this.httpClient = httpClient;
        this.auth = auth;
        this.api = api;
    }

    /** Fetches candidates for local verification; cancellation aborts active requests and cancels queued work. */
    public CompletableFuture<List<String>> fetchKeys(BiancaConfiguration configuration) {
        return submit(configuration, Login::loadKeys);
    }

    /** Fetches the catalog only for the appliance whose existing key was verified locally. */
    public CompletableFuture<List<Program>> fetchPrograms(BiancaConfiguration configuration, String matchedKey) {
        return submit(configuration, login -> login.loadPrograms(matchedKey));
    }

    private <T> CompletableFuture<T> submit(BiancaConfiguration configuration, AccountOperation<T> operation) {
        logger.debug("Queueing account key lookup (appliance selector configured: {})",
                !configuration.applianceId.isEmpty());
        CompletableFuture<T> result = new CompletableFuture<>();
        Login login = new Login(configuration.username.trim(), configuration.password, configuration.applianceId,
                result);
        var task = ThreadPoolManager.getPool("candy-account").submit(() -> {
            try {
                result.complete(resolve(login, operation));
            } catch (InterruptedException e) {
                logger.debug("Account key lookup interrupted");
                Thread.currentThread().interrupt();
                result.cancel(false);
            } catch (CandyProtocolException e) {
                logger.debug("Account key lookup failed ({})", e.getClass().getSimpleName());
                result.completeExceptionally(e);
            } catch (TimeoutException e) {
                logger.debug("Account key lookup timed out");
                result.completeExceptionally(new CandyProtocolException("@text/account-error-timeout", e));
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                logger.debug("Account HTTP transport failed ({})",
                        cause == null ? e.getClass().getSimpleName() : cause.getClass().getSimpleName());
                result.completeExceptionally(new CandyProtocolException("@text/account-error-transport", e));
            } catch (RuntimeException e) {
                logger.debug("Unexpected failure during account key lookup ({})", e.getClass().getSimpleName());
                result.completeExceptionally(e);
            } catch (Exception e) {
                logger.debug("Account HTTP client unavailable ({})", e.getClass().getSimpleName());
                result.completeExceptionally(new CandyProtocolException("@text/account-error-client", e));
            }
        });
        result.whenComplete((keys, failure) -> {
            if (result.isCancelled()) {
                logger.debug("Cancelling account key lookup");
                Request request = login.active.get();
                if (request != null) {
                    request.abort(new CancellationException("Account setup cancelled"));
                }
                task.cancel(false);
            }
        });
        return result;
    }

    private <T> T resolve(Login login, AccountOperation<T> operation) throws Exception {
        try {
            logger.debug("Starting isolated account HTTP client");
            httpClient.start();
            httpClient.getCookieStore().removeAll();
            // OAuth is handled here; retain 401 responses even when no WWW-Authenticate challenge is present.
            httpClient.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
            return operation.load(login);
        } finally {
            logger.debug("Stopping account HTTP client");
            boolean interrupted = Thread.interrupted();
            try {
                httpClient.stop();
            } catch (InterruptedException e) {
                interrupted = true;
                logger.warn("Candy account HTTP client shutdown was interrupted");
            } catch (Exception e) {
                logger.warn("Could not stop the Candy account HTTP client ({})", e.getClass().getSimpleName());
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @FunctionalInterface
    private interface AccountOperation<T> {
        T load(Login login) throws Exception;
    }

    private final class Login {
        private final String username;
        private final String password;
        private final String applianceId;
        private final CompletableFuture<?> result;
        private final AtomicReference<@Nullable Request> active = new AtomicReference<>();
        private final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(120);
        private final String state = UUID.randomUUID().toString();
        private int requests;
        private String bearerToken = "";

        private Login(String username, String password, String applianceId, CompletableFuture<?> result) {
            this.username = username;
            this.password = password;
            this.applianceId = applianceId;
            this.result = result;
        }

        private List<String> loadKeys()
                throws InterruptedException, ExecutionException, TimeoutException, CandyProtocolException {
            JsonArray root = appliances();
            logger.debug("Account inventory contains {} appliance entries", root.size());
            Set<String> keys = new LinkedHashSet<>();
            boolean matched = false;
            for (JsonElement element : root) {
                JsonObject appliance = unwrap(object(element));
                String id = text(appliance, "id");
                if (!applianceId.isEmpty() && !applianceId.equals(id)) {
                    continue;
                }
                matched = true;
                String key = text(appliance, "encryption_key");
                if (!CandyCodec.isSupportedKey(key) && id.matches("[A-Za-z0-9_-]{1,128}")) {
                    logger.debug("Requesting missing appliance key metadata");
                    try {
                        ContentResponse detail = request(
                                URI.create(api + "/api/v1/appliances/" + id + ".json?with_programs=0"), Map.of(),
                                bearerToken);
                        requireApiSuccess(detail);
                        key = text(unwrap(object(parse(detail))), "encryption_key");
                    } catch (CandyAuthenticationException e) {
                        throw e;
                    } catch (CandyProtocolException e) {
                        logger.debug("Optional appliance metadata could not be read ({})",
                                e.getClass().getSimpleName());
                        if (!applianceId.isEmpty()) {
                            throw e;
                        }
                    } catch (ExecutionException | TimeoutException e) {
                        logger.debug("Optional appliance metadata request failed ({})", e.getClass().getSimpleName());
                        // A partial inventory can still contain the key for the local washer.
                    }
                }
                if (CandyCodec.isSupportedKey(key)) {
                    keys.add(key);
                }
            }
            if (!applianceId.isEmpty() && !matched) {
                logger.debug("The configured appliance selector did not match the account inventory");
                throw new CandyApplianceSelectionException();
            }
            if (keys.isEmpty()) {
                logger.debug("Account lookup returned no supported appliance keys");
                throw new CandyProtocolException("@text/account-error-key");
            }
            logger.debug("Account key lookup completed with {} distinct supported candidates", keys.size());
            return List.copyOf(keys);
        }

        private JsonArray appliances()
                throws InterruptedException, ExecutionException, TimeoutException, CandyProtocolException {
            String refreshToken = login();
            logger.debug("Exchanging account refresh token for API access");
            ContentResponse tokenResponse = request(URI.create(auth + "/services/oauth2/token"), Map.of("grant_type",
                    "hybrid_refresh", "client_id", CLIENT_ID, "refresh_token", refreshToken, "format", "json"), "");
            if (tokenResponse.getStatus() == 429 || tokenResponse.getStatus() >= 500) {
                throw new CandyProtocolException("@text/account-error-transport");
            }
            if (tokenResponse.getStatus() != 200) {
                throw new CandyAuthenticationException();
            }
            JsonObject tokens = object(parse(tokenResponse));
            String bearer = text(tokens, "id_token");
            if (bearer.isEmpty()) {
                bearer = text(tokens, "access_token");
            }
            if (bearer.isBlank() || bearer.contains("\r") || bearer.contains("\n")) {
                logger.debug("Token exchange did not return a usable access token");
                throw new CandyAuthenticationException();
            }
            bearerToken = bearer;
            logger.debug("Account API access obtained; requesting appliance inventory");
            ContentResponse response = request(URI.create(api + "/api/v1/appliances.json?with_programs=1"), Map.of(),
                    bearer);
            requireApiSuccess(response);
            JsonElement root = parse(response);
            if (root.isJsonObject()) {
                root = root.getAsJsonObject().get("appliances");
            }
            if (root == null || !root.isJsonArray() || root.getAsJsonArray().size() > 50) {
                logger.warn("Account appliance inventory has an unsupported structure or exceeds supported bounds");
                throw new CandyProtocolException("@text/account-error-response");
            }
            return root.getAsJsonArray();
        }

        private List<Program> loadPrograms(String matchedKey) throws Exception {
            for (JsonElement element : appliances()) {
                JsonObject appliance = unwrap(object(element));
                String id = text(appliance, "id");
                if (!applianceId.isEmpty() && !applianceId.equals(id)) {
                    continue;
                }
                String candidateKey = text(appliance, "encryption_key");
                if (CandyCodec.isSupportedKey(candidateKey) && !matchedKey.equals(candidateKey)) {
                    continue;
                }
                if ((!CandyCodec.isSupportedKey(text(appliance, "encryption_key")) || !appliance.has("programs"))
                        && id.matches("[A-Za-z0-9_-]{1,128}")) {
                    try {
                        ContentResponse detail = request(
                                URI.create(api + "/api/v1/appliances/" + id + ".json?with_programs=1"), Map.of(),
                                bearerToken);
                        requireApiSuccess(detail);
                        appliance = unwrap(object(parse(detail)));
                    } catch (CandyAuthenticationException e) {
                        throw e;
                    } catch (CandyProtocolException e) {
                        if (!applianceId.isEmpty() || matchedKey.equals(candidateKey)) {
                            throw e;
                        }
                        logger.debug("Skipping optional catalog metadata for an unmatched appliance ({})",
                                e.getClass().getSimpleName());
                        continue;
                    } catch (ExecutionException | TimeoutException e) {
                        if (!applianceId.isEmpty() || matchedKey.equals(candidateKey)) {
                            throw e;
                        }
                        logger.debug("Skipping an unavailable catalog enrichment request for an unmatched appliance");
                        continue;
                    }
                }
                if (matchedKey.equals(text(appliance, "encryption_key"))) {
                    if (!"Bianca".equalsIgnoreCase(text(appliance, "interface_type"))) {
                        throw new CandyProtocolException("@text/catalog-error-interface");
                    }
                    JsonElement programs = appliance.get("programs");
                    if (programs == null) {
                        throw new CandyProtocolException("@text/catalog-error-response");
                    }
                    List<Program> catalog = BiancaProgramCatalog.parse(programs);
                    logger.debug("Loaded {} program entries for the locally matched Bianca", catalog.size());
                    return catalog;
                }
            }
            throw new CandyApplianceSelectionException();
        }

        private String login()
                throws InterruptedException, ExecutionException, TimeoutException, CandyProtocolException {
            logger.debug("Starting Salesforce account authorization");
            URI authorize = URI.create(auth + "/services/oauth2/authorize/expid_mobileCandy?"
                    + encode(Map.of("display", "touch", "response_type", "hybrid_token", "client_id", CLIENT_ID,
                            "scope", "api id openid refresh_token web", "redirect_uri", CALLBACK, "device_id",
                            "openhab", "state", state)));
            ContentResponse first = request(authorize, Map.of(), "");
            String location = first.getHeaders().get("Location");
            String source = location == null ? ""
                    : parameters(uri(authorize, location).getRawQuery()).getOrDefault("source", "");
            URI loginUri = authorize;
            ContentResponse page = first;
            if (!source.isEmpty()) {
                logger.debug("Authorization returned a login source; requesting the login form");
                String start = auth.getPath() + "/setup/secur/RemoteAccessAuthorizationPage.apexp?"
                        + encode(Map.of("source", source, "display", "touch"));
                loginUri = URI
                        .create(auth + "/login?" + encode(Map.of("display", "touch", "ec", "302", "startURL", start)));
                page = request(loginUri, Map.of(), "");
            }
            boolean submitted = false;
            for (int step = 0; step < 16; step++) {
                logger.debug("Processing account login step {} (HTTP {})", step + 1, page.getStatus());
                if (page.getStatus() == 429 || page.getStatus() >= 500) {
                    throw new CandyProtocolException("@text/account-error-transport");
                }
                String body = page.getContentAsString();
                String redirect = page.getHeaders().get("Location");
                Matcher callback = CALLBACK_URL.matcher(Parser.unescapeEntities(body, false));
                if (redirect != null && redirect.startsWith(CALLBACK)) {
                    return refreshToken(uri(loginUri, redirect));
                }
                while (callback.find()) {
                    URI candidate = uri(loginUri, callback.group());
                    if (parameters(candidate.getRawFragment()).containsKey("refresh_token")) {
                        return refreshToken(candidate);
                    }
                }
                Document document = Jsoup.parse(body, loginUri.toString());
                Element form = document.selectFirst("form:has(input[name=pw])");
                if (form != null) {
                    if (submitted) {
                        logger.debug("Account login returned to the credential form without completing authorization");
                        throw new CandyAuthenticationException();
                    }
                    URI action = form.attr("action").isEmpty() ? loginUri : uri(loginUri, form.absUrl("action"));
                    if (!sameOrigin(auth, action) || !(auth.getPath() + "/login").equals(action.getPath())) {
                        logger.warn("Refusing to submit account credentials to an unsupported login destination");
                        throw new CandyAuthenticationException("@text/account-error-redirect");
                    }
                    Map<String, String> fields = new LinkedHashMap<>();
                    for (Element input : form.select("input[name]")) {
                        fields.put(input.attr("name"), input.val());
                    }
                    fields.put("un", username);
                    fields.put("username", username);
                    fields.put("pw", password);
                    fields.put("hasRememberUn", "true");
                    fields.put("rememberUn", "on");
                    fields.putIfAbsent("lt", "standard");
                    fields.putIfAbsent("display", "touch");
                    logger.debug("Submitting account login form");
                    loginUri = action;
                    page = request(action, fields, "");
                    submitted = true;
                    continue;
                }
                if (redirect == null) {
                    Matcher script = SCRIPT_REDIRECT.matcher(body);
                    if (script.find()) {
                        redirect = Parser.unescapeEntities(script.group(1), false).replace("\\/", "/");
                    }
                }
                if (redirect == null) {
                    logger.warn(
                            "Account login requires an unsupported interactive step or its page structure has changed");
                    throw new CandyAuthenticationException();
                }
                URI next = uri(loginUri, redirect);
                if (CALLBACK.equals(next.getScheme() + "://" + next.getHost() + next.getPath())) {
                    return refreshToken(next);
                }
                loginUri = next;
                page = request(next, Map.of(), "");
            }
            logger.warn("Account login exceeded the supported number of redirect steps");
            throw new CandyAuthenticationException();
        }

        private String refreshToken(URI callback) throws CandyProtocolException {
            if (!"candy".equals(callback.getScheme()) || !"mobilesdk".equals(callback.getHost())
                    || !"/detect/oauth/done".equals(callback.getPath()) || callback.getUserInfo() != null
                    || callback.getPort() != -1) {
                logger.warn("Refusing an unsupported OAuth callback destination");
                throw new CandyAuthenticationException();
            }
            Map<String, String> values = parameters(callback.getRawFragment());
            String refresh = values.getOrDefault("refresh_token", "");
            if (refresh.isEmpty()) {
                logger.debug("OAuth callback did not include a refresh token");
                throw new CandyAuthenticationException();
            }
            if (!state.equals(values.get("state"))) {
                logger.warn("OAuth callback state validation failed; account authorization was rejected");
                throw new CandyAuthenticationException();
            }
            logger.debug("OAuth callback validated; account refresh token received");
            return refresh;
        }

        private ContentResponse request(URI target, Map<String, String> form, String bearer)
                throws InterruptedException, ExecutionException, TimeoutException, CandyProtocolException {
            if (result.isCancelled() || Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0 || ++requests > 40) {
                logger.debug("Account setup reached its time or request-count limit");
                throw new TimeoutException();
            }
            boolean apiRequest = !bearer.isEmpty();
            if (apiRequest ? !sameOrigin(api, target) : !trustedLogin(target)) {
                logger.warn("Refusing an account request to an unsupported destination");
                throw new CandyAuthenticationException("@text/account-error-redirect");
            }
            Request request = httpClient.newRequest(target).followRedirects(false)
                    .timeout(Math.min(remaining, TimeUnit.SECONDS.toNanos(10)), TimeUnit.NANOSECONDS)
                    .header("User-Agent", USER_AGENT).header("Accept-Language", "en-US");
            if (apiRequest) {
                request.header("Authorization", "Bearer " + bearer).header("Salesforce-Auth", "1").header("Brand", "0")
                        .header("Device-Language", "en").header("Device-Model", "openHAB")
                        .header("Device-Os", "Java 21").header("App-Version-Name", "3.14.1")
                        .header("App-Version-Code", "226");
            }
            if (!form.isEmpty()) {
                Fields fields = new Fields();
                form.forEach(fields::add);
                request.method("POST").content(new FormContentProvider(fields));
            }
            logger.debug("Account HTTP request {}: {} ({})", requests, form.isEmpty() ? "GET" : "POST",
                    apiRequest ? "appliance API" : "login");
            active.set(request);
            try {
                if (result.isCancelled()) {
                    throw new InterruptedException();
                }
                FutureResponseListener listener = new FutureResponseListener(request, 4 * 1024 * 1024);
                request.send(listener);
                ContentResponse response = listener.get(Math.min(remaining, TimeUnit.SECONDS.toNanos(10)),
                        TimeUnit.NANOSECONDS);
                logger.debug("Account HTTP response {}: status {}", requests, response.getStatus());
                return response;
            } finally {
                active.set(null);
                if (result.isCancelled() || Thread.currentThread().isInterrupted()) {
                    request.abort(new CancellationException("Account setup cancelled"));
                }
            }
        }
    }

    private boolean trustedLogin(URI target) {
        return target.getHost() != null && target.getUserInfo() == null && target.getFragment() == null
                && (sameOrigin(auth, target)
                        || "https".equals(target.getScheme()) && (target.getPort() == -1 || target.getPort() == 443)
                                && LOGIN_HOSTS.contains(target.getHost()));
    }

    private static boolean sameOrigin(URI expected, URI target) {
        return expected.getScheme().equals(target.getScheme()) && expected.getHost().equals(target.getHost())
                && expected.getPort() == target.getPort() && target.getUserInfo() == null;
    }

    private static URI uri(URI base, String value) throws CandyProtocolException {
        try {
            return base.resolve(value);
        } catch (IllegalArgumentException e) {
            throw new CandyAuthenticationException("@text/account-error-redirect", e);
        }
    }

    private static String encode(Map<String, String> values) {
        return values.entrySet().stream().map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
    }

    private static Map<String, String> parameters(@Nullable String encoded) throws CandyProtocolException {
        Map<String, String> values = new LinkedHashMap<>();
        if (encoded != null) {
            try {
                for (String pair : encoded.split("&")) {
                    String[] parts = pair.split("=", 2);
                    values.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                            parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "");
                }
            } catch (IllegalArgumentException e) {
                throw new CandyAuthenticationException("@text/account-error-response", e);
            }
        }
        return values;
    }

    private JsonElement parse(ContentResponse response) throws CandyProtocolException {
        try {
            JsonElement value = gson.fromJson(response.getContentAsString(), JsonElement.class);
            if (value != null) {
                return value;
            }
        } catch (JsonParseException e) {
            throw new CandyProtocolException("@text/account-error-response", e);
        }
        throw new CandyProtocolException("@text/account-error-response");
    }

    private static JsonObject object(JsonElement value) throws CandyProtocolException {
        if (!value.isJsonObject()) {
            throw new CandyProtocolException("@text/account-error-response");
        }
        return value.getAsJsonObject();
    }

    private static JsonObject unwrap(JsonObject value) throws CandyProtocolException {
        JsonElement appliance = value.get("appliance");
        return appliance == null ? value : object(appliance);
    }

    private static String text(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value != null && value.isJsonPrimitive() && !value.getAsJsonPrimitive().isBoolean() ? value.getAsString()
                : "";
    }

    private static void requireApiSuccess(ContentResponse response) throws CandyProtocolException {
        if (response.getStatus() == 401 || response.getStatus() == 403) {
            throw new CandyAuthenticationException();
        }
        if (response.getStatus() != 200) {
            throw new CandyProtocolException("@text/account-error-transport");
        }
    }
}
