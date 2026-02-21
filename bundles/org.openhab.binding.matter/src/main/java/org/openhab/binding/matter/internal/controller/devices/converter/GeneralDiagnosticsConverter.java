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
package org.openhab.binding.matter.internal.controller.devices.converter;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.GeneralDiagnosticsCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;

import com.google.gson.Gson;

/**
 * A converter for translating {@link GeneralDiagnosticsCluster} events and attributes to openHAB channels and
 * back again.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class GeneralDiagnosticsConverter extends GenericConverter<GeneralDiagnosticsCluster> {
    private Gson gson = new Gson();

    public GeneralDiagnosticsConverter(GeneralDiagnosticsCluster cluster, MatterBaseThingHandler handler,
            int endpointNumber, String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        return Collections.emptyMap();
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case GeneralDiagnosticsCluster.ATTRIBUTE_NETWORK_INTERFACES:
                updateThingAttributeProperty(message.path.attributeName,
                        message.value != null ? gson.toJson(message.value) : null);
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        logger.debug("initState");
        updateThingProperties(initializingCluster);
    }

    private void updateThingProperties(GeneralDiagnosticsCluster cluster) {
        updateThingAttributeProperty(GeneralDiagnosticsCluster.ATTRIBUTE_NETWORK_INTERFACES,
                cluster.networkInterfaces != null ? gson.toJson(cluster.networkInterfaces) : null);
    }
}
