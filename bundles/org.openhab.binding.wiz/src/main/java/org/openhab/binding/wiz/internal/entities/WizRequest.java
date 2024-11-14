/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wiz.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wiz.internal.enums.WizMethodType;

import com.google.gson.annotations.Expose;

/**
 * This POJO represents one WiZ UDP Request.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class WizRequest {
    @Expose
    private int id;

    @Expose
    private WizMethodType method;

    @Expose(serialize = false, deserialize = false)
    private String methodName;

    @Expose(deserialize = false)
    private @Nullable Param params;

    /**
     * Default constructor.
     *
     * @param type the {@link WizMethodType}
     * @param params {@link Param}
     */
    public WizRequest(final WizMethodType method, final @Nullable Param params) {
        this.method = method;
        this.methodName = method.getMethodName();
        this.params = params;
    }

    public @Nullable Param getParams() {
        return this.params;
    }

    public void setParams(final Param params) {
        this.params = params;
    }

    public WizMethodType getMethod() {
        return this.method;
    }

    public void setMethod(final WizMethodType method) {
        this.method = method;
        this.methodName = method.getMethodName();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
