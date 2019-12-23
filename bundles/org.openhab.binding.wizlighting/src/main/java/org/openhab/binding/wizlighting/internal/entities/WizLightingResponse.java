/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

/**
 * This POJO represents one Wiz Lighting UDP Response
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class WizLightingResponse {

    private int id;
    private @Nullable String method;
    private @Nullable Param param;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public @Nullable String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public @Nullable Param getParam() {
        return param;
    }

    public void setParam(Param param) {
        this.param = param;
    }
}
