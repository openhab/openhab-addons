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
package org.openhab.binding.atmofrance.internal;

import static org.openhab.binding.atmofrance.internal.AtmoFranceBindingConstants.BINDING_ID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atmofrance.internal.api.dto.PollenIndex;
import org.openhab.binding.atmofrance.internal.api.dto.Taxon;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.ui.icon.IconProvider;
import org.openhab.core.ui.icon.IconSet;
import org.openhab.core.ui.icon.IconSet.Format;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AtmoFranceIconProvider} is the class providing binding related icons.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = { IconProvider.class, AtmoFranceIconProvider.class })
@NonNullByDefault
public class AtmoFranceIconProvider implements IconProvider {
    private static final String NEUTRAL_COLOR = "#3d3c3c";
    private static final String DEFAULT_LABEL = "Atmo France Icons";
    private static final String AQ_ICON = "aq";
    private static final String POLLEN_ICON = "pollen";
    private static final String DEFAULT_DESCRIPTION = "Icons illustrating air quality levels provided by Atmo France";
    private static final List<String> TAXON_ICONS = Taxon.AS_SET.stream().map(Taxon::name).map(String::toLowerCase)
            .toList();

    private final Logger logger = LoggerFactory.getLogger(AtmoFranceIconProvider.class);
    private final TranslationProvider i18nProvider;
    private final Bundle bundle;

    @Activate
    public AtmoFranceIconProvider(final BundleContext context, final @Reference TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
        this.bundle = context.getBundle();
    }

    @Override
    public Set<IconSet> getIconSets() {
        return getIconSets(null);
    }

    @Override
    public Set<IconSet> getIconSets(@Nullable Locale locale) {
        String label = getText("label", DEFAULT_LABEL, locale);
        String description = getText("description", DEFAULT_DESCRIPTION, locale);

        return Set.of(new IconSet(BINDING_ID, label, description, Set.of(Format.SVG)));
    }

    private String getText(String entry, String defaultValue, @Nullable Locale locale) {
        String text = locale == null ? null : i18nProvider.getText(bundle, "iconset." + entry, defaultValue, locale);
        return text == null ? defaultValue : text;
    }

    @Override
    public @Nullable Integer hasIcon(String category, String iconSetId, Format format) {
        return Format.SVG.equals(format) && iconSetId.equals(BINDING_ID)
                && (category.equals(AQ_ICON) || category.equals(POLLEN_ICON) || TAXON_ICONS.contains(category)) ? 0
                        : null;
    }

    @Override
    public @Nullable InputStream getIcon(String category, String iconSetId, @Nullable String state, Format format) {
        int ordinal = -1;
        try {
            ordinal = state != null ? Integer.valueOf(state) : -1;
        } catch (NumberFormatException ignore) {
        }

        String iconName = "icon/%s.svg".formatted(category);
        if ((category.equals(AQ_ICON) && ordinal >= 0 && ordinal <= 9 && ordinal != 8)
                || (category.equals(POLLEN_ICON) && ordinal >= 0 && ordinal <= 5)) {
            iconName = iconName.replace(".svg", "-%d.svg".formatted(ordinal));
        }

        String result = "";
        URL iconResource = bundle.getEntry(iconName);
        if (iconResource != null) {
            try (InputStream stream = iconResource.openStream()) {
                result = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                if (TAXON_ICONS.contains(category)) {
                    String color = PollenIndex.stateColor(ordinal);
                    result = result.replaceAll(NEUTRAL_COLOR, color);
                }
            } catch (IOException e) {
                logger.warn("Unable to load ressource '{}': {}", iconResource.getPath(), e.getMessage());
            }
        }
        return result.isEmpty() ? null : new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
    }
}
