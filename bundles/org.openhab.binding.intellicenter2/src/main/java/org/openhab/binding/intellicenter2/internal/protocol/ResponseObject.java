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
package org.openhab.binding.intellicenter2.internal.protocol;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@SuppressWarnings("PMD.ForbiddenPackageUsageCheck")
@NonNullByDefault
public class ResponseObject {

    @SerializedName("objnam")
    private String objectName = "";
    private Map<Attribute, Object> params = Map.of();

    protected ResponseObject() {
    }

    private ResponseObject(Map<?, ?> m) {
        this.objectName = requireNonNull((String) m.get("objnam"));
        @SuppressWarnings("unchecked")
        var inputParams = (Map<String, Object>) m.get("params");
        this.params = new HashMap<>();
        inputParams.forEach((k, v) -> {
            try {
                params.put(Attribute.valueOf(k), v);
            } catch (IllegalArgumentException e) {
                System.err.println("No enum for '" + k + "'");
            }
        });
    }

    public ResponseObject(@Nullable ResponseObject response) {
        if (response != null) {
            this.objectName = response.getObjectName();
            this.params = response.getParams();
        }
    }

    public String getObjectName() {
        return objectName;
    }

    public Map<Attribute, Object> getParams() {
        return params;
    }

    public String getValueAsString(Attribute key) {
        return (String) Objects.requireNonNull(params.get(key));
    }

    public Optional<String> getOptionalValueAsString(Attribute key) {
        return Optional.ofNullable((String) params.get(key));
    }

    public int getValueAsInt(Attribute key) {
        return Integer.parseInt(getValueAsString(key));
    }

    public boolean getValueAsBoolean(Attribute key) {
        return "ON".equals(getValueAsString(key));
    }

    public List<ResponseObject> getValueAsResponseObjects(Attribute key) {
        @SuppressWarnings("unchecked")
        final List<Map<?, ?>> list = (List<Map<?, ?>>) params.get(key);
        final var responses = new ArrayList<ResponseObject>(list.size());
        list.forEach(m -> {
            responses.add(new ResponseObject(m));
        });
        return responses;
    }

    public <@NonNull T extends Enum<?>> T getValueAsEnum(Attribute a, Class<T> enumClass) {
        final Object value = requireNonNull(params.get(a));
        return Objects.requireNonNull(ICProtocol.GSON.fromJson(value.toString(), enumClass));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("objectName", objectName).append("params", params).toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        ResponseObject that = (ResponseObject) o;
        return new EqualsBuilder().append(objectName, that.objectName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(objectName).toHashCode();
    }
}
