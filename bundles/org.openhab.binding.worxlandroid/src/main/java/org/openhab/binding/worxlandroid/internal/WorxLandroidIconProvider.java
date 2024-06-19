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
package org.openhab.binding.worxlandroid.internal;

import static org.openhab.binding.worxlandroid.internal.WorxLandroidBindingConstants.BINDING_ID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * The {@link WorxLandroidIconProvider} is the class providing binding related icons.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = { IconProvider.class, WorxLandroidIconProvider.class })
@NonNullByDefault
public class WorxLandroidIconProvider implements IconProvider {
    private static final String DEFAULT_LABEL = "Worx Landroid Icons";
    private static final String DEFAULT_DESCRIPTION = "Icons illustrating channels provided by Worx Landroid binding.";

    private final Logger logger = LoggerFactory.getLogger(WorxLandroidIconProvider.class);
    private final BundleContext context;
    private final TranslationProvider i18nProvider;

    @Activate
    public WorxLandroidIconProvider(final BundleContext context, final @Reference TranslationProvider i18nProvider) {
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
        return iconSetId.equals(BINDING_ID) && format == Format.SVG ? 0 : null;
    }

    public @Nullable InputStream getIcon(String category, String state) {
        return getIcon(category, BINDING_ID, state, Format.SVG);
    }

    @Override
    public @Nullable InputStream getIcon(String category, String iconSetId, @Nullable String state, Format format) {
        String icon = getResource(category, true);
        if (icon.isEmpty()) {
            return null;
        }

        if (state != null) {
            String withState = "%s-%s".formatted(category, state.toString().toLowerCase());
            String iconWithState = getResource(withState, false);
            if (!iconWithState.isEmpty()) {
                icon = iconWithState;
            }
        }

        return new ByteArrayInputStream(icon.getBytes());
    }

    private String getResource(String iconName, boolean shouldExist) {
        String result = "";

        URL iconResource = context.getBundle().getEntry("icon/%s.svg".formatted(iconName));
        if (iconResource != null) {
            try (InputStream stream = iconResource.openStream()) {
                result = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.warn("Unable to load resource '{}': {}", iconResource.getPath(), e.getMessage());
            }
        } else if (shouldExist) {
            logger.warn("Unable to find icon '{}'", iconName);
        }
        return result;
    }
}
