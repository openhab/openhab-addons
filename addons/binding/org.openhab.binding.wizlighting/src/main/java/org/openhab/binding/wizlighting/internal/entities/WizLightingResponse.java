/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wizlighting.internal.entities;

/**
 * This POJO represents one Wiz Lighting UDP Response
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class WizLightingResponse {

    private int id;
    private String method;
    private Param param;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Param getParam() {
        return param;
    }

    public void setParam(Param param) {
        this.param = param;
    }
}
