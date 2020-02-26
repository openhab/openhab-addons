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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Duration;
import java.util.*;

import javax.measure.Quantity;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.*;
import org.openhab.binding.hive.internal.client.dto.*;
import org.openhab.binding.hive.internal.client.exception.*;
import org.openhab.binding.hive.internal.client.feature.*;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

/**
 * The default implementation of {@link NodeRepository}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class DefaultNodeRepository implements NodeRepository {
    private final HiveApiRequestFactory requestFactory;

    public DefaultNodeRepository(
            final HiveApiRequestFactory requestFactory
    ) {
        Objects.requireNonNull(requestFactory);

        this.requestFactory = requestFactory;
    }

    private static URI getEndpointPathForNode(final NodeId nodeId) {
        return HiveApiConstants.ENDPOINT_NODE.resolve(nodeId.toString());
    }

    private static <T> @Nullable T safeGetTargetValue(final @Nullable SettableFeatureAttribute<T> attribute) {
        if (attribute != null) {
            return attribute.getTargetValue();
        } else {
            return null;
        }
    }

    private static BigDecimal temperatureToDtoValue(final Quantity<Temperature> temperature) {
        // Convert temperature to celsius
        final Quantity<Temperature> celsiusTemperature = temperature.to(Units.CELSIUS);

        // Convert to BigDecimal
        final BigDecimal celsiusTemperatureDecimal = new BigDecimal(celsiusTemperature.getValue().toString());

        // Round temperature to 1 decimal place and return
        return celsiusTemperatureDecimal.setScale(1, RoundingMode.HALF_UP);
    }

    private static AutoBoostFeature getAutoBoostFeatureFromDto(
            final AutoBoostV1FeatureDto autoBoostV1FeatureDto
    ) throws HiveClientResponseException {
        // FIXME: temperature-units: Actually check temperature unit.
        return AutoBoostFeature.builder()
                .autoBoostDuration(FeatureAttributeFactory.getSettableFromDtoWithAdapter(
                        Duration::ofMinutes,
                        autoBoostV1FeatureDto.autoBoostDuration
                ))
                .autoBoostTargetHeatTemperature(FeatureAttributeFactory.getSettableFromDtoWithAdapter(
                        (val) -> Quantities.getQuantity(val, Units.CELSIUS),
                        autoBoostV1FeatureDto.autoBoostTargetHeatTemperature
                ))
                .build();
    }

    private static BatteryDeviceFeature getBatteryDeviceFeatureFromDto(
            final BatteryDeviceV1FeatureDto batteryDeviceV1FeatureDto
    ) throws HiveClientResponseException {
        return BatteryDeviceFeature.builder()
                .batteryLevel(FeatureAttributeFactory.getReadOnlyFromDtoWithAdapter(
                        BatteryLevel::new,
                        batteryDeviceV1FeatureDto.batteryLevel
                ))
                .batteryState(FeatureAttributeFactory.getReadOnlyFromDto(batteryDeviceV1FeatureDto.batteryState))
                .batteryVoltage(FeatureAttributeFactory.getReadOnlyFromDtoWithAdapter(
                        (val) -> Quantities.getQuantity(val, Units.VOLT),
                        batteryDeviceV1FeatureDto.batteryVoltage
                ))
                .batteryNotificationState(FeatureAttributeFactory.getReadOnlyFromDto(batteryDeviceV1FeatureDto.notificationState))
                .build();
    }

    private static LinksFeature getLinksFeatureFromDto(
            final LinksV1FeatureDto linksV1FeatureDto
    ) throws HiveClientResponseException {
        final FeatureAttributeFactory.Adapter<Set<LinkDto>, Set<Link>> linksAdapter = (list) -> {
            final Set<Link> links = new HashSet<>();

            for (final LinkDto linkDto : list) {
                final @Nullable NodeId boundNode = linkDto.boundNode;
                final @Nullable GroupId bindingGroupId = linkDto.bindingGroupId;

                if (boundNode == null) {
                    throw new HiveClientResponseException("Link bound node is unexpectedly null.");
                }

                if (bindingGroupId == null) {
                    throw new HiveClientResponseException("Link binding group id is unexpectedly null.");
                }

                links.add(new Link(boundNode, bindingGroupId));
            }

            return Collections.unmodifiableSet(links);
        };

        final FeatureAttributeFactory.Adapter<Set<ReverseLinkDto>, Set<ReverseLink>> reverseLinksAdapter = (list) -> {
            final Set<ReverseLink> reverseLinks = new HashSet<>();

            for (final ReverseLinkDto reverseLinkDto : list) {
                final @Nullable NodeId boundNode = reverseLinkDto.boundNode;
                final @Nullable Set<GroupId> bindingGroupId = reverseLinkDto.bindingGroupIds;

                if (boundNode == null) {
                    throw new HiveClientResponseException("Reverse link bound node is unexpectedly null.");
                }

                if (bindingGroupId == null) {
                    throw new HiveClientResponseException("Reverse link binding group id is unexpectedly null.");
                }

                reverseLinks.add(new ReverseLink(boundNode, bindingGroupId));
            }

            return Collections.unmodifiableSet(reverseLinks);
        };

        return LinksFeature.builder()
                .links(linksV1FeatureDto.links != null ? FeatureAttributeFactory.getReadOnlyFromDtoWithAdapter(
                        linksAdapter,
                        linksV1FeatureDto.links
                ) : null)
                .reverseLinks(linksV1FeatureDto.reverseLinks != null ? FeatureAttributeFactory.getReadOnlyFromDtoWithAdapter(
                        reverseLinksAdapter,
                        linksV1FeatureDto.reverseLinks
                ) : null)
                .build();
    }

    private static OnOffDeviceFeature getOnOffDeviceFeatureFromDto(
            final OnOffDeviceV1FeatureDto onOffDeviceV1FeatureDto
    ) throws HiveClientResponseException {
        return OnOffDeviceFeature.builder()
                .mode(FeatureAttributeFactory.getSettableFromDto(onOffDeviceV1FeatureDto.mode))
                .build();
    }

    private static PhysicalDeviceFeature getPhysicalDeviceFeatureFromDto(
            final PhysicalDeviceV1FeatureDto physicalDeviceV1FeatureDto
    ) throws HiveClientResponseException {
        final @Nullable FeatureAttribute<String> hardwareIdentifier;

        final @Nullable FeatureAttributeDto<String> nativeIdentifierAttributeDto = physicalDeviceV1FeatureDto.nativeIdentifier;
        final @Nullable FeatureAttributeDto<String> hardwareIdentifierAttributeDto = physicalDeviceV1FeatureDto.hardwareIdentifier;

        if (nativeIdentifierAttributeDto != null) {
            hardwareIdentifier = FeatureAttributeFactory.getReadOnlyFromDto(nativeIdentifierAttributeDto);
        } else if (hardwareIdentifierAttributeDto != null) {
            hardwareIdentifier = FeatureAttributeFactory.getReadOnlyFromDto(hardwareIdentifierAttributeDto);
        } else {
            hardwareIdentifier = null;
        }

        return PhysicalDeviceFeature.builder()
                .hardwareIdentifier(hardwareIdentifier)
                .model(FeatureAttributeFactory.getReadOnlyFromDto(physicalDeviceV1FeatureDto.model))
                .manufacturer(FeatureAttributeFactory.getReadOnlyFromDto(physicalDeviceV1FeatureDto.manufacturer))
                .softwareVersion(FeatureAttributeFactory.getReadOnlyFromDto(physicalDeviceV1FeatureDto.softwareVersion))
                .build();
    }

    private static HeatingThermostatFeature getHeatingThermostatFeatureFromDto(
            final HeatingThermostatV1FeatureDto heatingThermostatV1FeatureDto
    ) throws HiveClientResponseException {
        // FIXME: temperature-units: Actually check temperature unit.
        return HeatingThermostatFeature.builder()
                .operatingMode(FeatureAttributeFactory.getSettableFromDto(heatingThermostatV1FeatureDto.operatingMode))
                .operatingState(FeatureAttributeFactory.getReadOnlyFromDto(heatingThermostatV1FeatureDto.operatingState))
                .targetHeatTemperature(FeatureAttributeFactory.getSettableFromDtoWithAdapter(
                        (val) -> Quantities.getQuantity(val, Units.CELSIUS),
                        heatingThermostatV1FeatureDto.targetHeatTemperature
                ))
                .temporaryOperatingModeOverride(FeatureAttributeFactory.getSettableFromDto(heatingThermostatV1FeatureDto.temporaryOperatingModeOverride))
                .build();
    }

    private static TemperatureSensorFeature getTemperatureSensorFeatureFromDto(
            final TemperatureSensorV1FeatureDto temperatureSensorV1FeatureDto
    ) throws HiveClientResponseException {
        // FIXME: temperature-units: Actually check temperature unit.
        return TemperatureSensorFeature.builder()
                .temperature(FeatureAttributeFactory.getReadOnlyFromDtoWithAdapter(
                        (val) -> Quantities.getQuantity(val, Units.CELSIUS),
                        temperatureSensorV1FeatureDto.temperature
                ))
                .build();
    }

    private static TransientModeFeature getTransientModeFeatureFromDto(
            final TransientModeV1FeatureDto transientModeV1FeatureDto
    ) throws HiveClientResponseException {
        return TransientModeFeature.builder()
                .duration(FeatureAttributeFactory.getSettableFromDtoWithAdapter(
                        Duration::ofSeconds,
                        transientModeV1FeatureDto.duration
                ))
                .isEnabled(FeatureAttributeFactory.getSettableFromDto(transientModeV1FeatureDto.isEnabled))
                .startDatetime(FeatureAttributeFactory.getReadOnlyFromDto(transientModeV1FeatureDto.startDatetime))
                .endDatetime(FeatureAttributeFactory.getReadOnlyFromDto(transientModeV1FeatureDto.endDatetime))
                .build();
    }

    private static TransientModeHeatingActionsFeature getTransientModeHeatingActionsFeatureFromDto(
            final TransientModeV1FeatureDto transientModeV1FeatureDto
    ) throws HiveClientResponseException {
        final @Nullable FeatureAttributeDto<List<ActionDto>> actionsAttribute = transientModeV1FeatureDto.actions;
        if (actionsAttribute == null) {
            throw new HiveClientResponseException("Transient mode actions attribute is unexpectedly null.");
        }

        final @Nullable List<ActionDto> actions = actionsAttribute.reportedValue;
        if (actions == null) {
            throw new HiveClientResponseException("Transient mode action list is unexpectedly null.");
        }

        if (actions.isEmpty()) {
            throw new HiveClientResponseException("Transient mode action list is unexpectedly empty.");
        }
        final @Nullable ActionDto targetTempAction = actions.get(0);
        if (targetTempAction == null) {
            throw new HiveClientResponseException("Transient mode action is unexpectedly null.");
        }

        final @Nullable String targetTempString = targetTempAction.value;
        if (targetTempString == null) {
            throw new HiveClientResponseException("Transient Target Temperature is unexpectedly null.");
        }

        // FIXME: temperature-units: Actually check temperature unit.
        final Quantity<Temperature> targetHeatTemperature = Quantities.getQuantity(new BigDecimal(targetTempString), Units.CELSIUS);

        return TransientModeHeatingActionsFeature.builder()
                .boostTargetTemperature(FeatureAttributeFactory.getSettableFromDtoWithAdapter(
                        (val) -> targetHeatTemperature,
                        transientModeV1FeatureDto.actions
                ))
                .build();
    }

    private static WaterHeaterFeature getWaterHeaterFeatureFromDto(
            final WaterHeaterV1FeatureDto waterHeaterV1FeatureDto
    ) throws HiveClientResponseException {
        return WaterHeaterFeature.builder()
                .operatingMode(FeatureAttributeFactory.getSettableFromDto(waterHeaterV1FeatureDto.operatingMode))
                .isOn(FeatureAttributeFactory.getReadOnlyFromDto(waterHeaterV1FeatureDto.isOn))
                .temporaryOperatingModeOverride(FeatureAttributeFactory.getSettableFromDto(waterHeaterV1FeatureDto.temporaryOperatingModeOverride))
                .build();
    }

    private static ZigbeeDeviceFeature getZigbeeDeviceFeatureFromDto(
            final ZigbeeDeviceV1FeatureDto zigbeeDeviceV1FeatureDto
    ) throws HiveClientResponseException {
        return ZigbeeDeviceFeature.builder()
                .eui64(FeatureAttributeFactory.getReadOnlyFromDto(zigbeeDeviceV1FeatureDto.eui64))
                .averageLQI(FeatureAttributeFactory.getReadOnlyFromDto(zigbeeDeviceV1FeatureDto.averageLQI))
                .lastKnownLQI(FeatureAttributeFactory.getReadOnlyFromDto(zigbeeDeviceV1FeatureDto.lastKnownLQI))
                .averageRSSI(FeatureAttributeFactory.getReadOnlyFromDto(zigbeeDeviceV1FeatureDto.averageRSSI))
                .lastKnownRSSI(FeatureAttributeFactory.getReadOnlyFromDto(zigbeeDeviceV1FeatureDto.lastKnownRSSI))
                .build();
    }

    private static void updateFeaturesDtoWithAutoBoostFeature(
            final FeaturesDto featuresDto,
            final AutoBoostFeature autoBoostFeature
    ) {
        final @Nullable Duration autoBoostDurationTarget = safeGetTargetValue(autoBoostFeature.getAutoBoostDuration());
        final @Nullable Quantity<Temperature> autoBoostTargetHeatTemperatureTarget = safeGetTargetValue(autoBoostFeature.getAutoBoostTargetHeatTemperature());

        if (autoBoostDurationTarget != null
                || autoBoostTargetHeatTemperatureTarget != null
        ) {
            final AutoBoostV1FeatureDto autoBoostV1FeatureDto = new AutoBoostV1FeatureDto();
            featuresDto.autoboost_v1 = autoBoostV1FeatureDto;

            if (autoBoostDurationTarget != null) {
                final FeatureAttributeDto<Long> autoBoostDurationAttribute = new FeatureAttributeDto<>();
                autoBoostV1FeatureDto.autoBoostDuration = autoBoostDurationAttribute;
                // N.B. Unlike transient override we use minutes here.
                autoBoostDurationAttribute.targetValue = Math.max(1, autoBoostDurationTarget.toMinutes());
            }

            if (autoBoostTargetHeatTemperatureTarget != null) {
                final FeatureAttributeDto<BigDecimal> autoBoostTargetHeatTemperatureAttribute = new FeatureAttributeDto<>();
                autoBoostV1FeatureDto.autoBoostTargetHeatTemperature = autoBoostTargetHeatTemperatureAttribute;
                autoBoostTargetHeatTemperatureAttribute.targetValue = temperatureToDtoValue(autoBoostTargetHeatTemperatureTarget);
            }
        }
    }

    private static void updateFeaturesDtoWithOnOffDeviceFeature(
            final FeaturesDto featuresDto,
            final OnOffDeviceFeature onOffDeviceFeature
    ) {
        final @Nullable OnOffMode onOffModeTarget = safeGetTargetValue(onOffDeviceFeature.getMode());
        if (onOffModeTarget != null) {
            final OnOffDeviceV1FeatureDto onOffDeviceV1FeatureDto = new OnOffDeviceV1FeatureDto();
            featuresDto.on_off_device_v1 = onOffDeviceV1FeatureDto;

            final FeatureAttributeDto<OnOffMode> modeAttribute = new FeatureAttributeDto<>();
            onOffDeviceV1FeatureDto.mode = modeAttribute;
            modeAttribute.targetValue = onOffModeTarget;
        }
    }

    private static void updateFeaturesDtoWithHeatingThermostatFeature(
            final FeaturesDto featuresDto,
            final HeatingThermostatFeature heatingThermostatFeature
    ) {
        final @Nullable HeatingThermostatOperatingMode operatingModeTarget = safeGetTargetValue(heatingThermostatFeature.getOperatingMode());
        final @Nullable Quantity<Temperature> targetHeatTemperatureTarget = safeGetTargetValue(heatingThermostatFeature.getTargetHeatTemperature());
        final @Nullable OverrideMode temporaryOperatingModeOverrideTarget = safeGetTargetValue(heatingThermostatFeature.getTemporaryOperatingModeOverride());

        // If one of the HeatingThermostatFeature attributes has been set...
        if (operatingModeTarget != null
                || targetHeatTemperatureTarget != null
                || temporaryOperatingModeOverrideTarget != null
        ) {
            final HeatingThermostatV1FeatureDto heatingThermostatV1FeatureDto = new HeatingThermostatV1FeatureDto();
            featuresDto.heating_thermostat_v1 = heatingThermostatV1FeatureDto;

            if (operatingModeTarget != null) {
                final FeatureAttributeDto<HeatingThermostatOperatingMode> operatingModeFeatureAttributeDto = new FeatureAttributeDto<>();
                heatingThermostatV1FeatureDto.operatingMode = operatingModeFeatureAttributeDto;
                operatingModeFeatureAttributeDto.targetValue = operatingModeTarget;
            }

            if (targetHeatTemperatureTarget != null) {
                final FeatureAttributeDto<BigDecimal> targetHeatTemperatureAttribute = new FeatureAttributeDto<>();
                heatingThermostatV1FeatureDto.targetHeatTemperature = targetHeatTemperatureAttribute;
                targetHeatTemperatureAttribute.targetValue = temperatureToDtoValue(targetHeatTemperatureTarget);
            }

            if (temporaryOperatingModeOverrideTarget != null) {
                final FeatureAttributeDto<OverrideMode> temporaryOperatingModeAttributeDto = new FeatureAttributeDto<>();
                heatingThermostatV1FeatureDto.temporaryOperatingModeOverride = temporaryOperatingModeAttributeDto;
                temporaryOperatingModeAttributeDto.targetValue = temporaryOperatingModeOverrideTarget;
            }
        }
    }

    private static void updateFeaturesDtoWithTransientModeFeature(
            final FeaturesDto featuresDto,
            final TransientModeFeature transientModeFeature
    ) {
        final @Nullable Duration durationTarget = safeGetTargetValue(transientModeFeature.getDuration());
        final @Nullable Boolean isEnabledTarget = safeGetTargetValue(transientModeFeature.getIsEnabled());

        if (durationTarget != null || isEnabledTarget != null) {
            @Nullable TransientModeV1FeatureDto transientModeV1FeatureDto = featuresDto.transient_mode_v1;
            if (transientModeV1FeatureDto == null) {
                transientModeV1FeatureDto = new TransientModeV1FeatureDto();
                featuresDto.transient_mode_v1 = transientModeV1FeatureDto;
            }

            if (durationTarget != null) {
                final FeatureAttributeDto<Long> durationAttribute = new FeatureAttributeDto<>();
                transientModeV1FeatureDto.duration = durationAttribute;
                durationAttribute.targetValue = Math.max(1, durationTarget.getSeconds());
            }

            if (isEnabledTarget != null) {
                final FeatureAttributeDto<Boolean> isEnabledAttribute = new FeatureAttributeDto<>();
                transientModeV1FeatureDto.isEnabled = isEnabledAttribute;
                isEnabledAttribute.targetValue = isEnabledTarget;
            }
        }
    }

    private static void updateFeaturesDtoWithTransientModeHeatingActionsFeature(
            final FeaturesDto featuresDto,
            final TransientModeHeatingActionsFeature transientModeHeatingActionsFeature
    ) {
        final @Nullable Quantity<Temperature> boostTargetTemperatureTarget = safeGetTargetValue(transientModeHeatingActionsFeature.getBoostTargetTemperature());
        if (boostTargetTemperatureTarget != null) {
            @Nullable TransientModeV1FeatureDto transientModeV1FeatureDto = featuresDto.transient_mode_v1;
            if (transientModeV1FeatureDto == null) {
                transientModeV1FeatureDto = new TransientModeV1FeatureDto();
                featuresDto.transient_mode_v1 = transientModeV1FeatureDto;
            }

            final ActionDto actionDto = new ActionDto();
            actionDto.actionType = ActionType.GENERIC;
            actionDto.featureType = FeatureType.HEATING_THERMOSTAT_V1;
            actionDto.attribute = AttributeName.HEATING_THERMOSTAT_TARGET_HEAT_TEMPERATURE;
            actionDto.value = temperatureToDtoValue(boostTargetTemperatureTarget).toString();

            final FeatureAttributeDto<List<ActionDto>> actionsDto = new FeatureAttributeDto<>();
            transientModeV1FeatureDto.actions = actionsDto;
            actionsDto.targetValue = Collections.singletonList(actionDto);
        }
    }

    private static void updateFeaturesDtoWithWaterHeaterFeature(
            final FeaturesDto featuresDto,
            final WaterHeaterFeature waterHeaterFeature
    ) {
        final @Nullable WaterHeaterOperatingMode operatingModeTarget = safeGetTargetValue(waterHeaterFeature.getOperatingMode());
        final @Nullable OverrideMode temporaryOperatingModeOverrideTarget = safeGetTargetValue(waterHeaterFeature.getTemporaryOperatingModeOverride());

        if (operatingModeTarget != null || temporaryOperatingModeOverrideTarget != null) {
            final WaterHeaterV1FeatureDto waterHeaterV1FeatureDto = new WaterHeaterV1FeatureDto();
            featuresDto.water_heater_v1 = waterHeaterV1FeatureDto;

            if (operatingModeTarget != null) {
                final FeatureAttributeDto<WaterHeaterOperatingMode> operatingModeAttributeDto = new FeatureAttributeDto<>();
                waterHeaterV1FeatureDto.operatingMode = operatingModeAttributeDto;
                operatingModeAttributeDto.targetValue = operatingModeTarget;
            }

            if (temporaryOperatingModeOverrideTarget != null) {
                final FeatureAttributeDto<OverrideMode> temporaryOperatingModeOverrideAttribute = new FeatureAttributeDto<>();
                waterHeaterV1FeatureDto.temporaryOperatingModeOverride = temporaryOperatingModeOverrideAttribute;
                temporaryOperatingModeOverrideAttribute.targetValue = temporaryOperatingModeOverrideTarget;
            }
        }
    }

    private static Set<Node> parseNodesDto(
            final NodesDto nodesDto
    ) throws HiveClientResponseException {
        final @Nullable List<@Nullable NodeDto> nodeDtos = nodesDto.nodes;
        if (nodeDtos == null) {
            throw new HiveClientResponseException("Nodes list is unexpectedly null.");
        }

        final Set<Node> nodes = new HashSet<>();
        // For each node
        for (final @Nullable NodeDto nodeDto : nodeDtos) {
            final Node.Builder nodeBuilder = Node.builder();

            if (nodeDto == null) {
                throw new HiveClientResponseException("Node DTO is unexpectedly null.");
            }

            final @Nullable NodeId nodeId = nodeDto.id;
            if (nodeId == null) {
                throw new HiveClientResponseException("NodeId is unexpectedly null.");
            }
            nodeBuilder.id(nodeId);

            final @Nullable NodeId parentNodeId = nodeDto.parentNodeId;
            if (parentNodeId == null) {
                throw new HiveClientResponseException("ParentNodeId is unexpectedly null.");
            }
            nodeBuilder.parentNodeId(parentNodeId);

            final @Nullable String nodeName = nodeDto.name;
            if (nodeName == null) {
                throw new HiveClientResponseException("NodeName is unexpectedly null.");
            }
            nodeBuilder.name(nodeName);

            final @Nullable NodeType nodeType = nodeDto.nodeType;
            if (nodeType == null) {
                throw new HiveClientResponseException("NodeType is unexpectedly null.");
            }
            nodeBuilder.nodeType(nodeType);

            final @Nullable Protocol dtoProtocol = nodeDto.protocol;
            final Protocol protocol;
            if (dtoProtocol != null) {
                protocol = dtoProtocol;
            } else {
                protocol = Protocol.NONE;
            }
            nodeBuilder.protocol(protocol);

            final @Nullable FeaturesDto featuresDto = nodeDto.features;
            if (featuresDto == null) {
                throw new HiveClientResponseException("Node features unexpectedly null.");
            }

            final @Nullable AutoBoostV1FeatureDto autoBoostV1FeatureDto = featuresDto.autoboost_v1;
            final @Nullable BatteryDeviceV1FeatureDto batteryDeviceV1FeatureDto = featuresDto.battery_device_v1;
            final @Nullable DeviceManagementV1FeatureDto deviceManagementV1FeatureDto = featuresDto.device_management_v1;
            final @Nullable HeatingThermostatV1FeatureDto heatingThermostatV1FeatureDto = featuresDto.heating_thermostat_v1;
            final @Nullable LinksV1FeatureDto linksV1FeatureDto = featuresDto.links_v1;
            final @Nullable OnOffDeviceV1FeatureDto onOffDeviceV1FeatureDto = featuresDto.on_off_device_v1;
            final @Nullable PhysicalDeviceV1FeatureDto physicalDeviceV1FeatureDto = featuresDto.physical_device_v1;
            final @Nullable TemperatureSensorV1FeatureDto temperatureSensorV1FeatureDto = featuresDto.temperature_sensor_v1;
            final @Nullable TransientModeV1FeatureDto transientModeV1FeatureDto = featuresDto.transient_mode_v1;
            final @Nullable WaterHeaterV1FeatureDto waterHeaterV1FeatureDto = featuresDto.water_heater_v1;
            final @Nullable ZigbeeDeviceV1FeatureDto zigbeeDeviceV1FeatureDto = featuresDto.zigbee_device_v1;

            if (autoBoostV1FeatureDto != null) {
                nodeBuilder.putFeature(AutoBoostFeature.class, getAutoBoostFeatureFromDto(autoBoostV1FeatureDto));
            }
            if (batteryDeviceV1FeatureDto != null) {
                nodeBuilder.putFeature(BatteryDeviceFeature.class, getBatteryDeviceFeatureFromDto(batteryDeviceV1FeatureDto));
            }
            if (heatingThermostatV1FeatureDto != null) {
                nodeBuilder.putFeature(HeatingThermostatFeature.class, getHeatingThermostatFeatureFromDto(heatingThermostatV1FeatureDto));
            }
            if (linksV1FeatureDto != null) {
                nodeBuilder.putFeature(LinksFeature.class, getLinksFeatureFromDto(linksV1FeatureDto));
            }
            if (onOffDeviceV1FeatureDto != null) {
                nodeBuilder.putFeature(OnOffDeviceFeature.class, getOnOffDeviceFeatureFromDto(onOffDeviceV1FeatureDto));
            }
            if (physicalDeviceV1FeatureDto != null) {
                nodeBuilder.putFeature(PhysicalDeviceFeature.class, getPhysicalDeviceFeatureFromDto(physicalDeviceV1FeatureDto));
            }
            if (temperatureSensorV1FeatureDto != null) {
                nodeBuilder.putFeature(TemperatureSensorFeature.class, getTemperatureSensorFeatureFromDto(temperatureSensorV1FeatureDto));
            }
            if (transientModeV1FeatureDto != null) {
                nodeBuilder.putFeature(TransientModeFeature.class, getTransientModeFeatureFromDto(transientModeV1FeatureDto));

                if (heatingThermostatV1FeatureDto != null && transientModeV1FeatureDto.actions != null) {
                    nodeBuilder.putFeature(TransientModeHeatingActionsFeature.class, getTransientModeHeatingActionsFeatureFromDto(transientModeV1FeatureDto));
                }
            }
            if (waterHeaterV1FeatureDto != null) {
                nodeBuilder.putFeature(WaterHeaterFeature.class, getWaterHeaterFeatureFromDto(waterHeaterV1FeatureDto));
            }
            if (zigbeeDeviceV1FeatureDto != null) {
                nodeBuilder.putFeature(ZigbeeDeviceFeature.class, getZigbeeDeviceFeatureFromDto(zigbeeDeviceV1FeatureDto));
            }

            if (deviceManagementV1FeatureDto == null) {
                throw new HiveClientResponseException("DeviceManagement feature is unexpectedly null.");
            }

            final @Nullable ProductType productType;
            final @Nullable FeatureAttributeDto<ProductType> productTypeFeatureAttributeDto = deviceManagementV1FeatureDto.productType;
            if (productTypeFeatureAttributeDto == null) {
                // Force product type for synthetic nodes created by NANO1 hub.
                productType = ProductType.UNKNOWN;
            } else {
                productType = productTypeFeatureAttributeDto.reportedValue;
            }

            if (productType == null) {
                throw new HiveClientResponseException("ProductType is unexpectedly null.");
            }
            nodeBuilder.productType(productType);

            nodes.add(nodeBuilder.build());
        }

        return Collections.unmodifiableSet(nodes);
    }

    @Override
    public Set<Node> getAllNodes() throws HiveApiNotAuthorisedException, HiveClientResponseException,
            HiveApiUnknownException, HiveClientUnknownException, HiveClientRequestException {
        /* Send our get nodes request to the Hive API. */
        final HiveApiResponse response = this.requestFactory.newRequest(HiveApiConstants.ENDPOINT_NODES)
                .accept(HiveApiConstants.MEDIA_TYPE_API_V6_5_0_JSON)
                .get();

        RepositoryUtil.checkResponse("getAllNodes", response);

        final NodesDto nodesDto = response.getContent(NodesDto.class);

        return parseNodesDto(nodesDto);
    }

    @Override
    public String getAllNodesJson() throws HiveApiNotAuthorisedException, HiveApiUnknownException,
            HiveClientUnknownException, HiveClientRequestException {
        /* Send our get nodes request to the Hive API. */
        final HiveApiResponse response = this.requestFactory.newRequest(HiveApiConstants.ENDPOINT_NODES)
                .accept(HiveApiConstants.MEDIA_TYPE_API_V6_5_0_JSON)
                .get();

        RepositoryUtil.checkResponse("getAllNodesJson", response);

        return response.getRawContent();
    }

    @Override
    public @Nullable Node getNode(final NodeId nodeId)
            throws HiveApiNotAuthorisedException, HiveClientResponseException, HiveApiUnknownException,
            HiveClientUnknownException, HiveClientRequestException {
        /* Send our get node request to the Hive API. */
        final HiveApiResponse response = this.requestFactory.newRequest(getEndpointPathForNode(nodeId))
                .accept(HiveApiConstants.MEDIA_TYPE_API_V6_5_0_JSON)
                .get();

        RepositoryUtil.checkResponse("getNode", response);

        final NodesDto nodesDto = response.getContent(NodesDto.class);
        final Set<Node> nodes = parseNodesDto(nodesDto);

        if (nodes.isEmpty()) {
            // There is no node with that ID
            return null;
        } else if (nodes.size() == 1) {
            return nodes.iterator().next();
        } else {
            // Something has gone wrong.
            throw new HiveClientResponseException("Got multiple nodes when requesting node by ID!");
        }
    }

    @Override
    public @Nullable Node updateNode(final Node node) throws HiveApiNotAuthorisedException, HiveClientResponseException,
            HiveApiUnknownException, HiveClientUnknownException, HiveClientRequestException {
        /* Prepare DTO to send Hive API */
        final NodeDto nodeDto = new NodeDto();

        final FeaturesDto featuresDto = new FeaturesDto();
        nodeDto.features = featuresDto;

        for (final Map.Entry<Class<? extends Feature>, Feature> featureEntry : node.getFeatures().entrySet()) {
            final @Nullable Class<? extends Feature> featureClass = featureEntry.getKey();
            final @Nullable Feature feature = featureEntry.getValue();

            if (featureClass == null || feature == null) {
                throw new IllegalStateException("Something has gone very wrong with the feature map.");
            }

            if (featureClass.equals(AutoBoostFeature.class)) {
                updateFeaturesDtoWithAutoBoostFeature(featuresDto, (AutoBoostFeature) feature);
            } else if (featureClass.equals(OnOffDeviceFeature.class)) {
                updateFeaturesDtoWithOnOffDeviceFeature(featuresDto, (OnOffDeviceFeature) feature);
            } else if (featureClass.equals(HeatingThermostatFeature.class)) {
                updateFeaturesDtoWithHeatingThermostatFeature(featuresDto, (HeatingThermostatFeature) feature);
            } else if (featureClass.equals(TransientModeFeature.class)) {
                updateFeaturesDtoWithTransientModeFeature(featuresDto, (TransientModeFeature) feature);
            } else if (featureClass.equals(TransientModeHeatingActionsFeature.class)) {
                updateFeaturesDtoWithTransientModeHeatingActionsFeature(featuresDto, (TransientModeHeatingActionsFeature) feature);
            } else if (featureClass.equals(WaterHeaterFeature.class)) {
                updateFeaturesDtoWithWaterHeaterFeature(featuresDto, (WaterHeaterFeature) feature);
            }
        }

        final NodesDto nodesDto = new NodesDto();
        nodesDto.nodes = Collections.singletonList(nodeDto);

        final HiveApiResponse response = this.requestFactory.newRequest(getEndpointPathForNode(node.getId()))
                .accept(HiveApiConstants.MEDIA_TYPE_API_V6_5_0_JSON)
                .put(nodesDto);

        RepositoryUtil.checkResponse("updateNode", response);

        final NodesDto responseNodesDto = response.getContent(NodesDto.class);
        final Set<Node> responseNodes = parseNodesDto(responseNodesDto);

        if (responseNodes.size() == 1) {
            return responseNodes.iterator().next();
        } else {
            // Something has gone wrong.
            throw new HiveClientResponseException("The parsed response is malformed.");
        }
    }
}
