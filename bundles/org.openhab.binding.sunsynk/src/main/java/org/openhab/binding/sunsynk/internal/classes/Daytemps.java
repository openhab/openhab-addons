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
 * The minute following midnight returns an empty array
 * 
 * @author Lee Charlton - Initial contribution
 */

// {'infos': ['records': {'value': '12.3', 'value': '12.4'...} ],['records':{'value': '12.3', 'value': '12.4'...}]}

public class Daytemps {

    private Infos infos;

    class Infos {
        List<Record> records;
    }

    class Record {
        List<Double> value;
    }

    private String response_status = "okay";
    private double dc_temperature;
    private double ac_temperature;

    private void getLastValue() {
        try {
            Record dc_record = this.infos.records.get(0);
            Record ac_record = this.infos.records.get(1);
            this.dc_temperature = dc_record.value.get(dc_record.value.size() - 1);
            this.ac_temperature = dc_record.value.get(ac_record.value.size() - 1);
        } catch (Exception e) {
            this.response_status = "Failed to retrieve Inverter Temperature values";
        }
    }

    public Daytempsreturn InverterTemperatures() {
        getLastValue();
        return new Daytempsreturn(this.response_status, this.dc_temperature, this.ac_temperature);
    }
}
