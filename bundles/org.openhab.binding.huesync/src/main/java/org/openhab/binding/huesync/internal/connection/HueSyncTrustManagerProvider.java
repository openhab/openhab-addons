package org.openhab.binding.huesync.internal.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.PEMTrustManager;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.io.net.http.TrustAllTrustManager;

/**
 * Provides a {@link PEMTrustManager} to allow secure connections to a Hue HDMI
 * Sync Box
 * 
 * @author Patrik Gfeller - Initial Contribution
 *         Based on the hue binding implementation by Christoph Weitkamp
 */
@NonNullByDefault
public class HueSyncTrustManagerProvider implements TlsTrustManagerProvider {
    private static final String FILENAME = "hsb_cacert.pem";
    private final String hostname;

    private X509ExtendedTrustManager trustManager;

    public HueSyncTrustManagerProvider(String hostname) throws IOException, CertificateException {
        this.hostname = hostname;

        String certificate = readCertificateStringFromResource();
        this.trustManager = (certificate != null) ? new PEMTrustManager(certificate)
                : TrustAllTrustManager.getInstance();
    }

    @Override
    public String getHostName() {
        return this.hostname;
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return this.trustManager;
    }

    private String readCertificateStringFromResource() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(FILENAME);
        InputStream certInputStream = resource.openStream();
        return new String(certInputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
