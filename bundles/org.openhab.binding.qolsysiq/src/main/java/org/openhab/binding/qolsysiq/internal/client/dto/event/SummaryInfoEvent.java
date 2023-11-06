/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.qolsysiq.internal.client.dto.event;

import java.util.List;

import org.openhab.binding.qolsysiq.internal.client.dto.model.Partition;

/**
 * A {@link InfoEventType#SUMMARY} type of {@link InfoEvent} message sent by the panel
 *
 * @author Dan Cunningham - Initial contribution
 */
public class SummaryInfoEvent extends InfoEvent {
    public List<Partition> partitionList;

    public SummaryInfoEvent() {
        super(InfoEventType.SUMMARY);
    }
}
