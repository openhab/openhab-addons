/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.voice.googletts.internal.cloudapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Instant;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class calculates the request tokens required to access the
 * Google Translate TTS API
 *
 * @author André Duffeck - Initial contribution
 */
public class GoogleTTSToken {
    private static final Logger logger = LoggerFactory.getLogger(GoogleTTSToken.class);

    private static Long[] googleToken;

    public static String calculateToken(String text) throws IOException {
        Long[] tokens = getGoogleTTSToken();
        Long first_seed = tokens[0];
        Long second_seed = tokens[1];

        ByteBuffer buffer = Charset.forName("UTF-8").encode(text);

        Long a = new Long(first_seed);
        buffer.rewind();
        while (buffer.hasRemaining()) {
            a += buffer.get();
            a = workToken(a, "+-a^+6");
        }
        a = workToken(a, "+-3^+b+-f");
        a ^= second_seed;
        if (a < 0) {
            a = (a & 2147483647L) + 2147483648L;
        }
        a %= 1000000;
        String token = Long.toString(a) + "." + Long.toString(a ^ first_seed);
        logger.debug("Generated token for '{}': {}", text, token);
        return token;
    }

    private static Long[] getGoogleTTSToken() throws IOException {
        if (googleToken != null) {
            return googleToken;
        }

        URLConnection connection = new URL("https://translate.google.com/").openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
        InputStream is = connection.getInputStream();
        String source = IOUtils.toString(is);
        Long[] result = new Long[2];

        result[0] = Instant.now().toEpochMilli() / 3600;

        String tkkLine = new String(source).replaceFirst("(?s).*(TKK=.*?}\\)).*", "$1");
        String a = new String(tkkLine).replaceFirst(".*a\\\\x3d(-?\\d+).*", "$1");
        String b = new String(tkkLine).replaceFirst(".*b\\\\x3d(-?\\d+).*", "$1");
        result[1] = new Long(a) + new Long(b);

        logger.debug("Got Google TTS tokens: {}.{}", Long.toString(result[0]), Long.toString(result[1]));
        googleToken = result;
        return googleToken;
    }

    private static Long rShift(Long val, Long n) {
        if (val >= 0) {
            return val >> n;
        } else {
            return (val + 0x100000000L) >> n;
        }
    }

    private static Long workToken(Long a, String salt) {
        byte[] chars = salt.getBytes();
        for (int i = 0; i < salt.length(); i += 3) {
            byte c = chars[i + 2];
            Long d;

            if (c >= 'a') {
                d = new Long(c - 87);
            } else {
                d = (long) Character.getNumericValue(c);
            }
            if (chars[i + 1] == '+') {
                d = rShift(a, d);
            } else {
                d = a << d;
            }
            if (chars[i] == '+') {
                a = a + d & 4294967295L;
            } else {
                a = a ^ d;
            }
        }
        return a;
    }
}
