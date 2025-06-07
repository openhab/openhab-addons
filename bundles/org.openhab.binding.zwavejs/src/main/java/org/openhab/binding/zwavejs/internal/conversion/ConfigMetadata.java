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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.api.dto.Event;
import org.openhab.binding.zwavejs.internal.api.dto.MetadataType;
import org.openhab.binding.zwavejs.internal.api.dto.Value;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragment;
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

    public @Nullable State state;
    public @Nullable StateDescriptionFragment statePattern;
    public Type configType = Type.TEXT;

    public ConfigMetadata(int nodeId, Value data) {
        super(nodeId, data);

        this.configType = configTypeFromMetadata(data.metadata.type, data.value, data.commandClassName);
    }

    public ConfigMetadata(int nodeId, Event data) {
        super(nodeId, data);
    }

    private Type configTypeFromMetadata(MetadataType type, Object value, String commandClassName) {
        type = correctedType(type, value, commandClassName, null);
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
                logger.error(
                        "Node {}. Could not determine config type based on metadata.type: {}, fallback to 'Text' please file a bug report",
                        this.nodeId, type);
                return Type.TEXT;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BaseMetadata [");
        sb.append(", nodeId=" + nodeId);
        sb.append(", Id=" + id);
        sb.append(", label=" + label);
        sb.append(", description=" + description);
        sb.append(", unitSymbol=" + unitSymbol);
        sb.append(", value=" + value);
        sb.append(", itemType=" + itemType);
        sb.append(", writable=" + writable);
        sb.append(", writeProperty=" + writeProperty);
        sb.append(", state=" + state);
        sb.append(", configType=" + configType);
        sb.append(", statePattern=" + statePattern);
        sb.append(", commandClassName=" + commandClassName);
        sb.append(", commandClassId=" + commandClassId);
        sb.append(", endpoint=" + endpoint);
        sb.append("]");
        return sb.toString();
    }
}
