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

import java.util.concurrent.ThreadLocalRandom;

import org.openhab.binding.zwavejs.internal.api.dto.ValueId;
import org.openhab.binding.zwavejs.internal.config.ZwaveJSChannelConfiguration;

/**
 * @author Leo Siepel - Initial contribution
 */
public class NodeGetValueCommand extends BaseCommand {
    public int nodeId;
    public ValueId valueId;

    public NodeGetValueCommand(int nodeId, ZwaveJSChannelConfiguration config) {
        command = "node.get_value";
        this.nodeId = nodeId;
        this.messageId = String.format("getvalue|%s|%s|%s|%s|%s|%s|%s", config.endpoint, config.commandClassId,
                config.commandClassName, config.propertyKeyInt != null ? config.propertyKeyInt : config.propertyKeyStr,
                config.readProperty, nodeId, ThreadLocalRandom.current().nextInt(1, 5001));

        this.valueId = new ValueId();
        this.valueId.commandClass = config.commandClassId;
        this.valueId.endpoint = config.endpoint;
        this.valueId.propertyKey = config.propertyKeyInt != null ? config.propertyKeyInt : config.propertyKeyStr;
        this.valueId.property = config.readProperty;
    }
}
