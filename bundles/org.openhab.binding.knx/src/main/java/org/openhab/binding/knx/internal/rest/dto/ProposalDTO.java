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
package org.openhab.binding.knx.internal.rest.dto;

import java.util.List;

/**
 * The wizard's proposal: Things with their channels and Items, and the rooms they are placed in.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ProposalDTO {
    public List<ProposedThingDTO> things;
    public List<ProposedRoomDTO> rooms;
    public List<String> unknown;
    public List<LeftOutDTO> leftOut;
    public boolean readAtStart;
    public int thingsWithoutHandler;
    /** The device Things below the bridge, by UID. */
    public List<DeviceThingDTO> deviceThings;
}
