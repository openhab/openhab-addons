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

/**
 * Composite container for all YAML Composer configuration domains.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public record YamlComposerSettings(YamlOutputConfig output
// Future domains (e.g. FolderFilterConfig filter) will be added here
) {
    public static YamlComposerSettings fromMap(Map<String, Object> configMap) {
        return new YamlComposerSettings(YamlOutputConfig.fromMap(configMap));
    }

    public static YamlComposerSettings defaultConfig() {
        return new YamlComposerSettings(YamlOutputConfig.defaultConfig());
    }
}
