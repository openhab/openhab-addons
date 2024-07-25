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
package org.openhab.binding.sunsynk.internal.api.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Daytemps} is the internal class for Inverter temperature history information
 * from a Sun Synk Connect Account.
 * The minute following midnight returns an empty array.
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class Daytemps {
    private int code;
    private String msg = "";
    private boolean success;
    private Data data = new Data();
    private boolean response_status = true;
    private double dc_temperature;
    private double ac_temperature;

    class Data {
        private @Nullable List<Infos> infos = new ArrayList<Infos>();
    }

    @SuppressWarnings("unused")
    class Infos {
        private String unit = "";
        private @Nullable List<Record> records = new ArrayList<Record>();
        private String id = "";
        private String label = "";
    }

    @SuppressWarnings("unused")
    class Record {
        private String time = "";
        private double value;
        private String updateTime = "";
    }

    public void getLastValue() {

        List<Infos> infos = this.data.infos;
        if (infos != null) { // Sometimes after midnight infos or records are empty
            if (!infos.isEmpty()) {
                List<Record> dc_record = infos.get(0).records;
                List<Record> ac_record = infos.get(1).records;
                if (dc_record != null && ac_record != null) {
                    this.dc_temperature = dc_record.get(dc_record.size() - 1).value;
                    this.ac_temperature = ac_record.get(ac_record.size() - 1).value;
                    return;
                }
            }
        }
        this.response_status = false;
        // do nothing leave dc_ and ac_ temperature values as they are.
    }

    public String toString() {
        return "Content [code=" + code + ", msg=" + msg + "sucess=" + success + ", data=" + data + "]";
    }

    public Daytempsreturn inverterTemperatures() {
        return new Daytempsreturn(this.response_status, this.dc_temperature, this.ac_temperature);
    }
}
