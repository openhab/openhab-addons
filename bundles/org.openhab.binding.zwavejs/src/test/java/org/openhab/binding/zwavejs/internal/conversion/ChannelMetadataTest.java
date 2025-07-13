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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.zwavejs.internal.DataUtil;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.messages.ResultMessage;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class ChannelMetadataTest {

    private List<Node> getNodesFromStore(String filename) throws IOException {
        ResultMessage resultMessage = DataUtil.fromJson(filename, ResultMessage.class);
        return resultMessage.result.state.nodes;
    }

    private Node getNodeFromStore(String filename, int NodeId) throws IOException {
        return getNodesFromStore(filename).stream().filter(f -> f.nodeId == NodeId).findAny().get();
    }

    @Test
    public void testDetailsNode7Channel83() throws IOException {
        Node node = getNodeFromStore("store_4.json", 7);

        ChannelMetadata details = new ChannelMetadata(7, node.values.get(83));

        assertEquals("multilevel-sensor-power-1", details.id);
        assertEquals("Number:Power", details.itemType);
        assertEquals("EP1 Power", details.label);
        assertNull(details.description);
        assertEquals(new QuantityType<>(4.8, Units.WATT), details.state);
        assertEquals(false, details.writable);
        assertEquals(StateDescriptionFragmentBuilder.create().withPattern("%.2f %unit%").withReadOnly(true).build(),
                details.statePattern);
        assertEquals("W", details.unitSymbol);
    }

    @Test
    public void testDetailsNode7Channel84() throws IOException {
        Node node = getNodeFromStore("store_4.json", 7);

        ChannelMetadata details = new ChannelMetadata(7, node.values.get(84));

        assertEquals("meter-value-65537-1", details.id);
        assertEquals("Number:Energy", details.itemType);
        assertEquals("EP1 Electric Consumption", details.label);
        assertNull(details.description);
        assertEquals(new QuantityType<>(169.48, Units.KILOWATT_HOUR), details.state);
        assertEquals(false, details.writable);
        assertEquals(StateDescriptionFragmentBuilder.create().withPattern("%.2f %unit%").withReadOnly(true).build(),
                details.statePattern);
        assertEquals("kWh", details.unitSymbol);
    }

    @Test
    public void testDetailsNode7Channel85() throws IOException {
        Node node = getNodeFromStore("store_4.json", 7);

        ChannelMetadata details = new ChannelMetadata(7, node.values.get(85));

        assertEquals("meter-value-66049-1", details.id);
        assertEquals("Number:Power", details.itemType);
        assertEquals("EP1 Electric Consumption", details.label);
        assertNull(details.description);
        assertEquals(new QuantityType<>(4.8, Units.WATT), details.state);
        assertEquals(false, details.writable);
        assertEquals("W", details.unitSymbol);
    }

    @Test
    public void testDetailsNode7Channel86() throws IOException {
        Node node = getNodeFromStore("store_4.json", 7);

        ChannelMetadata details = new ChannelMetadata(7, node.values.get(86));

        assertEquals("meter-reset-1", details.id);
        assertEquals("Switch", details.itemType);
        assertEquals("EP1 Reset Accumulated Values", details.label);
        assertNull(details.description);
        assertEquals(UnDefType.NULL, details.state);
        assertEquals(true, details.writable);
        assertNull(details.statePattern);
        assertNull(details.unitSymbol);
    }

    @Test
    public void testDetailsNode7Channel1() throws IOException {
        Node node = getNodeFromStore("store_4.json", 5);

        ChannelMetadata details = new ChannelMetadata(5, node.values.get(1));

        assertEquals("configuration-total-alarm-duration", details.id);
        assertEquals("Number:Time", details.itemType);
        assertEquals("Total Alarm Duration", details.label);
        assertEquals("Total time the Leak Sensor will beep and light its LED in the event of a leak",
                details.description);
        assertEquals(new QuantityType<>(120, Units.MINUTE), details.state);
        assertEquals(true, details.writable);
        assertEquals("min", details.unitSymbol);
    }

    @Test
    public void testDetailsNode74Channel36() throws IOException {
        Node node = getNodeFromStore("store_4.json", 74);

        ChannelMetadata details = new ChannelMetadata(74, node.values.get(36));

        assertEquals("multilevel-sensor-humidity-2", details.id);
        assertEquals("Number:Dimensionless", details.itemType);
        assertEquals("EP2 Humidity", details.label);
        assertNull(details.description);
        assertEquals(new QuantityType<>(36, Units.PERCENT), details.state);
        assertEquals(false, details.writable);
        assertEquals(StateDescriptionFragmentBuilder.create().withPattern("%d %unit%").withReadOnly(true).build(),
                details.statePattern);
        assertEquals("%", details.unitSymbol);
    }

    @Test
    public void testDetailsNode74Channel7() throws IOException {
        Node node = getNodeFromStore("store_4.json", 74);

        ChannelMetadata details = new ChannelMetadata(74, node.values.get(7));

        assertEquals("thermostat-setpoint-setpoint-types-interpretation", details.id);
        assertEquals("String", details.itemType);
        assertEquals("setpointTypesInterpretation", details.label);
        assertNull(details.description);
        assertEquals(new StringType("B"), details.state);
        assertEquals(true, details.writable);
        assertNull(details.statePattern);
    }

    @Test
    public void testDetailsNode13Channel1() throws IOException {
        Node node = getNodeFromStore("store_4.json", 13);

        ChannelMetadata details = new ChannelMetadata(13, node.values.get(1));

        assertEquals("multilevel-switch-value", details.id);
        assertEquals("Number", details.itemType);
        assertEquals("Current Value", details.label);
        assertNull(details.description);
        assertEquals(new DecimalType(0.0), details.state);
        assertEquals(false, details.writable);

        StateDescriptionFragment statePattern = details.statePattern;
        assertNotNull(statePattern);
        assertEquals(BigDecimal.valueOf(0), statePattern.getMinimum());
        assertEquals(BigDecimal.valueOf(99), statePattern.getMaximum());
        assertNull(statePattern.getStep());
        assertEquals("%d", statePattern.getPattern());

        assertNull(details.unitSymbol);
    }

    @Test
    public void testDetailsNode35Channel5() throws IOException {
        Node node = getNodeFromStore("store_4.json", 35);

        ChannelMetadata details = new ChannelMetadata(35, node.values.get(5));

        assertEquals("configuration-transmission-retry-wait-time-255", details.id);
        assertEquals("Number:Time", details.itemType);
        assertEquals("Transmission Retry Wait Time", details.label);
        assertNull(details.description);
        assertEquals(new QuantityType<Time>("1400 ms"), details.state);
        assertEquals(true, details.writable);

        StateDescriptionFragment statePattern = details.statePattern;
        assertNotNull(statePattern);
        assertEquals(BigDecimal.valueOf(0), statePattern.getMinimum());
        assertEquals(BigDecimal.valueOf(255), statePattern.getMaximum());
        assertNull(statePattern.getStep());
        assertEquals("%d %unit%", statePattern.getPattern());

        assertEquals("ms", details.unitSymbol);
    }

    @Test
    public void testDetailsNode7Channel97() throws IOException {
        Node node = getNodeFromStore("store_4.json", 7);

        ChannelMetadata details = new ChannelMetadata(7, node.values.get(97));

        assertEquals("basic-restore-previous-2", details.id);
        assertEquals("Switch", details.itemType);
        assertEquals("EP2 Restore Previous Value", details.label);
        assertNull(details.description);
        assertEquals(UnDefType.NULL, details.state);
        assertEquals(true, details.writable);
        assertTrue(details.isAdvanced);
    }

    @Test
    public void testDetailsNode44Channel11() throws IOException {
        Node node = getNodeFromStore("store_4.json", 44);

        ChannelMetadata details = new ChannelMetadata(44, node.values.get(11));

        assertEquals("color-switch-color", details.id);
        assertEquals("Color", details.itemType);
        assertEquals("Current Color", details.label);
        assertNull(details.description);
        assertEquals(HSBType.fromRGB(0, 0, 0), details.state);
        assertEquals(false, details.writable);
        assertFalse(details.isAdvanced);
    }

    @Test
    public void testDetailsNode44Channel13() throws IOException {
        Node node = getNodeFromStore("store_4.json", 44);

        ChannelMetadata details = new ChannelMetadata(44, node.values.get(13));

        assertEquals("color-switch-hex-color", details.id);
        assertEquals("Color", details.itemType);
        assertEquals("RGB Color", details.label);
        assertNull(details.description);
        assertEquals(HSBType.fromRGB(0, 0, 0), details.state);
        assertEquals(true, details.writable);
        assertFalse(details.isAdvanced);
    }

    @Test
    public void testDetailsNode51Channel0() throws IOException {
        Node node = getNodeFromStore("store_4.json", 51);

        ChannelMetadata details = new ChannelMetadata(51, node.values.get(0));

        assertEquals("binary-switch-value", details.id);
        assertNull(details.description);
        assertEquals("Switch", details.itemType);
        assertEquals("Current Value", details.label);
        assertEquals(OnOffType.OFF, details.state);
        assertEquals(false, details.writable);
        assertNull(details.statePattern);
        assertNull(details.unitSymbol);
    }

    @Test
    public void testDetailsNode78Channel22() throws IOException {
        Node node = getNodeFromStore("store_4.json", 78);

        ChannelMetadata details = new ChannelMetadata(78, node.values.get(22));

        assertEquals("notification-access-control-door-state-simple", details.id);
        assertEquals("Switch", details.itemType);
        assertEquals("Door State (Simple)", details.label);
        assertNull(details.description);
        assertEquals(OnOffType.OFF, details.state);
        assertEquals(false, details.writable);
        assertFalse(details.isAdvanced);
    }

    @Test
    public void testDetailsNode66Channel43() throws IOException {
        Node node = getNodeFromStore("store_4.json", 66);

        ChannelMetadata details = new ChannelMetadata(66, node.values.get(43));

        assertEquals("notification-home-security-motion-sensor-status", details.id);
        assertEquals("Switch", details.itemType);
        assertEquals("Motion Sensor Status", details.label);
        assertNull(details.description);
        assertEquals(OnOffType.OFF, details.state);
        assertEquals(false, details.writable);
    }

    @Test
    public void testDetailsNode16Channel3() throws IOException {
        Node node = getNodeFromStore("store_4.json", 16);

        ChannelMetadata details = new ChannelMetadata(16, node.values.get(13));

        assertEquals(51, details.commandClassId);
        assertEquals("color-switch-color", details.id);
        assertEquals("Color", details.itemType);
        assertEquals("Target Color", details.label);
        assertNull(details.description);
        assertTrue(details.value instanceof Map);
        assertEquals(0L, ((Map<?, ?>) details.value).get("warmWhite"));
        assertEquals(0L, ((Map<?, ?>) details.value).get("coldWhite"));
        assertEquals(53L, ((Map<?, ?>) details.value).get("red"));
        assertEquals(3L, ((Map<?, ?>) details.value).get("green"));
        assertEquals(255L, ((Map<?, ?>) details.value).get("blue"));
        assertNotNull(details.state);
        assertEquals(HSBType.class, Objects.requireNonNull(details.state).getClass());
        assertEquals(ColorUtil.rgbToHsb(new int[] { 53, 3, 255 }), details.state);
        assertEquals(true, details.writable);
    }
}
