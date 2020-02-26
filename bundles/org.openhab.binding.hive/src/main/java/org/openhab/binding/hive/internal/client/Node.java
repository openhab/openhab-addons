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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hive.internal.client.feature.Feature;

/**
 *
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class Node {
    private final NodeId id;
    private final String name;
    private final NodeType nodeType;
    private final ProductType productType;
    private final Protocol protocol;
    private final NodeId parentNodeId;
    private final Map<Class<? extends Feature>, Feature> features;

    private Node(
            final NodeId id,
            final String name,
            final NodeType nodeType,
            final ProductType productType,
            final Protocol protocol,
            final NodeId parentNodeId,
            final Map<Class<? extends Feature>, Feature> features
    ) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(nodeType);
        Objects.requireNonNull(productType);
        Objects.requireNonNull(protocol);
        Objects.requireNonNull(parentNodeId);
        Objects.requireNonNull(features);

        this.id = id;
        this.name = name;
        this.nodeType = nodeType;
        this.productType = productType;
        this.protocol = protocol;
        this.parentNodeId = parentNodeId;
        this.features = features;
    }

    public NodeId getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public NodeType getNodeType() {
        return this.nodeType;
    }

    public ProductType getProductType() {
        return this.productType;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public NodeId getParentNodeId() {
        return this.parentNodeId;
    }

    public Map<Class<? extends Feature>, Feature> getFeatures() {
        return Collections.unmodifiableMap(this.features);
    }

    @SuppressWarnings({"unchecked", "cast.unsafe"})
    public <T extends Feature> @Nullable T getFeature(final Class<T> classOfFeature) {
        // N.B. "Unchecked cast" warning suppressed because we should be
        //      ensuring that the class of the value matches the key
        //      when they are put in the map.
        return (T) this.features.get(classOfFeature);
    }

    public <T extends Feature> Node withFeature(final Class<T> featureClass, final T feature) {
        return Node.builder()
                .from(this)
                .putFeature(featureClass, feature)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private @Nullable NodeId id;
        private @Nullable String name;
        private @Nullable NodeType nodeType;
        private @Nullable ProductType productType;
        private @Nullable Protocol protocol;
        private @Nullable NodeId parentNodeId;
        private Map<Class<? extends Feature>, Feature> features = new HashMap<>();

        public Builder from(final Node node) {
            Objects.requireNonNull(node);

            return this.id(node.getId())
                    .name(node.getName())
                    .nodeType(node.getNodeType())
                    .productType(node.getProductType())
                    .protocol(node.getProtocol())
                    .parentNodeId(node.getParentNodeId())
                    .features(node.getFeatures());
        }

        public Builder id(final NodeId id) {
            this.id = Objects.requireNonNull(id);

            return this;
        }

        public Builder name(final String name) {
            this.name = Objects.requireNonNull(name);

            return this;
        }

        public Builder nodeType(final NodeType nodeType) {
            this.nodeType = Objects.requireNonNull(nodeType);

            return this;
        }

        public Builder productType(final ProductType productType) {
            this.productType = Objects.requireNonNull(productType);

            return this;
        }

        public Builder protocol(final Protocol protocol) {
            this.protocol = Objects.requireNonNull(protocol);

            return this;
        }

        public Builder parentNodeId(final NodeId parentNodeId) {
            this.parentNodeId = Objects.requireNonNull(parentNodeId);

            return this;
        }

        public Builder features(final Map<Class<? extends Feature>, Feature> features) {
            this.features = new HashMap<>(features);

            return this;
        }

        public <F extends Feature> Builder putFeature(
                final Class<F> featureClass,
                final F feature
        ) {
            Objects.requireNonNull(featureClass);
            Objects.requireNonNull(feature);

            this.features.put(featureClass, feature);

            return this;
        }

        public Node build() {
            final @Nullable NodeId id = this.id;
            final @Nullable String name = this.name;
            final @Nullable NodeType nodeType = this.nodeType;
            final @Nullable ProductType productType = this.productType;
            final @Nullable Protocol protocol = this.protocol;
            final @Nullable NodeId parentNodeId = this.parentNodeId;

            if (id == null
                    || name == null
                    || nodeType == null
                    || productType == null
                    || protocol == null
                    || parentNodeId == null
            ) {
                throw new IllegalStateException(BuilderUtil.REQUIRED_ATTRIBUTE_NOT_SET_MESSAGE);
            }

            return new Node(
                    id,
                    name,
                    nodeType,
                    productType,
                    protocol,
                    parentNodeId,
                    Collections.unmodifiableMap(this.features)
            );
        }
    }
}
