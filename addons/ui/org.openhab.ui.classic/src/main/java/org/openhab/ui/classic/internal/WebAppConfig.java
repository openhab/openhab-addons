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
package org.openhab.ui.classic.internal;

import java.util.Map;

/**
 * This class holds Classic UI configuration values
 * and validates newly applied values.
 *
 * @author Vlad Ivanov - Initial contribution
 * @author Kai Kreuzer - adapted to Classic UI
 */
public class WebAppConfig {

    private static final String DEFAULT_SITEMAP = "default";
    private static final String DEFAULT_ICON_TYPE = "png";
    private static final boolean DEFAULT_HTML_CACHE_DISABLED = false;

    private String defaultSitemap = DEFAULT_SITEMAP;
    private String iconType = DEFAULT_ICON_TYPE;
    private boolean htmlCacheDisabled = DEFAULT_HTML_CACHE_DISABLED;

    public void applyConfig(Map<String, Object> configProps) {
        String configIconType = (String) configProps.get("iconType");
        String configDefaultSitemap = (String) configProps.get("defaultSitemap");
        Object configHtmlCacheDisabled = configProps.get("disableHtmlCache");

        if (configDefaultSitemap == null) {
            configDefaultSitemap = DEFAULT_SITEMAP;
        }

        if (configIconType == null) {
            configIconType = DEFAULT_ICON_TYPE;
        } else if (!configIconType.equalsIgnoreCase("svg") && !configIconType.equalsIgnoreCase("png")) {
            configIconType = DEFAULT_ICON_TYPE;
        }

        if (configHtmlCacheDisabled != null && "true".equalsIgnoreCase(configHtmlCacheDisabled.toString())) {
            htmlCacheDisabled = true;
        }

        iconType = configIconType;
        defaultSitemap = configDefaultSitemap;
    }

    public String getDefaultSitemap() {
        return defaultSitemap;
    }

    public String getIconType() {
        return iconType;
    }

    public boolean isHtmlCacheDisabled() {
        return htmlCacheDisabled;
    }
}
