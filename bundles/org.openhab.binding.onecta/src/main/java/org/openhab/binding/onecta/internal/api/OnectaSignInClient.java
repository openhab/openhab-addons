package org.openhab.binding.onecta.internal.api;

import java.net.HttpCookie;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.onecta.internal.OnectaConfiguration;
import org.openhab.binding.onecta.internal.api.dto.authentication.*;
import org.openhab.binding.onecta.internal.exception.DaikinCommunicationException;
import org.openhab.binding.onecta.internal.exception.DaikinCommunicationForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@NonNullByDefault
public class OnectaSignInClient {

    private final Logger logger = LoggerFactory.getLogger(OnectaSignInClient.class);

    public static final String PARAM_LOGIN_ID = "loginID";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_SESSION_EXPIRATION = "sessionExpiration";
    public static final String PARAM_TARGET_ENV = "targetEnv";
    public static final String PARAM_INCLUDE = "include";
    public static final String PARAM_LOGIN_MODE = "loginMode";
    public static final String PARAM_RISK_CONTEXT = "riskContext";
    public static final String PARAM_API_KEY = "APIKey";
    public static final String PARAM_SDK = "sdk";
    public static final String PARAM_AUTH_MODE = "authMode";
    public static final String PARAM_PAGE_URL = "pageURL";
    public static final String PARAM_SDK_BUILD = "sdkBuild";
    public static final String PARAM_SAML_CONTEXT = "samlContext";
    public static final String PARAM_LOGIN_TOKEN = "loginToken";
    public static final String PARAM_SAML_RESPONSE = "SAMLResponse";
    public static final String PARAM_RELAY_STATE = "RelayState";
    public static final String HTTPHEADER_X_AMZ_TARGET = "x-amz-target";

    private static final String DAIKIN_ISSUER = "https://cognito-idp.eu-west-1.amazonaws.com/eu-west-1_SLI9qJpc7/.well-known/openid-configuration";
    private static final String APIKEY = "3_xRB3jaQ62bVjqXU1omaEsPDVYC0Twi1zfq1zHPu_5HFT0zWkDvZJS97Yw1loJnTm";
    private static final String APIKEY_2 = "3_QebFXhxEWDc8JhJdBWmvUd1e0AaWJCISbqe4QIHrk_KzNVJFJ4xsJ2UZbl8OIIFY";
    private static final String OPENID_CLIENT_ID = "7rk39602f0ds8lk0h076vvijnb";

    private String refreshToken = "";
    private String userId = "";
    private String password = "";

    private RespAuthenticationRoot respAuthenticationRoot = new RespAuthenticationRoot();

    public String getToken() {
        return respAuthenticationRoot.getAuthenticationResult().getAccessToken();
    }

    public static String getSamlContext(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.split("=")[0].equals(PARAM_SAML_CONTEXT)) {
                return param.split("=")[1];
            }
            String value = param.split("=")[1];
        }
        return "";
    }

    protected void signIn() throws DaikinCommunicationException {
        signIn(this.userId, this.password);
    }

    protected void signIn(String userId, String password, String refreshToken) throws DaikinCommunicationException {
        this.userId = userId;
        this.password = password;
        this.refreshToken = refreshToken;
        if (refreshToken.isEmpty()) {
            signIn(userId, password);
        } else {
            fetchAccessToken();
        }
    }

    protected void signIn(String userId, String password) throws DaikinCommunicationException {
        this.userId = userId;
        this.password = password;

        HttpClient httpClient = OnectaConfiguration.getHttpClient();

        try {
            logger.info("Start logon to Daikin : " + userId);
            httpClient.setFollowRedirects(false);
            httpClient.getCookieStore().removeAll();
            // Step 1
            ContentResponse response = httpClient.GET(DAIKIN_ISSUER);

            JsonObject jsonResponse = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            RespOpenidConfiruration respOpenidConfiruration = Objects
                    .requireNonNull(new Gson().fromJson(jsonResponse.getAsJsonObject(), RespOpenidConfiruration.class));

            String authEndpoint = respOpenidConfiruration.getAuthorizationEndpoint();
            String tokenEndpoint = respOpenidConfiruration.getTokenEndpoint();
            String saml2Endpoint = respOpenidConfiruration.getAuthorizationEndpoint().replace("/oauth2/authorize",
                    "/saml2/idpresponse");

            // Step 2 create client Secret
            logger.debug("Create client Secret");
            int length = 32;
            boolean useLetters = true;
            boolean useNumbers = true;
            String createdClientSecret = RandomStringUtils.random(length, useLetters, useNumbers);

            // Step 3 create initial url
            logger.debug("Create initial url");
            final String redirectUri = "daikinunified%3A%2F%2Flogin";
            String url = authEndpoint
                    + String.format("?response_type=code&state=%s&client_id=%s&scope=openid&redirect_uri=%s",
                            createdClientSecret, OPENID_CLIENT_ID, redirectUri);
            response = httpClient.GET(url);
            String redirectUrl = response.getHeaders().get(HttpHeader.LOCATION);

            // Get csrf-cookies
            logger.debug("Get csrf-cookies");
            String cookieString = "";
            for (HttpCookie cookie : httpClient.getCookieStore().getCookies()) {
                if (cookie.getName().equalsIgnoreCase("xsrf-token") || cookie.getName().equals("csrf-state")
                        || cookie.getName().equals("csrf-state-legacy")) {
                    cookieString += String.format("%s=%s; ", cookie.getName(), cookie.getValue());
                }
            }

            // Step 4 Call the forward-url -> extract samlContext from request
            logger.debug("Call forward-url");
            response = httpClient.GET(redirectUrl);
            String samlContext = getSamlContext(response.getHeaders().get(HttpHeader.LOCATION).split("[?]")[1]);

            // Step 5 prepare request to get Api-Version
            logger.debug("Prepare request to get Api-Version");
            httpClient.setFollowRedirects(true);
            url = "https://cdns.gigya.com/js/gigya.js?apiKey=" + APIKEY;
            response = httpClient.GET(url);

            Pattern pattern = Pattern.compile("(\\d+-\\d-\\d+)");
            Matcher matcher = pattern.matcher(response.getContentAsString());
            matcher.find();
            String apiVersion = matcher.group();

            // Step 6 prepare request to get single-sign-on cookie
            logger.debug("Prepare request to get single-sign-on cookie");
            url = String.format("https://cdc.daikin.eu/accounts.webSdkBootstrap?apiKey=%s&sdk=js_latest&format=json",
                    APIKEY);
            httpClient.GET(url);
            // Get ssoCookie
            String ssoCookieString = "";
            for (HttpCookie cookie : httpClient.getCookieStore().getCookies()) {
                if (cookie.getName().equalsIgnoreCase("gmid") || cookie.getName().equalsIgnoreCase("ucid")
                        || cookie.getName().equalsIgnoreCase("hasGmid")) {
                    ssoCookieString += String.format("%s=%s; ", cookie.getName(), cookie.getValue());
                }
            }
            ssoCookieString += String.format("gig_bootstrap_%s=cdc_ver4; ", APIKEY);
            ssoCookieString += String.format("gig_canary_%s=false; ", APIKEY_2);
            ssoCookieString += String.format("gig_canary_ver_%s=%s; ", APIKEY_2, apiVersion);
            ssoCookieString += String.format("apiDomain_%s==cdc.daikin.eu; ", APIKEY_2);

            logger.debug("User logon to Daikin");
            response = httpClient.newRequest("https://cdc.daikin.eu/accounts.login").method(HttpMethod.POST)
                    .header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED) //
                    .header(HttpHeader.COOKIE, ssoCookieString) //
                    .param(PARAM_LOGIN_ID, userId)//
                    .param(PARAM_PASSWORD, password)//
                    .param(PARAM_SESSION_EXPIRATION, "31536000")//
                    .param(PARAM_TARGET_ENV, "jssdk")//
                    .param(PARAM_INCLUDE, "profile,")//
                    .param(PARAM_LOGIN_MODE, "standard")//
                    .param(PARAM_RISK_CONTEXT, "{\"b0\":7527,\"b2\":4,\"b5\":1")//
                    .param(PARAM_API_KEY, APIKEY)//
                    .param(PARAM_SDK, "js_latest") //
                    .param(PARAM_AUTH_MODE, "cookie")//
                    .param(PARAM_PAGE_URL,
                            "https://my.daikin.eu/content/daikinid-cdc-saml/en/login.html?samlContext=" + samlContext)//
                    .param(PARAM_SDK_BUILD, "12208").param("format", "json").send();

            // step 7 extract login-token
            logger.debug("Extract login-token");
            jsonResponse = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            RespStep7 respStep7 = Objects
                    .requireNonNull(new Gson().fromJson(jsonResponse.getAsJsonObject(), RespStep7.class));
            if (respStep7.getStatusCode() != 200) {
                throw new DaikinCommunicationForbiddenException(String.format("Error status: %s, Reason: %s",
                        respStep7.getStatusCode(), respStep7.getErrorMessage()));
            }

            String loginToken = respStep7.getSessionInfo().getLogin_token();

            // step 8 expand single-sign-on cookies with login-token
            logger.debug("Expand single-sign-on cookies with login-token");
            String tijd = String.valueOf(new Date().getTime() + 3600000);
            ssoCookieString += String.format("glt_%s=%s; ", APIKEY, loginToken);
            ssoCookieString += String.format("gig_loginToken_%s=%s; ", APIKEY_2, loginToken);
            ssoCookieString += String.format("gig_loginToken_%s_exp=%s; ", APIKEY_2, tijd);
            ssoCookieString += String.format("gig_loginToken_%s_visited=%s; ", APIKEY_2, "%2C" + APIKEY);

            url = String.format("https://cdc.daikin.eu/saml/v2.0/%s/idp/sso/continue", APIKEY);
            response = httpClient.newRequest(url).method(HttpMethod.POST)//
                    .header(HttpHeader.COOKIE, ssoCookieString)//
                    .param(PARAM_SAML_CONTEXT, samlContext)//
                    .param(PARAM_LOGIN_TOKEN, loginToken).send();

            pattern = Pattern.compile("name=\"SAMLResponse\" value=\"([^\"]+)");
            matcher = pattern.matcher(response.getContentAsString());
            matcher.find();
            String samlResponse = matcher.group().split("value=\"")[1];

            pattern = Pattern.compile("name=\"RelayState\" value=\"([^\"]+)");
            matcher = pattern.matcher(response.getContentAsString());
            matcher.find();
            String relayState = matcher.group().split("value=\"")[1];

            // Step 9 Get DaikinUnified
            logger.debug("Get DaikinUnified");
            httpClient.setFollowRedirects(false);
            httpClient.setRequestBufferSize(20000);
            response = httpClient.newRequest(saml2Endpoint).method(HttpMethod.POST)
                    .header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)//
                    .header(HttpHeader.COOKIE, cookieString)//
                    .param(PARAM_SAML_RESPONSE, samlResponse)//
                    .param(PARAM_RELAY_STATE, relayState).send();

            String daikinUnified = response.getHeaders().get(HttpHeader.LOCATION).split("code=")[1];
            httpClient.setFollowRedirects(true);

            // Step 10 get the Tokens
            logger.debug("Get Tokens to perform datatransfer");
            url = tokenEndpoint + "?grant_type=authorization_code&code=" + daikinUnified + "&state="
                    + createdClientSecret + "&client_id=" + OPENID_CLIENT_ID
                    + "&redirect_uri=daikinunified%3A%2F%2Flogin";
            response = httpClient.newRequest(url).method(HttpMethod.POST)
                    .header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)//
                    .header(HttpHeader.COOKIE, cookieString).send();

            jsonResponse = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            RespTokenResult respTokenResult = Objects
                    .requireNonNull(new Gson().fromJson(jsonResponse.getAsJsonObject(), RespTokenResult.class));

            this.refreshToken = respTokenResult.getRefreshToken();
            fetchAccessToken();
            logger.debug("Login successful");

        } catch (InterruptedException e) {
            logger.warn("Login failed" + e.getMessage());
        } catch (ExecutionException e) {
            logger.warn("DaikinCommunicationException" + e.getMessage());
            throw new DaikinCommunicationException("Connection error, See log for more info");
        } catch (TimeoutException e) {
            logger.warn("TimeoutException" + e.getMessage());
        } catch (DaikinCommunicationForbiddenException e) {
            logger.warn("DaikinCommunicationForbiddenException" + e.getMessage());
            throw new DaikinCommunicationForbiddenException(" " + e.getMessage());
        } catch (DaikinCommunicationException e) {
            throw new RuntimeException(e);
        }
    }

    public void fetchAccessToken() throws DaikinCommunicationException {
        logger.debug("Refresh token.");
        HttpClient httpClient = OnectaConfiguration.getHttpClient();

        respAuthenticationRoot = new RespAuthenticationRoot();
        ReqAuthenticationRoot reqAuthenticationRoot = new ReqAuthenticationRoot(OPENID_CLIENT_ID, this.refreshToken);

        Request request = httpClient.POST("https://cognito-idp.eu-west-1.amazonaws.com");

        request.header(HTTPHEADER_X_AMZ_TARGET, "AWSCognitoIdentityProviderService.InitiateAuth");
        request.header(HttpHeader.CONTENT_TYPE, "application/x-amz-json-1.1");
        request.content(
                new StringContentProvider(new Gson().toJson(reqAuthenticationRoot, ReqAuthenticationRoot.class)),
                MediaType.APPLICATION_JSON);

        ContentResponse response = null;
        respAuthenticationRoot.getAuthenticationResult().setAccessToken("");
        try {
            response = request.send();
            JsonObject jsonResponse = JsonParser.parseString(response.getContentAsString()).getAsJsonObject();
            respAuthenticationRoot = Objects
                    .requireNonNull(new Gson().fromJson(jsonResponse.getAsJsonObject(), RespAuthenticationRoot.class));
        } catch (InterruptedException e) {
            logger.warn("Not connected to Daikin Onecta: Type '{}' - Message '{}' .",
                    respAuthenticationRoot.get__type(), respAuthenticationRoot.getMessage());
            throw new DaikinCommunicationException(e);
        } catch (TimeoutException e) {
            logger.warn("Not connected to Daikin Onecta: Type '{}' - Message '{}' .",
                    respAuthenticationRoot.get__type(), respAuthenticationRoot.getMessage());
            throw new DaikinCommunicationException(e);
        } catch (ExecutionException e) {
            logger.warn("Not connected to Daikin Onecta: Type '{}' - Message '{}' .",
                    respAuthenticationRoot.get__type(), respAuthenticationRoot.getMessage());
            throw new DaikinCommunicationException(e);
        }
    }

    public Boolean isOnline() {
        return !this.refreshToken.isEmpty()
                && !this.respAuthenticationRoot.getAuthenticationResult().getAccessToken().isEmpty();
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
