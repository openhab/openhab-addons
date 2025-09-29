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
package org.openhab.binding.unifiaccess.internal.dto;

/**
 * Door detail
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class Door {
    public String id;
    public String name;
    public String fullName;
    public String floorId;
    public String type; // "door"
    public Boolean isBindHub; // must be bound for remote unlock
    public DoorState.LockState doorLockRelayStatus;
    public DoorState.DoorPosition doorPositionStatus;
}
