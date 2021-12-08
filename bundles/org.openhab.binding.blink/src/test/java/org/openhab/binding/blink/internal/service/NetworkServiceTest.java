/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.blink.internal.BlinkTestUtil;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkCommand;

import com.google.gson.Gson;

/**
 * Test class.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class NetworkServiceTest {

    @NonNullByDefault({})
    NetworkService networkService;

    @BeforeEach
    void setup() {
        networkService = spy(new NetworkService(new HttpClient(), new Gson()));
    }

    @Test
    void testIllegalArguments() {
        assertThrows(IllegalArgumentException.class, () -> networkService.arm(null, null, true));
        assertThrows(IllegalArgumentException.class, () -> networkService.arm(null, "123", true));
        BlinkAccount blinkAccount = new BlinkAccount();
        assertThrows(IllegalArgumentException.class, () -> networkService.arm(blinkAccount, "123", true));
    }

    @Test
    void testArmEndpointCalled() throws IOException {
        String networkId = "111";
        BlinkAccount blinkAccount = BlinkTestUtil.testBlinkAccount();
        BlinkCommand expectedArmed = new BlinkCommand();
        expectedArmed.id = 666L;
        BlinkCommand expectedDisarmed = new BlinkCommand();
        expectedDisarmed.id = 777L;
        String armUri = "/api/v1/accounts/" + blinkAccount.account.account_id + "/networks/" + networkId + "/state/arm";
        String disarmUri = "/api/v1/accounts/" + blinkAccount.account.account_id + "/networks/" + networkId
                + "/state/disarm";
        doReturn(expectedArmed).when(networkService).apiRequest(blinkAccount.account.tier, armUri, HttpMethod.POST,
                blinkAccount.auth.token, null, BlinkCommand.class);
        doReturn(expectedDisarmed).when(networkService).apiRequest(blinkAccount.account.tier, disarmUri,
                HttpMethod.POST, blinkAccount.auth.token, null, BlinkCommand.class);
        assertThat(networkService.arm(blinkAccount, networkId, true), is(expectedArmed.id));
        assertThat(networkService.arm(blinkAccount, networkId, false), is(expectedDisarmed.id));
    }
}
