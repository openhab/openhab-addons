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
package org.openhab.binding.avmfritz.internal.dto;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * See {@link AVMFritzBaseModel}.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "device")
public class DeviceModel extends AVMFritzBaseModel {

    private TemperatureModel temperature;
    private AlertModel alert;

    @XmlElement(name = "button", type = ButtonModel.class)
    private List<ButtonModel> buttons;

    private ETSUnitInfoModel etsiunitinfo;

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

    public List<ButtonModel> getButtons() {
        if (buttons == null) {
            return Collections.emptyList();
        }
        return buttons;
    }

    public void setButtons(List<ButtonModel> buttons) {
        this.buttons = buttons;
    }

    public ETSUnitInfoModel getEtsiunitinfo() {
        return etsiunitinfo;
    }

    public void setEtsiunitinfo(ETSUnitInfoModel etsiunitinfo) {
        this.etsiunitinfo = etsiunitinfo;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(temperature).append(",").append(alert).append(",")
                .append(getButtons()).append(",").append(etsiunitinfo).append("]").toString();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = { "etsideviceid", "unittype", "interfaces" })
    public static class ETSUnitInfoModel {
        public static final String HAN_FUN_UNITTYPE_SIMPLE_BUTTON = "273";
        public static final String HAN_FUN_UNITTYPE_SIMPLE_DETECTOR = "512";
        public static final String HAN_FUN_UNITTYPE_MAGNETIC_CONTACT = "513";
        public static final String HAN_FUN_UNITTYPE_OPTICAL_CONTACT = "514";
        public static final String HAN_FUN_UNITTYPE_MOTION_DETECTOR = "515";
        public static final String HAN_FUN_UNITTYPE_SMOKE_DETECTOR = "516";
        public static final String HAN_FUN_UNITTYPE_FLOOD_DETECTOR = "518";
        public static final String HAN_FUN_UNITTYPE_GLAS_BREAK_DETECTOR = "519";
        public static final String HAN_FUN_UNITTYPE_VIBRATION_DETECTOR = "520";

        public static final String HAN_FUN_INTERFACE_ALERT = "256";
        public static final String HAN_FUN_INTERFACE_KEEP_ALIVE = "277";
        public static final String HAN_FUN_INTERFACE_SIMPLE_BUTTON = "772";

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
            return new StringBuilder().append("[etsideviceid=").append(etsideviceid).append(",unittype=")
                    .append(unittype).append(",interfaces=").append(interfaces).append("]").toString();
        }
    }
}
