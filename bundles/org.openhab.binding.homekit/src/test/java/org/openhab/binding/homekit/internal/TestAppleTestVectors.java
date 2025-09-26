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
package org.openhab.binding.homekit.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicReference;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homekit.internal.crypto.CryptoUtils;
import org.openhab.binding.homekit.internal.crypto.SRPclient;

/**
 * Tests to validate the code against the test vectors in Apple HomeKit Accessory Protocol
 * Specification chapter 5.5.2 SRP Test Vectors.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestAppleTestVectors {
    // Modulus N
    private static final String N_hex = """
            FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1 29024E08 8A67CC74
            020BBEA6 3B139B22 514A0879 8E3404DD EF9519B3 CD3A431B 302B0A6D F25F1437
            4FE1356D 6D51C245 E485B576 625E7EC6 F44C42E9 A637ED6B 0BFF5CB6 F406B7ED
            EE386BFB 5A899FA5 AE9F2411 7C4B1FE6 49286651 ECE45B3D C2007CB8 A163BF05
            98DA4836 1C55D39A 69163FA8 FD24CF5F 83655D23 DCA3AD96 1C62F356 208552BB
            9ED52907 7096966D 670C354E 4ABC9804 F1746C08 CA18217C 32905E46 2E36CE3B
            E39E772C 180E8603 9B2783A2 EC07A28F B5C55DF0 6F4C52C9 DE2BCBF6 95581718
            3995497C EA956AE5 15D22618 98FA0510 15728E5A 8AAAC42D AD33170D 04507A33
            A85521AB DF1CBA64 ECFB8504 58DBEF0A 8AEA7157 5D060C7D B3970F85 A6E1E4C7
            ABF5AE8C DB0933D7 1E8C94E0 4A25619D CEE3D226 1AD2EE6B F12FFA06 D98A0864
            D8760273 3EC86A64 521F2B18 177B200C BBE11757 7A615D6C 770988C0 BAD946E2
            08E24FA0 74E5AB31 43DB5BFC E0FD108E 4B82D120 A93AD2CA FFFFFFFF FFFFFFFF
            """;

    // Generator g
    private static final String g_hex = """
            05
                    """;

    // Private key a
    private static final String a_hex = """
            60975527 035CF2AD 1989806F 0407210B C81EDC04 E2762A56 AFD529DD DA2D4393
                    """;

    // Public key A
    private static final String A_hex = """
            FAB6F5D2 615D1E32 3512E799 1CC37443 F487DA60 4CA8C923 0FCB04E5 41DCE628
            0B27CA46 80B0374F 179DC3BD C7553FE6 2459798C 701AD864 A91390A2 8C93B644
            ADBF9C00 745B942B 79F9012A 21B9B787 82319D83 A1F83628 66FBD6F4 6BFC0DDB
            2E1AB6E4 B45A9906 B82E37F0 5D6F97F6 A3EB6E18 2079759C 4F684783 7B62321A
            C1B4FA68 641FCB4B B98DD697 A0C73641 385F4BAB 25B79358 4CC39FC8 D48D4BD8
            67A9A3C1 0F8EA121 70268E34 FE3BBE6F F89998D6 0DA2F3E4 283CBEC1 393D52AF
            724A5723 0C604E9F BCE583D7 613E6BFF D67596AD 121A8707 EEC46944 95703368
            6A155F64 4D5C5863 B48F61BD BF19A53E AB6DAD0A 186B8C15 2E5F5D8C AD4B0EF8
            AA4EA500 8834C3CD 342E5E0F 167AD045 92CD8BD2 79639398 EF9E114D FAAAB919
            E14E8509 89224DDD 98576D79 385D2210 902E9F9B 1F2D86CF A47EE244 635465F7
            1058421A 0184BE51 DD10CC9D 079E6F16 04E7AA9B 7CF7883C 7D4CE12B 06EBE160
            81E23F27 A231D184 32D7D1BB 55C28AE2 1FFCF005 F57528D1 5A88881B B3BBB7FE
                                """;

    // Private key b
    private static final String b_hex = """
            E487CB59 D31AC550 471E81F0 0F6928E0 1DDA08E9 74A004F4 9E61F5D1 05284D20
                    """;

    // Public key B
    private static final String B_hex = """
            40F57088 A482D4C7 733384FE 0D301FDD CA9080AD 7D4F6FDF 09A01006 C3CB6D56
            2E41639A E8FA21DE 3B5DBA75 85B27558 9BDB2798 63C56280 7B2B9908 3CD1429C
            DBE89E25 BFBD7E3C AD3173B2 E3C5A0B1 74DA6D53 91E6A06E 465F037A 40062548
            39A56BF7 6DA84B1C 94E0AE20 8576156F E5C140A4 BA4FFC9E 38C3B07B 88845FC6
            F7DDDA93 381FE0CA 6084C4CD 2D336E54 51C464CC B6EC65E7 D16E548A 273E8262
            84AF2559 B6264274 215960FF F47BDD63 D3AFF064 D6137AF7 69661C9D 4FEE4738
            2603C88E AA098058 1D077584 61B777E4 356DDA58 35198B51 FEEA308D 70F75450
            B71675C0 8C7D8302 FD7539DD 1FF2A11C B4258AA7 0D234436 AA42B6A0 615F3F91
            5D55CC3B 966B2716 B36E4D1A 06CE5E5D 2EA3BEE5 A1270E87 51DA45B6 0B997B0F
            FDB0F996 2FEE4F03 BEE780BA 0A845B1D 92714217 83AE6601 A61EA2E3 42E4F2E8
            BC935A40 9EAD19F2 21BD1B74 E2964DD1 9FC845F6 0EFC0933 8B60B6B2 56D8CAC8
            89CCA306 CC370A0B 18C8B886 E95DA0AF 5235FEF4 393020D2 B7F30569 04759042
                                """;

    // Salt s
    private static final String s_hex = """
            BEB25379 D1A8581E B5A72767 3A2441EE
                    """;

    // Verifier v
    private static final String v_hex = """
            9B5E0617 01EA7AEB 39CF6E35 19655A85 3CF94C75 CAF2555E F1FAF759 BB79CB47
            7014E04A 88D68FFC 05323891 D4C205B8 DE81C2F2 03D8FAD1 B24D2C10 9737F1BE
            BBD71F91 2447C4A0 3C26B9FA D8EDB3E7 80778E30 2529ED1E E138CCFC 36D4BA31
            3CC48B14 EA8C22A0 186B222E 655F2DF5 603FD75D F76B3B08 FF895006 9ADD03A7
            54EE4AE8 8587CCE1 BFDE3679 4DBAE459 2B7B904F 442B041C B17AEBAD 1E3AEBE3
            CBE99DE6 5F4BB1FA 00B0E7AF 06863DB5 3B02254E C66E781E 3B62A821 2C86BEB0
            D50B5BA6 D0B478D8 C4E9BBCE C2176532 6FBD1405 8D2BBDE2 C33045F0 3873E539
            48D78B79 4F0790E4 8C36AED6 E880F557 427B2FC0 6DB5E1E2 E1D7E661 AC482D18
            E528D729 5EF74372 95FF1A72 D4027717 13F16876 DD050AE5 B7AD53CC B90855C9
            39566483 58ADFD96 6422F524 98732D68 D1D7FBEF 10D78034 AB8DCB6F 0FCF885C
            C2B2EA2C 3E6AC866 09EA058A 9DA8CC63 531DC915 414DF568 B09482DD AC1954DE
            C7EB714F 6FF7D44C D5B86F6B D1158109 30637C01 D0F6013B C9740FA2 C633BA89
                                """;

    // Scrambling parameter u
    private static final String u_hex = """
            03AE5F3C 3FA9EFF1 A50D7DBB 8D2F60A1 EA66EA71 2D50AE97 6EE34641 A1CD0E51
            C4683DA3 83E8595D 6CB56A15 D5FBC754 3E07FBDD D316217E 01A391A1 8EF06DFF
                                """;

    // Premaster secret S
    private static final String S_hex = """
            F1036FEC D017C823 9C0D5AF7 E0FCF0D4 08B009E3 6411618A 60B23AAB BFC38339
            72682312 14BAACDC 94CA1C53 F442FB51 C1B027C3 18AE238E 16414D60 D1881B66
            486ADE10 ED02BA33 D098F6CE 9BCF1BB0 C46CA2C4 7F2F174C 59A9C61E 2560899B
            83EF6113 1E6FB30B 714F4E43 B735C9FE 6080477C 1B83E409 3E4D456B 9BCA492C
            F9339D45 BC42E67C E6C02C24 3E49F5DA 42A869EC 855780E8 4207B8A1 EA6501C4
            78AAC0DF D3D22614 F531A00D 826B7954 AE8B14A9 85A42931 5E6DD366 4CF47181
            496A9432 9CDE8005 CAE63C2F 9CA4969B FE840019 24037C44 6559BDBB 9DB9D4DD
            142FBCD7 5EEF2E16 2C843065 D99E8F05 762C4DB7 ABD9DB20 3D41AC85 A58C05BD
            4E2DBF82 2A934523 D54E0653 D376CE8B 56DCB452 7DDDC1B9 94DC7509 463A7468
            D7F02B1B EB168571 4CE1DD1E 71808A13 7F788847 B7C6B7BF A1364474 B3B7E894
            78954F6A 8E68D45B 85A88E4E BFEC1336 8EC0891C 3BC86CF5 00978801 78D86135
            E7287234 58538858 D715B7B2 47406222 C1019F53 603F0169 52D49710 0858824C
                                """;

    // Session key K
    private static final String K_hex = """
            5CBC219D B052138E E1148C71 CD449896 3D682549 CE91CA24 F098468F 06015BEB
            6AF245C2 093F98C3 651BCA83 AB8CAB2B 580BBF02 184FEFDF 26142F73 DF95AC50
                                """;

    private static final String I = "alice";
    private static final String p = "password123";

    @Test
    void testBasicConversions() {
        BigInteger N1 = CryptoUtils.toBigInteger(N_hex);
        assertEquals(3072, N1.bitLength());

        BigInteger N2 = new BigInteger(1, CryptoUtils.toBytes(N_hex));
        assertEquals(3072, N2.bitLength());

        assertEquals(N1, N2);

        byte[] nBytes = CryptoUtils.toUnsigned(N1, 384);
        assertEquals(384, nBytes.length);
        assertArrayEquals(CryptoUtils.toBytes(N_hex), nBytes);

        BigInteger g1 = new BigInteger(1, CryptoUtils.toBytes(g_hex));
        assertEquals(5, g1.intValue());
        assertEquals(g1, CryptoUtils.toBigInteger(g_hex));
    }

    @Test
    void testClientKeyConversion() {
        BigInteger N = CryptoUtils.toBigInteger(N_hex);
        BigInteger g = CryptoUtils.toBigInteger(g_hex);

        BigInteger a = CryptoUtils.toBigInteger(a_hex);
        BigInteger A = CryptoUtils.toBigInteger(A_hex);

        BigInteger calcA = g.modPow(a, N);

        assertEquals(A, calcA);

        byte[] act;
        byte[] exp;

        act = CryptoUtils.toUnsigned(a, 32);
        exp = CryptoUtils.toBytes(a_hex);
        assertArrayEquals(exp, act);

        act = CryptoUtils.toUnsigned(A, 384);
        exp = CryptoUtils.toBytes(A_hex);
        assertArrayEquals(exp, act);
    }

    @Test
    void testClientVectors() {
        byte[] a = CryptoUtils.toBytes(a_hex);
        byte[] A = CryptoUtils.toBytes(A_hex);
        byte[] B = CryptoUtils.toBytes(B_hex);
        byte[] s = CryptoUtils.toBytes(s_hex);
        byte[] u = CryptoUtils.toBytes(u_hex);
        byte[] S = CryptoUtils.toBytes(S_hex);
        byte[] K = CryptoUtils.toBytes(K_hex);

        AtomicReference<SRPclient> clientRef = new AtomicReference<>();

        assertDoesNotThrow(() -> clientRef.set(new SRPclient(p, s, B, I, a)));

        SRPclient client = clientRef.get();
        assertNotNull(client);

        assertArrayEquals(A, CryptoUtils.toUnsigned(client.A, 384));
        assertArrayEquals(u, CryptoUtils.toUnsigned(client.u, 64));
        assertArrayEquals(S, CryptoUtils.toUnsigned(client.S, 384));
        assertArrayEquals(K, client.K);
    }

    @Test
    void testServerVectors() {
        byte[] b = CryptoUtils.toBytes(b_hex);
        byte[] A = CryptoUtils.toBytes(A_hex);
        byte[] B = CryptoUtils.toBytes(B_hex);
        byte[] s = CryptoUtils.toBytes(s_hex);
        byte[] u = CryptoUtils.toBytes(u_hex);
        byte[] v = CryptoUtils.toBytes(v_hex);
        byte[] S = CryptoUtils.toBytes(S_hex);
        byte[] K = CryptoUtils.toBytes(K_hex);

        Ed25519PrivateKeyParameters dummyLTPK = new Ed25519PrivateKeyParameters(new SecureRandom());
        byte[] dummyPID = "serverPairingId".getBytes(StandardCharsets.UTF_8);

        AtomicReference<SRPserver> serverRef = new AtomicReference<>();
        assertDoesNotThrow(() -> serverRef.set(new SRPserver(p, s, dummyPID, dummyLTPK, I, b)));

        SRPserver server = serverRef.get();
        assertNotNull(server);

        assertArrayEquals(b, CryptoUtils.toUnsigned(server.b, 32));
        assertArrayEquals(B, CryptoUtils.toUnsigned(server.B, 384));
        assertArrayEquals(v, CryptoUtils.toUnsigned(server.v, 384));

        assertDoesNotThrow(() -> server.createServerProof(A));
        assertArrayEquals(u, CryptoUtils.toUnsigned(server.u, 64));
        assertArrayEquals(S, CryptoUtils.toUnsigned(server.S, 384));
        assertArrayEquals(K, server.K);
    }
}
