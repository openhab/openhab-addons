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
/**
 * Validates the standalone cipher implementation against the recorded known answer vector.
 *
 * @author Bernd Weymann - Initial contribution
 */
public final class ReferenceCheck {

    /** Recorded from the binding's own cipher with the specification shift offsets. */
    private static final String EXPECTED = "6e6a0429e5da86c894f53a05e41b2546de01428e6cf2c1e6e08ba1a3b1cca544";

    public static void main(String[] args) throws Exception {
        byte[] key = new byte[32];
        byte[] plain = new byte[32];

        RijndaelReference cipher = RijndaelReference.withKeyMaterial(new int[] { 0, 1, 3, 4 }, key);
        byte[] produced = cipher.encryptBlock(plain, "");

        String hex = BlockCheck.toHex(produced);
        System.out.println("produced: " + hex);
        System.out.println("expected: " + EXPECTED);
        System.out.println(hex.equals(EXPECTED) ? "REFERENCE MATCHES THE BINDING" : "REFERENCE DIFFERS");
        System.exit(hex.equals(EXPECTED) ? 0 : 1);
    }

    private ReferenceCheck() {
    }
}
