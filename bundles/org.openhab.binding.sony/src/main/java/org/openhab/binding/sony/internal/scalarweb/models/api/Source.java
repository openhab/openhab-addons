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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the source identifier and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class Source {

    /** Pattern for a radio source URI */
    public static final Pattern RADIOPATTERN = Pattern.compile("radio:[af]m(?:\\?contentId=(\\d+))?");

    /** The source identifier */
    private @Nullable String source;

    /** The source title */
    private @Nullable String title;

    /** Whether the source is browsable */
    private @Nullable Boolean isBrowsable;

    /** Whether the source is playable */
    private @Nullable Boolean isPlayable;

    /** The source meta information */
    private @Nullable String meta;

    /** The source play action */
    private @Nullable String playAction;

    /** The source outputs */
    private @Nullable String @Nullable [] outputs;

    /**
     * Constructor used for deserialization only
     */
    public Source() {
    }

    /**
     * Gets the source meta information
     * 
     * @return the source meta information
     */
    public @Nullable String getMeta() {
        return meta;
    }

    /**
     * Gets the outputs for the source
     * 
     * @return the outputs for the source
     */
    public @Nullable String @Nullable [] getOutputs() {
        return outputs;
    }

    /**
     * Gets the play action for the source
     * 
     * @return the play action for the source
     */
    public @Nullable String getPlayAction() {
        return playAction;
    }

    /**
     * Gets the source identifier
     *
     * @return the source identifier
     */
    public @Nullable String getSource() {
        return source;
    }

    /**
     * Gets just the scheme part of the source uri
     * 
     * @return the scheme part of the source uri
     */
    public @Nullable String getSchemePart() {
        return getSchemePart(source);
    }

    /**
     * Gets the source part of the source uri
     * 
     * @return the source part of the source uri
     */
    public @Nullable String getSourcePart() {
        return getSourcePart(source);
    }

    /**
     * Gets the title of the source
     * 
     * @return the title of the source
     */
    public @Nullable String getTitle() {
        return title;
    }

    /**
     * Whether the source is browsable
     * 
     * @return whether the source is browsable
     */
    public @Nullable Boolean isBrowsable() {
        return isBrowsable;
    }

    /**
     * Whether the source is playable
     * 
     * @return whether the source is playable
     */
    public @Nullable Boolean isPlayable() {
        return isPlayable;
    }

    /**
     * Whether this source matches the given name (either by title or by source url)
     * 
     * @param name a non-null, possibly empty name
     * @return true for a match, false otherwise
     */
    public boolean isMatch(final String name) {
        Objects.requireNonNull(name, "name cannot be null");
        return StringUtils.equalsIgnoreCase(name, title) || StringUtils.equalsIgnoreCase(name, source);
    }

    /**
     * Helper method to extract the scheme part from a source uri
     * 
     * @param uri a possibly null, possibly empty source uri
     * @return the scheme part or null if not found
     */
    public static @Nullable String getSchemePart(final @Nullable String uri) {
        if (uri == null || StringUtils.isEmpty(uri)) {
            return null;
        }

        final int idx = uri.indexOf(":");
        return idx < 0 ? uri : uri.substring(0, idx);
    }

    /**
     * Helper method to extract the source part from a source uri
     * 
     * @param uri a possibly null, possibly empty source uri
     * @return the source part or null if not found
     */
    public static @Nullable String getSourcePart(final @Nullable String uri) {
        if (uri == null || StringUtils.isEmpty(uri)) {
            return null;
        }

        final int idx = uri.indexOf(":");
        return idx < 0 ? uri : uri.substring(idx + 1);
    }

    @Override
    public int hashCode() {
        final String localSource = source;
        return ((localSource == null) ? 0 : localSource.hashCode());
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
        return StringUtils.equals(source, ((Source) obj).source);
    }

    @Override
    public String toString() {
        return "Source [source=" + source + ", isBrowsable=" + isBrowsable + ", isPlayable=" + isPlayable + ", meta="
                + meta + ", playAction=" + playAction + ", outputs=" + Arrays.toString(outputs) + ", title=" + title
                + "]";
    }
}
