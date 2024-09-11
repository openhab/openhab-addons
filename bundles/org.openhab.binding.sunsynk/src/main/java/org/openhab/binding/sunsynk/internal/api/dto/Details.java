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

/**
 * The {@link Details} is the internal class for account detail information from a
 * Sun Synk Connect Account.
 *
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
@SuppressWarnings("unused")
public class Details {
    private int code;
    private String msg = "";
    private boolean success;
    private Inverterdata data = new Inverterdata();

    public Details() {
    }

    public List<Inverterdata.InverterInfo> getInverters() {
        return this.data.getInverters();
    }

    public ArrayList<String> getInverterUIDList() { // not used
        ArrayList<String> inverters = new ArrayList<String>();
        for (Details.Inverterdata.InverterInfo inv : getInverters()) {
            String uid = inv.getgsn() + inv.getsn();
            inverters.add(uid);
        }
        return inverters;
    }

    public ArrayList<Inverter> getInverters(String accessToken) {
        ArrayList<Inverter> inverters = new ArrayList<>();
        for (Details.Inverterdata.InverterInfo inv : getInverters()) {
            Inverter temp = new Inverter();
            String serialNo = inv.getsn();
            String gateSerialNo = inv.getgsn();
            temp.setGateSerialNo(gateSerialNo);
            temp.setSerialNo(serialNo);
            temp.setUID(gateSerialNo + serialNo);
            temp.setToken(accessToken);
            temp.setAlias(inv.getAlias());
            temp.setID(inv.getID());
            inverters.add(temp);
        }
        return inverters;
    }

    class Inverterdata {
        private int pageSize;
        private int pageNumber;
        private int total;
        private List<InverterInfo> infos = new ArrayList<InverterInfo>();

        public List<InverterInfo> getInverters() {
            return this.infos;
        }

        class InverterInfo {
            private int id;
            private String sn = "";
            private String alias = "";
            private String gsn = "";
            private int status;
            private int type;
            private String commTypeName = "";
            private String custCode = "";
            private Version version = new Version();
            private String model = "";
            private String equipMode = "";
            private int pac;
            private double etoday;
            private double etotal;
            private String updateAt = "";
            private int opened;
            private Plant plant = new Plant();

            public String getgsn() {
                return this.gsn;
            }

            public String getsn() {
                return sn;
            }

            public String getAlias() {
                return alias;
            }

            public String getID() {
                return String.valueOf(id);
            }

            public double getetoday() {
                return this.etoday;
            }

            public double getetotal() {
                return this.etotal;
            }

            class Plant {
                private int id;
                private String name = "";
                private int type;
                private String master = "";
                private String installer = "";
                private String email = "";
                private String phone = "";
                private GatewayVO gatewayVO = new GatewayVO();
                private boolean sunsynkEquip;
                private int protocolIdentifier;
                private int equipType;

                class GatewayVO {
                    private String gsn = "";
                    private int status;
                }
            }

            class Version {
                private String masterVer = "";
                private String softVer = "";
                private String hardVer = "";
                private String bmsVer = "";
            }
        }
    }
}
