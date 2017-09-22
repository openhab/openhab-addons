/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wizlighting.internal.entities;

import org.openhab.binding.wizlighting.internal.enums.WizLightingMethodType;

/**
 * This POJO represents one Wiz Lighting UDP Request.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class WizLightingRequest {
    private int id;

    private WizLightingMethodType method;
    private Param params;

    /**
     * Default constructor.
     *
     * @param type the {@link WizLightingMethodType}
     * @param params {@link Param}
     */
    public WizLightingRequest(final WizLightingMethodType method, final Param params) {
        this.method = method;
        this.params = params;
    }

    public Param getParams() {
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
