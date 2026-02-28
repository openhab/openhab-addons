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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.PEMTrustManager;
import org.openhab.core.io.net.http.PEMTrustManager.CertificateInstantiationException;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a {@link PEMTrustManager} to allow secure connections to any Hue Bridge.
 *
 * @author Christoph Weitkamp - Initial Contribution
 */
@NonNullByDefault
public class HueTlsTrustManagerProvider implements TlsTrustManagerProvider {

    private static final String PEM_CACERT_V1_FILENAME = "huebridge_cacert.pem";
    private static final String PEM_CACERT_V2_FILENAME = "huebridge_cacert_v2.pem";
    private final String hostname;
    private final boolean useSelfSignedCertificate;
    private final boolean isBridgeV3orHigher;

    private final Logger logger = LoggerFactory.getLogger(HueTlsTrustManagerProvider.class);

    private @Nullable X509ExtendedTrustManager trustManager;

    /**
     * Creates a new instance of {@link HueTlsTrustManagerProvider}.
     *
     * See the documentation for more details about 'Signify private CA Certificates V1 and V2 for Hue Bridges'.
     *
     * @see <a href=
     *      "https://developers.meethue.com/develop/application-design-guidance/using-https/">https://developers.meethue.com/develop/application-design-guidance/using-https/</a>
     *
     * @param hostname the hostname of the Hue Bridge
     * @param useSelfSignedCertificate true, to use the self-signed certificate downloaded from the Hue Bridge;
     *            false, to use the Signify private CA Certificate V1 or V2 for Hue Bridges from resources
     * @param isBridgeV3orHigher true, to use the 'Signify private CA Certificate V2 for Hue Bridges';
     *            false, to use the 'Signify private CA Certificate V1 for Hue Bridges'
     */
    public HueTlsTrustManagerProvider(String hostname, boolean useSelfSignedCertificate, boolean isBridgeV3orHigher) {
        this.hostname = hostname;
        this.useSelfSignedCertificate = useSelfSignedCertificate;
        this.isBridgeV3orHigher = isBridgeV3orHigher;
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

        // TODO V3 bridges currently don't provide the full certificate chain (missing intermediate certificate)
        if (isBridgeV3orHigher) {
            logger.error("Hue V3 Bridge has incomplete PEM certificate chains - defaulting to a TrustAllTrustManager");
            return TrustAllTrustManager.getInstance();
        }

        try {
            if (useSelfSignedCertificate) {
                logger.trace("Use self-signed certificate downloaded from Hue Bridge.");
                // use self-signed certificate downloaded from Hue Bridge
                localTrustManager = PEMTrustManager.getInstanceFromServer("https://" + getHostName());
            } else {
                logger.trace("Use Signify private CA Certificate for Hue Bridges from resources.");
                // use Signify private CA Certificate V1 or V2 for Hue Bridges from resources
                localTrustManager = getInstanceFromResource(
                        isBridgeV3orHigher ? PEM_CACERT_V2_FILENAME : PEM_CACERT_V1_FILENAME);
            }
            this.trustManager = localTrustManager;
        } catch (CertificateException | MalformedURLException e) {
            logger.debug("An unexpected exception occurred: {}", e.getMessage(), e);
        }
        return localTrustManager;
    }

    /**
     * Creates a {@link PEMTrustManager} instance by reading the PEM certificate from the given file.
     * This is useful if you have a private CA Certificate stored in a file.
     *
     * @param fileName name to the PEM file located in the resources folder
     * @return a {@link PEMTrustManager} instance
     * @throws CertificateInstantiationException
     */
    private PEMTrustManager getInstanceFromResource(String fileName) throws CertificateException {
        String pemCert = readPEMCertificateStringFromResource(fileName);
        if (pemCert != null) {
            return new PEMTrustManager(pemCert);
        }
        throw new CertificateInstantiationException(
                String.format("Certificate resource '%s' not found or not accessible.", fileName));
    }

    private @Nullable String readPEMCertificateStringFromResource(String fileName) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(fileName);
        if (resource != null) {
            try (InputStream certInputStream = resource.openStream()) {
                return new String(certInputStream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.error("An unexpected exception occurred: ", e);
            }
        }
        return null;
    }
}
