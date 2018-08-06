/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.ssl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for building a {@link SSLContext}
 *
 * @author Svilen Valkanov
 */

public class SSLContextBuilder {

    private static final String X_509_CERTIFICATE = "X.509";
    private static final String JKS_KEYSTORE_TYPE = "JKS";
    private static final String SSL_PROTOCOL = "SSL";

    private final Logger logger = LoggerFactory.getLogger(SSLContextBuilder.class);

    private BundleContext bundleContext;
    private KeyStore trustStore;
    private KeyStore keyStore;

    private SSLContextBuilder(BundleContext bundleContext)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        this.bundleContext = bundleContext;
        // The keystore has to be initialized before adding the certificate, otherwise exception will be thrown
        this.keyStore = KeyStore.getInstance(JKS_KEYSTORE_TYPE);
        this.trustStore = KeyStore.getInstance(JKS_KEYSTORE_TYPE);
        // It is valid to call load(null) it actually creates new InputStream and initializes the KeyStore with it
        this.keyStore.load(null);
        this.trustStore.load(null);

    }

    /**
     * Creates a SSLContextBuilder that can load key stores and certificates from a bundle or URL
     *
     * @param bundleContext used to load files from a bundle
     * @return SSLContextBuilder
     * @throws IOException if there is an I/O or format problem with the keystore data
     * @throws CertificateException if any of the certificates in the keystore could not be loaded
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found
     * @throws KeyStoreException if no Provider supports a KeyStoreSpi implementation for the specified type.
     */
    public static SSLContextBuilder create(BundleContext bundleContext)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        return new SSLContextBuilder(bundleContext);
    }

    /**
     * Initializes the {@link SSLContext} using the loaded certificates
     *
     * @return SSLContext
     * @throws IllegalStateException if no certificates are loaded
     * @throws KeyManagementException if initialization of KeyManager fails
     * @throws NoSuchAlgorithmException if no Provider supports a SSLContextSpi implementation for the SSL protocol
     * @throws KeyStoreException if this operation fails
     * @throws UnrecoverableKeyException if the key cannot be recovered (e.g. the given password is wrong).
     */
    public SSLContext build()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        if (keyStore.size() > 0 || trustStore.size() > 0) {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            TrustManager[] trustedManagers = trustManagerFactory.getTrustManagers();

            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "mihome".toCharArray());
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            SSLContext sslContext = SSLContext.getInstance(SSL_PROTOCOL);
            sslContext.init(keyManagers, trustedManagers, new SecureRandom());
            return sslContext;
        } else {
            throw new IllegalStateException("No certificates are loaded");
        }
    }

    /**
     * Loads a {@link KeyStore} from the bundle. Accepted key store type is jks
     *
     * @param directory directory path relative to the bundle base dir
     * @param keyStoreName the name of the key store
     * @param keyStorePass the key store password
     * @throws IOException if the keystore file was not found, there is an I/O or format problem with the keystore
     *             data, a password is required but not given, or if the given password was incorrect.
     * @throws CertificateException if any of the certificates in the keystore could not be loaded
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found
     * @return SSLContextBuilder
     */
    public SSLContextBuilder withTrustStore(String directory, String keyStoreName, String keyStorePass)
            throws NoSuchAlgorithmException, CertificateException, IOException {
        URL url = getFileURL(bundleContext, directory, keyStoreName);
        InputStream is = url.openStream();
        trustStore.load(is, keyStorePass.toCharArray());
        is.close();
        logger.debug("TrustStore {} loaded from directory {}", keyStoreName, directory);
        return this;
    }

    /**
     * Loads a {@link KeyStore} from the bundle. Accepted key store type is jks
     *
     * @param directory directory path relative to the bundle base dir
     * @param keyStoreName the name of the key store
     * @param keyStorePass the key store password
     * @throws IOException if the keystore file was not found, there is an I/O or format problem with the keystore
     *             data, a password is required but not given, or if the given password was incorrect.
     * @throws CertificateException if any of the certificates in the keystore could not be loaded
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found
     * @return SSLContextBuilder
     */
    public SSLContextBuilder withKeyStore(String directory, String keyStoreName, String keyStorePass)
            throws NoSuchAlgorithmException, CertificateException, IOException {
        URL url = getFileURL(bundleContext, directory, keyStoreName);
        InputStream is = url.openStream();
        keyStore.load(is, keyStorePass.toCharArray());
        is.close();
        logger.debug("KeyStore {} loaded from directory {}", keyStoreName, directory);
        return this;
    }

    /**
     * Loads a {@link Certificate} from the bundle. Accepted certificate type is X.509
     *
     * @param directory directory path relative to the bundle base dir
     * @param certificateName the name of the key certificate file
     * @throws KeyStoreException if the keystore has not been initialized
     * @throws IOException if the keystore file was not found, there is an I/O or format problem with the keystore
     *             data, a password is required but not given, or if the given password was incorrect
     * @throws CertificateException if the certificate has expired, it is not yet valid or on parsing errors
     * @return SSLContextBuilder
     */
    public SSLContextBuilder withTrustedCertificate(String directory, String certificateName)
            throws KeyStoreException, CertificateException, IOException {
        URL url = getFileURL(bundleContext, directory, certificateName);

        InputStream is = url.openStream();
        CertificateFactory factory = CertificateFactory.getInstance(X_509_CERTIFICATE);
        X509Certificate certificate = (X509Certificate) factory.generateCertificate(is);
        // Will throw exception if the certificate is expired
        certificate.checkValidity();
        trustStore.setCertificateEntry("mihome", certificate);
        is.close();
        logger.debug("Trusted certificate {} loaded from directory {}", certificateName, directory);
        return this;
    }

    private URL getFileURL(BundleContext bundleContext, String directory, String file) throws FileNotFoundException {
        Enumeration<URL> certURLsEnum = bundleContext.getBundle().findEntries(directory, file, false);

        if (certURLsEnum != null) {
            List<URL> certURLs = Collections.list(certURLsEnum);
            if (certURLs.size() == 1) {
                return certURLs.get(0);
            }
        }

        throw new FileNotFoundException(MessageFormat.format("File {0}/{1} not found in bundle {2}", directory, file,
                bundleContext.getBundle().getSymbolicName()));
    }
}
