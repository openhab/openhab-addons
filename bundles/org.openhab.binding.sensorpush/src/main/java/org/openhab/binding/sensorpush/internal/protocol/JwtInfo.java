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
package org.openhab.binding.sensorpush.internal.protocol;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Base64;
import java.util.Base64.Decoder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * JSON Web Token (JWT) Info
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class JwtInfo {

    private final Logger logger = LoggerFactory.getLogger(JwtInfo.class);

    public final JwtToken token;
    public final Instant expires;

    public JwtInfo(@Nullable String jwtString) {
        Gson gson = new Gson();
        Decoder decoder = Base64.getDecoder();
        Charset charset = Charset.forName("UTF-8");

        JwtToken token;

        if (jwtString == null) {
            throw new IllegalArgumentException("Null JWT token String");
        }
        String[] sections = jwtString.split("\\.");
        if (sections.length != 3) {
            logger.debug("JWT token has unexpected number of sections: {}", sections.length);
            throw new IllegalArgumentException("Invalid format for JWT token");
        }
        byte[] payload = decoder.decode(sections[1]);
        String payloadString = new String(payload, charset);
        logger.trace("JWT token payload JSON: {}", payloadString);
        try {
            token = gson.fromJson(payloadString, JwtToken.class);
        } catch (JsonSyntaxException e) {
            logger.debug("Error parsing JSON in JWT token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JSON in JWT token", e);
        }

        if (token == null) {
            throw new IllegalArgumentException("No content in JWT token");
        }
        if (token.exp == null) {
            throw new IllegalArgumentException("No exp field in JWT token");
        }

        expires = Instant.ofEpochSecond(token.exp);
        this.token = token;
    }
}
