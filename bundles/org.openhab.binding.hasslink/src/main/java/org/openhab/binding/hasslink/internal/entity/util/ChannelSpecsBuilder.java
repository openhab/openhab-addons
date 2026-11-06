/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hasslink.internal.entity.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.ChannelSpec;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.util.EntityUtils;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A fluent builder for constructing lists of {@link ChannelSpec} instances based on
 * Home Assistant {@link EntityState} metadata and attributes.
 *
 * <p>
 * This builder reduces boilerplate when defining channels by encapsulating the reference
 * to the entity state and context, avoiding repeated parameter passing in channel registration calls.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ChannelSpecsBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelSpecsBuilder.class);

    private final List<ChannelSpec> specs = new ArrayList<>();
    private final EntityState entityState;
    private final EntityContext context;
    private final long supportedFeatures;

    /**
     * Private constructor initializing the builder with the given entity state context and execution context.
     *
     * @param entityState the entity state context
     * @param context execution context supplying optional bridge handler
     */
    private ChannelSpecsBuilder(EntityState entityState, EntityContext context) {
        this.entityState = entityState;
        this.context = context;
        Long features = entityState.getAttributeAsLong("supported_features");
        this.supportedFeatures = features != null ? features : 0L;
    }

    /**
     * Creates a new instance of {@link ChannelSpecsBuilder} associated with the provided {@link EntityState} and
     * {@link EntityContext}.
     *
     * @param entityState the Home Assistant entity state context
     * @param context execution context supplying optional bridge handler
     * @return a new builder instance
     */
    public static ChannelSpecsBuilder create(EntityState entityState, EntityContext context) {
        return new ChannelSpecsBuilder(entityState, context);
    }

    // ==========================================
    // Central Master Add Method
    // ==========================================

    /**
     * Unconditionally adds a new channel specification with full metadata after validating channel UID rules.
     *
     * @param attribute the entity attribute key or channel ID
     * @param itemType openHAB {@link ItemType} instance
     * @param kind the channel kind (e.g., STATE, TRIGGER)
     * @param label optional custom label override
     * @param description optional custom description
     * @param defaultTags default semantic tags
     * @param autoUpdatePolicy openHAB auto-update policy override
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder add(String attribute, ItemType itemType, ChannelKind kind, @Nullable String label,
            @Nullable String description, Set<String> defaultTags, @Nullable AutoUpdatePolicy autoUpdatePolicy) {

        if (EntityType.isPrimary(attribute)) {
            specs.add(0, new ChannelSpec(attribute, itemType, kind, label, description, defaultTags, autoUpdatePolicy));
        } else if (EntityUtils.isValidUID(attribute)) {
            specs.add(new ChannelSpec(attribute, itemType, kind, label, description, defaultTags, autoUpdatePolicy));
        } else {
            LOGGER.debug("Invalid channel ID derived from attribute '{}'; skipping channel registration", attribute);
        }
        return this;
    }

    /**
     * Master add overload defaulting to ChannelKind.STATE.
     */
    public ChannelSpecsBuilder add(String attribute, ItemType itemType, @Nullable String label,
            @Nullable String description, Set<String> defaultTags, @Nullable AutoUpdatePolicy autoUpdatePolicy) {
        return add(attribute, itemType, ChannelKind.STATE, label, description, defaultTags, autoUpdatePolicy);
    }

    /**
     * Unconditionally adds a new channel specification with default metadata.
     *
     * @param attribute the entity attribute key or channel ID
     * @param itemType openHAB {@link ItemType} instance
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder add(String attribute, ItemType itemType) {
        return add(attribute, itemType, null, null, Set.of(), null);
    }

    // ==========================================
    // Primary Channel Overloads
    // ==========================================

    /**
     * Adds the primary state channel with a custom item type.
     *
     * @param itemType the openHAB {@link ItemType} instance
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addPrimaryChannel(ItemType itemType) {
        return add(EntityType.PRIMARY_ATTR, itemType, null, null, Set.of(), null);
    }

    /**
     * Adds the primary state channel with a custom item type and auto-update policy override.
     *
     * @param itemType the openHAB {@link ItemType} instance
     * @param autoUpdatePolicy openHAB auto-update policy override
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addPrimaryChannel(ItemType itemType, AutoUpdatePolicy autoUpdatePolicy) {
        return add(EntityType.PRIMARY_ATTR, itemType, null, null, Set.of(), autoUpdatePolicy);
    }

    /**
     * Adds the primary state channel with default semantic tags.
     *
     * @param itemType the openHAB {@link ItemType} instance
     * @param defaultTags default semantic tags
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addPrimaryChannel(ItemType itemType, Set<String> defaultTags) {
        return add(EntityType.PRIMARY_ATTR, itemType, null, null, defaultTags, null);
    }

    /**
     * Adds the primary state channel with default semantic tags and auto-update policy override.
     *
     * @param itemType the openHAB {@link ItemType} instance
     * @param defaultTags default semantic tags
     * @param autoUpdatePolicy openHAB auto-update policy override
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addPrimaryChannel( //
            ItemType itemType, //
            Set<String> defaultTags, //
            AutoUpdatePolicy autoUpdatePolicy) {
        return add(EntityType.PRIMARY_ATTR, itemType, null, null, defaultTags, autoUpdatePolicy);
    }

    /**
     * Adds the primary state channel with full metadata options.
     *
     * @param itemType openHAB {@link ItemType} instance
     * @param label optional custom label override
     * @param description optional custom description
     * @param defaultTags default semantic tags
     * @param autoUpdatePolicy openHAB auto-update policy override
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addPrimaryChannel( //
            ItemType itemType, //
            @Nullable String label, //
            @Nullable String description, //
            Set<String> defaultTags, //
            @Nullable AutoUpdatePolicy autoUpdatePolicy) {
        return add(EntityType.PRIMARY_ATTR, itemType, label, description, defaultTags, autoUpdatePolicy);
    }

    /**
     * Adds the primary state channel pre-configured for stateless action triggers (e.g., buttons or trigger actions).
     * Sets the item type to {@link ItemType#STRING} and auto-update policy to {@link AutoUpdatePolicy#VETO}.
     *
     * @param description optional description explaining the trigger action
     * @param defaultTags semantic tags to assign to the channel (e.g. Set.of("Control"))
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addPrimaryActionChannel(@Nullable String description, Set<String> defaultTags) {
        return add(EntityType.PRIMARY_ATTR, ItemType.STRING, null, description, defaultTags, AutoUpdatePolicy.VETO);
    }

    /**
     * Adds the primary state channel pre-configured for stateless action triggers without default tags.
     *
     * @param description optional description explaining the trigger action
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addPrimaryActionChannel(@Nullable String description) {
        return addPrimaryActionChannel(description, Set.of());
    }

    /**
     * Adds the primary state channel, automatically inferring its openHAB {@link ItemType}
     * from the entity's metadata and resolved units.
     *
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addPrimaryInferredChannel() {
        return addPrimaryChannel(inferPrimaryItemType());
    }

    /**
     * Adds the primary state channel with an auto-inferred item type and custom auto-update policy.
     *
     * @param autoUpdatePolicy openHAB auto-update policy override
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addPrimaryInferredChannel(AutoUpdatePolicy autoUpdatePolicy) {
        return addPrimaryChannel(inferPrimaryItemType(), autoUpdatePolicy);
    }

    /**
     * Adds the primary entity channel pre-configured as a stateless event trigger channel.
     *
     * @param description optional description explaining the event
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addPrimaryEventChannel(@Nullable String description) {
        return addEventAttr(EntityType.PRIMARY_ATTR, description);
    }

    /**
     * Adds the primary entity channel pre-configured as a stateless event trigger channel using default tags.
     *
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addPrimaryEventChannel() {
        return addPrimaryEventChannel(null);
    }

    // ==========================================
    // Attribute Channel Overloads
    // ==========================================

    /**
     * Adds an attribute channel specification if the attribute exists on the entity state.
     *
     * @param attribute attribute key to check on the entity state
     * @param itemType openHAB {@link ItemType} instance
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addAttr(String attribute, ItemType itemType) {
        return addAttr(attribute, itemType, null, null, Set.of(), null);
    }

    /**
     * Adds an attribute channel specification with default semantic tags if the attribute exists.
     *
     * @param attribute attribute key to check on the entity state
     * @param itemType openHAB {@link ItemType} instance
     * @param defaultTags default semantic tags
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addAttr(String attribute, ItemType itemType, Set<String> defaultTags) {
        return addAttr(attribute, itemType, null, null, defaultTags, null);
    }

    /**
     * Adds an attribute channel specification with an auto-update policy override if the attribute exists.
     *
     * @param attribute attribute key to check on the entity state
     * @param itemType openHAB {@link ItemType} instance
     * @param autoUpdatePolicy openHAB auto-update policy override
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addAttr(String attribute, ItemType itemType,
            @Nullable AutoUpdatePolicy autoUpdatePolicy) {
        return addAttr(attribute, itemType, null, null, Set.of(), autoUpdatePolicy);
    }

    /**
     * Adds an attribute channel specification with default semantic tags and auto-update policy override.
     *
     * @param attribute attribute key to check on the entity state
     * @param itemType openHAB {@link ItemType} instance
     * @param defaultTags default semantic tags
     * @param autoUpdatePolicy openHAB auto-update policy override
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addAttr( //
            String attribute, //
            ItemType itemType, //
            Set<String> defaultTags, //
            AutoUpdatePolicy autoUpdatePolicy) {
        return addAttr(attribute, itemType, null, null, defaultTags, autoUpdatePolicy);
    }

    /**
     * Adds an attribute channel specification with full metadata options if the attribute exists.
     *
     * @param attribute attribute key to check on the entity state
     * @param itemType openHAB {@link ItemType} instance
     * @param label optional custom label override
     * @param description optional custom description
     * @param defaultTags default semantic tags
     * @param autoUpdatePolicy openHAB auto-update policy override
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addAttr( //
            String attribute, //
            ItemType itemType, //
            @Nullable String label, //
            @Nullable String description, //
            Set<String> defaultTags, //
            @Nullable AutoUpdatePolicy autoUpdatePolicy) {
        if (entityState.hasAttribute(attribute)) {
            add(attribute, itemType, label, description, defaultTags, autoUpdatePolicy);
        }
        return this;
    }

    // =========================================================================
    // Attribute Event Channel Overloads
    // =========================================================================

    /**
     * Adds a specific entity attribute pre-configured as a stateless trigger channel.
     *
     * Note the Item type is ignored.
     *
     * @param attribute HA entity attribute key (e.g. "last_clicked", "action")
     * @param description optional description explaining the event
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addEventAttr(String attribute, @Nullable String description) {
        return add(attribute, ItemType.STRING, ChannelKind.TRIGGER, null, description, Set.of(), null);
    }

    /**
     * Adds a specific entity attribute pre-configured as a stateless event trigger channel using default tags.
     *
     * @param attribute HA entity attribute key (e.g. "last_clicked", "action")
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addEventAttr(String attribute) {
        return addEventAttr(attribute, null);
    }

    // ==========================================
    // Feature & Mode Conditional Overloads
    // ==========================================

    /**
     * Adds a channel specification if the entity supports the specified feature flag.
     *
     * @param featureFlag bitmask flag value to test against {@code supported_features}
     * @param channelId the channel ID
     * @param itemType the openHAB {@link ItemType} instance
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addIfSupported(long featureFlag, String channelId, ItemType itemType) {
        if (isSupportedFeature(featureFlag)) {
            add(channelId, itemType);
        }
        return this;
    }

    /**
     * Adds a channel specification with an auto-update policy override if the entity supports the specified feature.
     *
     * @param featureFlag bitmask flag value to test against {@code supported_features}
     * @param channelId the channel ID
     * @param itemType the openHAB {@link ItemType} instance
     * @param kind the openHAB {@link ChannelKind} (STATE or TRIGGER)
     * @param label optional custom label override
     * @param description optional custom description
     * @param defaultTags default semantic tags (e.g., "Status", "Control")
     * @param autoUpdatePolicy openHAB auto-update policy override
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addIfSupported( //
            long featureFlag, //
            String channelId, //
            ItemType itemType, //
            ChannelKind kind, //
            @Nullable String label, //
            @Nullable String description, //
            Set<String> defaultTags, //
            @Nullable AutoUpdatePolicy autoUpdatePolicy) {
        if (isSupportedFeature(featureFlag)) {
            add(channelId, itemType, kind, label, description, defaultTags, autoUpdatePolicy);
        }
        return this;
    }

    /**
     * Adds an attribute channel specification if the given feature flag bit is set and the attribute exists.
     *
     * @param featureFlag bitmask flag value to test against {@code supported_features}
     * @param attribute attribute key to check on the entity state
     * @param itemType openHAB {@link ItemType} instance
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addAttrIfSupported(long featureFlag, String attribute, ItemType itemType) {
        if (isSupportedFeature(featureFlag)) {
            addAttr(attribute, itemType);
        }
        return this;
    }

    /**
     * Adds an attribute channel specification with an auto-update policy if feature flag and attribute exist.
     *
     * @param featureFlag bitmask flag value to test against {@code supported_features}
     * @param attribute attribute key to check on the entity state
     * @param itemType openHAB {@link ItemType} instance
     * @param autoUpdatePolicy openHAB auto-update policy override
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addAttrIfSupported( //
            long featureFlag, //
            String attribute, //
            ItemType itemType, //
            @Nullable AutoUpdatePolicy autoUpdatePolicy) {
        if (isSupportedFeature(featureFlag)) {
            addAttr(attribute, itemType, autoUpdatePolicy);
        }
        return this;
    }

    /**
     * Adds an attribute channel if a supported modes attribute contains the target mode string.
     *
     * @param supportedModesAttr attribute holding supported modes (e.g. {@code "supported_color_modes"})
     * @param targetMode mode string to check for
     * @param attribute attribute key for the channel
     * @param itemType openHAB {@link ItemType} instance
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addAttrIfSupportedMode( //
            String supportedModesAttr, //
            String targetMode, //
            String attribute, //
            ItemType itemType) {
        if (entityState.hasAttribute(attribute)
                || EntityUtils.isModeInList(entityState, supportedModesAttr, targetMode)) {
            addAttr(attribute, itemType);
        }
        return this;
    }

    /**
     * Adds an attribute channel if ANY target mode exists within a supported modes attribute.
     *
     * @param supportedModesAttr attribute holding supported modes (e.g. {@code "supported_color_modes"})
     * @param targetModes set of target mode strings to match against
     * @param attribute attribute key for the channel
     * @param itemType openHAB {@link ItemType} instance
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addAttrIfAnySupportedMode( //
            String supportedModesAttr, //
            Set<String> targetModes, //
            String attribute, //
            ItemType itemType) {
        if (entityState.hasAttribute(attribute) || targetModes.stream()
                .anyMatch(mode -> EntityUtils.isModeInList(entityState, supportedModesAttr, mode))) {
            addAttr(attribute, itemType);
        }
        return this;
    }

    // ==========================================
    // Generic Attributes
    // ==========================================

    /**
     * Inspects the {@link EntityState} payload and automatically adds a generic channel for any unmapped
     * attribute not explicitly declared or part of standard internal metadata.
     *
     * @return this builder instance for fluent chaining
     */
    public ChannelSpecsBuilder addGenericAttributes() {
        for (String attribute : entityState.attributes().keySet()) {
            if (!EntityUtils.isValidUID(attribute) //
                    || alreadyAdded(attribute) //
                    || EntityState.isMetadataAttribute(attribute)) {
                continue;
            }
            ItemType inferredType = TypeInferrer.inferItemType(entityState, attribute);
            add(attribute, inferredType, null, null, Set.of(), AutoUpdatePolicy.VETO);
        }

        return this;
    }

    private boolean alreadyAdded(String attribute) {
        return specs.stream().anyMatch(spec -> spec.attribute().equals(attribute));
    }

    /**
     * Infers the appropriate openHAB {@link ItemType} for a primary state by resolving unit preferences
     * from the bridge context and delegating to {@link TypeInferrer}.
     *
     * @return the resolved {@link ItemType}
     */
    public ItemType inferPrimaryItemType() {
        String deviceClass = entityState.getAttributeAsString("device_class");
        String resolvedUnit = EntityUnitResolver.resolveDeviceClassUnit(entityState, context.getHaConfig(),
                deviceClass);
        return TypeInferrer.inferPrimaryItemType(entityState, resolvedUnit);
    }

    /**
     * Checks whether the entity supports a specific feature flag.
     *
     * @param featureFlag the bitmask flag to check
     * @return true if the feature is supported, false otherwise
     */
    public boolean isSupportedFeature(long featureFlag) {
        return (supportedFeatures & featureFlag) != 0;
    }

    /**
     * Builds and returns the unmodifiable list of configured channel specifications.
     *
     * @return unmodifiable list of {@link ChannelSpec} instances
     */
    public List<ChannelSpec> build() {
        return List.copyOf(specs);
    }
}
