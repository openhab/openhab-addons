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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.zwavejs.internal.DataUtil;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.messages.ResultMessage;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ConfigMetadataTest {

    private List<Node> getNodesFromStore(String filename) throws IOException {
        ResultMessage resultMessage = DataUtil.fromJson(filename, ResultMessage.class);
        return resultMessage.result.state.nodes;
    }

    private Node getNodeFromStore(String filename, int NodeId) throws IOException {
        return getNodesFromStore(filename).stream().filter(f -> f.nodeId == NodeId).findAny().get();
    }

    @Test
    public void testChannelDetailsStore4Node7Config1() throws IOException {
        Node node = getNodeFromStore("store_4.json", 7);

        ConfigMetadata details = new ConfigMetadata(7, node.values.get(23));

        assertEquals("configuration-key-s-1-associations-send-when-double-clicking-8", details.id);
        assertEquals(Type.INTEGER, details.configType);
        assertEquals("Key S 1 Associations : Send When Double Clicking", details.label);
        assertNull(details.description);
        assertEquals(true, details.writable);
        // assertEquals(BigDecimal.valueOf(0), details.statePattern.getMinimum());
        // assertEquals(BigDecimal.valueOf(1), details.statePattern.getMaximum());
        // assertEquals(BigDecimal.valueOf(1), details.statePattern.getStep());
        // assertEquals("%0.d", details.statePattern.getPattern());
        // assertEquals(new StateOption("0", "Activated"), details.statePattern.getOptions().get(0));
        // assertEquals(new StateOption("1", "Inactive"), details.statePattern.getOptions().get(1));

        assertNull(details.unitSymbol);
        assertNotNull(details.optionList);
        Map<String, String> optionList = details.optionList;
        if (optionList != null) {
            assertEquals(2, optionList.size());
            assertEquals("Enable", optionList.get("0"));
            assertEquals("Disable", optionList.get("1"));
        }
    }
}
