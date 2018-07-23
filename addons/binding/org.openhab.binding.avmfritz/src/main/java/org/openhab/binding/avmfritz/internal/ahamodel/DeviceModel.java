/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link AVMFritzBaseModel}.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 */
@XmlType(name = "device")
public class DeviceModel extends AVMFritzBaseModel {

    private TemperatureModel temperature;
    private AlertModel alert;
    private ButtonModel button;
    private Etsiunitinfo etsiunitinfo;

    public TemperatureModel getTemperature() {
        return temperature;
    }

    public void setTemperature(TemperatureModel temperatureModel) {
        this.temperature = temperatureModel;
    }

    public AlertModel getAlert() {
        return alert;
    }

    public void setAlert(AlertModel alertModel) {
        this.alert = alertModel;
    }

    public ButtonModel getButton() {
        return button;
    }

    public void setButton(ButtonModel buttonModel) {
        this.button = buttonModel;
    }

    public Etsiunitinfo getEtsiunitinfo() {
        return etsiunitinfo;
    }

    public void setEtsiunitinfo(Etsiunitinfo etsiunitinfo) {
        this.etsiunitinfo = etsiunitinfo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(super.toString()).append(getTemperature()).append(getAlert())
                .append(getButton()).append(getEtsiunitinfo()).toString();
    }

    @XmlType(propOrder = { "etsideviceid", "unittype", "interfaces" })
    public static class Etsiunitinfo {
        public static final String HAN_FUN_MAGNETIC_CONTACT_UNITTYPE = "513";
        public static final String HAN_FUN_OPTICAL_CONTACT_UNITTYPE = "514";
        public static final String HAN_FUN_MOTION_SENSOR_UNITTYPE = "515";
        public static final String HAN_FUN_SMOKE_DETECTOR_UNITTYPE = "516";
        public static final String HAN_FUN_SWITCH_UNITTYPE = "273";

        private String etsideviceid;
        private String unittype;
        private String interfaces;

        public String getEtsideviceid() {
            return etsideviceid;
        }

        public void setEtsideviceid(String etsideviceid) {
            this.etsideviceid = etsideviceid;
        }

        public String getUnittype() {
            return unittype;
        }

        public void setUnittype(String unittype) {
            this.unittype = unittype;
        }

        public String getInterfaces() {
            return interfaces;
        }

        public void setInterfaces(String interfaces) {
            this.interfaces = interfaces;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("etsideviceid", getEtsideviceid()).append("unittype", getUnittype())
                    .append("interfaces", getInterfaces()).toString();
        }
    }
}
