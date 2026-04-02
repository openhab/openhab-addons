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
package org.openhab.binding.hue.internal.connection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.PEMTrustManager;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a {@link X509ExtendedTrustManager} to allow secure connections to any Hue Bridge.
 *
 * @author Christoph Weitkamp - Initial Contribution
 * @author Andrew Fiddian-Green - Add support for intermediate certificates on V3 bridges
 */
@NonNullByDefault
public class HueTlsTrustManagerProvider implements TlsTrustManagerProvider {

    private static final String PEM_CACERT_FILENAME = "huebridge_cacert.pem";
    private final String hostname;
    private final boolean useSelfSignedCertificate;

    private final Logger logger = LoggerFactory.getLogger(HueTlsTrustManagerProvider.class);

    private @Nullable X509ExtendedTrustManager trustManager;

    /**
     * Creates a new instance of {@link HueTlsTrustManagerProvider}.
     *
     * See the documentation for more details about 'Signify private CA Certificates for Hue Bridges'.
     *
     * @see <a href=
     *      "https://developers.meethue.com/develop/application-design-guidance/using-https/">https://developers.meethue.com/develop/application-design-guidance/using-https/</a>
     *
     * @param hostname the host name of the Hue Bridge
     * @param useSelfSignedCertificate true, to use the self-signed certificate downloaded from the Hue Bridge;
     *            false, to use the Signify private CA Certificate(s) for Hue Bridges from resources
     */
    public HueTlsTrustManagerProvider(String hostname, boolean useSelfSignedCertificate) {
        this.hostname = hostname;
        this.useSelfSignedCertificate = useSelfSignedCertificate;
    }

    @Override
    public String getHostName() {
        return hostname;
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        X509ExtendedTrustManager localTrustManager = getPEMTrustManager();
        if (localTrustManager == null) {
            logger.error("Cannot get the PEM certificate - returning a TrustAllTrustManager");
        }
        return localTrustManager != null ? localTrustManager : TrustAllTrustManager.getInstance();
    }

    public @Nullable X509ExtendedTrustManager getPEMTrustManager() {
        X509ExtendedTrustManager localTrustManager = trustManager;
        if (localTrustManager != null) {
            return localTrustManager;
        }
        try {
            if (useSelfSignedCertificate) {
                logger.trace("Use self-signed certificate downloaded from Hue Bridge.");
                // use self-signed certificate downloaded from Hue Bridge
                localTrustManager = PEMTrustManager.getInstanceFromServer("https://" + getHostName());
            } else {
                logger.trace("Use Signify private CA Certificate(s) for Hue Bridges from resources.");
                // use Signify private CA Certificate(s) for Hue Bridges from resources
                localTrustManager = getInstanceFromResource(PEM_CACERT_FILENAME);
            }
            this.trustManager = localTrustManager;
        } catch (CertificateException | MalformedURLException e) {
            logger.warn("An unexpected exception occurred: {}", e.getMessage(), e);
        }
        return localTrustManager;
    }

    /**
     * Creates a {@link X509ExtendedTrustManager} instance by reading one or more PEM certificates from the given
     * file. The returned trust manager will trust all certificates that are signed by any of the certificates in
     * the PEM file, including certificates with intermediates. This is useful if you have private CA Certificate(s)
     * stored in a file.
     *
     * @param fileName name of the PEM file located in the resources folder
     * @return a {@link X509ExtendedTrustManager} instance
     * @throws CertificateException
     */
    private X509ExtendedTrustManager getInstanceFromResource(String fileName) throws CertificateException {
        String certificatesString = readPEMCertificatesStringFromResource(fileName);
        if (certificatesString == null) {
            throw new CertificateException("Certificate resource '" + fileName + "' not found or not accessible.");
        }
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            // load all certificates from the PEM file
            Collection<? extends Certificate> certificates;
            try (InputStream input = new ByteArrayInputStream(certificatesString.getBytes(StandardCharsets.UTF_8))) {
                certificates = certificateFactory.generateCertificates(input);
            }
            if (certificates.isEmpty()) {
                throw new CertificateException("No certificates found in " + fileName);
            }
            // build a key store containing all the certificates
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            int index = 0;
            for (Certificate cert : certificates) {
                keyStore.setCertificateEntry("cert-" + index++, cert);
            }
            // build a trust manager from this key store
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                if (trustManager instanceof X509ExtendedTrustManager x509) {
                    return x509;
                }
            }
            throw new CertificateException("No X509ExtendedTrustManager available.");
        } catch (Exception e) {
            throw new CertificateException("Failed to load certificates: " + e.getMessage(), e);
        }
    }

    /**
     * Reads the content of a PEM file from the resources folder and returns it as a string. It may contain multiple
     * certificates, e.g. a certificate chain with intermediate certificates. If the file is not found or cannot be
     * read, null is returned.
     *
     * @param fileName name of the PEM file located in the resources folder
     * @return the content of the PEM file as a string, or null if the file is not found or cannot be read
     */
    private @Nullable String readPEMCertificatesStringFromResource(String fileName) {
        if (HueTlsTrustManagerProvider.class.getClassLoader() instanceof URLClassLoader classLoader
                && classLoader.getResource(fileName) instanceof URL resource) {
            try (InputStream certInputStream = resource.openStream()) {
                return new String(certInputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.error("An unexpected exception occurred: ", e);
            }
        } else {
            logger.error("Resource '{}' not found", fileName);
        }
        return null;
    }
}
