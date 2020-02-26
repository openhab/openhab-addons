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
package org.openhab.binding.hive.internal.client.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.hive.internal.TestUtil;
import org.openhab.binding.hive.internal.client.*;
import org.openhab.binding.hive.internal.client.dto.*;
import org.openhab.binding.hive.internal.client.exception.HiveClientRequestException;
import org.openhab.binding.hive.internal.client.exception.HiveException;
import org.openhab.binding.hive.internal.client.feature.*;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public class DefaultNodeRepositoryTest {
    @NonNullByDefault({})
    @Mock
    private HiveApiRequestFactory requestFactory;

    @NonNullByDefault({})
    @Mock
    private HiveApiRequest request;

    @NonNullByDefault({})
    @Mock
    private HiveApiResponse response;


    @Before
    public void setUp() {
        initMocks(this);
    }

    private void setUpSuccessResponse(final NodesDto content) throws HiveClientRequestException {
        when(this.requestFactory.newRequest(any())).thenReturn(this.request);

        when(this.request.accept(any())).thenReturn(this.request);
        when(this.request.get()).thenReturn(this.response);

        when(this.response.getStatusCode()).thenReturn(200);

        when(this.response.getContent(eq(NodesDto.class))).thenReturn(content);
    }

    private static HeatingThermostatV1FeatureDto getGoodHeatingThermostatV1FeatureDto() {
        final HeatingThermostatV1FeatureDto heatingThermostatV1FeatureDto = new HeatingThermostatV1FeatureDto();
        heatingThermostatV1FeatureDto.operatingMode = TestUtil.createSimpleFeatureAttributeDto(HeatingThermostatOperatingMode.SCHEDULE);
        heatingThermostatV1FeatureDto.operatingState = TestUtil.createSimpleFeatureAttributeDto(HeatingThermostatOperatingState.OFF);
        heatingThermostatV1FeatureDto.targetHeatTemperature = TestUtil.createSimpleFeatureAttributeDto(BigDecimal.valueOf(20));
        heatingThermostatV1FeatureDto.temporaryOperatingModeOverride = TestUtil.createSimpleFeatureAttributeDto(OverrideMode.NONE);

        return heatingThermostatV1FeatureDto;
    }

    private static TransientModeV1FeatureDto getGoodTransientModeV1FeatureDtoWithoutActions() {
        final TransientModeV1FeatureDto transientModeV1FeatureDto = new TransientModeV1FeatureDto();
        transientModeV1FeatureDto.duration = TestUtil.createSimpleFeatureAttributeDto(60000L);
        transientModeV1FeatureDto.isEnabled = TestUtil.createSimpleFeatureAttributeDto(true);
        transientModeV1FeatureDto.startDatetime = TestUtil.createSimpleFeatureAttributeDto(Instant.now().atZone(ZoneId.systemDefault()));
        transientModeV1FeatureDto.endDatetime = TestUtil.createSimpleFeatureAttributeDto(Instant.now().atZone(ZoneId.systemDefault()));

        return transientModeV1FeatureDto;
    }

    @Test
    public void testGetNode() throws HiveException {
        /* Given */
        final NodesDto nodesDto = new NodesDto();
        nodesDto.nodes = Collections.emptyList();

        this.setUpSuccessResponse(nodesDto);

        final NodeId nodeId = TestUtil.NODE_ID_DEADBEEF;

        final DefaultNodeRepository nodeRepository = new DefaultNodeRepository(this.requestFactory);


        /* When */
        nodeRepository.getNode(nodeId);


        /* Then */
        verify(this.requestFactory, times(1)).newRequest(URI.create("nodes/" + nodeId.toString()));
    }

    /**
     * Make sure an exception is not thrown when the transient mode feature has
     * no actions.
     */
    @Test
    public void testHeatingWithMissingTransientActions() throws HiveException {
        /* Given */
        final NodeId nodeId = TestUtil.NODE_ID_DEADBEEF;
        final NodeDto nodeDto = TestUtil.createSimpleNodeDto(nodeId);
        assert nodeDto.features != null;

        // Add Heating Thermostat feature
        nodeDto.features.heating_thermostat_v1 = getGoodHeatingThermostatV1FeatureDto();

        // Add Transient Mode but leave actions null
        nodeDto.features.transient_mode_v1 = getGoodTransientModeV1FeatureDtoWithoutActions();

        final NodesDto nodesDto = new NodesDto();
        nodesDto.nodes = Collections.singletonList(nodeDto);

        this.setUpSuccessResponse(nodesDto);

        final DefaultNodeRepository nodeRepository = new DefaultNodeRepository(this.requestFactory);


        /* When */
        final @Nullable Node node = nodeRepository.getNode(nodeId);


        /* Then */
        // No exceptions hopefully!
        assertThat(node).isNotNull();

        final @Nullable HeatingThermostatFeature heatingThermostatFeature = node.getFeature(HeatingThermostatFeature.class);
        assertThat(heatingThermostatFeature).isNotNull();

        final @Nullable TransientModeFeature transientModeFeature = node.getFeature(TransientModeFeature.class);
        assertThat(transientModeFeature).isNotNull();

        final @Nullable TransientModeHeatingActionsFeature transientModeHeatingActionsFeature = node.getFeature(TransientModeHeatingActionsFeature.class);
        assertThat(transientModeHeatingActionsFeature).isNull();
    }

    @Test
    public void testHeatingWithGoodTransientActions() throws HiveException {
        /* Given */
        final NodeId nodeId = TestUtil.NODE_ID_DEADBEEF;
        final NodeDto nodeDto = TestUtil.createSimpleNodeDto(nodeId);
        assert nodeDto.features != null;

        // Add Heating Thermostat feature
        nodeDto.features.heating_thermostat_v1 = getGoodHeatingThermostatV1FeatureDto();

        // Add Transient Mode with action
        final int boostTargetTemperature = 22;
        final ActionDto actionDto = new ActionDto();
        actionDto.actionType = ActionType.GENERIC;
        actionDto.attribute = AttributeName.HEATING_THERMOSTAT_TARGET_HEAT_TEMPERATURE;
        actionDto.value = Integer.toString(boostTargetTemperature);

        nodeDto.features.transient_mode_v1 = getGoodTransientModeV1FeatureDtoWithoutActions();
        nodeDto.features.transient_mode_v1.actions = TestUtil.createSimpleFeatureAttributeDto(Collections.singletonList(actionDto));

        final NodesDto nodesDto = new NodesDto();
        nodesDto.nodes = Collections.singletonList(nodeDto);

        this.setUpSuccessResponse(nodesDto);

        final DefaultNodeRepository nodeRepository = new DefaultNodeRepository(this.requestFactory);


        /* When */
        final @Nullable Node node = nodeRepository.getNode(nodeId);


        /* Then */
        assertThat(node).isNotNull();

        final @Nullable HeatingThermostatFeature heatingThermostatFeature = node.getFeature(HeatingThermostatFeature.class);
        assertThat(heatingThermostatFeature).isNotNull();

        final @Nullable TransientModeFeature transientModeFeature = node.getFeature(TransientModeFeature.class);
        assertThat(transientModeFeature).isNotNull();

        final @Nullable TransientModeHeatingActionsFeature transientModeHeatingActionsFeature = node.getFeature(TransientModeHeatingActionsFeature.class);
        assertThat(transientModeHeatingActionsFeature).isNotNull();
        assertThat(transientModeHeatingActionsFeature.getBoostTargetTemperature().getDisplayValue()).isEqualTo(Quantities.getQuantity(boostTargetTemperature, Units.CELSIUS));
    }

    @Test
    public void testLinksFeatureNoLinks() throws HiveException {
        /* Given */
        final ReverseLinkDto reverseLinkDto = new ReverseLinkDto();
        reverseLinkDto.boundNode = TestUtil.NODE_ID_CAFEBABE;
        reverseLinkDto.bindingGroupIds = Collections.singleton(GroupId.TRVBM);

        final NodeDto nodeDto = TestUtil.createSimpleNodeDto(TestUtil.NODE_ID_DEADBEEF);
        assert nodeDto.features != null;
        nodeDto.features.links_v1 = new LinksV1FeatureDto();
        nodeDto.features.links_v1.reverseLinks = TestUtil.createSimpleFeatureAttributeDto(Collections.singleton(reverseLinkDto));

        final NodesDto nodesDto = new NodesDto();
        nodesDto.nodes = Collections.singletonList(nodeDto);

        this.setUpSuccessResponse(nodesDto);

        final DefaultNodeRepository nodeRepository = new DefaultNodeRepository(this.requestFactory);


        /* When */
        final @Nullable Node linksNode = nodeRepository.getNode(TestUtil.NODE_ID_DEADBEEF);


        /* Then */
        // No exceptions hopefully!
        assertThat(linksNode).isNotNull();

        final @Nullable LinksFeature linksFeature = linksNode.getFeature(LinksFeature.class);
        assertThat(linksFeature).isNotNull();

        assertThat(linksFeature.getLinks()).isNull();
        assertThat(linksFeature.getReverseLinks()).isNotNull();

        // TODO: verify more
    }

    @Test
    public void testLinksFeatureNoReverseLinks() throws HiveException {
        /* Given */
        final LinkDto linkDto = new LinkDto();
        linkDto.boundNode = TestUtil.NODE_ID_CAFEBABE;
        linkDto.bindingGroupId = GroupId.TRVBM;

        final NodeDto nodeDto = TestUtil.createSimpleNodeDto(TestUtil.NODE_ID_DEADBEEF);
        nodeDto.features.links_v1 = new LinksV1FeatureDto();
        nodeDto.features.links_v1.links = TestUtil.createSimpleFeatureAttributeDto(Collections.singleton(linkDto));

        final NodesDto nodesDto = new NodesDto();
        nodesDto.nodes = Collections.singletonList(nodeDto);

        this.setUpSuccessResponse(nodesDto);

        final DefaultNodeRepository nodeRepository = new DefaultNodeRepository(this.requestFactory);


        /* When */
        final @Nullable Node linksNode = nodeRepository.getNode(TestUtil.NODE_ID_DEADBEEF);


        /* Then */
        // No exceptions hopefully!
        assertThat(linksNode).isNotNull();

        final @Nullable LinksFeature linksFeature = linksNode.getFeature(LinksFeature.class);
        assertThat(linksFeature).isNotNull();

        assertThat(linksFeature.getLinks()).isNotNull();
        assertThat(linksFeature.getReverseLinks()).isNull();

        // TODO: verify more
    }

    @Test
    public void testAutoBoostFeature() throws HiveException {
        /* Given */
        final long autoBoostDurationMins = 30L;
        final BigDecimal autoBoostTargetHeatTemperature = BigDecimal.valueOf(22);

        final AutoBoostV1FeatureDto autoBoostV1FeatureDto = new AutoBoostV1FeatureDto();
        autoBoostV1FeatureDto.autoBoostDuration = TestUtil.createSimpleFeatureAttributeDto(autoBoostDurationMins);
        autoBoostV1FeatureDto.autoBoostTargetHeatTemperature = TestUtil.createSimpleFeatureAttributeDto(autoBoostTargetHeatTemperature);

        final NodeDto nodeDto = TestUtil.createSimpleNodeDto(TestUtil.NODE_ID_DEADBEEF);
        assert nodeDto.features != null;
        nodeDto.features.autoboost_v1 = autoBoostV1FeatureDto;

        final NodesDto nodesDto = new NodesDto();
        nodesDto.nodes = Collections.singletonList(nodeDto);

        this.setUpSuccessResponse(nodesDto);

        final DefaultNodeRepository nodeRepository = new DefaultNodeRepository(this.requestFactory);


        /* When */
        final @Nullable Node node = nodeRepository.getNode(TestUtil.NODE_ID_DEADBEEF);


        /* Then */
        // No exceptions hopefully!
        assertThat(node).isNotNull();

        final @Nullable AutoBoostFeature autoBoostFeature = node.getFeature(AutoBoostFeature.class);
        assertThat(autoBoostFeature).isNotNull();

        assertThat(autoBoostFeature.getAutoBoostDuration().getReportedValue()).isEqualTo(Duration.ofMinutes(autoBoostDurationMins));
        assertThat(autoBoostFeature.getAutoBoostDuration().getDisplayValue()).isEqualTo(Duration.ofMinutes(autoBoostDurationMins));

        assertThat(autoBoostFeature.getAutoBoostTargetHeatTemperature().getReportedValue()).isEqualTo(Quantities.getQuantity(autoBoostTargetHeatTemperature, Units.CELSIUS));
        assertThat(autoBoostFeature.getAutoBoostTargetHeatTemperature().getDisplayValue()).isEqualTo(Quantities.getQuantity(autoBoostTargetHeatTemperature, Units.CELSIUS));
    }
}
