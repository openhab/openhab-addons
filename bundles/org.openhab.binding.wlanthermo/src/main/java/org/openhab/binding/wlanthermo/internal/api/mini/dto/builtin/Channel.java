/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.wlanthermo.internal.api.mini.dto.builtin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Channel {

    @SerializedName("0")
    @Expose
    private Data _0;
    @SerializedName("1")
    @Expose
    private Data _1;
    @SerializedName("2")
    @Expose
    private Data _2;
    @SerializedName("3")
    @Expose
    private Data _3;
    @SerializedName("4")
    @Expose
    private Data _4;
    @SerializedName("5")
    @Expose
    private Data _5;
    @SerializedName("6")
    @Expose
    private Data _6;
    @SerializedName("7")
    @Expose
    private Data _7;
    @SerializedName("8")
    @Expose
    private Data _8;
    @SerializedName("9")
    @Expose
    private Data _9;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Channel() {
    }

    /**
     * 
     * @param _0
     * @param _1
     * @param _2
     * @param _3
     * @param _4
     * @param _5
     * @param _6
     * @param _7
     * @param _8
     * @param _9
     */
    public Channel(Data _0, Data _1, Data _2, Data _3, Data _4, Data _5, Data _6, Data _7, Data _8, Data _9) {
        this._0 = _0;
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
        this._4 = _4;
        this._5 = _5;
        this._6 = _6;
        this._7 = _7;
        this._8 = _8;
        this._9 = _9;
    }

    public Data get0() {
        return _0;
    }

    public void set0(Data _0) {
        this._0 = _0;
    }

    public Channel with0(Data _0) {
        this._0 = _0;
        return this;
    }

    public Data get1() {
        return _1;
    }

    public void set1(Data _1) {
        this._1 = _1;
    }

    public Channel with1(Data _1) {
        this._1 = _1;
        return this;
    }

    public Data get2() {
        return _2;
    }

    public void set2(Data _2) {
        this._2 = _2;
    }

    public Channel with2(Data _2) {
        this._2 = _2;
        return this;
    }

    public Data get3() {
        return _3;
    }

    public void set3(Data _3) {
        this._3 = _3;
    }

    public Channel with3(Data _3) {
        this._3 = _3;
        return this;
    }

    public Data get4() {
        return _4;
    }

    public void set4(Data _4) {
        this._4 = _4;
    }

    public Channel with4(Data _4) {
        this._4 = _4;
        return this;
    }

    public Data get5() {
        return _5;
    }

    public void set5(Data _5) {
        this._5 = _5;
    }

    public Channel with5(Data _5) {
        this._5 = _5;
        return this;
    }

    public Data get6() {
        return _6;
    }

    public void set6(Data _6) {
        this._6 = _6;
    }

    public Channel with6(Data _6) {
        this._6 = _6;
        return this;
    }

    public Data get7() {
        return _7;
    }

    public void set7(Data _7) {
        this._7 = _7;
    }

    public Channel with7(Data _7) {
        this._7 = _7;
        return this;
    }

    public Data get8() {
        return _8;
    }

    public void set8(Data _8) {
        this._8 = _8;
    }

    public Channel with8(Data _8) {
        this._8 = _8;
        return this;
    }

    public Data get9() {
        return _9;
    }

    public void set9(Data _9) {
        this._9 = _9;
    }

    public Channel with9(Data _9) {
        this._9 = _9;
        return this;
    }

    public Data getData(int i) {
        switch (i) {
            case 0:
                return get0();
            case 1:
                return get1();
            case 2:
                return get2();
            case 3:
                return get3();
            case 4:
                return get4();
            case 5:
                return get5();
            case 6:
                return get6();
            case 7:
                return get7();
            case 8:
                return get8();
            case 9:
                return get9();
            default:
                return null;
        }
    }
}
