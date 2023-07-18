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
package org.openhab.binding.lgthinq.handler;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;
import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.lgthinq.internal.LGThinQBindingConstants;
import org.openhab.binding.lgthinq.internal.LGThinQBridgeConfiguration;
import org.openhab.binding.lgthinq.internal.api.RestUtils;
import org.openhab.binding.lgthinq.internal.api.TokenManager;
import org.openhab.binding.lgthinq.internal.handler.LGThinQBridgeHandler;
import org.openhab.binding.lgthinq.lgservices.*;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;

/**
 * The {@link LGThinqBridgeTests}
 *
 * @author Nemer Daud - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@WireMockTest(httpPort = 8880)
class LGThinqBridgeTests {
    private static final Logger logger = LoggerFactory.getLogger(LGThinqBridgeTests.class);
    private final String fakeBridgeName = "fakeBridgeId";
    private String fakeLanguage = "pt-BR";
    private String fakeCountry = "BR";
    private String fakeUserName = "someone@some.url";
    private String fakePassword = "somepassword";
    private final String gtwResponse = JsonUtils.loadJson("gtw-response-1.json");
    private final String preLoginResponse = JsonUtils.loadJson("prelogin-response-1.json");
    private final String userIdType = "LGE";
    private final String loginSessionId = "emp;11111111;222222222";
    private final String loginSessionResponse = String.format(JsonUtils.loadJson("login-session-response-1.json"),
            loginSessionId, fakeUserName, userIdType, fakeUserName);
    private final String userInfoReturned = String.format(JsonUtils.loadJson("user-info-response-1.json"), fakeUserName,
            fakeUserName);
    private final String dashboardListReturned = JsonUtils.loadJson("dashboard-list-response-1.json");
    private final String dashboardWMListReturned = JsonUtils.loadJson("dashboard-list-response-wm.json");
    private final String secretKey = "gregre9812012910291029120912091209";
    private final String oauthTokenSearchKeyReturned = "{\"returnData\":\"" + secretKey + "\"}";
    private final String refreshToken = "12897238974bb327862378ef290128390273aa7389723894734de";
    private final String accessToken = "11a1222c39f16a5c8b3fa45bb4c9be2e00a29a69dced2fa7fe731f1728346ee669f1a96d1f0b4925e5aa330b6dbab882772";
    private final String sessionTokenReturned = String.format(JsonUtils.loadJson("session-token-response-1.json"),
            accessToken, refreshToken);

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    @Test
    public void testDiscoveryACThings() {
        setupAuthenticationMock();
        LGThinQApiClientService service1 = LGThinQApiClientServiceFactory.newACApiClientService(PLATFORM_TYPE_V1, mock(HttpClientFactory.class));
        LGThinQApiClientService service2 = LGThinQApiClientServiceFactory.newACApiClientService(PLATFORM_TYPE_V2, mock(HttpClientFactory.class));
        try {
            List<LGDevice> devices = service2.listAccountDevices("bridgeTest");
            assertEquals(devices.size(), 2);
        } catch (Exception e) {
            logger.error("Error testing facade", e);
        }
    }

    private void setupAuthenticationMock() {
        stubFor(get(GATEWAY_SERVICE_PATH_V2).willReturn(ok(gtwResponse)));
        String preLoginPwd = RestUtils.getPreLoginEncPwd(fakePassword);
        stubFor(post("/spx" + PRE_LOGIN_PATH).withRequestBody(containing("user_auth2=" + preLoginPwd))
                .willReturn(ok(preLoginResponse)));
        URI uri = UriBuilder.fromUri("http://localhost:8880").path("spx" + OAUTH_SEARCH_KEY_PATH)
                .queryParam("key_name", "OAUTH_SECRETKEY").queryParam("sever_type", "OP").build();
        stubFor(get(String.format("%s?%s", uri.getPath(), uri.getQuery())).willReturn(ok(oauthTokenSearchKeyReturned)));
        String fakeUserNameEncoded = URLEncoder.encode(fakeUserName, StandardCharsets.UTF_8);
        stubFor(post(V2_SESSION_LOGIN_PATH + fakeUserNameEncoded)
                .withRequestBody(containing("user_auth2=SOME_DUMMY_ENC_PWD"))
                .withHeader("X-Signature", equalTo("SOME_DUMMY_SIGNATURE"))
                .withHeader("X-Timestamp", equalTo("1643236928")).willReturn(ok(loginSessionResponse)));
        stubFor(get(V2_USER_INFO).willReturn(ok(userInfoReturned)));
        stubFor(get("/v1" + V2_LS_PATH).willReturn(ok(dashboardListReturned)));
        String currTimestamp = getCurrentTimestamp();
        Map<String, String> empData = new LinkedHashMap<>();
        empData.put("account_type", userIdType);
        empData.put("country_code", fakeCountry);
        empData.put("username", fakeUserName);

        stubFor(post("/emp/oauth2/token/empsession").withRequestBody(containing("account_type=" + userIdType))
                .withRequestBody(containing("country_code=" + fakeCountry))
                .withRequestBody(containing("username=" + URLEncoder.encode(fakeUserName, StandardCharsets.UTF_8)))
                .withHeader("lgemp-x-session-key", equalTo(loginSessionId)).willReturn(ok(sessionTokenReturned)));
        // faking some constants
        Bridge fakeThing = mock(Bridge.class);
        ThingUID fakeThingUid = mock(ThingUID.class);
        when(fakeThingUid.getId()).thenReturn(fakeBridgeName);
        when(fakeThing.getUID()).thenReturn(fakeThingUid);
        String tempDir = System.getProperty("java.io.tmpdir");
        LGThinQBindingConstants.THINQ_CONNECTION_DATA_FILE = tempDir + File.separator + "token.json";
        LGThinQBindingConstants.BASE_CAP_CONFIG_DATA_FILE = tempDir + File.separator + "thinq-cap.json";
        LGThinQBridgeHandler b = new LGThinQBridgeHandler(fakeThing, mock(HttpClientFactory.class));
        LGThinQBridgeHandler spyBridge = spy(b);
        doReturn(new LGThinQBridgeConfiguration(fakeUserName, fakePassword, fakeCountry, fakeLanguage, 60,
                "http://localhost:8880")).when(spyBridge).getConfigAs(any(Class.class));
        spyBridge.initialize();
        TokenManager tokenManager = new TokenManager(mock(HttpClient.class));
        try {
            if (!tokenManager.isOauthTokenRegistered(fakeBridgeName)) {
                tokenManager.oauthFirstRegistration(fakeBridgeName, fakeLanguage, fakeCountry, fakeUserNameEncoded,
                        fakePassword, "");
            }
        } catch (Exception e) {
            logger.error("Error testing facade", e);
        }
    }

    @BeforeEach
    void setUp() {
        String tempDir = System.getProperty("java.io.tmpdir");
        File f = new File(tempDir + File.separator + "token.json");
        f.deleteOnExit();
    }

    @Test
    public void testDiscoveryWMThings() {
        stubFor(get(GATEWAY_SERVICE_PATH_V2).willReturn(ok(gtwResponse)));
        String preLoginPwd = RestUtils.getPreLoginEncPwd(fakePassword);
        stubFor(post("/spx" + PRE_LOGIN_PATH).withRequestBody(containing("user_auth2=" + preLoginPwd))
                .willReturn(ok(preLoginResponse)));
        URI uri = UriBuilder.fromUri("http://localhost:8880").path("spx" + OAUTH_SEARCH_KEY_PATH)
                .queryParam("key_name", "OAUTH_SECRETKEY").queryParam("sever_type", "OP").build();
        stubFor(get(String.format("%s?%s", uri.getPath(), uri.getQuery())).willReturn(ok(oauthTokenSearchKeyReturned)));
        stubFor(post(V2_SESSION_LOGIN_PATH + URLEncoder.encode(fakeUserName, StandardCharsets.UTF_8))
                .withRequestBody(containing("user_auth2=SOME_DUMMY_ENC_PWD"))
                .withHeader("X-Signature", equalTo("SOME_DUMMY_SIGNATURE"))
                .withHeader("X-Timestamp", equalTo("1643236928")).willReturn(ok(loginSessionResponse)));
        stubFor(get(V2_USER_INFO).willReturn(ok(userInfoReturned)));
        stubFor(get("/v1" + V2_LS_PATH).willReturn(ok(dashboardWMListReturned)));
        String dataCollectedWM = JsonUtils.loadJson("wm-data-result.json");
        stubFor(get("/v1/service/devices/fakeDeviceId").willReturn(ok(dataCollectedWM)));
        String currTimestamp = getCurrentTimestamp();
        Map<String, String> empData = new LinkedHashMap<>();
        empData.put("account_type", userIdType);
        empData.put("country_code", fakeCountry);
        empData.put("username", fakeUserName);

        stubFor(post("/emp/oauth2/token/empsession").withRequestBody(containing("account_type=" + userIdType))
                .withRequestBody(containing("country_code=" + fakeCountry))
                .withRequestBody(containing("username=" + URLEncoder.encode(fakeUserName, StandardCharsets.UTF_8)))
                .withHeader("lgemp-x-session-key", equalTo(loginSessionId)).willReturn(ok(sessionTokenReturned)));

        Bridge fakeThing = mock(Bridge.class);
        ThingUID fakeThingUid = mock(ThingUID.class);
        when(fakeThingUid.getId()).thenReturn(fakeBridgeName);
        when(fakeThing.getUID()).thenReturn(fakeThingUid);
        String tempDir = System.getProperty("java.io.tmpdir");
        LGThinQBindingConstants.THINQ_USER_DATA_FOLDER = "" + tempDir;
        LGThinQBindingConstants.THINQ_CONNECTION_DATA_FILE = tempDir + File.separator + "token.json";
        LGThinQBindingConstants.BASE_CAP_CONFIG_DATA_FILE = tempDir + File.separator + "thinq-cap.json";
        LGThinQBridgeHandler b = new LGThinQBridgeHandler(fakeThing, mock(HttpClientFactory.class));

        final LGThinQWMApiClientService service2 = LGThinQApiClientServiceFactory.newWMApiClientService(PLATFORM_TYPE_V1, mock(HttpClientFactory.class));
        TokenManager tokenManager = new TokenManager(mock(HttpClient.class));
        try {
            if (!tokenManager.isOauthTokenRegistered(fakeBridgeName)) {
                tokenManager.oauthFirstRegistration(fakeBridgeName, fakeLanguage, fakeCountry, fakeUserName,
                        fakePassword, "http://localhost:8880");
            }
            List<LGDevice> devices = service2.listAccountDevices("bridgeTest");
            assertEquals(devices.size(), 1);
            // service2.getDeviceData(fakeBridgeName, "fakeDeviceId", new WasherDryerCapability());
        } catch (Exception e) {
            logger.error("Error testing facade", e);
        }
    }

    // @Test
    // void TestWakeUp() throws LGThinqApiException {
    // setupAuthenticationMock();
    // LGThinQWMApiClientService service = LGThinQWMApiV1ClientServiceImpl.getInstance();
    // service.wakeUp("xxx", "yyyy", true);
    // }
}
