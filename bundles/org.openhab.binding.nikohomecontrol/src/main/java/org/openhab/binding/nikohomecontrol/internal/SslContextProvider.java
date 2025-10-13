package org.openhab.binding.nikohomecontrol.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ResourceBundle;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to provide a shared SSLContext and Jetty SslContextFactory
 * that trusts the built-in Niko Home Control controller certificates.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public final class SslContextProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SslContextProvider.class);

    private @Nullable static SSLContext sslContext;
    private @Nullable static TrustManager @Nullable [] trustManagers = null;

    private SslContextProvider() {
    }

    public static synchronized SSLContext getSSLContext() throws CertificateException {
        SSLContext context = sslContext;
        if (context == null) {
            context = buildSSLContext();
        }
        sslContext = context;
        return context;
    }

    public static synchronized TrustManager[] getTrustManagers() throws CertificateException {
        TrustManager[] managers = trustManagers;
        if (managers == null) {
            managers = importCertificates();
        }
        trustManagers = managers;
        return managers;
    }

    public static synchronized SslContextFactory.Client getSslContextFactory() throws CertificateException {
        SslContextFactory.Client factory = new SslContextFactory.Client();
        factory.setSslContext(getSSLContext());
        factory.setEndpointIdentificationAlgorithm(null); // disable hostname verification (allows IP to be used)
        return factory;
    }

    private static SSLContext buildSSLContext() throws CertificateException {
        SSLContext context;
        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, getTrustManagers(), null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.debug("error with SSL context creation: {}", e.getMessage());
            throw new CertificateException("SSL context creation exception", e);
        }

        LOGGER.debug("Initialized SSLContext with embedded Niko Home Control certificates");
        return context;
    }

    private static TrustManager[] importCertificates() throws CertificateException {
        ResourceBundle certificatesBundle = ResourceBundle.getBundle("nikohomecontrol/certificates");

        try {
            // Load server public certificates into key store
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            InputStream certificateStream;
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            for (String certName : certificatesBundle.keySet()) {
                certificateStream = new ByteArrayInputStream(
                        certificatesBundle.getString(certName).getBytes(StandardCharsets.UTF_8));
                X509Certificate certificate = (X509Certificate) cf.generateCertificate(certificateStream);
                keyStore.setCertificateEntry(certName, certificate);
            }

            ResourceBundle.clearCache();

            // Create trust managers used to validate server
            TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmFactory.init(keyStore);
            return tmFactory.getTrustManagers();
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException e) {
            LOGGER.debug("error with SSL context creation: {} ", e.getMessage());
            throw new CertificateException("SSL context creation exception", e);
        } finally {
            ResourceBundle.clearCache();
        }
    }
}
