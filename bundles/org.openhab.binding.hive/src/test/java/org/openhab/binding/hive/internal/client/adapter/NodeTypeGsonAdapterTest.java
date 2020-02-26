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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.NodeType;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class NodeTypeGsonAdapterTest extends ComplexEnumGsonAdapterTest<NodeType, NodeTypeGsonAdapter> {
    @Override
    protected NodeTypeGsonAdapter getAdapter() {
        return new NodeTypeGsonAdapter();
    }

    @Override
    protected List<List<Object>> getGoodParams() {
        return Arrays.asList(
                Arrays.asList(NodeType.HUB, HiveApiConstants.NODE_TYPE_HUB),
                Arrays.asList(NodeType.RADIATOR_VALVE, HiveApiConstants.NODE_TYPE_RADIATOR_VALVE),
                Arrays.asList(NodeType.SYNTHETIC_DAYLIGHT, HiveApiConstants.NODE_TYPE_SYNTHETIC_DAYLIGHT),
                Arrays.asList(NodeType.SYNTHETIC_HOME_STATE, HiveApiConstants.NODE_TYPE_SYNTHETIC_HOME_STATE),
                Arrays.asList(NodeType.SYNTHETIC_RULE, HiveApiConstants.NODE_TYPE_SYNTHETIC_RULE),
                Arrays.asList(NodeType.THERMOSTAT, HiveApiConstants.NODE_TYPE_THERMOSTAT),
                Arrays.asList(NodeType.THERMOSTAT_UI, HiveApiConstants.NODE_TYPE_THERMOSTAT_UI)
        );
    }

    @Override
    protected NodeType getUnexpectedEnum() {
        return NodeType.UNEXPECTED;
    }

    @Override
    protected String getUnexpectedString() {
        return "http://alertme.com/schema/json/node.class.something.unexpected.json#";
    }
}
