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
package org.openhab.binding.netatmo.internal.providers;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.BINDING_ID;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.MeasureClass;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.thing.i18n.ChannelTypeI18nLocalizationService;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Extends the ChannelTypeProvider generating Channel Types based on {@link MeasureClass} enum.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Garnier - Localizing the extensible channel types
 *
 */
@NonNullByDefault
@Component(service = { NetatmoChannelTypeProvider.class, ChannelTypeProvider.class })
public class NetatmoChannelTypeProvider implements ChannelTypeProvider {
    private final Collection<ChannelType> channelTypes = new HashSet<>();

    @Activate
    public NetatmoChannelTypeProvider(final @Reference ChannelTypeI18nLocalizationService localizationService,
            final @Reference LocaleProvider localeProvider, ComponentContext componentContext) {
        final Bundle bundle = componentContext.getBundleContext().getBundle();
        MeasureClass.AS_SET.forEach(mc -> mc.channels.forEach((measureChannel, channelDetails) -> {
            StateChannelTypeBuilder channelTypeBuilder = ChannelTypeBuilder
                    .state(new ChannelTypeUID(BINDING_ID, measureChannel),
                            String.format("@text/extensible-channel-type.%s.label", measureChannel),
                            channelDetails.itemType)
                    .withStateDescriptionFragment(channelDetails.stateDescriptionFragment)
                    .withConfigDescriptionURI(channelDetails.configURI);
            ChannelType channelType = localizationService.createLocalizedChannelType(bundle, channelTypeBuilder.build(),
                    localeProvider.getLocale());
            channelTypes.add(channelType);
        }));
    }

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return Collections.unmodifiableCollection(channelTypes);
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channelTypes.stream().filter(ct -> ct.getUID().equals(channelTypeUID)).findFirst().orElse(null);
    }
}
