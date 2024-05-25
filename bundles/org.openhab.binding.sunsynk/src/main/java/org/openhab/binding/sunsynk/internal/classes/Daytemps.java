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
package org.openhab.binding.sunsynk.internal.classes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Daytemps} is the internal class for Inverter temperature history information
 * from the sunsynk Account.
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
    private String response_status = "okay";
    private double dc_temperature;
    private double ac_temperature;

    class Data {
        private List<Infos> infos = new ArrayList<Infos>();
    }

    class Infos {
        private String unit = "";
        List<Record> records = new ArrayList<Record>();
        private String id = "";
        private String label = "";
    }

    class Record {
        private String time = "";
        private double value;
        private String updateTime = "";
    }

    public void getLastValue() {
        try {
            Infos dc_record = this.data.infos.get(0);
            Infos ac_record = this.data.infos.get(1);
            this.dc_temperature = dc_record.records.get(dc_record.records.size() - 1).value;
            this.ac_temperature = ac_record.records.get(ac_record.records.size() - 1).value;
        } catch (Exception e) {
            this.response_status = "Failed to retrieve Inverter Temperature values";
        }
    }

    public String toString() {
        return "Content [code=" + code + ", msg=" + msg + "sucess=" + success + ", data=" + data + "]";
    }

    public Daytempsreturn inverterTemperatures() {
        return new Daytempsreturn(this.response_status, this.dc_temperature, this.ac_temperature);
    }
}
