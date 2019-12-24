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
 * The {@link AlarmEvtTypes} implements CurrStatus data class.
 *
 * @author Roie Geron - Initial contribution
 */

public class AlarmEvtTypes {

    @SerializedName("5")
    @Expose
    private _5 _5;
    @SerializedName("7")
    @Expose
    private _7 _7;

    public _5 get5() {
        return _5;
    }

    public void set5(_5 _5) {
        this._5 = _5;
    }

    public _7 get7() {
        return _7;
    }

    public void set7(_7 _7) {
        this._7 = _7;
    }

}