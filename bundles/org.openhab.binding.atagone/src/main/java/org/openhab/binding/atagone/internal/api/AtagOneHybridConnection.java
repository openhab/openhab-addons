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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads and writes through the portal, and fills in the report details the portal lacks from the local API when the
 * thermostat answers there. A failing local read never fails the poll.
 *
 * @author Florian Lettner - Initial contribution
 */
@NonNullByDefault
public class AtagOneHybridConnection implements AtagOneConnection {

    private final Logger logger = LoggerFactory.getLogger(AtagOneHybridConnection.class);

    private final AtagOneConnection primary;
    private volatile @Nullable AtagOneConnection details;
    private volatile boolean detailsAvailable;

    public AtagOneHybridConnection(AtagOneConnection primary) {
        this.primary = primary;
    }

    public void setDetailsSource(AtagOneConnection source) {
        details = source;
    }

    @Override
    public RetrieveReplyDTO retrieve() throws AtagOneCommunicationException {
        RetrieveReplyDTO reply = primary.retrieve();
        detailsAvailable = false;
        AtagOneConnection source = details;
        if (source != null) {
            try {
                reply.report.details = source.retrieve().report.details;
                detailsAvailable = true;
            } catch (AtagOneCommunicationException | RuntimeException e) {
                logger.debug("Local report details unavailable: {}", e.getMessage(), e);
            }
        }
        return reply;
    }

    @Override
    public Set<String> unsupportedChannels() {
        return detailsAvailable ? Set.of() : primary.unsupportedChannels();
    }

    @Override
    public void updateControl(ControlUpdateDTO controlUpdate, @Nullable DeviceConfigUpdateDTO configUpdate)
            throws AtagOneCommunicationException {
        primary.updateControl(controlUpdate, configUpdate);
    }

    @Override
    public void updateChSchedule(ScheduleDTO chSchedule) throws AtagOneCommunicationException {
        primary.updateChSchedule(chSchedule);
    }

    @Override
    public void updateDhwSchedule(ScheduleDTO dhwSchedule) throws AtagOneCommunicationException {
        primary.updateDhwSchedule(dhwSchedule);
    }
}
