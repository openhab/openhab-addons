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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

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
import org.openhab.binding.blink.internal.dto.BlinkValidation;

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
        this.accountService = spy(new AccountService(new HttpClient(), new Gson()));
    }

    @Test
    void testGenerateClientId() {
        String format = "BlinkCamera_\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{6}";
        assertThat(accountService.generateClientId(), matchesPattern(format));
    }

    @Test
    void testRandomNumberSuccessful() {
        IntStream.range(1, 10)
                .forEach(i -> assertThat(accountService.randomNumber(i), matchesPattern("\\d{" + i + "}")));
    }

    @Test
    void testRandomNumberExceptions() {
        assertThrows(IllegalArgumentException.class, () -> accountService.randomNumber(0));
        assertThrows(IllegalArgumentException.class, () -> accountService.randomNumber(10));
    }

    // ** API calls **

    @Test
    void testLoginIllegalArguments() {
        assertThrows(IllegalArgumentException.class, () -> accountService.login(null, "", true));
    }

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
        assertThat(accountService.login(config, generatedClientId, true), is(account));
    }

    @Test
    void testLoginReauthParams() throws IOException {
        AccountConfiguration config = testAccountConfiguration();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> paramCaptor = ArgumentCaptor.forClass(Map.class);
        doReturn(BlinkTestUtil.testBlinkAccount()).when(accountService).apiRequest(anyString(), anyString(),
                ArgumentMatchers.any(HttpMethod.class), isNull(), paramCaptor.capture(), eq(BlinkAccount.class));
        accountService.login(config, "123", false);
        assertThat(paramCaptor.getValue().size(), is(4));
        accountService.login(config, "123", true);
        assertThat(paramCaptor.getValue().size(), is(3));
    }

    @Test
    void testExceptionOnMissingLoginFields() throws IOException {
        AccountConfiguration config = testAccountConfiguration();
        BlinkAccount account = BlinkTestUtil.testBlinkAccount();
        account.account = null;
        doReturn(account).when(accountService).apiRequest(anyString(), anyString(),
                ArgumentMatchers.any(HttpMethod.class), isNull(), anyMap(), eq(BlinkAccount.class));
        assertThrows(IOException.class, () -> accountService.login(config, "", false));
    }

    private AccountConfiguration testAccountConfiguration() {
        AccountConfiguration config = new AccountConfiguration();
        config.email = "test@hurz.com";
        config.password = "secret";
        return config;
    }

    @Test
    void testVerifyPinIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> accountService.verifyPin(null, ""));
        BlinkAccount account = new BlinkAccount();
        assertThrows(IllegalArgumentException.class, () -> accountService.verifyPin(account, ""));
    }

    @Test
    void testVerifyPinApiCallAndParams() throws IOException {
        String pin = "123456";
        BlinkAccount account = BlinkTestUtil.testBlinkAccount();
        String uri = "/api/v4/account/" + account.account.account_id + "/client/" + account.account.client_id
                + "/pin/verify";
        Map<String, String> params = new HashMap<>();
        params.put("pin", pin);
        BlinkValidation result = new BlinkValidation();
        result.valid = true;
        doReturn(result).when(accountService).apiRequest(account.account.tier, uri, HttpMethod.POST, account.auth.token,
                params, BlinkValidation.class);
        assertThat(accountService.verifyPin(account, pin), is(true));
    }

    @Test
    void testVerifyPinInvalidResult() throws IOException {
        BlinkAccount account = BlinkTestUtil.testBlinkAccount();
        doReturn(null).when(accountService).apiRequest(anyString(), anyString(), ArgumentMatchers.any(HttpMethod.class),
                anyString(), anyMap(), eq(BlinkValidation.class));
        assertThat(accountService.verifyPin(account, ""), is(false));
    }

    @Test
    void testGetDevicesIllegalArguments() {
        assertThrows(IllegalArgumentException.class, () -> accountService.getDevices(null));
    }

    @Test
    void testGetDevicesApiCall() throws IOException {
        BlinkAccount account = BlinkTestUtil.testBlinkAccount();
        BlinkHomescreen result = new BlinkHomescreen();
        String uri = "/api/v3/accounts/" + account.account.account_id + "/homescreen";
        doReturn(result).when(accountService).apiRequest(account.account.tier, uri, HttpMethod.GET, account.auth.token,
                null, BlinkHomescreen.class);
        assertThat(accountService.getDevices(account), is(result));
    }
}
