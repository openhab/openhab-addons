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
package org.openhab.binding.homekit.internal.camera.helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Verifies downloaded native executables against SHA-256 digests pinned in the binding.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public final class DownloadVerifier {

    private DownloadVerifier() {
    }

    /**
     * Checks that the SHA-256 digest of the file equals the expected digest.
     *
     * @param file the downloaded file
     * @param expectedSha256 the pinned lower case hexadecimal digest, for the asset
     * @param asset the asset name, used in error messages
     * @throws IOException if the file cannot be read, or the digest does not match
     */
    public static void verify(Path file, String expectedSha256, String asset) throws IOException {
        String actual = sha256(file);
        if (!actual.equalsIgnoreCase(expectedSha256)) {
            throw new IOException(
                    "SHA-256 mismatch for " + asset + ": expected " + expectedSha256 + " but was " + actual);
        }
    }

    static String sha256(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                digest.update(buffer, 0, n);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 is not available", e);
        }
    }
}
