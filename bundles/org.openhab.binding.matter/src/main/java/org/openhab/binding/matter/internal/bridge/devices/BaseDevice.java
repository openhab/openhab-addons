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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.bridge.AttributeState;
import org.openhab.binding.matter.internal.bridge.BridgedEndpoint;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.items.StateChangeListener;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseDevice} is a base class for all devices that are managed by the bridge.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class BaseDevice implements StateChangeListener {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final GenericItem primaryItem;
    protected @Nullable Metadata primaryItemMetadata;
    protected final MatterBridgeClient client;
    protected final MetadataRegistry metadataRegistry;
    protected boolean activated = false;

    public BaseDevice(MetadataRegistry metadataRegistry, MatterBridgeClient client, GenericItem primaryItem) {
        this.metadataRegistry = metadataRegistry;
        this.client = client;
        this.primaryItem = primaryItem;
        this.primaryItemMetadata = metadataRegistry.get(new MetadataKey("matter", primaryItem.getUID()));
    }

    public abstract String deviceType();

    /**
     * Activate the device, this will return the device options for the device, inherited classes should override this
     * method to return the correct device options as well as set the initial state of the device and register listeners
     * 
     * @return the device options
     */
    protected abstract MatterDeviceOptions activate();

    /**
     * Dispose of the device, inherited classes should unregister the device and remove the listeners
     */
    public abstract void dispose();

    /**
     * Handle openHAB item state changes
     * 
     */
    public abstract void updateState(Item item, State state);

    /**
     * Handle matter events
     * 
     * @param clusterName the cluster name
     * @param attributeName the attribute name
     * @param data the raw matter data value
     */
    public abstract void handleMatterEvent(String clusterName, String attributeName, Object data);

    @Override
    public void stateChanged(Item item, State oldState, State newState) {
        logger.debug("{} state changed from {} to {}", item.getName(), oldState, newState);
        updateState(item, newState);
    }

    @Override
    public void stateUpdated(Item item, State state) {
    }

    public BridgedEndpoint activateBridgedEndpoint() {
        if (activated) {
            throw new IllegalStateException("Device already registered");
        }
        MatterDeviceOptions options = activate();
        activated = true;
        return new BridgedEndpoint(deviceType(), primaryItem.getName(), options.label, primaryItem.getName(),
                "Type " + primaryItem.getType(), String.valueOf(primaryItem.getName().hashCode()), options.clusters);
    }

    public String getName() {
        return primaryItem.getName();
    }

    /**
     * Set the state of an attribute of the endpoint.
     * 
     * @param clusterName the cluster name
     * @param attributeName the attribute name
     * @param state the state
     * @return a future that completes when the state is set
     */
    public CompletableFuture<Void> setEndpointState(String clusterName, String attributeName, Object state) {
        return client.setEndpointState(primaryItem.getName(), clusterName, attributeName, state);
    }

    /**
     * Set the states of the endpoint in a single transaction.
     * 
     * @param states
     * @return a future that completes when the states are set
     */
    public CompletableFuture<Void> setEndpointStates(List<AttributeState> states) {
        return client.setEndpointStates(primaryItem.getName(), states);
    }

    protected MetaDataMapping metaDataMapping(GenericItem item) {
        Metadata metadata = metadataRegistry.get(new MetadataKey("matter", item.getUID()));
        String label = item.getLabel();
        List<String> attributeList = List.of();
        Map<String, Object> config = new HashMap<>();
        if (metadata != null) {
            attributeList = Arrays.stream(metadata.getValue().split(",")).map(String::trim)
                    .collect(Collectors.toList());
            metadata.getConfiguration().forEach((key, value) -> {
                config.put(key.replace('-', '.').trim(), value);
            });
            if (config.get("label") instanceof String customLabel) {
                label = customLabel;
            }

            // convert the value of fixed labels into a cluster attribute
            if (config.get("fixedLabels") instanceof String fixedLabels) {
                List<KeyValue> labelList = parseFixedLabels(fixedLabels);
                config.put("fixedLabel.labelList", labelList);
            }
        }

        if (label == null) {
            label = item.getName();
        }

        return new MetaDataMapping(attributeList, config, label);
    }

    /**
     * This class is used to map the metadata to the endpoint options
     */
    class MetaDataMapping {
        public final List<String> attributes;
        /**
         * The config for the item, this will be a mix of custom mapping like "ON=1" and cluster attributes like
         * "clusterName.attributeName=2"
         */
        public final Map<String, Object> config;
        /**
         * The label for the item
         */
        public final String label;

        public MetaDataMapping(List<String> attributes, Map<String, Object> config, String label) {
            this.attributes = attributes;
            this.config = config;
            this.label = label;
        }

        /**
         * Get the attribute options from the config, this filters the entries to just keys like
         * "clusterName.attributeName"
         * 
         * @return
         */
        public Map<String, Object> getAttributeOptions() {
            return config.entrySet().stream().filter(entry -> entry.getKey().contains("."))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    class MatterDeviceOptions {
        public final Map<String, Map<String, Object>> clusters;
        public final String label;

        public MatterDeviceOptions(Map<String, Object> attributes, String label) {
            this.clusters = mapClusterAttributes(attributes);
            this.label = label;
        }
    }

    Map<String, Map<String, Object>> mapClusterAttributes(Map<String, Object> clusterAttributes) {
        Map<String, Map<String, Object>> returnMap = new HashMap<>();
        clusterAttributes.forEach((key, value) -> {
            String[] parts = key.split("\\.");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Key must be in the format 'clusterName.attributeName'");
            }
            String clusterName = parts[0];
            String attributeName = parts[1];

            // Get or create the child map for the clusterName
            Map<String, Object> attributes = returnMap.computeIfAbsent(clusterName, k -> new HashMap<>());

            // Update the attributeName with the value
            if (attributes != null) {
                attributes.put(attributeName, value);
            }
        });
        return returnMap;
    }

    private List<KeyValue> parseFixedLabels(String labels) {
        Map<String, String> keyValueMap = Arrays.stream(labels.split(",")).map(pair -> pair.trim().split("=", 2))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0].trim(), parts -> parts[1].trim()));
        return keyValueMap.entrySet().stream().map(entry -> new KeyValue(entry.getKey(), entry.getValue())).toList();
    }

    class KeyValue {
        public final String label;
        public final String value;

        public KeyValue(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }
}
