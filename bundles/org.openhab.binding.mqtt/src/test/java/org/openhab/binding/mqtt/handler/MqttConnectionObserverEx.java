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
package org.openhab.binding.mqtt.handler;

import java.util.concurrent.Semaphore;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;

/**
 * We need an extended MqttConnectionObserverEx for testing if the state changes are coming in the right order.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MqttConnectionObserverEx implements MqttConnectionObserver {
    public int counter = 0;
    public Semaphore semaphore = new Semaphore(1);

    public MqttConnectionObserverEx() throws InterruptedException {
        semaphore.acquire();
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        // First we expect a CONNECTING state and then a DISCONNECTED state change
        if (counter == 0 && state == MqttConnectionState.CONNECTING) {
            counter = 1;
        } else if (counter == 1 && state == MqttConnectionState.CONNECTED) {
            counter = 2;
            semaphore.release();
        } else if (counter == 1 && state == MqttConnectionState.DISCONNECTED) {
            counter = 2;
        }
    }
}
