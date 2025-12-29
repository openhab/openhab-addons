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
<<<<<<< Upstream, based on main
import org.openhab.binding.astro.internal.model.EclipseKind;
=======
<<<<<<< Upstream, based on main
>>>>>>> 0e1ea37 Initial commit for Moon phase revamp
import org.openhab.binding.astro.internal.model.SeasonName;
=======
import org.openhab.binding.astro.internal.model.EclipseKind;
import org.openhab.binding.astro.internal.model.MoonPhaseName;
>>>>>>> 11e99dd Initial commit for Moon phase revamp
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
<<<<<<< Upstream, based on main
    private static final String SEASON_SET = "season";
<<<<<<< Upstream, based on main
    private static final String SUN_ECLIPSE_SET = "sun_eclipse";
    private static final Set<String> ICON_SETS = Set.of(SEASON_SET, SUN_ECLIPSE_SET, ZODIAC_SET);
=======
    private static final Set<String> ICON_SETS = Set.of(SEASON_SET, ZODIAC_SET);
=======
    private static final String MOON_PHASE_SET = "moon_phase";
    private static final String MOON_ECLIPSE_SET = "moon_eclipse";
<<<<<<< Upstream, based on main
    private static final Set<String> ICON_SET = Set.of(ZODIAC_SET, MOON_PHASE_SET, MOON_ECLIPSE_SET);
>>>>>>> 11e99dd Initial commit for Moon phase revamp
<<<<<<< Upstream, based on main
>>>>>>> 0e1ea37 Initial commit for Moon phase revamp
=======
=======
    private static final String MOON_DAY_SET = "moon_day";
    private static final Set<String> ICON_SET = Set.of(ZODIAC_SET, MOON_PHASE_SET, MOON_ECLIPSE_SET, MOON_DAY_SET);
>>>>>>> 8573003 Adds moon-day icon set. Rebased.
>>>>>>> cbf0ca8 Adds moon-day icon set. Rebased.

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
<<<<<<< Upstream, based on main
        return Format.SVG.equals(format) && iconSetId.equals(BINDING_ID) && ICON_SETS.contains(category) ? 0 : null;
=======
        return Format.SVG.equals(format) && iconSetId.equals(BINDING_ID) && ICON_SET.contains(category) ? 0 : null;
>>>>>>> 11e99dd Initial commit for Moon phase revamp
    }

    @Override
    public @Nullable InputStream getIcon(String category, String iconSetId, @Nullable String state, Format format) {
<<<<<<< Upstream, based on main
        String iconName = String.format(Locale.ROOT, "icon/%s.svg", category);
=======
        String iconName = "icon/%s.svg".formatted(category);
<<<<<<< Upstream, based on main
>>>>>>> 24ede3e Initial commit for Moon phase revamp
        if (ICON_SETS.contains(category) && state != null) {
=======
        if (ICON_SET.contains(category) && state != null) {
>>>>>>> 11e99dd Initial commit for Moon phase revamp
            try {
<<<<<<< Upstream, based on main
<<<<<<< Upstream, based on main
                Enum<?> stateEnum = switch (category) {
                    case ZODIAC_SET -> ZodiacSign.valueOf(state);
                    case SEASON_SET -> SeasonName.valueOf(state);
                    case SUN_ECLIPSE_SET -> EclipseKind.valueOf(state);
                    default -> throw new IllegalArgumentException("Category of icon not found: %s".formatted(category));
                };
                iconName = iconName.replace(".", "-%s.".formatted(stateEnum.name().toLowerCase(Locale.US)));
            } catch (IllegalArgumentException e) {
=======
                Enum<?> iconState = switch (category) {
                    case ZODIAC_SET -> ZodiacSign.valueOf(state);
                    case MOON_PHASE_SET -> MoonPhaseName.valueOf(state);
                    case MOON_ECLIPSE_SET -> EclipseKind.valueOf(state);
=======
                String iconState = switch (category) {
                    case ZODIAC_SET -> ZodiacSign.valueOf(state).name().toLowerCase(Locale.US);
                    case MOON_PHASE_SET -> MoonPhaseName.valueOf(state).name().toLowerCase(Locale.US);
                    case MOON_ECLIPSE_SET -> EclipseKind.valueOf(state).name().toLowerCase(Locale.US);
                    case MOON_DAY_SET -> state;
>>>>>>> 8573003 Adds moon-day icon set. Rebased.
                    default -> throw new IllegalArgumentException("Unexpected icon category: %s".formatted(category));
                };
                iconName = iconName.replace(".", "-%s.".formatted(iconState));
            } catch (IllegalArgumentException e) {
                logger.info("Error getting dynamic icon: {}", e.getMessage());
>>>>>>> 11e99dd Initial commit for Moon phase revamp
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
