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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The {@link AbstractSnapshotDefinition}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractSnapshotDefinition implements SnapshotDefinition {

    protected Map<String, Object> otherInfo = new HashMap<>();

    @JsonAnySetter
    public void addOtherInfo(String propertyKey, Object value) {
        this.otherInfo.put(propertyKey, value);
    }

    @Nullable
    public Object getOtherInfo(String propertyKey) {
        return this.otherInfo.get(propertyKey);
    }

    private Map<String, Object> rawData = new HashMap<>();

    @JsonIgnore
    public Map<String, Object> getRawData() {
        return rawData;
    }

    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData;
    }
}
