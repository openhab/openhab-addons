/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.providers.models;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.StateDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.Expose;

/**
 * Defines a thing state definition. This class will be used to serialize any state description from the underlying
 * thing
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SonyThingStateDefinition {
    /** The logger */
    @Expose(serialize = false, deserialize = false)
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /** The stepping */
    private @Nullable BigDecimal step;

    /** The minimum */
    private @Nullable BigDecimal minimum;

    /** The maximum */
    private @Nullable BigDecimal maximum;

    /** Any pattern to apply */
    private @Nullable String pattern;

    /** Whether it is readonly or not */
    private @Nullable Boolean readonly;

    /** The channel options */
    private @Nullable Map<@Nullable String, @Nullable String> options;

    /**
     * Empty constructor used for deserialization
     */
    public SonyThingStateDefinition() {
    }

    /**
     * Constructs the thing state definition from the state description
     *
     * @param desc a possibly null state description
     */
    public SonyThingStateDefinition(final @Nullable StateDescription desc) {
        this.maximum = desc == null ? null : desc.getMaximum();
        this.minimum = desc == null ? null : desc.getMinimum();
        this.step = desc == null ? null : desc.getStep();
        this.pattern = desc == null ? null : desc.getPattern();
        this.readonly = desc == null ? null : desc.isReadOnly();

        this.options = new HashMap<>();
        if (desc != null) {
            desc.getOptions().stream().forEach(so -> {
                final String key = so.getValue();
                final String val = so.getLabel();
                if (this.options.containsKey(key)) {
                    // argh - stupid device has multiple values for the same key (bug on device)
                    logger.trace("Multiple values for key: {} - ignoring {}", key, val);
                } else {
                    this.options.put(key, val);
                }
            });
        }
    }

    @Override
    public String toString() {
        return "SonyThingStateDefinition [maximum=" + maximum + ", minimum=" + minimum + ", step=" + step + ", pattern="
                + pattern + ", readonly=" + readonly + ", options=" + options + "]";
    }
}
