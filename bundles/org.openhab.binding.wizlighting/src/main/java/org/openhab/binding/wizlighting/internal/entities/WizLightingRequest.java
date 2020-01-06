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
package org.openhab.binding.wizlighting.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wizlighting.internal.enums.WizLightingMethodType;

import com.google.gson.annotations.Expose;

/**
 * This POJO represents one WiZ Lighting UDP Request.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class WizLightingRequest {
    @Expose(serialize = true, deserialize = true)
    private int id;
    @Expose(serialize = true, deserialize = true)
    private WizLightingMethodType method;
    @Expose(serialize = true, deserialize = true)
    private @Nullable Param params;

    /**
     * Default constructor.
     *
     * @param type the {@link WizLightingMethodType}
     * @param params {@link Param}
     */
    public WizLightingRequest(final WizLightingMethodType method, final @Nullable Param params) {
        this.method = method;
        this.params = params;
    }

    public @Nullable Param getParams() {
        return this.params;
    }

    public void setParams(final Param params) {
        this.params = params;
    }

    public WizLightingMethodType getMethod() {
        return this.method;
    }

    public void setMethod(final WizLightingMethodType method) {
        this.method = method;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
