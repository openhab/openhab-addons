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
package org.openhab.ui.basic.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds BasicUI configuration values
 * and validates newly applied values.
 *
 * @author Vlad Ivanov - Initial contribution
 */
public class WebAppConfig {
    private static final String DEFAULT_SITEMAP = "default";
    private static final String DEFAULT_ICON_TYPE = "png";

    public static final String THEME_NAME_DEFAULT = "default";
    public static final String THEME_NAME_DARK = "dark";
    private static final String DEFAULT_THEME = THEME_NAME_DEFAULT;

    private String defaultSitemap = DEFAULT_SITEMAP;
    private String iconType = DEFAULT_ICON_TYPE;
    private String theme = DEFAULT_THEME;

    private List<String> cssClassList = new ArrayList<String>();

    private static final Map<String, String> CSS_CLASSES;
    private static final Map<String, Boolean> CSS_DEFAULT_VALUES;

    private static final String CONFIG_ENABLE_ICONS = "enableIcons";
    private static final String CONFIG_CONDENSED_LAYOUT = "condensedLayout";
    private static final String CONFIG_CAPITALIZE = "capitalizeValues";

    static {
        CSS_CLASSES = new HashMap<String, String>();
        CSS_CLASSES.put(CONFIG_ENABLE_ICONS, "ui-icons-enabled");
        CSS_CLASSES.put(CONFIG_CONDENSED_LAYOUT, "ui-layout-condensed");
        CSS_CLASSES.put(CONFIG_CAPITALIZE, "ui-capitalize-values");

        CSS_DEFAULT_VALUES = new HashMap<String, Boolean>();
        CSS_DEFAULT_VALUES.put(CONFIG_ENABLE_ICONS, true);
        CSS_DEFAULT_VALUES.put(CONFIG_CONDENSED_LAYOUT, false);
        CSS_DEFAULT_VALUES.put(CONFIG_CAPITALIZE, false);
    }

    private void applyCssClasses(Map<String, Object> configProps) {
        cssClassList.clear();

        for (String key : CSS_CLASSES.keySet()) {
            Boolean value = CSS_DEFAULT_VALUES.get(key);
            if (configProps.containsKey(key)) {
                value = configProps.get(key).toString().equalsIgnoreCase("true");
            }
            if (value) {
                cssClassList.add(CSS_CLASSES.get(key));
            }
        }
    }

    public void applyConfig(Map<String, Object> configProps) {
        String configDefaultSitemap = (String) configProps.get("defaultSitemap");
        String configIconType = (String) configProps.get("iconType");
        String configTheme = (String) configProps.get("theme");

        if (configDefaultSitemap == null) {
            configDefaultSitemap = DEFAULT_SITEMAP;
        }

        if (configIconType == null) {
            configIconType = DEFAULT_ICON_TYPE;
        } else if (!configIconType.equalsIgnoreCase("svg") && !configIconType.equalsIgnoreCase("png")) {
            configIconType = DEFAULT_ICON_TYPE;
        }

        if (configTheme == null) {
            configTheme = DEFAULT_THEME;
        }

        defaultSitemap = configDefaultSitemap;
        iconType = configIconType;
        theme = configTheme;

        applyCssClasses(configProps);
    }

    public String getDefaultSitemap() {
        return defaultSitemap;
    }

    public String getIconType() {
        return iconType;
    }

    public String getTheme() {
        return theme;
    }

    public String getCssClassList() {
        String result = " ";
        for (String item : cssClassList) {
            result += item + " ";
        }
        return result;
    }
}
