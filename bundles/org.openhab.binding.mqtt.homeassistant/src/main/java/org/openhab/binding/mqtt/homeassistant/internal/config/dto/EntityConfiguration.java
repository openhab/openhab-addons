/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.mqtt.homeassistant.internal.config.dto;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;

/**
 * Base class for home assistant configurations.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public abstract class EntityConfiguration extends AbstractComponentConfiguration {
    protected EntityConfiguration(Map<String, @Nullable Object> config, String defaultName) {
        super(config, defaultName);
    }

    @Nullable
    public String getJsonAttributesTopic() {
        return getOptionalString("json_attributes_topic");
    }

    @Nullable
    public Value getJsonAttributesTemplate() {
        return getOptionalValue("json_attributes_template");
    }
}
