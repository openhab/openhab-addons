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

package org.openhab.binding.touchwand.internal.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link Csc} implements Csc data class.
 *
 * @author Roie Geron - Initial contribution
 */
public class Csc {

    @SerializedName("sceneNo")
    @Expose
    private Integer sceneNo;
    @SerializedName("ts")
    @Expose
    private Integer ts;
    @SerializedName("keyAttr")
    @Expose
    private Integer keyAttr;

    public Integer getSceneNo() {
        return sceneNo;
    }

    public void setSceneNo(Integer sceneNo) {
        this.sceneNo = sceneNo;
    }

    public Integer getTs() {
        return ts;
    }

    public void setTs(Integer ts) {
        this.ts = ts;
    }

    public Integer getKeyAttr() {
        return keyAttr;
    }

    public void setKeyAttr(Integer keyAttr) {
        this.keyAttr = keyAttr;
    }

}