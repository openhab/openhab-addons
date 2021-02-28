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
package org.openhab.binding.tapocontrol.internal.helpers;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * TAPO-CIPHER
 * Based on K4CZP3R's p100-java-poc
 * 
 * @author Christian Wild - Initial Initial contribution
 */
public class TapoCipher {
    protected static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    protected static final String CIPHER_ALGORITHM = "AES";
    protected static final String CIPHER_CHARSET = "UTF-8";

    private Cipher enc_cipher;
    private Cipher dec_cipher;
    private MimeEncode mimeEncode;

    public TapoCipher() {
    }

    public TapoCipher(byte[] bArr, byte[] bArr2) throws Exception {
        mimeEncode = new MimeEncode();
        SecretKeySpec secretKeySpec = new SecretKeySpec(bArr, CIPHER_ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(bArr2);
        this.enc_cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        this.enc_cipher.init(1, secretKeySpec, ivParameterSpec);
        this.dec_cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        this.dec_cipher.init(2, secretKeySpec, ivParameterSpec);
    }

    public String encode(String str) throws Exception {
        byte[] doFinal;
        doFinal = this.enc_cipher.doFinal(str.getBytes(CIPHER_CHARSET));
        String encrypted = mimeEncode.encodeToString(doFinal);
        return encrypted.replace("\r\n", "");
    }

    public String decode(String str) throws Exception {
        byte[] data = mimeEncode.decode(str.getBytes(CIPHER_CHARSET));
        byte[] doFinal;
        doFinal = this.dec_cipher.doFinal(data);
        return new String(doFinal);
    }
}
