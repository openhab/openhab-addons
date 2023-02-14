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
package org.openhab.binding.freeboxos.internal.providers;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.BINDING_ID;
import static org.openhab.core.thing.DefaultSystemChannelTypeProvider.SYSTEM_CHANNEL_TYPE_UID_BATTERY_LEVEL;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.DisplayType;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState.ValueType;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointUi;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Component;

/**
 * Implementation providing channel types for Home dynamic channels
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component
public class HomeChannelTypeProvider implements ChannelTypeProvider {
    public static final ChannelTypeUID HOME_CHANNEL_TYPE_UID_STRING = new ChannelTypeUID(BINDING_ID, "home-string");
    public static final ChannelTypeUID HOME_CHANNEL_TYPE_UID_NUMBER = new ChannelTypeUID(BINDING_ID, "home-number");
    public static final ChannelTypeUID HOME_CHANNEL_TYPE_UID_INT_QTTY = new ChannelTypeUID(BINDING_ID, "home-int-qtty");
    public static final ChannelTypeUID HOME_CHANNEL_TYPE_UID_SWITCH = new ChannelTypeUID(BINDING_ID, "home-switch");
    public static final ChannelTypeUID HOME_CHANNEL_TYPE_UID_CONTACT = new ChannelTypeUID(BINDING_ID, "home-contact");

    public static final ChannelType HOME_CHANNEL_TYPE_STRING = ChannelTypeBuilder
            .state(HOME_CHANNEL_TYPE_UID_STRING, "String Channel", CoreItemFactory.STRING)
            .withDescription("String channel for FreeboxOs Home Device").build();

    public static final ChannelType HOME_CHANNEL_TYPE_NUMBER = ChannelTypeBuilder
            .state(HOME_CHANNEL_TYPE_UID_NUMBER, "Number Channel", CoreItemFactory.NUMBER)
            .withDescription("Number channel for FreeboxOs Home Device").build();

    public static final ChannelType HOME_CHANNEL_TYPE_INT_QTTY = ChannelTypeBuilder
            .state(HOME_CHANNEL_TYPE_UID_INT_QTTY, "Quantity Channel", CoreItemFactory.NUMBER)
            .withDescription("Quantity channel for FreeboxOs Home Device")
            .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create().withPattern("%d %unit%").build())
            .build();

    public static final ChannelType HOME_CHANNEL_TYPE_SWITCH = ChannelTypeBuilder
            .state(HOME_CHANNEL_TYPE_UID_SWITCH, "Switch Channel", CoreItemFactory.SWITCH)
            .withDescription("Switch channel for FreeboxOs Home Device").build();

    public static final ChannelType HOME_CHANNEL_TYPE_CONTACT = ChannelTypeBuilder
            .state(HOME_CHANNEL_TYPE_UID_CONTACT, "Contact Channel", CoreItemFactory.CONTACT)
            .withDescription("Contact channel for FreeboxOs Home Device").build();
    private static final Set<ChannelType> HOME_CHANNEL_TYPES = Set.of(HOME_CHANNEL_TYPE_STRING,
            HOME_CHANNEL_TYPE_NUMBER, HOME_CHANNEL_TYPE_INT_QTTY, HOME_CHANNEL_TYPE_SWITCH, HOME_CHANNEL_TYPE_CONTACT);

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return HOME_CHANNEL_TYPES;
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return HOME_CHANNEL_TYPES.stream().filter(ct -> ct.getUID().equals(channelTypeUID)).findFirst().orElse(null);
    }

    public static @Nullable ChannelTypeUID getChannelType(String name, ValueType valueType, EndpointUi ui) {
        switch (valueType) {
            case STRING:
                return HOME_CHANNEL_TYPE_UID_STRING;
            case INT:
                return name.equals("battery") ? SYSTEM_CHANNEL_TYPE_UID_BATTERY_LEVEL
                        : ui.unit() != null ? HOME_CHANNEL_TYPE_UID_INT_QTTY : HOME_CHANNEL_TYPE_UID_NUMBER;
            case BOOL:
                return ui.display() == DisplayType.TOGGLE ? HOME_CHANNEL_TYPE_UID_SWITCH
                        : HOME_CHANNEL_TYPE_UID_CONTACT;
            case FLOAT:
            case UNKNOWN:
            case VOID:
            default:
                return null;
        }
    }

    public static @Nullable String getAcceptedType(ValueType valueType, EndpointUi ui) {
        switch (valueType) {
            case STRING:
                return CoreItemFactory.STRING;
            case INT:
                if ("%".equals(ui.unit())) {
                    return "Number:Dimensionless";
                } else if ("sec".equals(ui.unit())) {
                    return "Number:Time";
                }
                return CoreItemFactory.NUMBER;
            case BOOL:
                return ui.display() == DisplayType.TOGGLE ? CoreItemFactory.SWITCH : CoreItemFactory.CONTACT;
            case FLOAT:
            case UNKNOWN:
            case VOID:
            default:
                return null;
        }
    }
}
