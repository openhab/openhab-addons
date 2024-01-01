/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tr064.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tr064.internal.dto.config.ChannelTypeDescription;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDServiceType;
import org.openhab.binding.tr064.internal.dto.scpd.service.SCPDActionType;

/**
 * The {@link Tr064ChannelConfig} class holds a channel configuration
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Tr064ChannelConfig {
    private final ChannelTypeDescription channelTypeDescription;
    private final SCPDServiceType service;
    private @Nullable SCPDActionType getAction;
    private String dataType = "";
    private @Nullable String parameter;

    public Tr064ChannelConfig(ChannelTypeDescription channelTypeDescription, SCPDServiceType service) {
        this.channelTypeDescription = channelTypeDescription;
        this.service = service;
    }

    public Tr064ChannelConfig(Tr064ChannelConfig o) {
        this.channelTypeDescription = o.channelTypeDescription;
        this.service = o.service;
        this.getAction = o.getAction;
        this.dataType = o.dataType;
        this.parameter = o.parameter;
    }

    public ChannelTypeDescription getChannelTypeDescription() {
        return channelTypeDescription;
    }

    public SCPDServiceType getService() {
        return service;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public @Nullable SCPDActionType getGetAction() {
        return getAction;
    }

    public void setGetAction(SCPDActionType getAction) {
        this.getAction = getAction;
    }

    public @Nullable String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public String toString() {
        final SCPDActionType getAction = this.getAction;
        return "Tr064ChannelConfig{" + "channelType=" + channelTypeDescription.getName() + ", getAction="
                + ((getAction == null) ? "(null)" : getAction.getName()) + ", dataType='" + dataType + ", parameter='"
                + parameter + "'}";
    }
}
