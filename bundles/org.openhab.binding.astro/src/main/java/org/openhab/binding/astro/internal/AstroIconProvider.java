/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.astro.internal;

import static org.openhab.binding.astro.internal.AstroBindingConstants.BINDING_ID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.model.SeasonName;
import org.openhab.binding.astro.internal.model.ZodiacSign;
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
 * The {@link AstroIconProvider} is the class providing binding related icons.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = { IconProvider.class, AstroIconProvider.class })
@NonNullByDefault
public class AstroIconProvider implements IconProvider {
    private static final String DEFAULT_LABEL = "Astro Icons";
    private static final String DEFAULT_DESCRIPTION = "Icons provided for the Astro Binding";
    private static final String ZODIAC_SET = "zodiac";
    private static final String SEASON_SET = "season";
    private static final Set<String> ICON_SETS = Set.of(SEASON_SET, ZODIAC_SET);

    private final Logger logger = LoggerFactory.getLogger(AstroIconProvider.class);
    private final TranslationProvider i18nProvider;
    private final Bundle bundle;

    @Activate
    public AstroIconProvider(final BundleContext context, final @Reference TranslationProvider i18nProvider) {
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
        return Format.SVG.equals(format) && iconSetId.equals(BINDING_ID) && ICON_SETS.contains(category) ? 0 : null;
    }

    @Override
    public @Nullable InputStream getIcon(String category, String iconSetId, @Nullable String state, Format format) {
        String iconName = "icon/%s.svg".formatted(Locale.ROOT, category);
        if (ICON_SETS.contains(category) && state != null) {
            try {
                Enum<?> stateEnum = switch (category) {
                    case ZODIAC_SET -> ZodiacSign.valueOf(state);
                    case SEASON_SET -> SeasonName.valueOf(state);
                    default -> throw new IllegalArgumentException("Category of icon not found: %s".formatted(category));
                };
                iconName = iconName.replace(".", "-%s.".formatted(stateEnum.name().toLowerCase(Locale.US)));
            } catch (IllegalArgumentException e) {
                // Invalid state for the icon set, we'll remain on default icon
                logger.warn("Error retrieving icon name '{}' - using default: {}", state, e.getMessage());
            }
        }

        String result = "";

        URL iconResource = bundle.getEntry(iconName);
        try (InputStream stream = iconResource.openStream()) {
            result = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("Unable to load resource '{}': {}", iconResource.getPath(), e.getMessage());
        }

        return result.isEmpty() ? null : new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
    }
}
