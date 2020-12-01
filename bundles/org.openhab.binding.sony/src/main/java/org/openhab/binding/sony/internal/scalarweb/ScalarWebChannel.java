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
package org.openhab.binding.sony.internal.scalarweb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.openhab.binding.sony.internal.SonyUtil;

/**
 * This class represents a channel for the scalar web component. The channel
 * will provide management of the various data items required by the scalar web
 * service and provide a mechanism to convert those properties to/from a
 * channel.
 *
 * The service, category, id makes the channel unique and will create a channel
 * id like: {service}#{category}-{id} or {service}#{id} (if id=category for
 * simple services)
 *
 * Where the service is the group and the id/category will be the channel ID.
 *
 * The id, category, base channel id and paths are then stored/retrieved in/from
 * the channel's properties.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ScalarWebChannel {
    /** The separator for a group */
    public static final char CATEGORYSEPARATOR = '-';

    /**
     * Channel ID identifier of the dummy dynamic channel (MUST MATCH scalar.thing)
     */
    private static final String DYNAMIC = "dynamic";

    /** The service identifier for the channel (group id) */
    private final String service;

    /** The category of the channel */
    private final String category;

    /** The identifier for the path (channel id) */
    private final String id;

    /** Non-null, never empty path to the sony item (channel properties) */
    private final String[] paths;

    /** Hash map of custom properties */
    private final Map<String, String> properties = new HashMap<>();

    // Channel property keys
    private static final String CNL_PROPPREFIX = "prop-";
    private static final String CNL_PROPLEN = "propLen";
    private static final String CNL_CHANNELID = "channelId";
    private static final String CNL_CHANNELCATEGORY = "channelCategory";
    public static final String CNL_BASECHANNELID = "baseChannelId";

    /**
     * Creates the web channel from the service, channel ID and possibly some
     * additional paths
     *
     * @param service the non-null, non-empty service
     * @param category the non-null, non-empty channel category
     * @param id the non-null, non-empty channel id
     * @param paths the possibly null, possibly empty list of paths
     */
    public ScalarWebChannel(final String service, final String category, final String id,
            final @Nullable String @Nullable [] paths) {
        Validate.notEmpty(service, "service cannot be empty");
        Validate.notEmpty(category, "category cannot be empty");
        Validate.notEmpty(id, "id cannot be empty");

        this.service = service;
        this.category = category;
        this.id = id;
        this.paths = paths == null ? new String[0] : SonyUtil.convertNull(paths).toArray(new String[0]);
    }

    /**
     * Creates the web channel from a thing channel
     *
     * @param channel a non-null thing channel
     */
    public ScalarWebChannel(final Channel channel) {
        // no (easy) way to prevent an NPE if we get a null channel
        this(channel.getUID(), channel);
    }

    /**
     * Instantiates a new scalar web channel based on the channel. Note that the
     * channelUID may disagree with the channel.getUID in the case of mapping
     * channels.
     *
     * @param channelUID the non-null channel UID
     * @param channel the non-null channel
     */
    public ScalarWebChannel(final ChannelUID channelUID, final Channel channel) {
        Objects.requireNonNull(channelUID, "channelUID cannot be null");
        Objects.requireNonNull(channel, "channel cannot be null");

        if (StringUtils.equals(channelUID.getIdWithoutGroup(), DYNAMIC)) {
            category = DYNAMIC;
            id = DYNAMIC;
            paths = new String[0];
            service = DYNAMIC;
        } else {
            final String groupId = channelUID.getGroupId();
            if (groupId == null) {
                throw new IllegalArgumentException("ChannelUID must have a group: " + channel);
            }
            service = groupId;

            this.properties.putAll(channel.getProperties());

            // Remove our internal properties and use them
            id = this.properties.remove(ScalarWebChannel.CNL_CHANNELID);
            if (id == null || StringUtils.isEmpty(id)) {
                throw new IllegalArgumentException("Channel must contain a ID: " + channel);
            }

            category = this.properties.remove(ScalarWebChannel.CNL_CHANNELCATEGORY);
            if (category == null || StringUtils.isEmpty(category)) {
                throw new IllegalArgumentException("Channel must contain a category: " + channel);
            }

            final String pathLenStr = this.properties.remove(ScalarWebChannel.CNL_PROPLEN);
            if (pathLenStr != null) {
                try {
                    final Integer propLen = Integer.parseInt(pathLenStr);
                    final List<String> tempPath = new ArrayList<>();
                    for (int x = 0; x < propLen; x++) {
                        tempPath.add(this.properties.remove(ScalarWebChannel.CNL_PROPPREFIX + x));

                    }

                    paths = tempPath.toArray(new String[tempPath.size()]);
                } catch (final NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Channel path length is not numeric: " + pathLenStr + " - " + channel);
                }
            } else {
                throw new IllegalArgumentException("Channel path length is missing (and is required): " + channel);
            }
        }
    }

    /**
     * Gets the service identifier
     *
     * @return the service identifier
     */
    public String getService() {
        return service;
    }

    /**
     * Gets the channel category
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Gets the channel id
     *
     * @return the channel id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the sony path
     *
     * @return the non-null, non-empty path to the sony item
     */
    public String[] getPaths() {
        return paths;
    }

    /**
     * Gets the path part
     *
     * @return a possibly null, never empty path part
     */
    public @Nullable String getPathPart(final int idx) {
        return idx >= 0 && idx < paths.length ? paths[idx] : null;
    }

    /**
     * Adds a property to the channel
     *
     * @param key a non-null, non-empty key
     * @param value a non-null, non-emtpy value
     */
    public void addProperty(final String key, final String value) {
        Validate.notEmpty(key, "key cannot be empty");
        Validate.notEmpty(value, "value cannot be empty");
        properties.put(key, value);
    }

    /**
     * Gets a specific property by key
     *
     * @param key a non-null non-empty key
     * @return a possibly null, possibly empty value
     */
    public @Nullable String getProperty(final String key) {
        Validate.notEmpty(key, "key cannot be empty");
        return properties.get(key);
    }

    /**
     * Gets a specific property by key returning a default value if not found or
     * whose value is null
     *
     * @param key a non-null, non-empty key
     * @param defaultValue a non-null, possibly empty default value
     * @return a non-null, possibly empty value
     */
    public String getProperty(final String key, final String defaultValue) {
        Validate.notEmpty(key, "key cannot be empty");
        Objects.requireNonNull(defaultValue, "defaultValue cannot be null");
        final String propVal = properties.get(key);
        return propVal == null ? defaultValue : propVal;
    }

    /**
     * Determines if a property key has been assigned
     *
     * @param key a non-null, non-empty key
     * @return true if the property has been defined, false otherwise
     */
    public boolean hasProperty(final String key) {
        Validate.notEmpty(key, "key cannot be empty");
        return properties.containsKey(key);
    }

    /**
     * Removes a property assignment
     *
     * @param key a non-null, non-empty key
     * @return true if the property was found and removed, false otherwise
     */
    public @Nullable String removeProperty(final String key) {
        Validate.notEmpty(key, "key cannot be empty");
        return properties.remove(key);
    }

    /**
     * Returns the properties for this channel based on the paths
     *
     * @return a non-null, non-empty properties
     */
    public Map<String, String> getProperties() {
        final Map<String, String> props = new HashMap<>(properties);
        props.put(ScalarWebChannel.CNL_PROPLEN, String.valueOf(paths.length));
        for (int x = 0; x < paths.length; x++) {
            props.put(ScalarWebChannel.CNL_PROPPREFIX + x, paths[x]);
        }
        props.put(ScalarWebChannel.CNL_CHANNELCATEGORY, category);
        props.put(ScalarWebChannel.CNL_CHANNELID, id);
        return props;
    }

    /**
     * Gets the channel id based on group ID and channel id
     *
     * @return the channel id
     */
    public String getChannelId() {
        return SonyUtil.createChannelId(service, createChannelId(category, id));
    }

    /**
     * Helper method to create a simply channel id from the id (where the category
     * is the id)
     *
     * @param id the non-null, non-empty id
     * @return a non-null, non-empty channel id
     */
    public static String createChannelId(final String id) {
        Validate.notEmpty(id, "id cannot be empty");
        return createChannelId(id, id);
    }

    /**
     * Helper method to create a channel id from the category/id
     *
     * @param category the non-null, non-empty category
     * @param id the non-null, non-empty id
     * @return a non-null, non-empty channel id
     */
    public static String createChannelId(final String category, final String id) {
        Validate.notEmpty(id, "id cannot be empty");
        return StringUtils.equalsIgnoreCase(category, id) ? id : (category + CATEGORYSEPARATOR + id);
    }

    @Override
    public String toString() {
        return getChannelId() + " (cid=" + id + ", ctgy=" + category + ", path=" + StringUtils.join(paths, ',') + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + category.hashCode();
        result = prime * result + id.hashCode();
        return result;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ScalarWebChannel other = (ScalarWebChannel) obj;
        return StringUtils.equals(category, other.category) && StringUtils.equals(id, other.id);
    }
}
