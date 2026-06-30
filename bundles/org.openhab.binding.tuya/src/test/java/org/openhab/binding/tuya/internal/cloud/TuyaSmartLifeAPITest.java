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
package org.openhab.binding.tuya.internal.cloud;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.test.java.JavaTest;

import com.google.gson.Gson;

/**
 * The {@link TuyaSmartLifeAPITest} is a test class for the {@link TuyaSmartLifeAPI} class
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class TuyaSmartLifeAPITest extends JavaTest {
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) ApiStatusCallback callback;
    private @Mock @NonNullByDefault({}) ScheduledExecutorService scheduler;
    private final Gson gson = new Gson();

    private final TuyaSmartLifeAPI api = new TuyaSmartLifeAPI(callback, scheduler, gson, httpClient);

    private final String accessToken = "3f4eda2bdec17232f67c0b188af3eec1";
    private final String rid = "14582a92-8deb-4722-8e51-e06f753791ac";
    private final String sid = "";

    private final String hash_key = "12837573883acb1aac431dd110356b42";
    private final String query_encdata = "7/Hug3y5ApGqO/liIT17B/rMrggl7sWwgGOhGP2dMAE6mUgqM2onGtai";
    private final String body_encdata = "o0/+mh8args/8OFu5N5AYYEuQcesnNA3UjUOyerzUVHIaBVQppi333rl";

    private final long now = 1588925778000L;

    @Test
    public void secretGeneratingTest() {
        String secretString = api.secret_generating(rid, sid, hash_key);
        Assertions.assertEquals("66651659aec32028", secretString);
    }

    @Test
    public void signTest() {
        var headers = Map.of( //
                "X-appKey", TuyaSmartLifeAPI.TUYA_CLIENT_ID, //
                "X-requestId", rid, //
                "X-sid", sid, //
                "X-time", Long.toString(now), //
                "X-token", accessToken);

        var sign = api.restful_sign(hash_key, query_encdata, body_encdata, headers);
        Assertions.assertEquals("b58c01b21bced33cc5386fb7f1577824a350c9a6d813302c1833a1e24263ce0f", sign);
    }
}
