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

import org.eclipse.jdt.annotation.Nullable;

/**
 * A group object of a device.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class GroupObjectDTO {
    public @Nullable Integer number;
    public String name;
    public @Nullable String text;
    public @Nullable String function;
    public @Nullable String dataType;
    public @Nullable Integer sizeBits;
    public boolean read;
    public boolean write;
    public boolean communication;
    public boolean transmit;
    public boolean update;
    public boolean readOnInit;
    public List<String> groupAddresses;
    public @Nullable String sendingGroupAddress;
}
