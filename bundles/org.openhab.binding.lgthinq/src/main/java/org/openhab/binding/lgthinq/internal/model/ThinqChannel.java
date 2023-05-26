/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.internal.model;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ThinqChannel} class.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ThinqChannel {

    @Nullable
    ThinqDevice device;
    private final DataType type;
    @Nullable
    private final String unitDisplayPattern;
    private final String name;
    private final String label;
    private final String description;
    private final boolean isDynamic;
    private final boolean isReadOnly;
    private final boolean isAdvanced;
    @Nullable
    private final ThinqChannelGroup channelGroup;

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ThinqChannel that = (ThinqChannel) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public ThinqChannel(DataType type, @Nullable String unitDisplayPattern, String name, String label,
            String description, boolean isDynamic, boolean isReadOnly, boolean isAdvanced,
            @Nullable ThinqChannelGroup channelGroup) {
        this.type = type;
        this.unitDisplayPattern = unitDisplayPattern;
        this.name = name;
        this.label = label;
        this.description = description;
        this.isDynamic = isDynamic;
        this.isReadOnly = isReadOnly;
        this.isAdvanced = isAdvanced;
        this.channelGroup = channelGroup;
        if (channelGroup != null && !channelGroup.getChannels().contains(this)) {
            channelGroup.getChannels().add(this);
        }
    }

    public @Nullable ThinqChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public boolean isAdvanced() {
        return isAdvanced;
    }

    public String getLabel() {
        return label;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public @Nullable String getUnitDisplayPattern() {
        return unitDisplayPattern;
    }

    public @Nullable ThinqDevice getDevice() {
        return device;
    }

    public DataType getType() {
        return type;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
