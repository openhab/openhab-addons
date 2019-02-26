/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.ui.iconset.classic.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.ui.icon.AbstractResourceIconProvider;
import org.eclipse.smarthome.ui.icon.IconProvider;
import org.eclipse.smarthome.ui.icon.IconSet;
import org.eclipse.smarthome.ui.icon.IconSet.Format;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This icon provider provides the classic icons (dating from openHAB). They are packaged
 * within this bundle and served from there.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@Component
public class ClassicIconProvider extends AbstractResourceIconProvider implements IconProvider {

    private final Logger logger = LoggerFactory.getLogger(ClassicIconProvider.class);

    static final String ICONSET_ID = "classic";

    @Override
    public Set<IconSet> getIconSets(Locale locale) {
        Set<Format> formats = new HashSet<>(2);
        formats.add(Format.PNG);
        formats.add(Format.SVG);
        String label = i18nProvider.getText(context.getBundle(), "iconset.label", "Classic Icons", locale);
        String description = i18nProvider.getText(context.getBundle(), "iconset.description",
                "This is a modernized version of the original icon set of openHAB 1.", locale);
        IconSet iconSet = new IconSet(ICONSET_ID, label, description, formats);
        return Collections.singleton(iconSet);
    }

    @Override
    protected InputStream getResource(String iconSetId, String resourceName) {
        if (ClassicIconProvider.ICONSET_ID.equals(iconSetId)) {
            URL iconResource = context.getBundle().getEntry("icons/" + resourceName);
            try {
                return iconResource.openStream();
            } catch (IOException e) {
                logger.error("Failed to read icon '{}': {}", resourceName, e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected boolean hasResource(String iconSetId, String resourceName) {
        if (ClassicIconProvider.ICONSET_ID.equals(iconSetId)) {
            URL iconResource = context.getBundle().getEntry("icons/" + resourceName);
            return iconResource != null;
        } else {
            return false;
        }
    }

    @Override
    protected Integer getPriority() {
        return 0;
    }

    @Override
    @Reference
    protected void setTranslationProvider(TranslationProvider i18nProvider) {
        super.setTranslationProvider(i18nProvider);
    }

    @Override
    protected void unsetTranslationProvider(TranslationProvider i18nProvider) {
        super.unsetTranslationProvider(i18nProvider);
    }

}
