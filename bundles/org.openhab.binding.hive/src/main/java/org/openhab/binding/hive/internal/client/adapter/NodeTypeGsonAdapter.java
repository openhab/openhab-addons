/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.adapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.NodeType;

/**
 * A gson {@link com.google.gson.TypeAdapter} for {@link NodeType}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class NodeTypeGsonAdapter extends ComplexEnumGsonTypeAdapterBase<NodeType> {
    public NodeTypeGsonAdapter() {
        super(EnumMapper.builder(NodeType.class)
                .setUnexpectedValue(NodeType.UNEXPECTED)
                .add(NodeType.HUB, HiveApiConstants.NODE_TYPE_HUB)
                .add(NodeType.RADIATOR_VALVE, HiveApiConstants.NODE_TYPE_RADIATOR_VALVE)
                .add(NodeType.SYNTHETIC_DAYLIGHT, HiveApiConstants.NODE_TYPE_SYNTHETIC_DAYLIGHT)
                .add(NodeType.SYNTHETIC_HOME_STATE, HiveApiConstants.NODE_TYPE_SYNTHETIC_HOME_STATE)
                .add(NodeType.SYNTHETIC_RULE, HiveApiConstants.NODE_TYPE_SYNTHETIC_RULE)
                .add(NodeType.THERMOSTAT, HiveApiConstants.NODE_TYPE_THERMOSTAT)
                .add(NodeType.THERMOSTAT_UI, HiveApiConstants.NODE_TYPE_THERMOSTAT_UI)
                .build());
    }
}
