/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tesla.internal.protocol;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import org.openhab.binding.tesla.TeslaBindingConstants;

/**
 * The {@link TokenRequest} is a datastructure to capture
 * authentication/credentials required to log into the
 * Tesla Remote Service
 *
 * @author Karel Goderis - Initial contribution
 * @author Nicolai Grødum - Adding token based auth
 */
public abstract class TokenRequest {
    private String client_id;
    private String client_secret;

    TokenRequest() throws GeneralSecurityException {
        byte[] ci = { 115, -51, 67, -104, -107, 16, -116, -114, -11, -120, 41, 84, -106, -15, -67, 78, -10, -24, -47,
                124, 35, 73, 10, 43, -9, 123, 127, 126, -114, 58, 23, 3, 115, -70, -115, 46, 17, 87, -115, 31, -67, -90,
                -107, -100, 59, 18, -19, 91, 95, -52, 82, 91, -37, -83, -74, 39, 12, 59, 14, -81, 3, 95, -111, 72 };

        byte[] cs = { -28, 97, -94, 108, 69, -40, 111, 53, 88, -57, 82, 111, 57, 98, 116, -63, -75, -37, 16, 95, 2,
                -113, -46, -112, 32, 73, -43, 23, -114, 38, -110, -85, -42, 41, 98, 118, 30, -2, -11, 93, 22, 89, 56,
                105, -128, 20, -24, -108, 76, 31, -19, 60, 69, -98, -122, 54, 67, 19, 72, -37, 106, 62, -120, -52 };

        SecretKeySpec key = new SecretKeySpec(TeslaBindingConstants.API_NAME.getBytes(), "AES");
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
            byte[] plainText = new byte[ci.length];
            cipher.init(Cipher.DECRYPT_MODE, key);
            int ptLength = cipher.update(ci, 0, ci.length, plainText, 0);
            cipher.doFinal(plainText, ptLength);
            this.client_id = new String(plainText);

            cipher.init(Cipher.DECRYPT_MODE, key);
            ptLength = cipher.update(cs, 0, cs.length, plainText, 0);
            cipher.doFinal(plainText, ptLength);
            this.client_secret = new String(plainText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | ShortBufferException
                | IllegalBlockSizeException | BadPaddingException e) {
            throw e;
        }
    }

}
