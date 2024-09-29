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
package org.openhab.binding.paradoxalarm.internal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.paradoxalarm.internal.communication.messages.partition.PartitionCommand;
import org.openhab.binding.paradoxalarm.internal.communication.messages.partition.PartitionCommandPayload;
import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;

/**
 * The {@link TestCreateCommandPayload} This test tests the proper build of command payload object for partition entity
 * with all types of commands.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class TestCreateCommandPayload {

    @Test
    public void testCreatePayload() {
        for (PartitionCommand command : PartitionCommand.values()) {
            for (int partitionNumber = 1; partitionNumber <= 8; partitionNumber++) {
                PartitionCommandPayload payload = new PartitionCommandPayload(partitionNumber, command);
                assertNibble(partitionNumber, command, payload);
            }
        }
    }

    private void assertNibble(int partitionNumber, PartitionCommand command, PartitionCommandPayload payload) {
        byte[] bytes = payload.getBytes();
        int payloadIndexOfByteToCheck = 6 + (partitionNumber - 1) / 2;
        byte byteValue = bytes[payloadIndexOfByteToCheck];
        if ((partitionNumber - 1) % 2 == 0) {
            assertTrue(ParadoxUtil.getHighNibble(byteValue) == command.getCommand());
        } else {
            assertTrue(ParadoxUtil.getLowNibble(byteValue) == command.getCommand());
        }
    }
}
