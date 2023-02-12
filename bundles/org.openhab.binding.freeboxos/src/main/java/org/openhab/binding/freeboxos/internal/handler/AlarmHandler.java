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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.BINDING_ID;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState.ValueType;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointUi;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointUi.DisplayType;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EpType;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.HomeNode;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;

/**
 * The {@link AlarmHandler} is responsible for handling everything associated to
 * any Freebox Home Alarm thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AlarmHandler extends ApiConsumerHandler {

    public AlarmHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        HomeNode node = getManager(HomeManager.class).getHomeNode(getClientId());
        if (node != null) {
            ThingBuilder thingBuilder = editThing();
            node.showEndpoints().stream().filter(ep -> ep.epType() == EpType.SIGNAL).forEach(endPoint -> {
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), endPoint.name());
                Configuration channelConf = new Configuration();
                channelConf.put("id", endPoint.id());
                ChannelBuilder builder = ChannelBuilder
                        .create(channelUID, getAcceptedType(endPoint.valueType(), endPoint.ui()))
                        .withConfiguration(channelConf).withLabel(endPoint.label())
                        .withType(getChannelType(endPoint.valueType(), endPoint.ui()));
                thingBuilder.withChannel(builder.build());

            });
            updateThing(thingBuilder.build());
        }
    }

    private @Nullable ChannelTypeUID getChannelType(ValueType valueType, EndpointUi ui) {
        switch (valueType) {
            case STRING:
                return new ChannelTypeUID(BINDING_ID, "home-string");
            case INT:
                return new ChannelTypeUID(BINDING_ID, "home-number");
            case BOOL:
            case FLOAT:
            case UNKNOWN:
            case VOID:
            default:
                return null;
        }
    }

    private @Nullable String getAcceptedType(ValueType valueType, EndpointUi ui) {
        switch (valueType) {
            case STRING:
                return CoreItemFactory.STRING;
            case INT:
                if (ui.display() == DisplayType.SLIDER) {
                    return CoreItemFactory.DIMMER;
                }
                return CoreItemFactory.NUMBER;
            case BOOL:
            case FLOAT:
            case UNKNOWN:
            case VOID:
            default:
                return null;
        }
    }

    @Override
    protected void internalPoll() throws FreeboxException {
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        return super.internalHandleCommand(channelId, command);
    }
}
