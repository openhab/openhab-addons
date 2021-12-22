package org.openhab.binding.mybmw.internal.handler;

import static org.openhab.binding.mybmw.internal.utils.HTTPConstants.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mybmw.internal.dto.auth.AuthQueryResponse;
import org.openhab.binding.mybmw.internal.dto.auth.AuthResponse;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;

class AuthTest {

    void testAuth() {
        String user = "usr";
        String pwd = "pwd";

        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        HttpClient authHttpClient = new HttpClient(sslContextFactory);
        try {
            authHttpClient.start();
            Request firstRequest = authHttpClient
                    .newRequest("https://" + BimmerConstants.EADRAX_SERVER_MAP.get(BimmerConstants.REGION_ROW)
                            + "/eadrax-ucs/v1/presentation/oauth/config");
            firstRequest.header("ocp-apim-subscription-key",
                    BimmerConstants.OCP_APIM_KEYS.get(BimmerConstants.REGION_ROW));
            firstRequest.header("x-user-agent", "android(v1.07_20200330);bmw;1.7.0(11152)");

            ContentResponse firstResponse = firstRequest.send();
            System.out.println(firstResponse.getContentAsString());
            AuthQueryResponse aqr = Converter.getGson().fromJson(firstResponse.getContentAsString(),
                    AuthQueryResponse.class);

            String verifier_bytes = RandomStringUtils.randomAlphanumeric(64);
            String code_verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(verifier_bytes.getBytes());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(code_verifier.getBytes(StandardCharsets.UTF_8));
            String code_challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            String state_bytes = RandomStringUtils.randomAlphanumeric(16);
            String state = Base64.getUrlEncoder().withoutPadding().encodeToString(state_bytes.getBytes());

            String authUrl = aqr.gcdmBaseUrl + BimmerConstants.OAUTH_ENDPOINT;
            System.out.println(authUrl);
            Request loginRequest = authHttpClient.POST(authUrl);
            loginRequest.header("Content-Type", "application/x-www-form-urlencoded");

            MultiMap<String> baseParams = new MultiMap<String>();
            baseParams.put("client_id", aqr.clientId);
            baseParams.put("response_type", "code");
            baseParams.put("redirect_uri", aqr.returnUrl);
            baseParams.put("state", state);
            baseParams.put("nonce", "login_nonce");
            baseParams.put("scope", String.join(" ", aqr.scopes));
            baseParams.put("code_challenge", code_challenge);
            baseParams.put("code_challenge_method", "S256");

            MultiMap<String> loginParams = new MultiMap<String>(baseParams);
            loginParams.put("grant_type", "authorization_code");
            loginParams.put("username", user);
            loginParams.put("password", pwd);
            loginRequest.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED,
                    UrlEncoded.encode(loginParams, StandardCharsets.UTF_8, false), StandardCharsets.UTF_8));
            ContentResponse secondResonse = loginRequest.send();
            System.out.println(secondResonse.getContentAsString());
            String authCode = getAuthCode(secondResonse.getContentAsString());
            System.out.println(authCode);

            MultiMap<String> authParams = new MultiMap<String>(baseParams);
            authParams.put("authorization", authCode);
            Request authRequest = authHttpClient.POST(authUrl).followRedirects(false);
            authRequest.header("Content-Type", "application/x-www-form-urlencoded");
            authRequest.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED,
                    UrlEncoded.encode(authParams, StandardCharsets.UTF_8, false), StandardCharsets.UTF_8));
            ContentResponse authResponse = authRequest.send();
            System.out.println(authResponse.getHeaders());
            System.out.println("Response " + authResponse.getHeaders().get(HttpHeader.LOCATION));
            String code = AuthTest.codeFromUrl(authResponse.getHeaders().get(HttpHeader.LOCATION));
            System.out.println("Code " + code);
            System.out.println("Auth");

            System.out.println(aqr.tokenEndpoint);
            // AuthenticationStore authenticationStore = authHttpClient.getAuthenticationStore();
            // BasicAuthentication ba = new BasicAuthentication(new URI(aqr.tokenEndpoint), Authentication.ANY_REALM,
            // aqr.clientId, aqr.clientSecret);
            // authenticationStore.addAuthentication(ba);
            Request codeRequest = authHttpClient.POST(aqr.tokenEndpoint);
            String basicAuth = "Basic "
                    + Base64.getUrlEncoder().encodeToString((aqr.clientId + ":" + aqr.clientSecret).getBytes());
            System.out.println(basicAuth);
            codeRequest.header("Content-Type", "application/x-www-form-urlencoded");
            codeRequest.header(AUTHORIZATION, basicAuth);

            MultiMap<String> codeParams = new MultiMap<String>();
            codeParams.put("code", code);
            codeParams.put("code_verifier", code_verifier);
            codeParams.put("redirect_uri", aqr.returnUrl);
            codeParams.put("grant_type", "authorization_code");
            System.out.println(codeParams);
            // codeParams.put(RESPONSE_TYPE, TOKEN);
            // codeParams.put(AUTHORIZATION, basicAuth);
            codeRequest.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED,
                    UrlEncoded.encode(codeParams, StandardCharsets.UTF_8, false), StandardCharsets.UTF_8));
            ContentResponse codeResponse = codeRequest.send();
            System.out.println(codeResponse.getContentAsString());
            AuthResponse ar = Converter.getGson().fromJson(codeResponse.getContentAsString(), AuthResponse.class);
            Token t = new Token();
            t.setType(ar.tokenType);
            t.setToken(ar.accessToken);
            t.setExpiration(ar.expiresIn);
            System.out.println(t.getBearerToken());

            HttpClient apiHttpClient = new HttpClient(sslContextFactory);
            apiHttpClient.start();

            MultiMap vehicleParams = new MultiMap();
            vehicleParams.put("tireGuardMode", "ENABLED");
            vehicleParams.put("appDateTime", Long.toString(System.currentTimeMillis()));
            vehicleParams.put("apptimezone", "60.0");
            // vehicleRequest.param("tireGuardMode", "ENABLED");
            // vehicleRequest.param("appDateTime", Long.toString(System.currentTimeMillis()));
            // vehicleRequest.param("apptimezone", "60.0");
            // vehicleRequest.
            // // System.out.println(vehicleParams);
            // vehicleRequest.content(new StringContentProvider(CONTENT_TYPE_JSON_ENCODED, vehicleParams.toString(),
            // StandardCharsets.UTF_8));
            // System.out.println(vehicleRequest.getHeaders());
            String params = UrlEncoded.encode(codeParams, StandardCharsets.UTF_8, false);

            String vehicleUrl = "https://" + BimmerConstants.EADRAX_SERVER_MAP.get(BimmerConstants.REGION_ROW)
                    + "/eadrax-vcs/v1/vehicles";
            System.out.println(vehicleUrl);
            Request vehicleRequest = apiHttpClient.newRequest(vehicleUrl).param("tireGuardMode", "ENABLED")
                    .param("appDateTime", Long.toString(System.currentTimeMillis())).param("apptimezone", "60.0");
            // vehicleRequest.header("Content-Type", "application/x-www-form-urlencoded");
            vehicleRequest.header(HttpHeader.AUTHORIZATION, t.getBearerToken());
            vehicleRequest.header("accept", "application/json");
            vehicleRequest.header("x-user-agent", "android(v1.07_20200330);bmw;1.7.0(11152)");
            vehicleRequest.header("accept-language", "en");

            System.out.println(vehicleRequest.getParams());
            ContentResponse vehicleResponse = vehicleRequest.send();
            System.out.println(vehicleResponse.getStatus());
            System.out.println(vehicleResponse.getContentAsString());
            System.out.println(vehicleResponse.getHeaders());

        } catch (

        Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getAuthCode(String response) {
        String[] keys = response.split("&");
        for (int i = 0; i < keys.length; i++) {
            if (keys[i].startsWith(AUTHORIZATION)) {
                String authCode = keys[i].split("=")[1];
                authCode = authCode.split("\"")[0];
                return authCode;
            }
        }
        return Constants.EMPTY;
    }

    public static String codeFromUrl(String encodedUrl) {
        final MultiMap<String> tokenMap = new MultiMap<String>();
        UrlEncoded.decodeTo(encodedUrl, tokenMap, StandardCharsets.US_ASCII);
        final StringBuilder codeFound = new StringBuilder();
        tokenMap.forEach((key, value) -> {
            if (value.size() > 0) {
                String val = value.get(0);
                if (key.endsWith(CODE)) {
                    codeFound.append(val.toString());
                }
            }
        });
        return codeFound.toString();
    }

    @Test
    public void testRandom() {
        String verfier_code = "oaczELGSZgFjYrRgtOiJpyoOOUzaJ8FNixGZXazR_plJXRKoI1VqqNecllr5iZYn58ey7GE3XvNLyu2dP0WCQA";
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(verfier_code.getBytes(StandardCharsets.UTF_8));
            String code_challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            System.out.println(code_challenge);
            String pCodeChallenge = "9ct8DdFwC_EJ-f9SN-ePCirAnqZMA06Qudq6zWzLSIs";
            System.out.println(pCodeChallenge);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // DEBUG:bimmer_connected.account:Authenticating againstwith MyBMW flow.
        // DEBUG:bimmer_connected.account:Verfier Bytes
        // oaczELGSZgFjYrRgtOiJpyoOOUzaJ8FNixGZXazR/plJXRKoI1VqqNecllr5iZYn58ey7GE3XvNLyu2dP0WCQA==
        // DEBUG:bimmer_connected.account:Verfier Code b'
        // DEBUG:bimmer_connected.account:Challenge Bytes
        // b'\xf5\xcb|\r\xd1p\x0b\xf1\t\xf9\xffR7\xe7\x8f\n*\xc0\x9e\xa6L\x03N\x90\xb9\xda\xba\xcdl\xcbH\x8b'
        // DEBUG:bimmer_connected.account:Challenge Code b'9ct8DdFwC_EJ-f9SN-ePCirAnqZMA06Qudq6zWzLSIs'
        // DEBUG:bimmer_connected.account:State Bytes b'T\xa0n3\xb9\xee\xc79\xf07\x1f\xf9\xd2j5\xf6'
        // DEBUG:bimmer_connected.account:State b'VKBuM7nuxznwNx_50mo19g'
    }
}
