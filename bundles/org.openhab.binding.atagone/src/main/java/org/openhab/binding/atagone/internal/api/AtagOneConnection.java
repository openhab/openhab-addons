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
package org.openhab.binding.atagone.internal.api;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.binding.atagone.internal.dto.RetrieveReplyDTO;
import org.openhab.binding.atagone.internal.dto.ScheduleDTO;

/**
 * Transport between the handler and an ATAG ONE thermostat.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public interface AtagOneConnection {

    RetrieveReplyDTO retrieve() throws AtagOneCommunicationException;

    void updateControl(ControlUpdateDTO controlUpdate, @Nullable DeviceConfigUpdateDTO configUpdate)
            throws AtagOneCommunicationException;

    void updateChSchedule(ScheduleDTO chSchedule) throws AtagOneCommunicationException;

    void updateDhwSchedule(ScheduleDTO dhwSchedule) throws AtagOneCommunicationException;

    /**
     * @return channels this transport cannot supply, published as UNDEF
     */
    default Set<String> unsupportedChannels() {
        return Set.of();
    }
}
