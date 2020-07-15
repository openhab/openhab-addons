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
package org.openhab.binding.lifx.internal;

import static org.openhab.binding.lifx.internal.LifxBindingConstants.*;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link LifxChannelFactoryImpl} creates dynamic LIFX channels.
 *
 * @author Wouter Born - Add i18n support
 */
@NonNullByDefault
@Component(service = LifxChannelFactory.class, immediate = true)
public class LifxChannelFactoryImpl implements LifxChannelFactory {

    private static final String COLOR_ZONE_LABEL_KEY = "channel-type.lifx.colorzone.label";
    private static final String COLOR_ZONE_DESCRIPTION_KEY = "channel-type.lifx.colorzone.description";

    private static final String TEMPERATURE_ZONE_LABEL_KEY = "channel-type.lifx.temperaturezone.label";
    private static final String TEMPERATURE_ZONE_DESCRIPTION_KEY = "channel-type.lifx.temperaturezone.description";

    private @NonNullByDefault({}) Bundle bundle;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;
    private @NonNullByDefault({}) LocaleProvider localeProvider;

    @Override
    public Channel createColorZoneChannel(ThingUID thingUID, int index) {
        String label = getText(COLOR_ZONE_LABEL_KEY, index);
        String description = getText(COLOR_ZONE_DESCRIPTION_KEY, index);
        return ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_COLOR_ZONE + index), "Color")
                .withType(CHANNEL_TYPE_COLOR_ZONE).withLabel(label).withDescription(description).build();
    }

    @Override
    public Channel createTemperatureZoneChannel(ThingUID thingUID, int index) {
        String label = getText(TEMPERATURE_ZONE_LABEL_KEY, index);
        String description = getText(TEMPERATURE_ZONE_DESCRIPTION_KEY, index);
        return ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_TEMPERATURE_ZONE + index), "Dimmer")
                .withType(CHANNEL_TYPE_TEMPERATURE_ZONE).withLabel(label).withDescription(description).build();
    }

    private @Nullable String getDefaultText(String key) {
        return i18nProvider.getText(bundle, key, key, Locale.ENGLISH);
    }

    private String getText(String key, Object... arguments) {
        Locale locale = localeProvider != null ? localeProvider.getLocale() : Locale.ENGLISH;
        if (i18nProvider == null) {
            return key;
        }

        String text = i18nProvider.getText(bundle, key, getDefaultText(key), locale, arguments);
        return text != null ? text : key;
    }

    @Activate
    protected void activate(ComponentContext componentContext) {
        this.bundle = componentContext.getBundleContext().getBundle();
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        this.bundle = null;
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }

    @Reference
    protected void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = null;
    }
}
