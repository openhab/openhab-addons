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
package org.openhab.binding.matter.internal.bridge.devices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ModeSelectCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ModeSelectCluster.ModeOptionStruct;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.SemanticTags;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.State;

/**
 * The {@link ModeSelectDevice} is a device that represents a Mode Select device.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ModeSelectDevice extends BaseDevice {

    // Matter mode -> openHAB value
    private Map<Integer, String> modeMappings = new HashMap<>();
    // openHAB value -> Matter mode
    private Map<String, Integer> valueMappings = new HashMap<>();

    public ModeSelectDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client, GenericItem item) {
        super(metadataRegistry, client, item);
    }

    @Override
    public String deviceType() {
        return "ModeSelect";
    }

    @Override
    protected MatterDeviceOptions activate() {
        primaryItem.addStateChangeListener(this);
        MetaDataMapping primaryMetadata = metaDataMapping(primaryItem);
        Map<String, Object> attributeMap = primaryMetadata.getAttributeOptions();
        String description = Objects.requireNonNullElse(primaryMetadata.config.get("description"),
                Objects.requireNonNullElse(primaryItem.getLabel(), primaryItem.getName())).toString();
        String modes = Objects.requireNonNullElse(primaryMetadata.config.get("modes"), "").toString();
        AtomicInteger indexCounter = new AtomicInteger(0);
        AtomicReference<SemanticTags.Namespace> standardNamespace = new AtomicReference<>();
        List<ModeOptionStruct> supportedModes = Arrays.stream(modes.split(",")).map(supportedMode -> {
            String[] parts = supportedMode.split(":");
            String mode = parts[0];
            String label = parts.length > 1 ? parts[1] : mode;
            int key = indexCounter.getAndIncrement();
            List<ModeSelectCluster.SemanticTagStruct> semanticTags = new ArrayList<>();
            if (parts.length > 2) {
                for (int i = 2; i < parts.length; i++) {
                    String tagString = parts[i].trim();
                    if (tagString.isEmpty()) {
                        continue;
                    }
                    String[] tagParts = tagString.split("\\.");
                    if (tagParts.length != 2) {
                        logger.debug("Invalid semantic tag format (expected namespace.tag): {}", tagString);
                        continue;
                    }
                    String namespaceName = tagParts[0].toUpperCase();
                    String tagName = tagParts[1].toUpperCase().replace('-', '_').replace(' ', '_');
                    try {
                        SemanticTags.Namespace namespaceEnum = SemanticTags.Namespace.valueOf(namespaceName);
                        SemanticTags.Namespace existingNamespace = standardNamespace.get();
                        if (existingNamespace == null) {
                            standardNamespace.set(namespaceEnum);
                        } else if (existingNamespace != namespaceEnum) {
                            logger.debug("Multiple namespaces in semantic tags: {}", namespaceName);
                            continue;
                        }
                        BaseCluster.MatterEnum matchingTag = Arrays.stream(namespaceEnum.getTags())
                                .filter(t -> ((Enum<?>) t).name().equals(tagName)).findFirst().orElse(null);
                        if (matchingTag == null) {
                            logger.debug("Unknown semantic tag '{}' for namespace '{}'", tagName, namespaceName);
                            continue;
                        }
                        int tagId = matchingTag.getValue();
                        semanticTags.add(new ModeSelectCluster.SemanticTagStruct(null, tagId));
                    } catch (IllegalArgumentException e) {
                        logger.debug("Unknown semantic tag '{}' for namespace '{}'", tagName, namespaceName);
                        continue;
                    }
                }
            }
            if (semanticTags.isEmpty()) {
                semanticTags = defaultSemanticTags();
            }
            modeMappings.put(key, mode);
            valueMappings.put(mode, key);
            return new ModeSelectCluster.ModeOptionStruct(label, key, semanticTags);
        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        attributeMap.put(ModeSelectCluster.CLUSTER_PREFIX + "." + ModeSelectCluster.ATTRIBUTE_DESCRIPTION, description);
        attributeMap.put(ModeSelectCluster.CLUSTER_PREFIX + "." + ModeSelectCluster.ATTRIBUTE_SUPPORTED_MODES,
                supportedModes);
        attributeMap.put(ModeSelectCluster.CLUSTER_PREFIX + "." + ModeSelectCluster.ATTRIBUTE_CURRENT_MODE,
                valueMappings.getOrDefault(primaryItem.getState().toString(), 0));
        SemanticTags.Namespace ns = standardNamespace.get();
        if (ns != null) {
            attributeMap.put(ModeSelectCluster.CLUSTER_PREFIX + "." + ModeSelectCluster.ATTRIBUTE_STANDARD_NAMESPACE,
                    ns.getId());
        }
        return new MatterDeviceOptions(attributeMap, primaryMetadata.label);
    }

    private List<ModeSelectCluster.SemanticTagStruct> defaultSemanticTags() {
        return Arrays.asList(new ModeSelectCluster.SemanticTagStruct(0, 0));
    }

    @Override
    public void dispose() {
        primaryItem.removeStateChangeListener(this);
    }

    @Override
    public void handleMatterEvent(String clusterName, String attributeName, Object data) {
        switch (attributeName) {
            case ModeSelectCluster.ATTRIBUTE_CURRENT_MODE:
                if (data instanceof Number number) {
                    updateMode(number.intValue());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void updateState(Item item, State state) {
        String stateString = state.toString();
        if (valueMappings.get(stateString) instanceof Integer mode) {
            setEndpointState(ModeSelectCluster.CLUSTER_PREFIX, ModeSelectCluster.ATTRIBUTE_CURRENT_MODE, mode);
        } else {
            logger.debug("Unknown mode: {}", stateString);
        }
    }

    private void updateMode(Integer mode) {
        String value = modeMappings.get(mode);
        if (value != null) {
            if (primaryItem instanceof GroupItem groupItem) {
                groupItem.send(new StringType(value));
            } else if (primaryItem instanceof StringItem stringItem) {
                stringItem.send(new StringType(value));
            } else if (primaryItem instanceof NumberItem numberItem) {
                numberItem.send(new DecimalType(value));
            } else if (primaryItem instanceof SwitchItem switchItem) {
                switchItem.send(OnOffType.from(value));
            } else if (primaryItem instanceof RollershutterItem rollershutterItem) {
                switch (value.toUpperCase()) {
                    case "UP":
                    case "DOWN":
                        rollershutterItem.send(UpDownType.valueOf(value.toUpperCase()));
                        break;
                    case "STOP":
                    case "MOVE":
                        rollershutterItem.send(StopMoveType.valueOf(value.toUpperCase()));
                        break;
                    default:
                        try {
                            rollershutterItem.send(new PercentType(Integer.parseInt(value)));
                        } catch (NumberFormatException ignored) {
                        }
                        break;
                }
            } else {
                logger.debug("Unknown item type: {}", primaryItem.getClass().getSimpleName());
            }
        } else {
            logger.debug("Unknown mode: {}", mode);
        }
    }
}
