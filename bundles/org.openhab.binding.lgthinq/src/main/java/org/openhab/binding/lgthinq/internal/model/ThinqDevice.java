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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ThinqDevice} class.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class ThinqDevice {
    private final String type;
    private final String label;
    private final String description;

    private final List<ThinqChannel> channels;
    private final List<DeviceParameter> configParameter;
    private final List<ThinqChannelGroup> groups;

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public List<ThinqChannel> getChannels() {
        return channels;
    }

    public List<ThinqChannelGroup> getGroups() {
        return groups;
    }

    public ThinqDevice(String type, String label, String description, List<ThinqChannel> channels,
            List<DeviceParameter> configParameter, List<ThinqChannelGroup> groups) {
        this.type = type;
        this.label = label;
        this.description = description;
        this.channels = channels;
        this.configParameter = configParameter;
        this.groups = groups;
        this.channels.forEach(c -> {
            c.device = this;
        });
        this.groups.forEach(g -> {
            g.setDevice(this);
        });
    }

    public String getLabel() {
        return label;
    }

    public List<DeviceParameter> getConfigParameter() {
        return configParameter;
    }
}
