/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.resol.internal.ResolBindingConstants;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Component;

import de.resol.vbus.Specification;
import de.resol.vbus.SpecificationFile.Unit;

/**
 * @author Raphael Mack - Initial Contribution
 *
 */
@Component(service = { ChannelTypeProvider.class, ResolChannelTypeProvider.class })
@NonNullByDefault
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
                ChannelType ctype = ChannelTypeBuilder
                        .state(channelTypeUID, u.getUnitFamily().toString(), itemTypeForUnit(u))
                        .withStateDescriptionFragment(StateDescriptionFragmentBuilder.create()
                                .withPattern("%." + precision + "f " + u.getUnitTextText().replace("%", "%%"))
                                .withReadOnly(true).build())
                        .build();

                channelTypes.put(channelTypeUID, ctype);
            }
        }
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (channelTypes.containsKey(channelTypeUID)) {
            return channelTypes.get(channelTypeUID);
        } else {
            return null;
        }
    }

    public static String itemTypeForUnit(Unit u) {
        String itemType = "Number";
        switch (u.getUnitFamily()) {
            case Temperature:
                itemType += ":Temperature";
                break;
            case Energy:
                itemType += ":Energy";
                break;
            case VolumeFlow:
                itemType += ":VolumetricFlowRate";
                break;
            case Pressure:
                itemType += ":Pressure";
                break;
            case Volume:
                itemType += ":Volume";
                break;
            case Time:
                itemType += ":Time";
                break;
            case Power:
                itemType += ":Power";
                break;
            case None:
                switch (u.getUnitCodeText()) {
                    case "Hertz":
                        itemType += ":Frequency";
                        break;
                    case "Hectopascals":
                        itemType += ":Pressure";
                        break;
                    case "MetersPerSecond":
                        itemType += ":Speed";
                        break;
                    case "Milliamperes":
                        itemType += ":ElectricCurrent";
                        break;
                    case "Milliseconds":
                        itemType += ":Time";
                        break;
                    case "Ohms":
                        itemType += ":ElectricResistance";
                        break;
                    case "Percent":
                        itemType += ":Dimensionless";
                        break;
                    case "PercentRelativeHumidity":
                        itemType += ":Dimensionless";
                        break;
                    case "Volts":
                        itemType += ":ElectricPotential";
                        break;
                    case "WattsPerSquareMeter":
                        itemType += ":Intensity";
                        break;
                }
                break;
            default:
        }
        return itemType;
    }
}
