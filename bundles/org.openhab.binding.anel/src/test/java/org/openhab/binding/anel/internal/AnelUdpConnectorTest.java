/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.anel.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openhab.binding.anel.internal.auth.AnelAuthentication;
import org.openhab.binding.anel.internal.auth.AnelAuthentication.AuthMethod;

/**
 * This test requires a physical Anel device!
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
@Disabled // requires a physically available device in the local network
public class AnelUdpConnectorTest {

    /*
     * The IP and ports for the Anel device under test.
     */
    private static final String HOST = "192.168.6.63"; // 63 / 64
    private static final int PORT_SEND = 7500; // 7500 / 75001
    private static final int PORT_RECEIVE = 7700; // 7700 / 7701
    private static final String USER = "user7";
    private static final String PASSWORD = "anel";

    /* The device may have an internal delay of 200ms, plus network latency! Should not be <1sec. */
    private static final int WAIT_FOR_DEVICE_RESPONSE_MS = 1000;

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private final Queue<String> receivedMessages = new ConcurrentLinkedQueue<>();

    @Nullable
    private static AnelUdpConnector connector;

    @BeforeAll
    public static void prepareConnector() {
        connector = new AnelUdpConnector(HOST, PORT_RECEIVE, PORT_SEND, EXECUTOR_SERVICE);
    }

    @AfterAll
    @SuppressWarnings("null")
    public static void closeConnection() {
        connector.disconnect();
    }

    @BeforeEach
    @SuppressWarnings("null")
    public void connectIfNotYetConnected() throws Exception {
        Thread.sleep(100);
        receivedMessages.clear(); // clear all previously received messages

        if (!connector.isConnected()) {
            connector.connect(receivedMessages::offer, false);
        }
    }

    @Test
    public void connectionTest() throws Exception {
        final String response = sendAndReceiveSingle(IAnelConstants.BROADCAST_DISCOVERY_MSG);
        /*
         * Expected example response:
         * "NET-PwrCtrl:NET-CONTROL    :192.168.0.244:255.255.255.0:192.168.0.1:0.5.163.21.4.71:Nr. 1,0:Nr. 2,1:Nr. 3,0:Nr. 4,0:Nr. 5,0:Nr. 6,0:Nr. 7,0:Nr. 8,0:248:80:NET-PWRCTRL_04.6:H:xor:"
         */
        assertThat(response, startsWith(IAnelConstants.STATUS_RESPONSE_PREFIX + IAnelConstants.STATUS_SEPARATOR));
    }

    @Test
    public void toggleSwitch1() throws Exception {
        toggleSwitch(1);
    }

    @Test
    public void toggleSwitch2() throws Exception {
        toggleSwitch(2);
    }

    @Test
    public void toggleSwitch3() throws Exception {
        toggleSwitch(3);
    }

    @Test
    public void toggleSwitch4() throws Exception {
        toggleSwitch(4);
    }

    @Test
    public void toggleSwitch5() throws Exception {
        toggleSwitch(5);
    }

    @Test
    public void toggleSwitch6() throws Exception {
        toggleSwitch(6);
    }

    @Test
    public void toggleSwitch7() throws Exception {
        toggleSwitch(7);
    }

    @Test
    public void toggleSwitch8() throws Exception {
        toggleSwitch(8);
    }

    private void toggleSwitch(int switchNr) throws Exception {
        assertThat(switchNr, allOf(greaterThan(0), lessThan(9)));
        final int index = 5 + switchNr;

        // get state of switch 1
        final String status = sendAndReceiveSingle(IAnelConstants.BROADCAST_DISCOVERY_MSG);
        final String[] segments = status.split(IAnelConstants.STATUS_SEPARATOR);
        assertThat(segments[5 + switchNr], anyOf(endsWith(",1"), endsWith(",0")));
        final boolean switch1state = segments[index].endsWith(",1");

        // toggle state of switch 1
        final String auth = AnelAuthentication.getUserPasswordString(USER, PASSWORD, AuthMethod.of(status));
        final String command = "Sw_" + (switch1state ? "off" : "on") + switchNr + auth;
        final String status2 = sendAndReceiveSingle(command);

        // assert new state of switch 1
        assertThat(status2.trim(), not(endsWith(":Err")));
        final String[] segments2 = status2.split(IAnelConstants.STATUS_SEPARATOR);
        final String expectedState = segments2[index].substring(0, segments2[index].length() - 1)
                + (switch1state ? "0" : "1");
        assertThat(segments2[index], equalTo(expectedState));
    }

    @Test
    public void withoutCredentials() throws Exception {
        final String status2 = sendAndReceiveSingle("Sw_on1");
        assertThat(status2.trim(), endsWith(":NoPass:Err"));
        Thread.sleep(3100); // locked for 3 seconds
    }

    private String sendAndReceiveSingle(final String msg) throws Exception {
        final Set<String> response = sendAndReceive(msg);
        assertThat(response, hasSize(1));
        return response.iterator().next();
    }

    @SuppressWarnings("null")
    private Set<String> sendAndReceive(final String msg) throws Exception {
        assertThat(receivedMessages, is(empty()));
        connector.send(msg);
        Thread.sleep(WAIT_FOR_DEVICE_RESPONSE_MS);
        final Set<String> response = new LinkedHashSet<>();
        while (!receivedMessages.isEmpty()) {
            final String receivedMessage = receivedMessages.poll();
            if (receivedMessage != null) {
                response.add(receivedMessage);
            }
        }
        return response;
    }
}
