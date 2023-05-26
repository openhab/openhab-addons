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

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ThinqChannelGroup} class.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ThinqChannelGroup {
    private final List<ThinqChannel> channels;
    private ThinqDevice device;
    private final String name;
    private final String description;
    private final String label;

    public ThinqChannelGroup(List<ThinqChannel> channels, ThinqDevice device, String name, String description,
            String label) {
        this.channels = channels;
        this.device = device;
        this.name = name;
        this.description = description;
        this.label = label;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ThinqChannelGroup that = (ThinqChannelGroup) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getLabel() {
        return label;
    }

    public List<ThinqChannel> getChannels() {
        return channels;
    }

    public ThinqDevice getDevice() {
        return device;
    }

    public void setDevice(ThinqDevice device) {
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
