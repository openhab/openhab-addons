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
package org.openhab.binding.mynice.internal.ssl;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.TrustAllTrustManager;

/**
 * Factory to create SSLSocketFactory.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class BcJsseSocketFactory {
    private static volatile boolean initialized = false;

    private BcJsseSocketFactory() {
    }

    public static SSLSocketFactory get() {
        initializeOnce();

        try {
            SSLContext context = SSLContext.getInstance("TLS", "BCJSSE");
            context.init(null, new TrustManager[] { TrustAllTrustManager.getInstance() }, new SecureRandom());
            return context.getSocketFactory();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyManagementException e) {
            throw new IllegalStateException("Unable to initialize BCJSSE SSLContext", e);
        }
    }

    private static void initializeOnce() {
        if (initialized) {
            return;
        }
        synchronized (BcJsseSocketFactory.class) {
            if (initialized) {
                return;
            }
            // Set required system properties for legacy SSL/TLS connections
            // Bouncy Castle specific properties set via System properties for highest precedence
            System.setProperty("org.bouncycastle.jsse.client.acceptLegacy", "true");
            System.setProperty("org.bouncycastle.jsse.client.allowLegacyInitiatedRenegotiation", "true");

            // Get the existing BC provider from the platform (provided by bcprov bundle)
            if (Security.getProvider("BC") == null) {
                Security.insertProviderAt(new BouncyCastleProvider(), 1);
            }

            // Get or register the BCJSSE provider
            if (Security.getProvider("BCJSSE") == null) {
                // Initialize BCJSSE in non-FIPS mode. It will find the "BC" provider we just registered.
                // This is the correct way to ensure it honors the legacy system properties
                // and should resolve the 'insufficient_security' error.
                Security.addProvider(new BouncyCastleJsseProvider(false));
            }
            initialized = true;
        }
    }
}
