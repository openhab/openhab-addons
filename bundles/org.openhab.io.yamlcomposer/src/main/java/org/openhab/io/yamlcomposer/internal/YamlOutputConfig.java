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
package org.openhab.io.yamlcomposer.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;

/**
 * YamlOutputConfig holds the configuration options for YAML output.
 *
 * @param maxLineWidth the maximum line width before text wrapping occurs
 * @param splitLines whether to split long lines that exceed the maximum line width
 * @param sectionSpacing the number of blank lines to insert between sections
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public record YamlOutputConfig(int maxLineWidth, boolean splitLines, int sectionSpacing) {

    // Sane, non-configurable defaults for openHAB YAML
    public static final FlowStyle DEFAULT_FLOW_STYLE = FlowStyle.BLOCK;
    public static final ScalarStyle DEFAULT_SCALAR_STYLE = ScalarStyle.PLAIN;
    public static final int DEFAULT_INDENT = 2;
    public static final int DEFAULT_INDICATOR_INDENT = 2;
    public static final boolean DEFAULT_INDENT_WITH_INDICATOR = true;

    public static YamlOutputConfig fromMap(Map<String, Object> config) {
        int maxLineWidth = parseInt(config.get("maxLineWidth"), 80, 20);
        boolean splitLines = parseBoolean(config.get("splitLines"), true);
        int sectionSpacing = parseInt(config.get("sectionSpacing"), 1, 0);

        return new YamlOutputConfig(maxLineWidth, splitLines, sectionSpacing);
    }

    private static int parseInt(@Nullable Object val, int defaultValue, int minVal) {
        if (val instanceof Number n) {
            return Math.max(minVal, n.intValue());
        } else if (val instanceof String s) {
            try {
                return Math.max(minVal, Integer.parseInt(s.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static boolean parseBoolean(@Nullable Object val, boolean defaultValue) {
        if (val instanceof Boolean b) {
            return b;
        } else if (val instanceof String s) {
            return Boolean.parseBoolean(s.trim());
        }
        return defaultValue;
    }

    public static YamlOutputConfig defaultConfig() {
        return fromMap(Map.of());
    }
}
