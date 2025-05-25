/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.lgthinq.internal.LGThinQBridgeConfiguration;
import org.openhab.binding.lgthinq.internal.handler.LGThinQBridgeHandler;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientService;
import org.openhab.binding.lgthinq.lgservices.LGThinQApiClientServiceFactory;
import org.openhab.binding.lgthinq.lgservices.LGThinQWMApiClientService;
import org.openhab.binding.lgthinq.lgservices.api.RestUtils;
import org.openhab.binding.lgthinq.lgservices.api.TokenManager;
import org.openhab.binding.lgthinq.lgservices.model.LGDevice;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCanonicalSnapshot;
import org.openhab.binding.lgthinq.lgservices.model.devices.ac.ACCapability;
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
@NonNullByDefault
@SuppressWarnings({ "unchecked", "null" })
class LGThinqBridgeTests {
    private final Logger logger = LoggerFactory.getLogger(LGThinqBridgeTests.class);
    private final String fakeBridgeName = "fakeBridgeId";
    private final String fakeLanguage = "pt-BR";
    private final String fakeCountry = "BR";
    private final String fakeUserName = "someone@some.url";
    private final String fakePassword = "somepassword";
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

    LGThinqBridgeTests() throws IOException {
    }

    @Test
    public void testDiscoveryACThings() throws Exception {
        setupAuthenticationMock();
        LGThinQApiClientService<ACCapability, ACCanonicalSnapshot> service2 = LGThinQApiClientServiceFactory
                .newACApiClientService(LG_API_PLATFORM_TYPE_V2, mock(HttpClientFactory.class));
        try {
            List<LGDevice> devices = service2.listAccountDevices("bridgeTest");
            assertEquals(devices.size(), 2);
        } catch (Exception e) {
            logger.error("Error testing facade", e);
        }
    }

    static class LGThinQBridgeHandlerTest extends LGThinQBridgeHandler {

        public LGThinQBridgeHandlerTest(Bridge bridge, HttpClientFactory httpClientFactory) {
            super(bridge, httpClientFactory);
        }

        @Override
        public <T> T getConfigAs(Class<T> configurationClass) {
            return super.getConfigAs(configurationClass);
        }
    }

    private void setupAuthenticationMock() throws Exception {
        stubFor(get(LG_API_GATEWAY_SERVICE_PATH_V2).willReturn(ok(gtwResponse)));
        String preLoginPwd = RestUtils.getPreLoginEncPwd(fakePassword);
        stubFor(post("/spx" + LG_API_PRE_LOGIN_PATH).withRequestBody(containing("user_auth2=" + preLoginPwd))
                .willReturn(ok(preLoginResponse)));
        URI uri = UriBuilder.fromUri("http://localhost:8880").path("spx" + LG_API_OAUTH_SEARCH_KEY_PATH)
                .queryParam("key_name", "OAUTH_SECRETKEY").queryParam("sever_type", "OP").build();
        stubFor(get(String.format("%s?%s", uri.getPath(), uri.getQuery())).willReturn(ok(oauthTokenSearchKeyReturned)));
        String fakeUserNameEncoded = URLEncoder.encode(fakeUserName, StandardCharsets.UTF_8);
        stubFor(post(LG_API_V2_SESSION_LOGIN_PATH + fakeUserNameEncoded)
                .withRequestBody(containing("user_auth2=SOME_DUMMY_ENC_PWD"))
                .withHeader("X-Signature", equalTo("SOME_DUMMY_SIGNATURE"))
                .withHeader("X-Timestamp", equalTo("1643236928")).willReturn(ok(loginSessionResponse)));
        stubFor(get(LG_API_V2_USER_INFO).willReturn(ok(userInfoReturned)));
        stubFor(get("/v1" + LG_API_V2_LS_PATH).willReturn(ok(dashboardListReturned)));
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
        System.setProperty("THINQ_CONNECTION_DATA_FILE", tempDir + File.separator + "token.json");
        System.setProperty("BASE_CAP_CONFIG_DATA_FILE", tempDir + File.separator + "thinq-cap.json");
        LGThinQBridgeHandlerTest b = new LGThinQBridgeHandlerTest(fakeThing, mock(HttpClientFactory.class));
        LGThinQBridgeHandlerTest spyBridge = spy(b);
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
    public void testDiscoveryWMThings() throws Exception {
        stubFor(get(LG_API_GATEWAY_SERVICE_PATH_V2).willReturn(ok(gtwResponse)));
        String preLoginPwd = RestUtils.getPreLoginEncPwd(fakePassword);
        stubFor(post("/spx" + LG_API_PRE_LOGIN_PATH).withRequestBody(containing("user_auth2=" + preLoginPwd))
                .willReturn(ok(preLoginResponse)));
        URI uri = UriBuilder.fromUri("http://localhost:8880").path("spx" + LG_API_OAUTH_SEARCH_KEY_PATH)
                .queryParam("key_name", "OAUTH_SECRETKEY").queryParam("sever_type", "OP").build();
        stubFor(get(String.format("%s?%s", uri.getPath(), uri.getQuery())).willReturn(ok(oauthTokenSearchKeyReturned)));
        stubFor(post(LG_API_V2_SESSION_LOGIN_PATH + URLEncoder.encode(fakeUserName, StandardCharsets.UTF_8))
                .withRequestBody(containing("user_auth2=SOME_DUMMY_ENC_PWD"))
                .withHeader("X-Signature", equalTo("SOME_DUMMY_SIGNATURE"))
                .withHeader("X-Timestamp", equalTo("1643236928")).willReturn(ok(loginSessionResponse)));
        stubFor(get(LG_API_V2_USER_INFO).willReturn(ok(userInfoReturned)));
        stubFor(get("/v1" + LG_API_V2_LS_PATH).willReturn(ok(dashboardWMListReturned)));
        String dataCollectedWM = JsonUtils.loadJson("wm-data-result.json");
        stubFor(get("/v1/service/devices/fakeDeviceId").willReturn(ok(dataCollectedWM)));
        Map<String, String> empData = new LinkedHashMap<>();
        empData.put("account_type", userIdType);
        empData.put("country_code", fakeCountry);
        empData.put("username", fakeUserName);

        stubFor(post("/emp/oauth2/token/empsession").withRequestBody(containing("account_type=" + userIdType))
                .withRequestBody(containing("country_code=" + fakeCountry))
                .withRequestBody(containing("username=" + URLEncoder.encode(fakeUserName, StandardCharsets.UTF_8)))
                .withHeader("lgemp-x-session-key", equalTo(loginSessionId)).willReturn(ok(sessionTokenReturned)));

        String tempDir = Objects.requireNonNull(System.getProperty("java.io.tmpdir"),
                "java.io.tmpdir environment variable must be set");
        System.setProperty("THINQ_USER_DATA_FOLDER", tempDir);
        System.setProperty("THINQ_CONNECTION_DATA_FILE", tempDir + File.separator + "token.json");
        System.setProperty("BASE_CAP_CONFIG_DATA_FILE", tempDir + File.separator + "thinq-cap.json");
        // LGThinQBridgeHandler b = new LGThinQBridgeHandler(fakeThing, mock(HttpClientFactory.class));

        final LGThinQWMApiClientService service2 = LGThinQApiClientServiceFactory
                .newWMApiClientService(LG_API_PLATFORM_TYPE_V1, mock(HttpClientFactory.class));
        TokenManager tokenManager = new TokenManager(mock(HttpClient.class));
        try {
            if (!tokenManager.isOauthTokenRegistered(fakeBridgeName)) {
                tokenManager.oauthFirstRegistration(fakeBridgeName, fakeLanguage, fakeCountry, fakeUserName,
                        fakePassword, "http://localhost:8880");
            }
            List<LGDevice> devices = service2.listAccountDevices("bridgeTest");
            assertEquals(devices.size(), 1);
            // service2.getDeviceData(fakeBridgeName, "fakeDeviceId", new DishWasherCapability());
        } catch (Exception e) {
            logger.error("Error testing facade", e);
        }
    }
}
