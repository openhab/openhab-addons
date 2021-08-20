/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.airquality.internal.icon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.ui.icon.AbstractResourceIconProvider;
import org.openhab.core.ui.icon.IconProvider;
import org.openhab.core.ui.icon.IconSet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The custom icon provider supports custom icons for AirQuality binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(immediate = false, service = { IconProvider.class })
@NonNullByDefault
public class AirQualityIconProvider extends AbstractResourceIconProvider {
    private final Logger logger = LoggerFactory.getLogger(AirQualityIconProvider.class);
    private final @NonNullByDefault({}) ClassLoader classLoader = AirQualityIconProvider.class.getClassLoader();

    @Activate
    public AirQualityIconProvider(final @Reference TranslationProvider i18nProvider) {
        super(i18nProvider);
    }

    @Override
    protected @Nullable InputStream getResource(String iconSetId, String resourceName) {
        String name = String.format("icons/%s", resourceName);
        try (InputStream stream = classLoader.getResourceAsStream(name)) {
            if (stream != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                stream.transferTo(baos);
                return new ByteArrayInputStream(baos.toByteArray());
            }
        } catch (IOException e) {
            logger.warn("Unable to load ressource '{}' : {}", name, e.getMessage());
        }
        return null;
    }

    @Override
    protected boolean hasResource(String iconSetId, String resourceName) {
        return classLoader.getResource(String.format("icons/%s", resourceName)) != null;
    }

    @Override
    public Set<IconSet> getIconSets(@Nullable Locale locale) {
        return super.getIconSets();
    }

    @Override
    protected Integer getPriority() {
        return 3;
    }
}
