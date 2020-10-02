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

package org.openhab.binding.touchwand.internal.dto;

/**
 * The {@link Csc} implements Csc data class.
 *
 * @author Roie Geron - Initial contribution
 */
public class Csc {

    private int sceneNo;
    private int ts;
    private int keyAttr;

    public int getSceneNo() {
        return sceneNo;
    }

    public void setSceneNo(int sceneNo) {
        this.sceneNo = sceneNo;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public int getKeyAttr() {
        return keyAttr;
    }

    public void setKeyAttr(int keyAttr) {
        this.keyAttr = keyAttr;
    }
}
