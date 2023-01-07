/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.wwn.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the data needed to do a WWN update request back to Nest.
 *
 * @author David Bennett - Initial contribution
 */
public class WWNUpdateRequest {
    private final String updatePath;
    private final Map<String, Object> values;

    private WWNUpdateRequest(Builder builder) {
        this.updatePath = builder.basePath + builder.identifier;
        this.values = builder.values;
    }

    public String getUpdatePath() {
        return updatePath;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public static class Builder {
        private String basePath;
        private String identifier;
        private Map<String, Object> values = new HashMap<>();

        public Builder withBasePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public Builder withIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder withAdditionalValue(String field, Object value) {
            values.put(field, value);
            return this;
        }

        public WWNUpdateRequest build() {
            return new WWNUpdateRequest(this);
        }
    }
}
