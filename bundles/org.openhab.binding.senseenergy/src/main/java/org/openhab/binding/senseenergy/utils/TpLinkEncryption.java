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
package org.openhab.binding.senseenergy.utils;

import java.io.UnsupportedEncodingException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link TpLinkEncryption }
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class TpLinkEncryption {
    static final private int STARTKEY = 0xAB;

    static public byte[] encrypt(String unencrypted) {

        try {
            return encrypt(unencrypted.getBytes("UTF-8"), unencrypted.length());
        } catch (UnsupportedEncodingException e) {
            return new byte[0];
        }
    }

    static public byte[] encrypt(byte[] unencrypted, int l) {
        int length = (l == 0) ? unencrypted.length : l;
        int key = STARTKEY;

        byte[] encrypted = new byte[length];

        for (int i = 0; i < length; i++) {
            key = key ^ unencrypted[i];
            encrypted[i] = (byte) key;
        }

        return encrypted;
    }

    static public byte[] decrypt(byte[] crypted, int l) {
        int key = STARTKEY;
        int a;

        int length = (l == 0) ? crypted.length : l;

        byte[] decrypted = new byte[length];
        for (int i = 0; i < length; i++) {
            a = key ^ crypted[i];
            key = crypted[i];
            decrypted[i] = (byte) a;
        }

        return decrypted;
    }
}
