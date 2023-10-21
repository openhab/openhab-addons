/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.myq.internal.handler;

import static org.openhab.binding.myq.internal.MyQBindingConstants.*;

import java.io.IOException;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openhab.binding.myq.internal.MyQDiscoveryService;
import org.openhab.binding.myq.internal.config.MyQAccountConfiguration;
import org.openhab.binding.myq.internal.dto.AccountDTO;
import org.openhab.binding.myq.internal.dto.AccountsDTO;
import org.openhab.binding.myq.internal.dto.DeviceDTO;
import org.openhab.binding.myq.internal.dto.DevicesDTO;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MyQAccountHandler} is responsible for communicating with the MyQ API based on an account.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MyQAccountHandler extends BaseBridgeHandler implements AccessTokenRefreshListener {
    private static final int REQUEST_TIMEOUT_MS = 10_000;

    /*
     * MyQ oAuth relate fields
     */
    private static final String CLIENT_SECRET = "VUQ0RFhuS3lQV3EyNUJTdw==";
    private static final String CLIENT_ID = "ANDROID_CGI_MYQ";
    private static final String REDIRECT_URI = "com.myqops://android";
    private static final String SCOPE = "MyQ_Residential offline_access";
    /*
     * MyQ authentication API endpoints
     */
    private static final String LOGIN_BASE_URL = "https://partner-identity.myq-cloud.com";
    private static final String LOGIN_AUTHORIZE_URL = LOGIN_BASE_URL + "/connect/authorize";
    private static final String LOGIN_TOKEN_URL = LOGIN_BASE_URL + "/connect/token";
    // this should never happen, but lets be safe and give up after so many redirects
    private static final int LOGIN_MAX_REDIRECTS = 30;
    /*
     * MyQ device and account API endpoints
     */
    private static final String ACCOUNTS_URL = "https://accounts.myq-cloud.com/api/v6.0/accounts";
    private static final String DEVICES_URL = "https://devices.myq-cloud.com/api/v5.2/Accounts/%s/Devices";
    private static final String CMD_LAMP_URL = "https://account-devices-lamp.myq-cloud.com/api/v5.2/Accounts/%s/lamps/%s/%s";
    private static final String CMD_DOOR_URL = "https://account-devices-gdo.myq-cloud.com/api/v5.2/Accounts/%s/door_openers/%s/%s";

    private static final Integer RAPID_REFRESH_SECONDS = 5;
    private final Logger logger = LoggerFactory.getLogger(MyQAccountHandler.class);
    private final Gson gsonLowerCase = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private final OAuthFactory oAuthFactory;
    private @Nullable Future<?> normalPollFuture;
    private @Nullable Future<?> rapidPollFuture;
    private @Nullable AccountsDTO accounts;

    private List<DeviceDTO> devicesCache = new ArrayList<DeviceDTO>();
    private @Nullable OAuthClientService oAuthService;
    private Integer normalRefreshSeconds = 60;
    private HttpClient httpClient;
    private String username = "";
    private String password = "";
    private String userAgent = "";
    // force login, even if we have a token
    private boolean needsLogin = false;

    public MyQAccountHandler(Bridge bridge, HttpClient httpClient, final OAuthFactory oAuthFactory) {
        super(bridge);
        this.httpClient = httpClient;
        this.oAuthFactory = oAuthFactory;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        MyQAccountConfiguration config = getConfigAs(MyQAccountConfiguration.class);
        normalRefreshSeconds = config.refreshInterval;
        username = config.username;
        password = config.password;
        // MyQ can get picky about blocking user agents apparently
        userAgent = ""; // no agent string
        needsLogin = true;
        updateStatus(ThingStatus.UNKNOWN);
        restartPolls(false);
    }

    @Override
    public void dispose() {
        stopPolls();
        OAuthClientService oAuthService = this.oAuthService;
        if (oAuthService != null) {
            oAuthService.removeAccessTokenRefreshListener(this);
            oAuthFactory.ungetOAuthService(getThing().toString());
            this.oAuthService = null;
        }
    }

    @Override
    public void handleRemoval() {
        oAuthFactory.deleteServiceAndAccessToken(getThing().toString());
        super.handleRemoval();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MyQDiscoveryService.class);
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        List<DeviceDTO> localDeviceCaches = devicesCache;
        if (childHandler instanceof MyQDeviceHandler deviceHandler) {
            localDeviceCaches.stream().filter(d -> deviceHandler.getSerialNumber().equalsIgnoreCase(d.serialNumber))
                    .findFirst().ifPresent(deviceHandler::handleDeviceUpdate);
        }
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
        logger.debug("Auth Token Refreshed, expires in {}", tokenResponse.getExpiresIn());
    }

    /**
     * Sends a door action to the MyQ API
     *
     * @param device
     * @param action
     */
    public void sendDoorAction(DeviceDTO device, String action) {
        sendAction(device, action, CMD_DOOR_URL);
    }

    /**
     * Sends a lamp action to the MyQ API
     *
     * @param device
     * @param action
     */
    public void sendLampAction(DeviceDTO device, String action) {
        sendAction(device, action, CMD_LAMP_URL);
    }

    private void sendAction(DeviceDTO device, String action, String urlFormat) {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Account offline, ignoring action {}", action);
            return;
        }

        try {
            ContentResponse response = sendRequest(
                    String.format(urlFormat, device.accountId, device.serialNumber, action), HttpMethod.PUT, null,
                    null);
            if (HttpStatus.isSuccess(response.getStatus())) {
                restartPolls(true);
            } else {
                logger.debug("Failed to send action {} : {}", action, response.getContentAsString());
            }
        } catch (InterruptedException | MyQCommunicationException | MyQAuthenticationException e) {
            logger.debug("Could not send action", e);
        }
    }

    /**
     * Last known state of MyQ Devices
     *
     * @return cached MyQ devices
     */
    public @Nullable List<DeviceDTO> devicesCache() {
        return devicesCache;
    }

    private void stopPolls() {
        stopNormalPoll();
        stopRapidPoll();
    }

    private synchronized void stopNormalPoll() {
        stopFuture(normalPollFuture);
        normalPollFuture = null;
    }

    private synchronized void stopRapidPoll() {
        stopFuture(rapidPollFuture);
        rapidPollFuture = null;
    }

    private void stopFuture(@Nullable Future<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    private synchronized void restartPolls(boolean rapid) {
        stopPolls();
        if (rapid) {
            normalPollFuture = scheduler.scheduleWithFixedDelay(this::normalPoll, 35, normalRefreshSeconds,
                    TimeUnit.SECONDS);
            rapidPollFuture = scheduler.scheduleWithFixedDelay(this::rapidPoll, 3, RAPID_REFRESH_SECONDS,
                    TimeUnit.SECONDS);
        } else {
            normalPollFuture = scheduler.scheduleWithFixedDelay(this::normalPoll, 0, normalRefreshSeconds,
                    TimeUnit.SECONDS);
        }
    }

    private void normalPoll() {
        stopRapidPoll();
        fetchData();
    }

    private void rapidPoll() {
        fetchData();
    }

    private synchronized void fetchData() {
        try {
            if (accounts == null) {
                getAccounts();
            }
            getDevices();
        } catch (MyQCommunicationException e) {
            logger.debug("MyQ communication error", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (MyQAuthenticationException e) {
            logger.debug("MyQ authentication error", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            stopPolls();
        } catch (InterruptedException e) {
            // we were shut down, ignore
        }
    }

    /**
     * This attempts to navigate the MyQ oAuth login flow in order to obtain a @AccessTokenResponse
     *
     * @return AccessTokenResponse token
     * @throws InterruptedException
     * @throws MyQCommunicationException
     * @throws MyQAuthenticationException
     */
    private AccessTokenResponse login()
            throws InterruptedException, MyQCommunicationException, MyQAuthenticationException {
        try {
            // make sure we have a fresh session
            URI authUri = new URI(LOGIN_BASE_URL);
            CookieStore store = httpClient.getCookieStore();
            store.get(authUri).forEach(cookie -> {
                store.remove(authUri, cookie);
            });

            String codeVerifier = generateCodeVerifier();

            ContentResponse loginPageResponse = getLoginPage(codeVerifier);

            // load the login page to get cookies and form parameters
            Document loginPage = Jsoup.parse(loginPageResponse.getContentAsString());
            Element form = loginPage.select("form").first();
            Element requestToken = loginPage.select("input[name=__RequestVerificationToken]").first();
            Element returnURL = loginPage.select("input[name=ReturnUrl]").first();

            if (form == null || requestToken == null) {
                throw new MyQCommunicationException("Could not load login page");
            }

            // url that the form will submit to
            String action = LOGIN_BASE_URL + form.attr("action");

            // post our user name and password along with elements from the scraped form
            String location = postLogin(action, requestToken.attr("value"), returnURL.attr("value"));
            if (location == null) {
                throw new MyQAuthenticationException("Could not login with credentials");
            }

            // finally complete the oAuth flow and retrieve a JSON oAuth token response
            ContentResponse tokenResponse = getLoginToken(location, codeVerifier);
            String loginToken = tokenResponse.getContentAsString();

            try {
                AccessTokenResponse accessTokenResponse = gsonLowerCase.fromJson(loginToken, AccessTokenResponse.class);
                if (accessTokenResponse == null) {
                    throw new MyQAuthenticationException("Could not parse token response");
                }
                getOAuthService().importAccessTokenResponse(accessTokenResponse);
                return accessTokenResponse;
            } catch (JsonSyntaxException e) {
                throw new MyQCommunicationException("Invalid Token Response " + loginToken);
            }
        } catch (IOException | ExecutionException | TimeoutException | OAuthException | URISyntaxException e) {
            throw new MyQCommunicationException(e.getMessage());
        }
    }

    private void getAccounts() throws InterruptedException, MyQCommunicationException, MyQAuthenticationException {
        ContentResponse response = sendRequest(ACCOUNTS_URL, HttpMethod.GET, null, null);
        accounts = parseResultAndUpdateStatus(response, gsonLowerCase, AccountsDTO.class);
    }

    private void getDevices() throws InterruptedException, MyQCommunicationException, MyQAuthenticationException {
        AccountsDTO localAccounts = accounts;
        if (localAccounts == null) {
            return;
        }

        List<DeviceDTO> currentDevices = new ArrayList<DeviceDTO>();

        for (AccountDTO account : localAccounts.accounts) {
            ContentResponse response = sendRequest(String.format(DEVICES_URL, account.id), HttpMethod.GET, null, null);
            DevicesDTO devices = parseResultAndUpdateStatus(response, gsonLowerCase, DevicesDTO.class);
            currentDevices.addAll(devices.items);
            devices.items.forEach(device -> {
                ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, device.deviceFamily);
                if (SUPPORTED_DISCOVERY_THING_TYPES_UIDS.contains(thingTypeUID)) {
                    for (Thing thing : getThing().getThings()) {
                        ThingHandler handler = thing.getHandler();
                        if (handler != null && ((MyQDeviceHandler) handler).getSerialNumber()
                                .equalsIgnoreCase(device.serialNumber)) {
                            ((MyQDeviceHandler) handler).handleDeviceUpdate(device);
                        }
                    }
                }
            });
        }
        devicesCache = currentDevices;
    }

    private synchronized ContentResponse sendRequest(String url, HttpMethod method, @Nullable ContentProvider content,
            @Nullable String contentType)
            throws InterruptedException, MyQCommunicationException, MyQAuthenticationException {
        AccessTokenResponse tokenResponse = null;
        // if we don't need to force a login, attempt to use the token we have
        if (!needsLogin) {
            try {
                tokenResponse = getOAuthService().getAccessTokenResponse();
            } catch (OAuthException | IOException | OAuthResponseException e) {
                // ignore error, will try to login below
                logger.debug("Error accessing token, will attempt to login again", e);
            }
        }

        // if no token, or we need to login, do so now
        if (tokenResponse == null) {
            tokenResponse = login();
            needsLogin = false;
        }

        Request request = httpClient.newRequest(url).method(method).agent(userAgent)
                .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .header("Authorization", authTokenHeader(tokenResponse));
        if (content != null & contentType != null) {
            request = request.content(content, contentType);
        }

        // use asyc jetty as the API service will response with a 401 error when credentials are wrong,
        // but not a WWW-Authenticate header which causes Jetty to throw a generic execution exception which
        // prevents us from knowing the response code
        logger.trace("Sending {} to {}", request.getMethod(), request.getURI());
        final CompletableFuture<ContentResponse> futureResult = new CompletableFuture<>();
        request.send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(Result result) {
                Response response = result.getResponse();
                futureResult.complete(new HttpContentResponse(response, getContent(), getMediaType(), getEncoding()));
            }
        });

        try {
            ContentResponse result = futureResult.get();
            logger.trace("Account Response - status: {} content: {}", result.getStatus(), result.getContentAsString());
            return result;
        } catch (ExecutionException e) {
            throw new MyQCommunicationException(e.getMessage());
        }
    }

    private <T> T parseResultAndUpdateStatus(ContentResponse response, Gson parser, Class<T> classOfT)
            throws MyQCommunicationException {
        if (HttpStatus.isSuccess(response.getStatus())) {
            try {
                T responseObject = parser.fromJson(response.getContentAsString(), classOfT);
                if (responseObject != null) {
                    if (getThing().getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                    return responseObject;
                } else {
                    throw new MyQCommunicationException("Bad response from server");
                }
            } catch (JsonSyntaxException e) {
                throw new MyQCommunicationException("Invalid JSON Response " + response.getContentAsString());
            }
        } else if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
            // our tokens no longer work, will need to login again
            needsLogin = true;
            throw new MyQCommunicationException("Token was rejected for request");
        } else {
            throw new MyQCommunicationException(
                    "Invalid Response Code " + response.getStatus() + " : " + response.getContentAsString());
        }
    }

    /**
     * Returns the MyQ login page which contains form elements and cookies needed to login
     *
     * @param codeVerifier
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    private ContentResponse getLoginPage(String codeVerifier)
            throws InterruptedException, ExecutionException, TimeoutException {
        try {
            Request request = httpClient.newRequest(LOGIN_AUTHORIZE_URL) //
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS) //
                    .param("client_id", CLIENT_ID) //
                    .param("code_challenge", generateCodeChallange(codeVerifier)) //
                    .param("code_challenge_method", "S256") //
                    .param("redirect_uri", REDIRECT_URI) //
                    .param("response_type", "code") //
                    .param("scope", SCOPE) //
                    .agent(userAgent).followRedirects(true);
            request.header("Accept", "\"*/*\"");
            request.header("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":").getBytes()));
            logger.debug("Sending {} to {}", request.getMethod(), request.getURI());
            ContentResponse response = request.send();
            logger.debug("Login Code {} Response {}", response.getStatus(), response.getContentAsString());
            return response;
        } catch (NoSuchAlgorithmException e) {
            throw new ExecutionException(e.getCause());
        }
    }

    /**
     * Sends configured credentials and elements from the login page in order to obtain a redirect location header value
     *
     * @param url
     * @param requestToken
     * @param returnURL
     * @return The location header value
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @Nullable
    private String postLogin(String url, String requestToken, String returnURL)
            throws InterruptedException, ExecutionException, TimeoutException {
        /*
         * on a successful post to this page we will get several redirects, and a final 301 to:
         * com.myqops://ios?code=0123456789&scope=MyQ_Residential%20offline_access&iss=https%3A%2F%2Fpartner-identity.
         * myq-cloud.com
         *
         * We can then take the parameters out of this location and continue the process
         */
        Fields fields = new Fields();
        fields.add("Email", username);
        fields.add("Password", password);
        fields.add("__RequestVerificationToken", requestToken);
        fields.add("ReturnUrl", returnURL);

        Request request = httpClient.newRequest(url).method(HttpMethod.POST) //
                .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS) //
                .content(new FormContentProvider(fields)) //
                .agent(userAgent) //
                .followRedirects(false);
        setCookies(request);

        logger.debug("Posting Login to {}", url);
        ContentResponse response = request.send();

        String location = null;

        // follow redirects until we match our REDIRECT_URI or hit a redirect safety limit
        for (int i = 0; i < LOGIN_MAX_REDIRECTS && HttpStatus.isRedirection(response.getStatus()); i++) {

            String loc = response.getHeaders().get("location");
            if (logger.isTraceEnabled()) {
                logger.trace("Redirect Login: Code {} Location Header: {} Response {}", response.getStatus(), loc,
                        response.getContentAsString());
            }
            if (loc == null) {
                logger.debug("No location value");
                break;
            }
            if (loc.indexOf(REDIRECT_URI) == 0) {
                location = loc;
                break;
            }
            request = httpClient.newRequest(LOGIN_BASE_URL + loc).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .agent(userAgent).followRedirects(false);
            setCookies(request);
            response = request.send();
        }
        return location;
    }

    /**
     * Final step of the login process to get an oAuth access response token
     *
     * @param redirectLocation
     * @param codeVerifier
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    private ContentResponse getLoginToken(String redirectLocation, String codeVerifier)
            throws InterruptedException, ExecutionException, TimeoutException {
        try {
            Map<String, String> params = parseLocationQuery(redirectLocation);

            Fields fields = new Fields();
            fields.add("client_id", CLIENT_ID);
            fields.add("client_secret", Base64.getEncoder().encodeToString(CLIENT_SECRET.getBytes()));
            fields.add("code", params.get("code"));
            fields.add("code_verifier", codeVerifier);
            fields.add("grant_type", "authorization_code");
            fields.add("redirect_uri", REDIRECT_URI);
            fields.add("scope", params.get("scope"));

            Request request = httpClient.newRequest(LOGIN_TOKEN_URL) //
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS) //
                    .content(new FormContentProvider(fields)) //
                    .method(HttpMethod.POST) //
                    .agent(userAgent).followRedirects(true);
            setCookies(request);
            ContentResponse response = request.send();
            if (logger.isTraceEnabled()) {
                logger.trace("Login Code {} Response {}", response.getStatus(), response.getContentAsString());
            }
            return response;
        } catch (URISyntaxException e) {
            throw new ExecutionException(e.getCause());
        }
    }

    private OAuthClientService getOAuthService() {
        OAuthClientService oAuthService = this.oAuthService;
        if (oAuthService == null || oAuthService.isClosed()) {
            oAuthService = oAuthFactory.createOAuthClientService(getThing().toString(), LOGIN_TOKEN_URL,
                    LOGIN_AUTHORIZE_URL, CLIENT_ID, CLIENT_SECRET, SCOPE, false);
            oAuthService.addAccessTokenRefreshListener(this);
            this.oAuthService = oAuthService;
        }
        return oAuthService;
    }

    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    private String generateCodeChallange(String codeVerifier) throws NoSuchAlgorithmException {
        byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(bytes, 0, bytes.length);
        byte[] digest = messageDigest.digest();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    private Map<String, String> parseLocationQuery(String location) throws URISyntaxException {
        URI uri = new URI(location);
        return Arrays.stream(uri.getQuery().split("&")).map(str -> str.split("="))
                .collect(Collectors.toMap(str -> str[0], str -> str[1]));
    }

    private void setCookies(Request request) {
        for (HttpCookie c : httpClient.getCookieStore().getCookies()) {
            request.cookie(c);
        }
    }

    private String authTokenHeader(AccessTokenResponse tokenResponse) {
        return tokenResponse.getTokenType() + " " + tokenResponse.getAccessToken();
    }

    /**
     * Exception for authenticated related errors
     */
    class MyQAuthenticationException extends Exception {
        private static final long serialVersionUID = 1L;

        public MyQAuthenticationException(String message) {
            super(message);
        }
    }

    /**
     * Generic exception for non authentication related errors when communicating with the MyQ service.
     */
    class MyQCommunicationException extends IOException {
        private static final long serialVersionUID = 1L;

        public MyQCommunicationException(@Nullable String message) {
            super(message);
        }
    }
}
