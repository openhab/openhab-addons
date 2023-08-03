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
package org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.application;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link IntentDto}
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class ExtrasDto {

    @JsonProperty
    private String query;

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "Extras{" + "query = '" + query + '\'' + "}";
    }
}
