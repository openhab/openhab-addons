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
package org.openhab.binding.resol.internal.providers;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.resol.internal.ResolBindingConstants;

import de.resol.vbus.Specification;
import de.resol.vbus.SpecificationFile.Unit;

/**
 * @author Raphael Mack - Initial Contribution
 *
 */
public class ResolChannelTypeProvider implements ChannelTypeProvider {
    private Map<ChannelTypeUID, ChannelType> channelTypes = new ConcurrentHashMap<ChannelTypeUID, ChannelType>();

    public ResolChannelTypeProvider() {
        // let's add all channel types from known by the resol-vbus java library

        Specification spec = Specification.getDefaultSpecification();

        Unit[] units = spec.getUnits();
        for (Unit u : units) {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(ResolBindingConstants.BINDING_ID, u.getUnitCodeText());

            // maybe we could use pfv.getPacketFieldSpec().getPrecision() here
            int precision = 1;
            if (u.getUnitId() >= 0) {

                ChannelType ctype = ChannelTypeBuilder.state(channelTypeUID, u.getUnitFamily().toString(), "Number")
                        .withStateDescription(new StateDescription(null, null, null,
                                "%." + precision + "f " + u.getUnitTextText().replace("%", "%%"), true, null))
                        .build();

                channelTypes.put(channelTypeUID, ctype);
            }
        }
    }

    @Override
    public Collection<@NonNull ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(@NonNull ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (channelTypes.containsKey(channelTypeUID)) {
            return channelTypes.get(channelTypeUID);
        } else {
            return null;
        }
    }
}
