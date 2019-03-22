/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.mqttembeddedbroker.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.io.mqttembeddedbroker.internal.EmbeddedBrokerServiceImpl;
import org.openhab.io.mqttembeddedbroker.internal.ServiceConfiguration;

/**
 * Tests cases for {@link MqttBrokerHandler}. The tests provide mocks for supporting entities using Mockito.
 *
 * @author David Graeff - Initial contribution
 */
public class MqttEmbeddedBrokerServiceTest {

    // Create an accept all trust manager for a client SSLContext
    @SuppressWarnings("unused")
    private static class X509ExtendedTrustManagerEx extends X509ExtendedTrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
                throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                throws CertificateException {
        }
    }

    private EmbeddedBrokerServiceImpl subject;

    private @Mock MqttService service;

    // TODO: wait for NetworkServerTls implementation to enable secure connections as well
    // @Mock
    // NetworkServerTls networkServerTls;

    @Before
    public void setUp() throws ConfigurationException, MqttException, GeneralSecurityException, IOException {
        MockitoAnnotations.initMocks(this);
        subject = new EmbeddedBrokerServiceImpl();
        subject.setMqttService(service);
        // subject.setNetworkServerTls(networkServerTls);
        // SSLContext c = SSLContext.getInstance("TLS");
        // c.init(null, new X509ExtendedTrustManager[] { new X509ExtendedTrustManagerEx() }, new SecureRandom());
        // when(networkServerTls.createSSLContext(anyObject())).thenReturn(c);
    }

    @After
    public void cleanUp() {
        subject.deactivate();
    }

    @Test
    public void connectToEmbeddedServer() throws InterruptedException, IOException {
        ServiceConfiguration config = new ServiceConfiguration();
        config.username = "username";
        config.password = "password";
        config.port = 12345;
        config.secure = false;
        config.persistenceFile = "";

        subject.initialize(config);

        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();

        MqttBrokerConnection c = subject.getConnection();

        MqttConnectionObserver mqttConnectionObserver = (state, error) -> {
            if (state == MqttConnectionState.CONNECTED) {
                semaphore.release();
            }
        };
        c.addConnectionObserver(mqttConnectionObserver);
        if (c.connectionState() == MqttConnectionState.CONNECTED) {
            semaphore.release();
        }

        // Start the connection and wait until timeout or connected callback returns.
        semaphore.tryAcquire(3000, TimeUnit.MILLISECONDS);

        c.removeConnectionObserver(mqttConnectionObserver);

        assertThat(c.getUser(), is("username"));
        assertThat(c.getPassword(), is("password"));

        assertThat(c.connectionState(), is(MqttConnectionState.CONNECTED));
        verify(service).addBrokerConnection(anyString(), eq(c));
    }
}
