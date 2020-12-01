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
package org.openhab.binding.sony.internal.providers.models;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

/**
 * This class represents the thing definition that will be used to serialize/deserialize. A thing definition is really a
 * cross between a thing and a thing type. The thing definition will be used to generate thing types specific to sony
 * models and whose channels will use well-known (already defined) channel types. However, it will also be used in the
 * thing initialization to customize the channels the model has and the options (via a dynamic state provider) those
 * channels will have.
 *
 * In other words, a thing definition will create a new thing type specific to a model and will dynamically create
 * channels for that model and use state options specific to that sony model. Confusing eh?
 * 
 * Note that this can be either created via the constructor (which will enforce null/empty checks) or via
 * deserialization (which can provide null/empty variables)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyThingDefinition {
    /** List<SonyThingDefinition> type token for gson deserialization */
    @Expose(serialize = false)
    public static final Type LISTTYPETOKEN = new TypeToken<ArrayList<SonyThingDefinition>>() {
    }.getType();

    /** The associated service (scalar, etc) */
    private @Nullable String service;

    /** The associated configuration uri */
    private @Nullable String configUri;

    /** The associated model name */
    private @Nullable String modelName;

    /** The label for the thing type */
    private @Nullable String label;

    /** The description for the thing type */
    private @Nullable String description;

    /** The channel group id to label for any channel groups */
    private @Nullable Map<@Nullable String, @Nullable String> channelGroups;

    /** The channel definitions for the thing type */
    private @Nullable List<@Nullable SonyThingChannelDefinition> channels;

    /**
     * Empty constructor used for deserialization
     */
    public SonyThingDefinition() {
    }

    /**
     * Constructs the definition from the passed arguments
     *
     * @param service a non-null, non-empty service
     * @param configUri a non-null, non-empty config uri
     * @param modelName a non-null, non-empty model name
     * @param label a non-null, non-empty label
     * @param description a non-null, non-empty description
     * @param channelGroups a non-null, possibly empty map of channel groups
     * @param channels a non-null, possibly empty list of channels
     */
    public SonyThingDefinition(final String service, final String configUri, final String modelName, final String label,
            final String description, final Map<String, String> channelGroups,
            final List<SonyThingChannelDefinition> channels) {
        Validate.notEmpty(service, "service cannot be empty");
        Validate.notEmpty(configUri, "configUri cannot be empty");
        Validate.notEmpty(modelName, "modelName cannot be empty");
        Validate.notEmpty(label, "label cannot be empty");
        Validate.notEmpty(description, "description cannot be empty");
        Objects.requireNonNull(channelGroups, "channelGroups cannot be null");
        Objects.requireNonNull(channels, "channels cannot be null");

        this.service = service;
        this.configUri = configUri;
        this.modelName = modelName;
        this.label = label;
        this.description = description;
        this.channelGroups = Collections.unmodifiableMap(new HashMap<>(channelGroups));
        this.channels = Collections.unmodifiableList(new ArrayList<>(channels));
    }

    /**
     * Returns the service associated with this thing type
     *
     * @return a possibly null, possibly empty service
     */
    public @Nullable String getService() {
        return service;
    }

    /**
     * Returns the config URI associated with this thing type
     *
     * @return a possibly null, possibly empty config URI
     */
    public @Nullable String getConfigUri() {
        return configUri;
    }

    /**
     * Returns the model name associated with this type
     *
     * @return a possibly null, possibly empty model name
     */
    public @Nullable String getModelName() {
        return modelName;
    }

    /**
     * Returns a label associated with this type
     *
     * @return a non-null, non-empty label
     */
    public String getLabel() {
        return StringUtils.defaultIfEmpty(label, "Sony " + modelName);
    }

    /**
     * Returns a description associated with this type
     *
     * @return a non-null, non-empty description
     */
    public String getDescription() {
        return StringUtils.defaultIfEmpty(description, "Sony " + modelName);
    }

    /**
     * Returns the channel groups for this type
     *
     * @return a non-null, possibly empty map of channel groups
     */
    public Map<String, String> getChannelGroups() {
        final @Nullable Map<@Nullable String, @Nullable String> localChannelGroups = channelGroups;
        if (localChannelGroups == null) {
            return new HashMap<>();
        }

        // ugly nullable work around
        final Map<String, String> rc = new HashMap<>();
        localChannelGroups.entrySet().stream().forEach(e -> {
            final String localKey = e.getKey();
            final String localValue = e.getValue();
            if (localKey != null && StringUtils.isNotEmpty(localKey) && localValue != null
                    && StringUtils.isNotEmpty(localValue)) {
                rc.put(localKey, localValue);
            }
        });

        return rc;
    }

    /**
     * Returns the channel definitions for this type
     *
     * @return a non-null, possibly empty list of {@link SonyThingChannelDefinition}
     */
    public List<SonyThingChannelDefinition> getChannels() {
        final List<@Nullable SonyThingChannelDefinition> localChannels = channels;
        return localChannels == null ? new ArrayList<>()
                : (List<SonyThingChannelDefinition>) localChannels.stream().filter(chl -> chl != null)
                        .collect(Collectors.toList());
    }

    /**
     * Returns the channel definition for a given channel id
     *
     * @param channelId a non-null, non-empty channel id
     * @return a channel definition or null if not found
     */
    public @Nullable SonyThingChannelDefinition getChannel(final String channelId) {
        Validate.notEmpty(channelId, "channelId cannot be empty");
        final List<@Nullable SonyThingChannelDefinition> localChannels = channels;
        if (localChannels != null) {
            return localChannels.stream()
                    .filter(chl -> chl != null && StringUtils.equalsIgnoreCase(channelId, chl.getChannelId()))
                    .findFirst().orElse(null);
        }
        return null;
    }

    @Override
    public String toString() {
        return "SonyThingDefinition [service=" + service + ", configUri=" + configUri + ", modelName=" + modelName
                + ", label=" + label + ", description=" + description + ", channelGroups=" + channelGroups
                + ", channels=" + channels + "]";
    }
}
