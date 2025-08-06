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
package org.openhab.binding.zwavejs.internal.api.dto.commands;

import org.openhab.binding.zwavejs.internal.api.dto.ValueId;

/**
 * @author Garrett Scoville - Initial contribution
 */
public class MulticastSetValueCommand extends BaseCommand {
    public int[] nodeIDs;
    public ValueId valueId;
    public Object value;

    public MulticastSetValueCommand(int[] nodeIDs, Object commandClass, Integer endpoint, String property,
            Object value) {
        command = "multicast_group.set_value";
        this.nodeIDs = nodeIDs;
        this.value = value;

        this.valueId = new ValueId();
        this.valueId.commandClass = commandClass;
        this.valueId.endpoint = endpoint;
        this.valueId.property = property;
    }
}
