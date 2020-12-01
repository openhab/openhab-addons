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
package org.openhab.binding.sony.internal.providers.sources;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class holds meta information (such as ignoring certain model names or converting one model name to
 * another) and is used for deserialization only
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
class MetaInfo {
    /** Whether the information is enabled or not */
    private final boolean enabled;

    /** The list of model names to ignore */
    private final List<Pattern> ignoreModelName;

    /** The list of channel ids to ignore */
    private final List<Pattern> ignoreChannelId;

    /** The list of model names to convert */
    private final List<MetaConvert> modelNameConvert;

    /** The list of channel ids to convert (in case of a rename) */
    private final List<MetaConvert> channelIdConvert;

    /**
     * Constructs a default metainfo with nothing ignored or converted
     */
    MetaInfo() {
        this.enabled = true;
        this.ignoreModelName = Collections.emptyList();
        this.ignoreChannelId = Collections.emptyList();
        this.modelNameConvert = Collections.emptyList();
        this.channelIdConvert = Collections.emptyList();
    }

    /**
     * Constructs the meta info from the lists
     * 
     * @param enabled if the information is enabled or not
     * @param ignoreModelName a non-null, possibly empty list of model names to ignore
     * @param ignoreChannelId a non-null, possibly empty list of channel names to ignore
     * @param modelNameConvert a non-null, possibly empty list of model names to convert
     * @param channelIdConvert a non-null, possibly empty list of channel names to convert
     */
    MetaInfo(boolean enabled, final List<Pattern> ignoreModelName, final List<Pattern> ignoreChannelId,
            final List<MetaConvert> modelNameConvert, final List<MetaConvert> channelIdConvert) {
        Objects.requireNonNull(ignoreModelName, "ignoreModelName cannot be null");
        Objects.requireNonNull(ignoreChannelId, "ignoreChannelId cannot be null");
        Objects.requireNonNull(modelNameConvert, "modelNameConvert cannot be null");
        Objects.requireNonNull(channelIdConvert, "channelIdConvert cannot be null");

        this.enabled = enabled;
        this.ignoreModelName = Collections.unmodifiableList(ignoreModelName);
        this.ignoreChannelId = Collections.unmodifiableList(ignoreChannelId);
        this.modelNameConvert = Collections.unmodifiableList(modelNameConvert);
        this.channelIdConvert = Collections.unmodifiableList(channelIdConvert);
    }

    /**
     * Returns whether the information is enabled (true) or not (false)
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns true if the specified model name should be ignored
     * 
     * @param modelName a non-null, non-empty model name
     * @return true if it should be ignored, false otherwise
     */
    public boolean isIgnoredModelName(final String modelName) {
        Validate.notEmpty(modelName, "modelName cannot be empty");
        return ignoreModelName.stream().anyMatch(s -> s.matcher(modelName).matches());
    }

    /**
     * Returns true if the specified channel id should be ignored
     * 
     * @param channelId a non-null, non-empty channel id
     * @return true if it should be ignored, false otherwise
     */
    public boolean isIgnoredChannelId(final String channelId) {
        Validate.notEmpty(channelId, "channelId cannot be empty");
        return ignoreChannelId.stream().anyMatch(s -> s.matcher(channelId).matches());
    }

    /**
     * Provides the converted model name (or the original if not converted)
     * 
     * @param modelName a non-null, non-empty model name
     * @return a non-null, non-empty converted model name (or the original if it shouldn't be converted)
     */
    public String getModelName(final String modelName) {
        Validate.notEmpty(modelName, "modelName cannot be empty");

        return getNewName(modelName, modelNameConvert);
    }

    /**
     * Provides the converted channel id (or the original if not converted)
     * 
     * @param channelId a non-null, non-empty channel id
     * @return a non-null, non-empty converted channel id (or the original if it shouldn't be converted)
     */
    public String getChannelId(final String channelId) {
        Validate.notEmpty(channelId, "channelId cannot be empty");

        return getNewName(channelId, channelIdConvert);
    }

    /**
     * Provides the converted name (or the original if not converted)
     * 
     * @param name a non-null, non-empty name
     * @param convert a possibly null list of converts
     * @return a non-null, non-empty converted name (or the original if it shouldn't be converted)
     */
    public String getNewName(final String name, final List<MetaConvert> convert) {
        Validate.notEmpty(name, "name cannot be empty");

        for (final MetaConvert mc : convert) {
            final String newName = mc.getNewName();
            final Pattern oldName = mc.getOldName();
            if (oldName.matcher(name).matches()) {
                return newName;
            }
        }
        return name;
    }
}
