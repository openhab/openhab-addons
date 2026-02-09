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

import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Factory to create SSLSocketFactory.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class BcJsseSocketFactory {
    private static volatile boolean initialized = false;

    private BcJsseSocketFactory() {
        // Private constructor
    }

    public static SSLSocketFactory get() {
        try {
            initializeOnce();

            // Create a TrustManager that trusts all certificates
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate @Nullable [] certs, @Nullable String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate @Nullable [] certs, @Nullable String authType) {
                }
            } };

            // Explicitly request the context from the BCJSSE provider by name
            SSLContext context = SSLContext.getInstance("TLS", "BCJSSE");
            context.init(null, trustAllCerts, new SecureRandom());
            return context.getSocketFactory();
        } catch (Exception e) {
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
            Provider bcProvider = Security.getProvider("BC");
            if (bcProvider == null) {
                bcProvider = new BouncyCastleProvider();
                Security.insertProviderAt(bcProvider, 1);
            }

            // Get or register the BCJSSE provider
            Provider bcJsseProvider = Security.getProvider("BCJSSE");
            if (bcJsseProvider == null) {
                // Initialize BCJSSE in non-FIPS mode. It will find the "BC" provider we just registered.
                // This is the correct way to ensure it honors the legacy system properties
                // and should resolve the 'insufficient_security' error.
                bcJsseProvider = new BouncyCastleJsseProvider(false);
                Security.addProvider(bcJsseProvider);
            }
            initialized = true;
        }
    }
}
