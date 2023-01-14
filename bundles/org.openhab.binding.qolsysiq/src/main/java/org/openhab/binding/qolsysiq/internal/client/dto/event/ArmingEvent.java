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

import org.openhab.binding.qolsysiq.internal.client.dto.model.PartitionStatus;

/**
 * An {@link EventType.ARMING} type of {@link Event} message sent from the panel
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ArmingEvent extends Event {
    public PartitionStatus armingType;
    public Integer partitionId;
    public Integer delay;

    public ArmingEvent() {
        super(EventType.ARMING);
    }
}
