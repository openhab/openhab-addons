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
package org.openhab.binding.gce.internal.model;

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PortDefinition} enum defines and handle port
 * definition constants
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
public enum PortDefinition {
    COUNTER("count", "C", "GetCount", 8),
    ANALOG("analog", "A", "GetAn", 4),
    RELAY("led", "O", "GetOut", 8),
    CONTACT("btn", "I", "GetIn", 8);

    public final String nodeName; // Name used in the status xml file
    public final String portName; // Name used by the M2M protocol
    public final String m2mCommand; // associated M2M command
    public final int quantity; // base number of ports

    private PortDefinition(String nodeName, String portName, String m2mCommand, int quantity) {
        this.nodeName = nodeName;
        this.portName = portName;
        this.m2mCommand = m2mCommand;
        this.quantity = quantity;
    }

    public static final EnumSet<PortDefinition> AS_SET = EnumSet.allOf(PortDefinition.class);

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public boolean isAdvanced(int id) {
        return id >= quantity;
    }

    public static PortDefinition fromM2MCommand(String m2mCommand) {
        return AS_SET.stream().filter(v -> m2mCommand.startsWith(v.m2mCommand)).findFirst().get();
    }

    public static PortDefinition fromPortName(String portName) {
        return AS_SET.stream().filter(v -> portName.startsWith(v.portName)).findFirst().get();
    }

    public static PortDefinition fromGroupId(String groupId) {
        return valueOf(groupId.toUpperCase());
    }

    public static String asChannelId(String portDefinition) {
        String portKind = portDefinition.substring(0, 1);
        PortDefinition result = AS_SET.stream().filter(v -> v.portName.equals(portKind)).findFirst().get();
        return "%s#%s".formatted(result.toString(), portDefinition.substring(1));
    }
}
