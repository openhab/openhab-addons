/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bmwconnecteddrive.internal.dto.NetworkError;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FingerprintTest} Test Discovery Results
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class FingerprintTest {
    private final Logger logger = LoggerFactory.getLogger(FingerprintTest.class);

    public void testDiscoveryFingerprint() {
        Bridge b = mock(Bridge.class);
        when(b.getUID()).thenReturn(new ThingUID("bmwconnecteddrive", "account", "user"));
        HttpClientFactory hcf = mock(HttpClientFactory.class);
        ConnectedDriveBridgeHandler bh = new ConnectedDriveBridgeHandler(b, hcf);
        // when(bh.getThing()).thenReturn(b);

        bh.onResponse(Constants.EMPTY_JSON);
        assertEquals(Constants.EMPTY_JSON, bh.getDiscoveryFingerprint(), "Empty Response");

        bh.onResponse(null);
        assertEquals(Constants.EMPTY_JSON, bh.getDiscoveryFingerprint(), "Empty Response");

        String content = FileReader.readFileInString("src/test/resources/webapi/connected-drive-account-info.json");
        bh.onResponse(content);
        String fingerprint = bh.getDiscoveryFingerprint();
        logger.info("{}", fingerprint);
        assertFalse(fingerprint.contains("My Real"), "Anonymous Fingerprint");
        assertFalse(fingerprint.contains("MY_REAL_VIN"), "Anonymous Fingerprint");

        NetworkError err = new NetworkError();
        err.url = "Some URL";
        err.status = 500;
        err.reason = "Internal Server Error";
        bh.onError(err);
        assertEquals(err.toJson(), bh.getDiscoveryFingerprint(), "Empty Response");
    }
}
