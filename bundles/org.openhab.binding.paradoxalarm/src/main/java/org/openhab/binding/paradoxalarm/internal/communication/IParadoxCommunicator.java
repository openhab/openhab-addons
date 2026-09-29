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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.openhab.binding.paradoxalarm.internal.model.ZoneStateFlags;

/**
 * The {@link IParadoxCommunicator} is representing the functionality of communication implementation.
 * If another Paradox alarm system is used this interface must be implemented.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public interface IParadoxCommunicator extends IParadoxInitialLoginCommunicator {

    void refreshMemoryMap();

    List<byte[]> getPartitionFlags();

    ZoneStateFlags getZoneStateFlags();

    void executeCommand(String commandAsString);

    Map<Integer, String> getPartitionLabels();

    Map<Integer, String> getZoneLabels();

    void initializeData();

    MemoryMap getMemoryMap();

    /**
     * Pushes the supplied time to the panel clock via the EVO serial passthrough command.
     *
     * @param time the date/time to set on the panel; must include a valid time-zone so the correct wall-clock values
     *            are
     *            encoded
     */
    void syncTime(ZonedDateTime time);
}
