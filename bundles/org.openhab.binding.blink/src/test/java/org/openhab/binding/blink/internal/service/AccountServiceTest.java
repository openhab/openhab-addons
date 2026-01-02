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
package org.openhab.binding.blink.internal.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.blink.internal.BlinkTestUtil;
import org.openhab.binding.blink.internal.config.AccountConfiguration;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkHomescreen;
import org.openhab.core.storage.json.internal.JsonStorageService;

import com.google.gson.Gson;

/**
 * Test class.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
class AccountServiceTest {

    @NonNullByDefault({})
    AccountService accountService;

    @BeforeEach
    void setup() {
        this.accountService = spy(
                new AccountService(new HttpClient(), new JsonStorageService().getStorage("blink_test"), new Gson()));
    }

    // ** API calls **

    /**
     * @Test
     *       void testLoginIllegalArguments() {
     *       assertThrows(IllegalArgumentException.class, () -> accountService.initialLogin(null, ""));
     *       }
     **/

    @Test
    void testLoginApiCallAndParams() throws IOException {
        String generatedClientId = "dummy_client";
        AccountConfiguration config = testAccountConfiguration();
        Map<String, String> params = new HashMap<>();
        params.put("email", config.email);
        params.put("password", config.password);
        params.put("unique_id", generatedClientId);
        BlinkAccount account = BlinkTestUtil.testBlinkAccount();
        doReturn(account).when(accountService).apiRequest("prod", "/api/v5/account/login", HttpMethod.POST, null,
                params, BlinkAccount.class);
    }

    @Test
    void testLoginReauthParams() throws IOException {
        AccountConfiguration config = testAccountConfiguration();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> paramCaptor = ArgumentCaptor.forClass(Map.class);
        doReturn(BlinkTestUtil.testBlinkAccount()).when(accountService).apiRequest(anyString(), anyString(),
                ArgumentMatchers.any(HttpMethod.class), isNull(), paramCaptor.capture(), eq(BlinkAccount.class));
        accountService.loginStage1WithUsername(config, "hw1");
        assertThat(paramCaptor.getValue().size(), is(4)); /// TODO XXX this had (false), and the below was (true).
        accountService.loginStage1WithUsername(config, "hw1");
        assertThat(paramCaptor.getValue().size(), is(3));
    }

    @Test
    void testExceptionOnMissingLoginFields() throws IOException {
        AccountConfiguration config = testAccountConfiguration();
        BlinkAccount account = BlinkTestUtil.testBlinkAccount();
        account.account = null;
        doReturn(account).when(accountService).apiRequest(anyString(), anyString(),
                ArgumentMatchers.any(HttpMethod.class), isNull(), anyMap(), eq(BlinkAccount.class));
        assertThrows(IOException.class, () -> accountService.loginStage1WithUsername(config, ""));
    }

    private AccountConfiguration testAccountConfiguration() {
        AccountConfiguration config = new AccountConfiguration();
        config.email = "test@hurz.com";
        config.password = "secret";
        return config;
    }

    // @Test
    // void testVerifyPinIllegalArgument() {
    // assertThrows(IllegalArgumentException.class, () -> accountService.old_verifyPin(null, ""));
    // BlinkAccount account = new BlinkAccount();
    // assertThrows(IllegalArgumentException.class, () -> accountService.old_verifyPin(account, ""));
    // }
    //

    // @Test
    // void testVerifyPinApiCallAndParams() throws IOException {
    // String pin = "123456";
    // BlinkAccount account = BlinkTestUtil.testBlinkAccount();
    // String uri = "/api/v4/account/" + account.account.account_id + "/client/" + account.account.client_id
    // + "/pin/verify";
    // Map<String, String> params = new HashMap<>();
    // params.put("pin", pin);
    // BlinkValidation result = new BlinkValidation();
    // result.valid = true;
    // doReturn(result).when(accountService).apiRequest(account.account.tier, uri, HttpMethod.POST,
    // account.auth.access_token, params, BlinkValidation.class);
    // assertThat(accountService.old_verifyPin(account, pin), is(true));
    // }

    // @Test
    // void testVerifyPinInvalidResult() throws IOException {
    // BlinkAccount account = BlinkTestUtil.testBlinkAccount();
    // doReturn(null).when(accountService).apiRequest(anyString(), anyString(), ArgumentMatchers.any(HttpMethod.class),
    // anyString(), anyMap(), eq(BlinkValidation.class));
    // assertThat(accountService.old_verifyPin(account, ""), is(false));
    // }

    @Test
    void testGetDevicesIllegalArguments() {
        assertThrows(IllegalArgumentException.class, () -> accountService.getDevices(null));
    }

    @Test
    void testGetDevicesApiCall() throws IOException {
        BlinkAccount account = BlinkTestUtil.testBlinkAccount();
        BlinkHomescreen result = new BlinkHomescreen();
        String uri = "/api/v3/accounts/" + account.account.account_id + "/homescreen";
        doReturn(result).when(accountService).apiRequest(account.account.tier, uri, HttpMethod.GET,
                account.auth.access_token, null, BlinkHomescreen.class);
        assertThat(accountService.getDevices(account), is(result));
    }
}
