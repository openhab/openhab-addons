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
package org.openhab.binding.icloud.internal.utilities;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Trustmanager that is used to verify that a downloaded certificate is issue by Apple (src/main/resources/apple_root_ca.cer).
 * In case the root certificate from Apple changes, the binding will need to be updated.
 * At the time of writing (January 2020), the root certificate is valid until 2035.
 *
 * @author Erwin Hoeckx
 */
public class AppleCATrustManager implements X509TrustManager {
    private final Logger logger = LoggerFactory.getLogger(AppleCATrustManager.class);
    @NonNull
    private X509Certificate rootCertificate;

    public AppleCATrustManager() {
        rootCertificate = loadRootCertificate();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
        logger.debug("Check if client is trusted is not implemented");
    }

    /**
     * Load the root certificate from the bundle. In case this is no longer valid, immediately throw
     * an exception. The bundle won't be able to function securely without this root certificate.
     *
     * @return the root certificate from Apple
     */
    private X509Certificate loadRootCertificate() {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("apple_root_ca.cer");
        if (resource != null) {
            try (InputStream rootCertificateStream = resource.openStream()) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate rootCertificate =  (X509Certificate) cf.generateCertificate(rootCertificateStream);
                if(rootCertificate.getNotAfter().before(new Date())) {
                    throw new IllegalArgumentException("The root CA certificate found has expired. The binding needs to be updated with the new root certificate!");
                }
                return rootCertificate;
            } catch (IOException | CertificateException e) {
                throw new IllegalArgumentException("Failed to load CA certificate", e);
            }
        } else {
            throw new IllegalArgumentException("Root CA certificate required for validation of certificate(s) is unavailable");
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] certs, String s) throws CertificateException {
        for (X509Certificate cert : certs) {
            verifyCertificateValid(cert, certs);
        }
        logger.debug("Certificate succesfully verified");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{rootCertificate};
    }

    /**
     * Verify the certificate being checked is valid. This is checked using the following rules:
     * - if the issuer is null, throw an error
     * - if the issuer is equal to the root certificate
     *   -verify and return
     * - if the issuer is not equal to the root certificate
     *   - determine the validated issuer
     *   - validate the certificate
     * @param certificateUsed the certificate to check
     * @param allRetrievedCertificates all certificates provided by the verification chain
     * @throws CertificateException in case there is a verification-error
     */
    private void verifyCertificateValid(X509Certificate certificateUsed, X509Certificate[] allRetrievedCertificates) throws CertificateException {
        try {
            X500Principal issuerPrincipal = certificateUsed.getIssuerX500Principal();
            if (issuerPrincipal == null) {
                throw new IllegalArgumentException("The certificate being verified doesn't have an issues, and is therefore not trusted");
            } else if (issuerPrincipal.equals(rootCertificate.getIssuerX500Principal())) {
                certificateUsed.verify(rootCertificate.getPublicKey());
                logger.debug("Certificate is verified (direct or indirect) by Apple's root certificate");
            } else {
                logger.trace("Certificate issuer is not directly verified by Apple's root certificate, checking if the issuer is verified instead");
                X509Certificate issuerCertificate = findValidIssuerCertificate(certificateUsed, allRetrievedCertificates);
                certificateUsed.verify(issuerCertificate.getPublicKey());
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchProviderException e) {
            throw new IllegalArgumentException("Verification of certificate failed, connection refused to prevent security leak", e);
        }
    }

    private X509Certificate findValidIssuerCertificate(X509Certificate certificateUsed, X509Certificate[] certificates) {
        try {
            for (X509Certificate certificate : certificates) {
                if(certificate.equals(certificateUsed)) continue;
                Principal certificatePrincipal = certificate.getSubjectX500Principal();
                if(certificatePrincipal.equals(rootCertificate.getIssuerX500Principal())) {
                    certificateUsed.verify(rootCertificate.getPublicKey());
                } else if (certificatePrincipal.equals(certificateUsed.getIssuerX500Principal())) {
                    certificateUsed.verify(certificate.getPublicKey());
                    return certificate;
                }
            }
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            throw new IllegalStateException("One of the provided certificates cannot be verified against it's issuing certificate");
        }
        throw new IllegalStateException("No issuer is found for issuing principal, certificate(s) not trusted");
    }
}
