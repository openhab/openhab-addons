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
package org.openhab.binding.bmwconnecteddrive.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwconnecteddrive.internal.dto.remote.ExecutionStatus;
import org.openhab.binding.bmwconnecteddrive.internal.dto.remote.ExecutionStatusContainer;
import org.openhab.binding.bmwconnecteddrive.internal.handler.RemoteServiceHandler.ExecutionState;
import org.openhab.binding.bmwconnecteddrive.internal.util.FileReader;

import com.google.gson.Gson;

/**
 * The {@link RemoteStatusTest} Test json responses from ConnectedDrive Portal
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class RemoteStatusTest {
    private static final Gson GSON = new Gson();

    @Test
    public void testStatus() {
        String resource1 = FileReader.readFileInString("src/test/resources/webapi/remote-services/pending.json");
        ExecutionStatusContainer esc = GSON.fromJson(resource1, ExecutionStatusContainer.class);
        ExecutionStatus execStatus = esc.executionStatus;
        assertEquals(ExecutionState.PENDING.name(), execStatus.status, "Status");
    }
}
