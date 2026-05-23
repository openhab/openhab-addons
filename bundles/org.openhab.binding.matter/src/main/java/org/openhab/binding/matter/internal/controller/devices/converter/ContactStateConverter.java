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

import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_CONTACT_STATEVALUE;
import static org.openhab.binding.matter.internal.MatterBindingConstants.CHANNEL_ID_CONTACT_STATEVALUE;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BooleanStateCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * A converter for mapping {@link BooleanStateCluster} to Contact semantics.
 *
 * Matter contact sensors define TRUE as closed/contact and FALSE as open/no contact.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class ContactStateConverter extends GenericConverter<BooleanStateCluster> {

    public ContactStateConverter(BooleanStateCluster cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix) {
        super(cluster, handler, endpointNumber, labelPrefix);
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Channel contactStateChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, CHANNEL_ID_CONTACT_STATEVALUE), CoreItemFactory.CONTACT)
                .withType(CHANNEL_CONTACT_STATEVALUE).build();
        return Collections.singletonMap(contactStateChannel, null);
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case BooleanStateCluster.ATTRIBUTE_STATE_VALUE -> {
                if (message.value instanceof Boolean booleanValue) {
                    updateState(CHANNEL_ID_CONTACT_STATEVALUE,
                            booleanValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
                }
            }
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        Boolean stateValue = initializingCluster.stateValue;
        if (stateValue == null) {
            updateState(CHANNEL_ID_CONTACT_STATEVALUE, UnDefType.NULL);
            return;
        }

        updateState(CHANNEL_ID_CONTACT_STATEVALUE, stateValue ? OpenClosedType.CLOSED : OpenClosedType.OPEN);
    }
}
