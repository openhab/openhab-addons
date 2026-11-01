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
package org.openhab.binding.mqtt.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.osgi.service.cm.ConfigurationException;

/**
 * Test cases for {@link BrokerHandler}.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class BrokerHandlerTest extends JavaTest {

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callbackMock;
    private @Mock @NonNullByDefault({}) Bridge thingMock;

    private @NonNullByDefault({}) MqttBrokerConnectionEx connection;
    private @NonNullByDefault({}) BrokerHandler handler;
    private @NonNullByDefault({}) ScheduledExecutorService scheduler;

    @BeforeEach
    public void setUp() {
        scheduler = new ScheduledThreadPoolExecutor(1);
        connection = spy(new MqttBrokerConnectionEx("10.10.0.10", 80, false, "BrokerHandlerTest"));
        connection.setTimeoutExecutor(scheduler, 10000);
        connection.setConnectionCallback(connection);

        Configuration config = new Configuration();
        when(thingMock.getConfiguration()).thenReturn(config);

        handler = spy(new BrokerHandlerEx(thingMock, connection));
        handler.setCallback(callbackMock);
    }

    @AfterEach
    public void tearDown() {
        scheduler.shutdownNow();
    }

    @Test
    public void handlerInitWithoutUrl() throws IllegalArgumentException {
        // Assume it is a real handler and not a mock as defined above
        handler = new BrokerHandler(thingMock);
        assertThrows(IllegalArgumentException.class, this::initializeHandlerWaitForTimeout);
    }

    @Test
    public void createBrokerConnection() {
        Configuration config = new Configuration();
        config.put("host", "10.10.0.10");
        config.put("port", 80);
        when(thingMock.getConfiguration()).thenReturn(config);
        handler.initialize();
        verify(handler).createBrokerConnection();
    }

    private BrokerHandlerConfig clientCertConfig(boolean secure, boolean hostnameValidated, boolean certificatePin,
            boolean publicKeyPin) {
        BrokerHandlerConfig config = new BrokerHandlerConfig();
        config.secure = secure;
        config.hostnameValidated = hostnameValidated;
        config.certificatepin = certificatePin;
        config.publickeypin = publicKeyPin;
        config.clientKeystore = "/nonexistent/client.p12";
        return config;
    }

    @Test
    public void clientCertificateRefusedWithoutBrokerVerification() {
        MqttBrokerConnection mockedConnection = mock(MqttBrokerConnection.class);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> handler.assignClientCertificate(clientCertConfig(true, false, false, false), mockedConnection));
        assertTrue(e.getMessage().contains("requires hostname validation"));
        verify(mockedConnection, never()).setKeyManagerFactory(any());
    }

    @Test
    public void clientCertificateNotRefusedWithHostnameValidationOrPinning() {
        MqttBrokerConnection mockedConnection = mock(MqttBrokerConnection.class);
        // Not refused by the verification check: loading the (missing) keystore fails instead
        for (BrokerHandlerConfig config : new BrokerHandlerConfig[] { clientCertConfig(true, true, false, false),
                clientCertConfig(true, false, true, false), clientCertConfig(true, false, false, true) }) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> handler.assignClientCertificate(config, mockedConnection));
            assertTrue(e.getMessage().startsWith("Unable to load client keystore"));
        }
    }

    private static final char[] KEYSTORE_PASSWORD = "changeit".toCharArray();

    /** Creates a keystore with a fresh self-signed certificate for localhost (not trusted by any CA). */
    private Path createSelfSignedKeystore(Path dir) throws IOException, InterruptedException {
        Path keystore = dir.resolve("selfsigned.p12");
        String keytool = Path.of(System.getProperty("java.home"), "bin", "keytool").toString();
        Process process = new ProcessBuilder(keytool, "-genkeypair", "-alias", "test", "-keyalg", "RSA", "-keysize",
                "2048", "-validity", "30", "-dname", "CN=localhost", "-ext", "san=dns:localhost", "-storetype",
                "PKCS12", "-keystore", keystore.toString(), "-storepass", new String(KEYSTORE_PASSWORD))
                .redirectErrorStream(true).start();
        process.getInputStream().readAllBytes();
        assertThat(process.waitFor(), is(0));
        return keystore;
    }

    private X509Certificate[] loadChain(Path keystore) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(keystore)) {
            keyStore.load(in, KEYSTORE_PASSWORD);
        }
        return new X509Certificate[] { (X509Certificate) keyStore.getCertificate("test") };
    }

    private BrokerHandlerConfig realKeystoreConfig(Path keystore, boolean hostnameValidated, boolean certificatePin) {
        BrokerHandlerConfig config = clientCertConfig(true, hostnameValidated, certificatePin, false);
        config.clientKeystore = keystore.toString();
        config.clientKeystorePassword = new String(KEYSTORE_PASSWORD);
        return config;
    }

    @Test
    public void clientCertificateWithHostnameValidationRejectsUntrustedServerCertificate(@TempDir Path dir)
            throws Exception {
        Path keystore = createSelfSignedKeystore(dir);
        MqttBrokerConnection mockedConnection = mock(MqttBrokerConnection.class);

        handler.assignClientCertificate(realKeystoreConfig(keystore, true, false), mockedConnection);

        verify(mockedConnection).setKeyManagerFactory(any());
        ArgumentCaptor<TrustManager[]> captor = ArgumentCaptor.forClass(TrustManager[].class);
        verify(mockedConnection).setTrustManagers(captor.capture());
        X509Certificate[] selfSignedChain = loadChain(keystore);
        for (TrustManager trustManager : captor.getValue()) {
            X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
            assertThrows(CertificateException.class, () -> x509TrustManager.checkServerTrusted(selfSignedChain, "RSA"));
        }
    }

    @Test
    public void clientCertificateWithPinningKeepsPinTrustManager(@TempDir Path dir) throws Exception {
        Path keystore = createSelfSignedKeystore(dir);
        MqttBrokerConnection mockedConnection = mock(MqttBrokerConnection.class);

        handler.assignClientCertificate(realKeystoreConfig(keystore, true, true), mockedConnection);

        verify(mockedConnection).setKeyManagerFactory(any());
        verify(mockedConnection, never()).setTrustManagers(any());
    }

    @Test
    public void clientCertificateIgnoredIfNotSecure() {
        MqttBrokerConnection mockedConnection = mock(MqttBrokerConnection.class);
        assertDoesNotThrow(
                () -> handler.assignClientCertificate(clientCertConfig(false, false, false, false), mockedConnection));
        verify(mockedConnection, never()).setKeyManagerFactory(any());
    }

    @Disabled("Temporarily disabled as broken since May 2022")
    @Test
    public void handlerInit() throws InterruptedException, IllegalArgumentException {
        assertThat(initializeHandlerWaitForTimeout(), is(true));

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callbackMock, atLeast(3)).statusUpdated(eq(thingMock), statusInfoCaptor.capture());
        assertThat(statusInfoCaptor.getValue().getStatus(), is(ThingStatus.ONLINE));
    }

    /**
     * Utility method for tests that need the handler to be initialized to go on.
     *
     * @return Return true if successful. You usually want to use:
     *         assertThat(initializeHandlerWaitForTimeout(), is(true));
     * @throws InterruptedException
     * @throws IllegalArgumentException
     * @throws MqttException
     * @throws ConfigurationException
     */
    boolean initializeHandlerWaitForTimeout() throws InterruptedException, IllegalArgumentException {
        MqttBrokerConnection c = connection;

        MqttConnectionObserverEx o = new MqttConnectionObserverEx();
        c.addConnectionObserver(o);

        assertThat(connection.connectionState(), is(MqttConnectionState.DISCONNECTED));
        handler.initialize();
        waitForAssert(() -> verify(connection, times(2)).addConnectionObserver(any()));
        waitForAssert(() -> verify(connection, times(1)).start());
        // First we expect a CONNECTING state and then a CONNECTED unique state change
        waitForAssert(() -> assertThat(o.counter, is(2)));
        // First we expect a CONNECTING state and then a CONNECTED state change
        // (and other CONNECTED after the future completes)
        waitForAssert(() -> verify(handler, times(3)).connectionStateChanged(any(), any()));
        return true;
    }
}
