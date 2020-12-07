/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.omatic.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URISyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.omatic.internal.api.model.OMaticMachine;
import org.openhab.binding.omatic.internal.api.model.OMaticMachineState;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * The {@link OMaticTest}
 *
 * @author Joseph (Seaside) Hagberg - Initial contribution
 */
@NonNullByDefault
public class OMaticTest {

    @BeforeEach
    public void setUp() throws Exception {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.ALL);
    }

    @Test
    public void testStateMachine() throws URISyntaxException, InterruptedException {
        OMaticMachineThingConfig config = new OMaticMachineThingConfig();
        config.setActiveThreshold(100.0);
        config.setIdleTime(1);
        config.setTimerDelay(1);
        OMaticMachine machine = new OMaticMachine(null, config);
        machine.powerInput(100.0);
        assertEquals(machine.getState(), OMaticMachineState.ACTIVE);
        machine.powerInput(9);
        assertEquals(machine.getState(), OMaticMachineState.IDLE);
        Thread.sleep(1100);
        machine.powerInput(9);
        assertEquals(OMaticMachineState.COMPLETE, machine.getState());
    }
}
// Log info started completed
// Write readme
// Write tutorial
