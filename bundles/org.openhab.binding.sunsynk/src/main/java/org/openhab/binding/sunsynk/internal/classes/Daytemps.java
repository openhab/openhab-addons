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

import java.util.List;

/**
 * The {@link Daytemps} is the internal class for Inverter temperature history information
 * from the sunsynk Account.
 * The minute following midnight returns an empty array.
 * 
 * @author Lee Charlton - Initial contribution
 */

public class Daytemps {
    private int code;
    private String msg;
    private Data data;
    private String response_status = "okay";
    private double dc_temperature;
    private double ac_temperature;

    class Data {
        private List<Infos> infos;
    }

    class Infos {
        private String unit;
        List<Record> records;
        private String id;
        private String label;
    }

    class Record {
        private String time;
        private double value;
        private String updateTime;
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

    public Daytempsreturn inverterTemperatures() {
        // getLastValue();
        return new Daytempsreturn(this.response_status, this.dc_temperature, this.ac_temperature);
    }
}
/*
 * "{"code":0,"msg":"Success","data":{"infos":[
 * {"unit":"℃","records":[{"time":"2024-03-22 00:00:40","value":"36.6","updateTime":null},
 * {"time":"2024-03-22 00:01:46","value":"36.6","updateTime":null},
 * {"time":"2024-03-22 00:02:53","value":"36.6","updateTime":null},
 * ...
 * {"time":"2024-03-22 15:24:24","value":"41.1","updateTime":null},
 * {"time":"2024-03-22 15:25:31","value":"41.0","updateTime":null}],"id":76,"label":"DC TEMP"},
 * 
 * {"unit":"℃","records":[{"time":"2024-03-22 00:00:40","value":"29.1","updateTime":null},
 * {"time":"2024-03-22 00:01:46","value":"29.1","updateTime":null},
 * {"time":"2024-03-22 00:02:53","value":"29.1","updateTime":null},
 * {"time":"2024-03-22 00:03:59","value":"29.1","updateTime":null},
 * ...
 * {"time":"2024-03-22 15:24:24","value":"33.3","updateTime":null},
 * {"time":"2024-03-22 15:25:31","value":"33.2","updateTime":null}],"id":77,"label":"AC TEMP"}]},"success":true}"
 */
