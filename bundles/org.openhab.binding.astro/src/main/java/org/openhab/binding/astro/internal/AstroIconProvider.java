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
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.astro.internal.model.EclipseKind;
import org.openhab.binding.astro.internal.model.MoonPhase;
import org.openhab.binding.astro.internal.model.SeasonName;
import org.openhab.binding.astro.internal.model.ZodiacSign;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
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
    private static final String MOON_DAY_SET = "moon_day";
    private static final String MOON_ECLIPSE_SET = "moon_eclipse";
    private static final String MOON_PHASE_SET = "moon_phase";
    private static final String SEASON_SET = "season";
    private static final String SUN_ECLIPSE_SET = "sun_eclipse";
    private static final String ZODIAC_SET = "zodiac";
    private static final Set<String> ICON_SETS = Set.of(MOON_DAY_SET, MOON_ECLIPSE_SET, MOON_PHASE_SET, SEASON_SET,
            SUN_ECLIPSE_SET, ZODIAC_SET);

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
        String set = category.equals(MOON_PHASE_SET) ? MOON_DAY_SET : category;
        String resourceWithoutState = "icon/" + set + "." + format.toString();
        if (state == null) {
            return getResource(resourceWithoutState);
        }

        try {
            String iconState = switch (category) {
                case SEASON_SET -> SeasonName.valueOf(state).name();
                case ZODIAC_SET -> ZodiacSign.valueOf(state).name();
                case MOON_ECLIPSE_SET, SUN_ECLIPSE_SET -> EclipseKind.valueOf(state).name();
                case MOON_PHASE_SET -> Integer.toString(MoonPhase.valueOf(state).getAgeDays());
                case MOON_DAY_SET -> {
                    try {
                        var age = QuantityType.valueOf(state);
                        if (age.toUnit(Units.DAY) instanceof QuantityType ageInDays) {
                            yield Integer.toString(ageInDays.intValue());
                        }
                    } catch (NumberFormatException ignore) {
                    }
                    throw new IllegalArgumentException("Unable to use state '%s' for '%s'".formatted(state, category));
                }
                default -> throw new IllegalArgumentException("Icon category '%s' not found".formatted(category));
            };
            String resourceWithState = "icon/" + set + "-" + iconState + "." + format.toString();
            return getResource(resourceWithState);
        } catch (IllegalArgumentException e) {
            logger.debug("Use icon {} as state {} is not found", resourceWithoutState, state);
            return getResource(resourceWithoutState);
        }
    }

    private @Nullable InputStream getResource(String iconName) {
        if (bundle.getEntry(iconName.toLowerCase(Locale.ROOT)) instanceof URL iconResource) {
            try (InputStream stream = iconResource.openStream()) {
                byte[] icon = stream.readAllBytes();
                return new ByteArrayInputStream(icon);
            } catch (IOException e) {
                logger.warn("Unable to load resource '{}': {}", iconResource.getPath(), e.getMessage());
            }
        }
        return null;
    }
}
