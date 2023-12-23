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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class RequestObject {

    @SerializedName("objnam")
    private final String objectName;
    @Nullable
    private final Collection<Attribute> keys;
    @Nullable
    private final Map<Attribute, String> params;

    public RequestObject(final String objname, final Collection<Attribute> keys) {
        this.objectName = requireNonNull(objname);
        this.keys = Collections.unmodifiableCollection(keys);
        this.params = null;
    }

    public RequestObject(final String objname, final Attribute... keys) {
        this(objname, List.of(keys));
    }

    public RequestObject(final String objectName, Map<Attribute, String> params) {
        this.objectName = objectName;
        this.params = new HashMap<>(params);
        this.keys = null;
    }

    @SerializedName("objnam")
    public String getObjectName() {
        return objectName;
    }
}
