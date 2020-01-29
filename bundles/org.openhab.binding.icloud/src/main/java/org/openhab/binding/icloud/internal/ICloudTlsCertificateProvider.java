/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.icloud.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.net.http.TlsCertificateProvider;
import org.eclipse.smarthome.io.net.http.TrustAllTrustMananger;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;

import static org.openhab.binding.icloud.internal.ICloudBindingConstants.CERTIFICATE_TMP_PATH;

/**
 * Provides a TrustManager for https://fmipmobile.icloud.com
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@Component
@NonNullByDefault
public class ICloudTlsCertificateProvider implements TlsCertificateProvider {
    private final Logger logger = LoggerFactory.getLogger(ICloudTlsCertificateProvider.class);

    @Override
    public String getHostName() {
        return "fmipmobile.icloud.com";
    }

    @Override
    public URL getCertificate() {
        logger.debug("Getting certificate");

        //If the certificate already exists in the expected location, it's been actively pulled retrieved in the past.
        //Try to use that certificate; if it's not valid, the binding will automatically refresh it on the next query cycle.
        URL resource = getCertificateURL();
        if (resource != null) {
            logger.debug("Certificate file found");
            return resource;
        } else {
            updateCertificate();
            resource = getCertificateURL();
            if(resource != null) {
                return resource;
            }
            throw new IllegalStateException("Certificate resource not found or not accessible");
        }
    }

    private @Nullable URL getCertificateURL() {
        try {
            if(CERTIFICATE_TMP_PATH.toFile().exists()) {
                return CERTIFICATE_TMP_PATH.toUri().toURL();
            } else {
                logger.debug("Certifiate file not found");
            }
        } catch (MalformedURLException e) {
            logger.warn("iCloud certificate URL {} is malformed", CERTIFICATE_TMP_PATH);
        }
        return null;
    }

    public void updateCertificate() throws IllegalStateException {
        logger.debug("Updating certificate from server");
        String certificateContent = retrieveCertificateFromService();
        try {
            Files.write(CERTIFICATE_TMP_PATH, certificateContent.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.warn("Failed to persist iCloud certificate");
            throw new IllegalStateException("Failed to refresh iCloud certificate");
        }
    }

    private String retrieveCertificateFromService() {
        try {
            TrustManager trm = TrustAllTrustMananger.getInstance();

            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, new TrustManager[]{trm}, null);
            SSLSocketFactory factory = sc.getSocketFactory();
            try(SSLSocket socket = (SSLSocket) factory.createSocket(getHostName(), 443)) {
                socket.startHandshake();
                SSLSession session = socket.getSession();
                java.security.cert.Certificate latestServerCert = session.getPeerCertificates()[0];
                StringBuilder certStringBuilder = new StringBuilder();
                certStringBuilder.append("-----BEGIN CERTIFICATE-----\n");
                String certStr = Base64.getEncoder().encodeToString(latestServerCert.getEncoded());
                certStringBuilder.append(splitCertString(certStr)).append("\n");
                certStringBuilder.append("-----END CERTIFICATE-----\n");
                logger.debug("Successfully updated certificate");
                return certStringBuilder.toString();
            } catch(IOException e) {
                logger.warn("Error retrieving certificate from server", e.getMessage());
                throw new IllegalStateException("Failed to retrieve certificate");
            }

        } catch(KeyManagementException | NoSuchAlgorithmException | CertificateEncodingException e) {
            logger.warn("Error creating SSL environment for retrieving certificate", e.getMessage());
            throw new IllegalStateException("Failed to create SSL environment while updating certificate");
        }
    }

    private static String splitCertString(String cert) {
        return cert.replaceAll("(.{64})", "$1\n");
    }
}
