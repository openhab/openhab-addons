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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents a general setting request. Please note the API supports setting multiple values at a time but
 * we limit this to one setting at a time
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class GeneralSettingsRequest {
    /** The list of settings to set */
    private final List<Setting> settings;

    /**
     * Constructs the setting request from the target/value/uri
     * 
     * @param target a non-null, non-empty target
     * @param value a non-null, possibly empty value
     * @param uri a possibly null, possibly empty URI target
     */
    public GeneralSettingsRequest(final String target, final String value, final @Nullable String uri) {
        Validate.notEmpty(target, "target cannot be empty");
        Objects.requireNonNull(value, "value cannot be null");
        settings = Collections.singletonList(new Setting(target, value, uri));
    }

    @Override
    public String toString() {
        return "GeneralSettingsRequest [settings=" + settings + "]";
    }

    /**
     * Represents the structure of an individual setting
     */
    @NonNullByDefault
    private class Setting {
        /** The setting target */
        private final String target;

        /** the setting value */
        private final String value;

        /** the setting uri */
        private final @Nullable String uri;

        /**
         * Constructs the setting from the target/value
         * 
         * @param target a non-null, non-empty target
         * @param value a non-null, possibly empty value
         * @param uri a possibly null uri
         */
        private Setting(final String target, final String value, final @Nullable String uri) {
            Validate.notEmpty(target, "target cannot be empty");
            Objects.requireNonNull(value, "value cannot be null");
            this.target = target;
            this.value = value;
            this.uri = uri;
        }

        @Override
        public String toString() {
            return "Setting [target=" + target + ", uri=" + uri + ", value=" + value + "]";
        }
    }
}
