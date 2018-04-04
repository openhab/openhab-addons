/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.rest;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the data needed to do an update request back to Nest.
 *
 * @author David Bennett - Initial Contribution
 */
public class NestUpdateRequest {
    private final String updateUrl;
    private final Map<String, Object> values;

    private NestUpdateRequest(Builder builder) {
        this.updateUrl = builder.baseUrl + builder.identifier;
        this.values = builder.values;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public static class Builder {
        private String baseUrl;
        private String identifier;
        private Map<String, Object> values = new HashMap<>();

        public Builder withBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
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

        public NestUpdateRequest build() {
            return new NestUpdateRequest(this);
        }
    }
}
