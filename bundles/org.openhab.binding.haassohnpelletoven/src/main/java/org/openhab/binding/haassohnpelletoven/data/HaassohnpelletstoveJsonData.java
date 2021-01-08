/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.haassohnpelletoven.data;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link HaassohnpelletstoveJsonData} is the Java class used to map the JSON
 * response to a Oven request.
 *
 * @author Christian Feininger - Initial contribution
 */
public class HaassohnpelletstoveJsonData {
    metadata meta;
    boolean prg;
    boolean wprg;
    String mode;
    String sp_temp;
    String is_temp;
    String ht_char;
    wprogram[] weekprogram;
    err[] error;
    boolean eco_mode;
    boolean pgi;
    String ignitions;
    String on_time;
    String consumption;
    String maintenance_in;
    String cleaning_in;

    /***
     * Get the nonce
     *
     * @return nonce
     */
    public String getNonce() {
        return this.meta.Getnonce();
    }

    /**
     * Returns the is Temperature of the Oven
     *
     * @return
     */
    public String getisTemp() {
        return is_temp;
    }

    /***
     * JSON response
     *
     * @return JSON response as object
     */
    public HaassohnpelletstoveJsonData getResponse() {
        return this;
    }

    public class metadata {
        String sw_version;
        String hw_version;
        String bootl_version;
        String wifi_sw_version;
        String wifi_bootl_version;
        String sn;
        String typ;
        String language;
        String nonce;
        String eco_editable;
        String ts;
        String ean;
        boolean rau;
        String[] wlan_features;

        public String Getnonce() {
            return nonce;
        }
    }

    public class err {
        String time;
        String nr;
    }

    public class wprogram {
        String day;
        String begin;
        String end;
        String temp;
    }

    public @Nullable String getMode() {
        return mode;
    }

    public @Nullable String getspTemp() {
        return sp_temp;
    }

    public boolean getPrg() {
        return prg;
    }
}
