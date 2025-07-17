/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.serial.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.serial.internal.handler.SerialBridgeConfiguration;
import org.openhab.binding.serial.internal.handler.SerialBridgeHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;

/**
 * Provides a special implementation of CommonBridgeHandler that allows
 * access to the input- and output streams and changes the visibility
 * for #receiveAndProcess so that it can be used in the tests.
 *
 * @author Roland Tapken - Initial contribution
 */
@NonNullByDefault
public class MockupBridgeHandler extends SerialBridgeHandler {

    private final SerialBridgeConfiguration config;

    MockupBridgeHandler(Bridge bridgeMock, SerialPortManager serialPortManager, SerialBridgeConfiguration config) {
        super(bridgeMock, serialPortManager);
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T getConfigAs(Class<T> configurationClass) {
        return (T) this.config;
    }

    @Override
    public void receiveAndProcess(final StringBuilder sb, final boolean firstAttempt) {
        super.receiveAndProcess(sb, firstAttempt);
    }
}
