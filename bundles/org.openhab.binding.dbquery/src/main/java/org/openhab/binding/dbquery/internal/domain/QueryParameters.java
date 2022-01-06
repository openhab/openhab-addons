/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.dbquery.internal.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Query parameters
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class QueryParameters {
    public static final QueryParameters EMPTY = new QueryParameters(Collections.emptyMap());
    private final Map<String, @Nullable Object> params;

    private QueryParameters() {
        this.params = new HashMap<>();
    }

    public QueryParameters(Map<String, @Nullable Object> params) {
        this.params = params;
    }

    public void setParameter(String name, @Nullable Object value) {
        params.put(name, value);
    }

    public @Nullable Object getParameter(String paramName) {
        return params.get(paramName);
    }

    public Map<String, @Nullable Object> getAll() {
        return Collections.unmodifiableMap(params);
    }

    public int size() {
        return params.size();
    }
}
