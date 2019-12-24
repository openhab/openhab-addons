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

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link _5} implements CurrStatus data class.
 *
 * @author Roie Geron - Initial contribution
 */
public class _5 {

    @SerializedName("evts")
    @Expose
    private List<Integer> evts = null;
    @SerializedName("evtDescs")
    @Expose
    private List<Object> evtDescs = null;

    public List<Integer> getEvts() {
        return evts;
    }

    public void setEvts(List<Integer> evts) {
        this.evts = evts;
    }

    public List<Object> getEvtDescs() {
        return evtDescs;
    }

    public void setEvtDescs(List<Object> evtDescs) {
        this.evtDescs = evtDescs;
    }

}