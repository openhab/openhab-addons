/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mideaac.internal.Utils;

/**
 * The {@link SecurityTest} decodes the AES encrypted byte array portion
 * of the Discovery byte array. This is used in the MideaACDiscoveryServiceTest.
 *
 * @author Robert Eckhoff - Initial Contribution
 */
@NonNullByDefault
public class SecurityTest {

    @SuppressWarnings("null")
    private CloudProvider cloudProvider = mock(CloudProvider.class);;
    private Security security = new Security(cloudProvider);

    @SuppressWarnings("null")
    @BeforeEach
    public void setUp() {
        cloudProvider = mock(CloudProvider.class);
        security = new Security(cloudProvider);
    }

    // This is from a real life session. I test the Discovery String against the
    // returned result from the aesDecrypt()
    @Test
    public void testAesDecrypt() throws Exception {
        // Mock the getSignKey method to return a non-null value
        when(cloudProvider.getSignKey()).thenReturn("xhdiwjnchekd4d512chdjx5d8e4c394D2D7S");

        // Prepare the encrypted data
        byte[] encryptData = Utils.hexStringToByteArray(
                "AF55C8897BEA338348DA7FC0B3EF1F1C889CD57C06462D83069558B66AF14A2D66353F52BAECA68AEB4C3948517F276F72D8A3AD4652EFA55466D58975AEB8D948842E20FBDCA6339558C848ECE09211F62B1D8BB9E5C25DBA7BF8E0CC4C77944BDFB3E16E33D88768CC4C3D0658937D0BB19369BF0317B24D3A4DE9E6A13106");

        // Perform the decryption
        byte[] result = security.aesDecrypt(encryptData);

        // Verify the result
        assertNotNull(result);
        assertNotEquals(0, result.length);

        // Compare to the actual reply
        String decryptedString = new String(result, StandardCharsets.US_ASCII);
        byte[] reply = Utils.hexStringToByteArray(
                "F600A8C02C19000030303030303050303030303030305131423838433239353634334243303030300B6E65745F61635F343342430000870002000000000000000000AC00ACAC00000000B88C295643BC150023082122000300000000000000000000000000000000000000000000000000000000000000000000");
        String reply1 = new String(reply, StandardCharsets.US_ASCII);
        assertEquals(reply1, decryptedString);
    }

    @Test
    public void testGetEncKey() throws NoSuchAlgorithmException {
        // Mock the cloudProvider's getSignKey method
        when(cloudProvider.getSignKey()).thenReturn("xhdiwjnchekd4d512chdjx5d8e4c394D2D7S");

        // Call the getEncKey method
        SecretKeySpec key = security.getEncKey();

        // Verify the result
        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
    }
}
