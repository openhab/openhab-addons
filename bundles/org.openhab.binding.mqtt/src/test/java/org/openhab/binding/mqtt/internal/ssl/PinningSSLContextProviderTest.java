/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.internal.ssl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.openhab.core.util.HexUtils;

/**
 * Tests cases for {@link PinTrustManager}.
 *
 * @author David Graeff - Initial contribution
 */
public class PinningSSLContextProviderTest {

    @Test
    public void getDigestDataFor() throws NoSuchAlgorithmException, CertificateException, FileNotFoundException {
        // Load test certificate
        InputStream inputCert = getClass().getResourceAsStream("cert.pem");
        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(inputCert);

        PinTrustManager pinTrustManager = new PinTrustManager();
        PinMessageDigest pinMessageDigest = pinTrustManager.getMessageDigestForSigAlg(certificate.getSigAlgName());
        String hashForCert = HexUtils
                .bytesToHex(pinMessageDigest.digest(pinTrustManager.getEncoded(PinType.CERTIFICATE_TYPE, certificate)));
        String expectedHash = "41fa6d40d1e8f53ac81a395ac13b1efa10917718f1ebe3ac278925716d630b72".toUpperCase();
        assertThat(hashForCert, is(expectedHash));

        String hashForPublicKey = HexUtils
                .bytesToHex(pinMessageDigest.digest(pinTrustManager.getEncoded(PinType.PUBLIC_KEY_TYPE, certificate)));
        String expectedPubKeyHash = "9a6f30e67ae9723579da2575c35daf7da3b370b04ac0bde031f5e1f5e4617eb8".toUpperCase();
        assertThat(hashForPublicKey, is(expectedPubKeyHash));
    }

    // Test if X509Certificate.getEncoded() is called if it is a certificate pin and
    // X509Certificate.getPublicKey().getEncoded() is called if it is a public key pinning.
    @Test
    public void certPinCallsX509CertificateGetEncoded() throws NoSuchAlgorithmException, CertificateException {
        PinTrustManager pinTrustManager = new PinTrustManager();
        pinTrustManager.addPinning(Pin.LearningPin(PinType.CERTIFICATE_TYPE));

        // Mock a certificate
        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getEncoded()).thenReturn(new byte[0]);
        when(certificate.getSigAlgName()).thenReturn("SHA256withRSA");

        pinTrustManager.checkServerTrusted(new X509Certificate[] { certificate }, null);
        verify(certificate).getEncoded();
    }

    // Test if X509Certificate.getEncoded() is called if it is a certificate pin and
    // X509Certificate.getPublicKey().getEncoded() is called if it is a public key pinning.
    @Test
    public void pubKeyPinCallsX509CertificateGetPublicKey() throws NoSuchAlgorithmException, CertificateException {
        PinTrustManager pinTrustManager = new PinTrustManager();
        pinTrustManager.addPinning(Pin.LearningPin(PinType.PUBLIC_KEY_TYPE));

        // Mock a certificate
        PublicKey publicKey = mock(PublicKey.class);
        when(publicKey.getEncoded()).thenReturn(new byte[0]);

        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getSigAlgName()).thenReturn("SHA256withRSA");
        when(certificate.getPublicKey()).thenReturn(publicKey);

        pinTrustManager.checkServerTrusted(new X509Certificate[] { certificate }, null);
        verify(publicKey).getEncoded();
    }

    /**
     * Overwrite {@link #getMessageDigestForSigAlg(String)} method and return a pre-defined {@link PinMessageDigest}.
     */
    public static class PinTrustManagerEx extends PinTrustManager {
        private final PinMessageDigest pinMessageDigest;

        PinTrustManagerEx(PinMessageDigest pinMessageDigest) {
            this.pinMessageDigest = pinMessageDigest;
        }

        @Override
        @NonNull
        PinMessageDigest getMessageDigestForSigAlg(@NonNull String sigAlg) throws CertificateException {
            return pinMessageDigest;
        }
    }

    @Test
    public void learningMode() throws NoSuchAlgorithmException, CertificateException {
        PinMessageDigest pinMessageDigest = new PinMessageDigest("SHA-256");
        PinTrustManager pinTrustManager = new PinTrustManagerEx(pinMessageDigest);
        byte[] testCert = { 1, 2, 3 };
        byte[] digestOfTestCert = pinMessageDigest.digest(testCert);

        // Add a certificate pin in learning mode to a trust manager
        Pin pin = Pin.LearningPin(PinType.CERTIFICATE_TYPE);
        pinTrustManager.addPinning(pin);
        assertThat(pinTrustManager.pins.size(), is(1));

        // Mock a callback
        PinnedCallback callback = mock(PinnedCallback.class);
        pinTrustManager.setCallback(callback);

        // Mock a certificate
        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getEncoded()).thenReturn(testCert);
        when(certificate.getSigAlgName()).thenReturn("SHA256withRSA");

        // Perform an SSL certificate check
        pinTrustManager.checkServerTrusted(new X509Certificate[] { certificate }, null);

        // After a first connect learning mode should turn into check mode. It should have learned the hash data and
        // message digest, returned by PinTrustManager.getMessageDigestForSigAlg().
        assertThat(pin.learning, is(false));
        assertThat(pin.pinData, is(digestOfTestCert));
        assertThat(pin.hashDigest, is(pinMessageDigest));
        // We expect callbacks
        verify(callback).pinnedLearnedHash(eq(pin));
        verify(callback).pinnedConnectionAccepted();
    }

    @Test
    public void checkMode() throws NoSuchAlgorithmException, CertificateException {
        PinTrustManager pinTrustManager = new PinTrustManager();
        PinMessageDigest pinMessageDigest = new PinMessageDigest("SHA-256");
        byte[] testCert = { 1, 2, 3 };
        byte[] digestOfTestCert = pinMessageDigest.digest(testCert);

        // Add a certificate pin in checking mode to a trust manager
        Pin pin = Pin.CheckingPin(PinType.CERTIFICATE_TYPE, pinMessageDigest, digestOfTestCert);
        pinTrustManager.addPinning(pin);
        assertThat(pinTrustManager.pins.size(), is(1));

        // Mock a callback
        PinnedCallback callback = mock(PinnedCallback.class);
        pinTrustManager.setCallback(callback);

        // Mock a certificate
        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getEncoded()).thenReturn(testCert);
        when(certificate.getSigAlgName()).thenReturn("SHA256withRSA");

        // Perform an SSL certificate check
        pinTrustManager.checkServerTrusted(new X509Certificate[] { certificate }, null);

        // After a first connect learning mode should turn into check mode
        assertThat(pin.learning, is(false));
        assertThat(pin.pinData, is(digestOfTestCert));
        assertThat(pin.hashDigest, is(pinMessageDigest));
        // We expect callbacks
        verify(callback, times(0)).pinnedLearnedHash(eq(pin));
        verify(callback).pinnedConnectionAccepted();
    }
}
