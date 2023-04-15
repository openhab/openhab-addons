/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteoalerte.internal.handler.MeteoAlerteHandler;
import org.openhab.binding.meteoalerte.internal.json.ResponseFieldDTO.AlertLevel;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;
import org.openhab.core.ui.icon.IconProvider;
import org.openhab.core.ui.icon.IconSet;
import org.openhab.core.ui.icon.IconSet.Format;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = { IconProvider.class })
@NonNullByDefault
public class MeteoAlertIconProvider implements IconProvider {
    private static final Set<String> ICONS = Set.of(WAVE.replace("-", "_"), AVALANCHE, HEAT, FREEZE.replace("-", "_"),
            FLOOD, SNOW, STORM, RAIN.replace("-", "_"), WIND);
    private static final IconSet ICON_SET = new IconSet(BINDING_ID, "Météo Alerte Icons",
            "These are the icons describing weather events", Set.of(Format.SVG));

    private final Logger logger = LoggerFactory.getLogger(MeteoAlerteHandler.class);
    private final ClassLoader classLoader;

    @Activate
    public MeteoAlertIconProvider() {
        classLoader = Objects.requireNonNull(MeteoAlertIconProvider.class.getClassLoader());
    }

    @Override
    public Set<IconSet> getIconSets() {
        return Set.of(ICON_SET);
    }

    @Override
    public Set<IconSet> getIconSets(@Nullable Locale locale) {
        return getIconSets();
    }

    @Override
    public @Nullable Integer hasIcon(String category, String iconSetId, Format format) {
        return ICONS.contains(category) && format == Format.SVG ? 7 : null;
    }

    @Override
    public @Nullable InputStream getIcon(String category, String iconSetId, @Nullable String value, Format format) {
        String icon = getResource(category);

        if (icon.isEmpty()) {
            return null;
        }

        if (value != null) {
            try {
                State state = DecimalType.valueOf(value);

                AlertLevel alertLevel = ALERT_LEVELS.entrySet().stream().filter(entry -> entry.getValue().equals(state))
                        .findFirst().map(entry -> entry.getKey()).orElse(AlertLevel.UNKNOWN);

                icon = icon.replaceAll(UNKNOWN_COLOR, ALERT_COLORS.getOrDefault(alertLevel, UNKNOWN_COLOR));

            } catch (NumberFormatException e) {
                logger.debug("{} is not a valid DecimalType", value);
            }
        }

        return new ByteArrayInputStream(icon.getBytes());
    }

    private String getResource(String iconName) {
        String result = "";
        String iconPath = "picto/%s.svg".formatted(iconName);
        try (InputStream stream = classLoader.getResourceAsStream(iconPath)) {
            if (stream != null) {
                result = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.warn("Unable to load ressource '{}' : {}", iconPath, e.getMessage());
        }
        return result;
    }

}
