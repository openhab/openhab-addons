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
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.apache.commons.io.FileUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection.Protocol;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.eclipse.smarthome.test.java.JavaTest;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.moquette.broker.RetainedMessage;
import io.moquette.broker.subscriptions.Topic;

/**
 * Tests connections with the embedded broker. Checks for credential based login,
 * check for SSL connections.
 *
 * @author David Graeff - Initial contribution
 */
public class MqttEmbeddedBrokerServiceTest extends JavaTest {
    private final Logger logger = LoggerFactory.getLogger(MqttEmbeddedBrokerServiceTest.class);

    private EmbeddedBrokerService subject;
    private Map<String, Object> config = new HashMap<>();
    private @Mock MqttService service;

    @Before
    public void setUp() throws ConfigurationException, MqttException, GeneralSecurityException, IOException {
        MockitoAnnotations.initMocks(this);

        config.put("username","username");
        config.put("password", "password");
        config.put("port",12345);
        config.put("secure",false);
        config.put("persistenceFile","");

        subject = new EmbeddedBrokerService(service, config);
    }

    @After
    public void cleanUp() {
        subject.deactivate();
    }

    public void waitForConnectionChange(MqttBrokerConnection c, MqttConnectionState expectedState)
            throws InterruptedException {
        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();

        MqttConnectionObserver mqttConnectionObserver = (state, error) -> {
            if (state == expectedState) {
                semaphore.release();
            }
        };
        c.addConnectionObserver(mqttConnectionObserver);
        if (c.connectionState() == expectedState) {
            semaphore.release();
        }

        // Start the connection and wait until timeout or connected callback returns.
        semaphore.tryAcquire(3000, TimeUnit.MILLISECONDS);

        c.removeConnectionObserver(mqttConnectionObserver);

    }

    @Test
    public void connectUnsecureAndTestCredentials() throws InterruptedException, IOException, ExecutionException {
        MqttBrokerConnection c = subject.getConnection();
        assertNotNull(c);
        waitForConnectionChange(c, MqttConnectionState.CONNECTED);

        assertThat(c.getUser(), is("username"));
        assertThat(c.getPassword(), is("password"));

        assertThat(c.connectionState(), is(MqttConnectionState.CONNECTED));
        verify(service).addBrokerConnection(anyString(), eq(c));

        // Connect with a second connection but wrong credentials
        MqttBrokerConnection wrongCredentials = new MqttBrokerConnection(Protocol.TCP, c.getHost(), c.getPort(), false,
                "wrongCred");
        wrongCredentials.setCredentials("someUser", "somePassword");
        try {
            if (wrongCredentials.start().get()) {
                fail("Wrong credentials accepted!");
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        wrongCredentials.stop().get();

        // Connect with a second connection but correct credentials
        MqttBrokerConnection correctCredentials = new MqttBrokerConnection(Protocol.TCP, c.getHost(), c.getPort(),
                false, "correctCred");
        correctCredentials.setCredentials(c.getUser(), c.getPassword());
        try {
            if (!correctCredentials.start().get()) {
                fail("Couldn't connect although correct credentials");
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        correctCredentials.stop().get();
    }

    @Test
    public void connectSecure() throws InterruptedException, IOException {
        config.put("secure", true);
        subject.modified(config);

        MqttBrokerConnection c = subject.getConnection();
        assertNotNull(c);

        waitForConnectionChange(c, MqttConnectionState.CONNECTED);

        assertThat(c.getUser(), is("username"));
        assertThat(c.getPassword(), is("password"));

        assertThat(c.connectionState(), is(MqttConnectionState.CONNECTED));
        verify(service).addBrokerConnection(anyString(), eq(c));
    }

    @Test
    public void testPersistence() throws InterruptedException, IOException, ExecutionException {
        config.put("persistenceFile","persist.mqtt");
        Path path = Paths.get(ConfigConstants.getUserDataFolder()).toAbsolutePath();
        File jksFile = path.resolve("persist.mqtt").toFile();

        if (jksFile.exists()) {
            jksFile.delete();
        }

        subject.modified(config);

        MqttBrokerConnection c = subject.getConnection();
        assertNotNull(c);

        waitForConnectionChange(c, MqttConnectionState.CONNECTED);

        c.publish("demotopic", "testtest".getBytes(), 2, true).get();

        // Stop server -> close persistence storage and sync it to disk
        subject.deactivate();
        assertTrue(jksFile.exists());
        // this is needed to ensure the file is correctly written
        waitForAssert(() -> assertEquals(12288, jksFile.length()));

        // The original file is still open, create a temp file for examination
        File temp = File.createTempFile("abc", ".tmp");
        temp.deleteOnExit();
        FileUtils.copyFile(jksFile, temp);

        MVStore mvStore = new MVStore.Builder().fileName(temp.getAbsolutePath()).autoCommitDisabled().open();
        MVMap<Topic, RetainedMessage> openMap = mvStore.openMap("retained_store");

        assertThat(openMap.size(), is(1));
        for (Map.Entry<Topic, RetainedMessage> entry : openMap.entrySet()) {
            assertThat(entry.getKey().toString(), is("demotopic"));
            assertThat(new String(entry.getValue().getPayload()), is("testtest"));
        }
    }
}
