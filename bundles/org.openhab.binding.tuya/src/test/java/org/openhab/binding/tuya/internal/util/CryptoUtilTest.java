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
package org.openhab.binding.tuya.internal.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tuya.internal.local.ProtocolVersion;
import org.openhab.core.util.HexUtils;

/**
 * The {@link CryptoUtilTest} is a test class for the {@link CryptoUtil} class
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class CryptoUtilTest {

    @Test
    public void testSessionKeyGeneration() {
        byte[] deviceKey = "5c8c3ccc1f0fbdbb".getBytes(StandardCharsets.UTF_8);
        byte[] localKey = HexUtils.hexToBytes("db7b8a7ea8fa28be568531c6e22a2d7e");
        byte[] remoteKey = HexUtils.hexToBytes("30633665666638323536343733353036");
        byte[] expectedSessionKey = HexUtils.hexToBytes("afe2349b17e2cc833247ccb1a52e8aae");

        byte[] sessionKey = CryptoUtil.generateSessionKey(localKey, remoteKey, deviceKey, ProtocolVersion.V3_4);

        assertThat(sessionKey, is(expectedSessionKey));
    }

    @Test
    public void hmac() {
        byte[] deviceKey = "5c8c3ccc1f0fbdbb".getBytes(StandardCharsets.UTF_8);
        byte[] localKey = HexUtils.hexToBytes("2F4311CF69649F40166D4B98E7F9ABAA");
        byte[] hmac = CryptoUtil.hmac(localKey, deviceKey);
        assertThat(HexUtils.bytesToHex(Objects.requireNonNull(hmac)),
                is("31FE0A4FEBB62025703E825E6867BA40AB91BD1F37D765A5396683BB97FC9C7F"));
    }
}
