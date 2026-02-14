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
package org.openhab.binding.restify.internal;

import static org.openhab.binding.restify.internal.RestifyBindingConstants.CONFIGURATION_PID;

import java.io.Serial;
import java.io.Serializable;
import java.util.Dictionary;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RestifyBinding} stores global binding configuration and exposes it to other
 * binding components.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = CONFIGURATION_PID, service = { RestifyBinding.class, ManagedService.class })
public class RestifyBinding implements ManagedService, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(RestifyBinding.class);
    private volatile RestifyBindingConfig config = RestifyBindingConfig.DEFAULT;

    @Override
    @NonNullByDefault({})
    public void updated(Dictionary<String, ?> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }

        this.config = new RestifyBindingConfig(getBoolean(properties, "enforceAuthentication", false),
                getNullableString(properties, "defaultBasic"), getNullableString(properties, "defaultBearer"));
        logger.debug("Loaded configuration: {}", config);
    }

    public RestifyBindingConfig getConfig() {
        return config;
    }

    private static boolean getBoolean(Dictionary<String, ?> properties, String key, boolean defaultValue) {
        var value = properties.get(key);
        return switch (value) {
            case Boolean bool -> bool;
            case String text -> Boolean.parseBoolean(text);
            default -> defaultValue;
        };
    }

    private static @Nullable String getNullableString(Dictionary<String, ?> properties, String key) {
        var value = properties.get(key);
        return switch (value) {
            case String text -> {
                var trimmed = text.trim();
                yield trimmed.isEmpty() ? null : trimmed;
            }
            default -> null;
        };
    }
}
