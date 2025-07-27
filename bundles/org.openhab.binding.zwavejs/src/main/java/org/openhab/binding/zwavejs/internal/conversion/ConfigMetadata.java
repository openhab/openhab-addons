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
package org.openhab.binding.zwavejs.internal.conversion;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.api.dto.MetadataType;
import org.openhab.binding.zwavejs.internal.api.dto.Value;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ConfigMetadata} class represents configuration metadata information for a Z-Wave node.
 * It contains various properties and methods to handle metadata and state information.
 * 
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ConfigMetadata extends BaseMetadata {

    private Logger logger = LoggerFactory.getLogger(ConfigMetadata.class);

    public Type configType = Type.TEXT;

    public ConfigMetadata(int nodeId, Value data) {
        super(nodeId, data);

        this.configType = configTypeFromMetadata(data.metadata.type, data.value, data.commandClass);
    }

    public ConfigMetadata(int nodeId, Event data) {
        super(nodeId, data);
    }

    private Type configTypeFromMetadata(MetadataType type, Object value, int commandClass) {
        type = correctedType(type, value, commandClass, null);
        switch (type) {
            case NUMBER:
                return Type.INTEGER;
            // Might be future cases that require DECIMAL, might depend on scale?
            case COLOR:
                return Type.TEXT;
            case BOOLEAN:
                // switch (or contact ?)
                return Type.BOOLEAN;
            case STRING:
            case STRING_ARRAY:
                return Type.TEXT;
            default:
                logger.warn(
                        "Node {}. Could not determine config type based on metadata.type: {}, fallback to 'Text' please file a bug report",
                        this.nodeId, type);
                return Type.TEXT;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ConfigMetadata [");
        sb.append(", nodeId=" + nodeId);
        sb.append(", Id=" + id);
        sb.append(", label=" + label);
        sb.append(", description=" + description);
        sb.append(", unitSymbol=" + unitSymbol);
        sb.append(", value=" + value);
        sb.append(", itemType=" + itemType);
        sb.append(", writable=" + writable);
        sb.append(", readProperty=" + readProperty);
        sb.append(", writeProperty=" + writeProperty);
        sb.append(", configType=" + configType);
        sb.append(", commandClassName=" + commandClassName);
        sb.append(", commandClassId=" + commandClassId);
        sb.append(", endpoint=" + endpoint);
        sb.append("]");
        return sb.toString();
    }
}
