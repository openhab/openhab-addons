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
package org.openhab.binding.hive.internal.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.hive.internal.TestUtil;
import org.openhab.binding.hive.internal.client.dto.*;

/**
 * Tests for {@link GsonJsonService}.
 *
 * <p>
 *     These are more like small integration tests than unit tests but it is
 *     simpler to put here because we don't need all the OSGi stuff to work.
 * </p>
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class GsonJsonServiceTest {
    @NonNullByDefault({})
    private JsonService jsonService;

    @Before
    public void setUp() {
        this.jsonService = new GsonJsonService();
    }

    @SuppressWarnings("null")
    @Test
    public void testSessions() throws IOException {
        /* Given */
        final String json = TestUtil.getResourceAsString("/exampleSessions.json");

        final SessionId sessionId = new SessionId("mytoken");
        final String username = "hiveuser@example.com";
        final UserId userId = new UserId(UUID.fromString("deadbeef-dead-beef-dead-beefdeadbeef"));


        /* When */
        final SessionsDto sessionsDto = jsonService.fromJson(json, SessionsDto.class);


        /* Then */
        assertThat(sessionsDto).isNotNull();
        assertThat(sessionsDto.sessions).isNotNull();
        assertThat(sessionsDto.sessions.size()).isEqualTo(1);

        SessionDto sessionDto = sessionsDto.sessions.get(0);

        assertThat(sessionDto.id).isEqualTo(sessionId);
        assertThat(sessionDto.sessionId).isEqualTo(sessionId);
        assertThat(sessionDto.username).isEqualTo(username);
        assertThat(sessionDto.userId).isEqualTo(userId);
        assertThat(sessionDto.extCustomerLevel).isEqualTo(1);
        assertThat(sessionDto.latestSupportedApiVersion).isEqualTo("6");
    }

    @SuppressWarnings("null")
    @Test
    public void testNodesRoot() throws IOException {
        /* Given */
        String json = TestUtil.getResourceAsString("/exampleThermostatNode.json");

        final NodeId nodeId = new NodeId(UUID.fromString("deadbeef-dead-beef-dead-beefdeadbeef"));
        final String nodeName = "Thermostat 1";
        final HiveApiInstant createdOn = new HiveApiInstant(Instant.ofEpochMilli(1513173613796L));
        final HiveApiInstant firstInstall = new HiveApiInstant(Instant.ofEpochMilli(1513173925623L));
        final UserId userId = new UserId(UUID.fromString("deadbeef-dead-beef-dead-beefdeadbeef"));


        /* When */
        final NodesDto nodesDto = jsonService.fromJson(json, NodesDto.class);


        /* Then */
        // Check root fields parsed correctly
        assertThat(nodesDto).isNotNull();
        assertThat(nodesDto.nodes).isNotNull();
        assertThat(nodesDto.nodes.size()).isEqualTo(1);

        // Check node level fields parsed correctly
        final NodeDto nodeDto = nodesDto.nodes.get(0);
        assertThat(nodeDto.id).isEqualTo(nodeId);
        assertThat(nodeDto.name).isEqualTo(nodeName);
        assertThat(nodeDto.nodeType).isEqualTo(NodeType.THERMOSTAT);
        assertThat(nodeDto.parentNodeId).isEqualTo(nodeId);
        assertThat(nodeDto.createdOn).isEqualTo(createdOn);
        assertThat(nodeDto.firstInstall).isEqualTo(firstInstall);
        assertThat(nodeDto.userId).isEqualTo(userId);
        assertThat(nodeDto.ownerId).isEqualTo(userId);
        assertThat(nodeDto.homeId).isEqualTo("deadbeef-dead-beef-dead-beefdeadbeef");
        assertThat(nodeDto.features).isNotNull();
        assertThat(nodeDto.lastUpgradeSucceeded).isFalse();
    }

    @SuppressWarnings("null")
    @Test
    public void testNodesFeatureDeviceManagement() throws IOException {
        /* Given */
        String json = TestUtil.getResourceAsString("/exampleThermostatNode.json");


        /* When */
        final NodesDto nodesDto = jsonService.fromJson(json, NodesDto.class);
        final NodeDto nodeDto = nodesDto.nodes.get(0);


        /* Then */
        assertThat(nodeDto.features).isNotNull();
        assertThat(nodeDto.features.device_management_v1).isNotNull();
        assertThat(nodeDto.features.device_management_v1.featureType).isNotNull();
        assertThat(nodeDto.features.device_management_v1.featureType.reportedValue).isEqualTo(FeatureType.DEVICE_MANAGEMENT_V1);
        assertThat(nodeDto.features.device_management_v1.productType).isNotNull();
        assertThat(nodeDto.features.device_management_v1.productType.reportedValue).isEqualTo(ProductType.HEATING);
    }

    @SuppressWarnings("null")
    @Test
    public void testNodesFeatureHeatingThermostat() throws IOException {
        /* Given */
        String json = TestUtil.getResourceAsString("/exampleThermostatNode.json");


        /* When */
        final NodesDto nodesDto = jsonService.fromJson(json, NodesDto.class);
        final NodeDto nodeDto = nodesDto.nodes.get(0);


        /* Then */
        assertThat(nodeDto.features).isNotNull();
        assertThat(nodeDto.features.heating_thermostat_v1).isNotNull();
        assertThat(nodeDto.features.heating_thermostat_v1.featureType).isNotNull();
        assertThat(nodeDto.features.heating_thermostat_v1.featureType.reportedValue).isEqualTo(FeatureType.HEATING_THERMOSTAT_V1);
        assertThat(nodeDto.features.heating_thermostat_v1.operatingMode).isNotNull();
        assertThat(nodeDto.features.heating_thermostat_v1.operatingMode.reportedValue).isEqualTo(HeatingThermostatOperatingMode.SCHEDULE);
        assertThat(nodeDto.features.heating_thermostat_v1.targetHeatTemperature).isNotNull();
        assertThat(nodeDto.features.heating_thermostat_v1.targetHeatTemperature.reportedValue).isEqualTo(BigDecimal.valueOf(7));
    }

    @SuppressWarnings("null")
    @Test
    public void testNodesFeatureOnOffDevice() throws IOException {
        /* Given */
        String json = TestUtil.getResourceAsString("/exampleThermostatNode.json");


        /* When */
        final NodesDto nodesDto = jsonService.fromJson(json, NodesDto.class);
        final NodeDto nodeDto = nodesDto.nodes.get(0);


        /* Then */
        assertThat(nodeDto.features).isNotNull();
        assertThat(nodeDto.features.on_off_device_v1).isNotNull();
        assertThat(nodeDto.features.on_off_device_v1.featureType).isNotNull();
        assertThat(nodeDto.features.on_off_device_v1.featureType.reportedValue).isEqualTo(FeatureType.ON_OFF_DEVICE_V1);
        assertThat(nodeDto.features.on_off_device_v1.mode).isNotNull();
        assertThat(nodeDto.features.on_off_device_v1.mode.reportedValue).isEqualTo(OnOffMode.ON);
    }

    @SuppressWarnings("null")
    @Test
    public void testNodesFeatureTemperatureSensor() throws IOException {
        /* Given */
        String json = TestUtil.getResourceAsString("/exampleThermostatNode.json");


        /* When */
        final NodesDto nodesDto = jsonService.fromJson(json, NodesDto.class);
        final NodeDto nodeDto = nodesDto.nodes.get(0);


        /* Then */
        assertThat(nodeDto.features).isNotNull();
        assertThat(nodeDto.features.temperature_sensor_v1).isNotNull();
        assertThat(nodeDto.features.temperature_sensor_v1.featureType).isNotNull();
        assertThat(nodeDto.features.temperature_sensor_v1.featureType.reportedValue).isEqualTo(FeatureType.TEMPERATURE_SENSOR_V1);
        assertThat(nodeDto.features.temperature_sensor_v1.temperature).isNotNull();
        assertThat(nodeDto.features.temperature_sensor_v1.temperature.reportedValue).isEqualTo(BigDecimal.valueOf(19));
    }

    @SuppressWarnings("null")
    @Test
    public void testNodesFeatureTransientMode() throws IOException {
        /* Given */
        String json = TestUtil.getResourceAsString("/exampleThermostatNode.json");


        /* When */
        final NodesDto nodesDto = jsonService.fromJson(json, NodesDto.class);
        final NodeDto nodeDto = nodesDto.nodes.get(0);

        /* Then */
        assertThat(nodeDto.features).isNotNull();
        assertThat(nodeDto.features.transient_mode_v1).isNotNull();
        assertThat(nodeDto.features.transient_mode_v1.duration).isNotNull();
        assertThat(nodeDto.features.transient_mode_v1.duration.reportedValue).isEqualTo(1800);
        assertThat(nodeDto.features.transient_mode_v1.endDatetime).isNotNull();
        assertThat(nodeDto.features.transient_mode_v1.endDatetime.reportedValue).isEqualTo(ZonedDateTime.of(
                2020,
                3,
                1,
                22,
                5,
                44,
                442000000,
                ZoneOffset.UTC
        ));

        // TODO: Test more properties
    }

    @SuppressWarnings("null")
    @Test
    public void testNodesFeatureLinks() throws IOException {
        /* Given */
        String json = TestUtil.getResourceAsString("/exampleThermostatNode.json");


        /* When */
        final NodesDto nodesDto = jsonService.fromJson(json, NodesDto.class);
        final NodeDto nodeDto = nodesDto.nodes.get(0);


        /* Then */
        assertThat(nodeDto.features).isNotNull();
        assertThat(nodeDto.features.links_v1).isNotNull();
        assertThat(nodeDto.features.links_v1.featureType).isNotNull();
        assertThat(nodeDto.features.links_v1.featureType.reportedValue).isEqualTo(FeatureType.LINKS_V1);
        assertThat(nodeDto.features.links_v1.links).isNull();
        assertThat(nodeDto.features.links_v1.reverseLinks).isNotNull();

        assertThat(nodeDto.features.links_v1.reverseLinks.reportedValue.size()).isEqualTo(2);
        final ReverseLinkDto reverseLinkDto = nodeDto.features.links_v1.reverseLinks.reportedValue.iterator().next();
        assertThat(reverseLinkDto.boundNode).isEqualTo(new NodeId(UUID.fromString("deadbeef-dead-beef-dead-beefdeadbeef")));
        assertThat(reverseLinkDto.bindingGroupIds).containsExactly(GroupId.TRVBM);
    }
}
