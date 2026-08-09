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
package org.openhab.binding.atv.internal.client.auth;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An SRP-6a group: the safe prime {@code N} and generator {@code g}.
 *
 * <p>
 * The two RFC 5054 groups used for all SRP handshakes:
 * <ul>
 * <li>{@link #RFC5054_2048} (g=2) — AirPlay legacy device authentication.</li>
 * <li>{@link #RFC5054_3072} (g=5) — HAP Pair-Setup for MRP, AirPlay (HAP) and Companion.</li>
 * </ul>
 *
 * @param prime the group prime N
 * @param generator the group generator g
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record SrpGroup(BigInteger prime, BigInteger generator) {

    /** RFC 5054 2048-bit group (generator 2), used by AirPlay legacy authentication. */
    public static final SrpGroup RFC5054_2048 = new SrpGroup(
            new BigInteger("AC6BDB41324A9A9BF166DE5E1389582FAF72B6651987EE07FC3192943DB56050"
                    + "A37329CBB4A099ED8193E0757767A13DD52312AB4B03310DCD7F48A9DA04FD50"
                    + "E8083969EDB767B0CF6095179A163AB3661A05FBD5FAAAE82918A9962F0B93B8"
                    + "55F97993EC975EEAA80D740ADBF4FF747359D041D5C33EA71D281E446B14773B"
                    + "CA97B43A23FB801676BD207A436C6481F1D2B9078717461A5B9D32E688F87748"
                    + "544523B524B0D57D5EA77A2775D2ECFA032CFBDBF52FB3786160279004E57AE6"
                    + "AF874E7303CE53299CCC041C7BC308D82A5698F3A8D0C38271AE35F8E9DBFBB6"
                    + "94B5C803D89F7AE435DE236D525F54759B65E372FCD68EF20FA7111F9E4AFF73", 16),
            BigInteger.TWO);

    /** RFC 5054 3072-bit group (generator 5), used by HAP Pair-Setup. */
    public static final SrpGroup RFC5054_3072 = new SrpGroup(
            new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74"
                    + "020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F1437"
                    + "4FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED"
                    + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF05"
                    + "98DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB"
                    + "9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3B"
                    + "E39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF695581718"
                    + "3995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33"
                    + "A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7"
                    + "ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864"
                    + "D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E2"
                    + "08E24FA074E5AB3143DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF", 16),
            BigInteger.valueOf(5));
}
