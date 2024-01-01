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
package org.openhab.binding.meteoalerte.internal;

import static org.openhab.binding.meteoalerte.internal.MeteoAlerteBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteoalerte.internal.json.ResponseFieldDTO.AlertLevel;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.ui.icon.IconProvider;
import org.openhab.core.ui.icon.IconSet;
import org.openhab.core.ui.icon.IconSet.Format;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MeteoAlertIconProvider} is the class providing binding related icons.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = { IconProvider.class, MeteoAlertIconProvider.class })
@NonNullByDefault
public class MeteoAlertIconProvider implements IconProvider {
    private static final String DEFAULT_LABEL = "Météo Alerte Icons";
    private static final String DEFAULT_DESCRIPTION = "Icons illustrating weather events provided by Météo Alerte";
    private static final Set<String> ICONS = Set.of(WAVE, AVALANCHE, HEAT, FREEZE, FLOOD, SNOW, STORM, RAIN, WIND,
            "meteo_france");

    private final Logger logger = LoggerFactory.getLogger(MeteoAlertIconProvider.class);
    private final BundleContext context;
    private final TranslationProvider i18nProvider;

    @Activate
    public MeteoAlertIconProvider(final BundleContext context, final @Reference TranslationProvider i18nProvider) {
        this.context = context;
        this.i18nProvider = i18nProvider;
    }

    @Override
    public Set<IconSet> getIconSets() {
        return getIconSets(null);
    }

    @Override
    public Set<IconSet> getIconSets(@Nullable Locale locale) {
        String label = getText("label", DEFAULT_LABEL, locale);
        String description = getText("decription", DEFAULT_DESCRIPTION, locale);

        return Set.of(new IconSet(BINDING_ID, label, description, Set.of(Format.SVG)));
    }

    private String getText(String entry, String defaultValue, @Nullable Locale locale) {
        String text = defaultValue;
        if (locale != null) {
            text = i18nProvider.getText(context.getBundle(), "iconset." + entry, defaultValue, locale);
            text = text == null ? defaultValue : text;
        }
        return text;
    }

    @Override
    public @Nullable Integer hasIcon(String category, String iconSetId, Format format) {
        return ICONS.contains(category) && iconSetId.equals(BINDING_ID) && format == Format.SVG ? 0 : null;
    }

    public @Nullable InputStream getIcon(String category, String state) {
        return getIcon(category, BINDING_ID, state, Format.SVG);
    }

    @Override
    public @Nullable InputStream getIcon(String category, String iconSetId, @Nullable String state, Format format) {
        String icon = getResource(category);
        if (icon.isEmpty()) {
            return null;
        }

        if (state != null) {
            try {
                Integer ordinal = Integer.valueOf(state);
                AlertLevel alertLevel = ordinal < AlertLevel.values().length ? AlertLevel.values()[ordinal]
                        : AlertLevel.UNKNOWN;
                icon = icon.replaceAll(AlertLevel.UNKNOWN.color, alertLevel.color);
            } catch (NumberFormatException e) {
                logger.debug("{} is not a valid DecimalType", state);
            }
        }

        return new ByteArrayInputStream(icon.getBytes());
    }

    private String getResource(String iconName) {
        String result = "";

        URL iconResource = context.getBundle().getEntry("icon/%s.svg".formatted(iconName));
        try (InputStream stream = iconResource.openStream()) {
            result = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("Unable to load ressource '{}' : {}", iconResource.getPath(), e.getMessage());
        }
        return result;
    }
}
