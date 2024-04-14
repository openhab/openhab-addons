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

/**
 * The {@link Client} is the internal class for Client information from the sunsynk Account.
 *
 * @author Lee Charlton - Initial contribution
 */

public class Details {

    // public class AccountDetails{
    private int code;
    private String msg;
    private boolean success;
    private Inverterdata data;

    public Details() {
    }

    public List<Inverterdata.InverterInfo> getInverters() {
        return this.data.getInverters();
    }

    public ArrayList<String> getInverterUIDList() { // not used
        ArrayList<String> inverters = new ArrayList<String>();
        for (Details.Inverterdata.InverterInfo Inv : getInverters()) {
            String UID = Inv.getgsn() + Inv.getsn();
            inverters.add(UID);
        }
        return inverters;
    }

    public ArrayList<Inverter> getInverters(String accessToken) {
        ArrayList<Inverter> inverters = new ArrayList<>();
        for (Details.Inverterdata.InverterInfo Inv : getInverters()) {
            Inverter temp = new Inverter();
            String serialNo = Inv.getsn();
            String gateSerialNo = Inv.getgsn();
            temp.setGateSerialNo(gateSerialNo);
            temp.setSerialNo(serialNo);
            temp.setUID(gateSerialNo + serialNo);
            temp.setToken(accessToken);
            temp.setAlias(Inv.getAlias());
            temp.setID(Inv.getID());
            inverters.add(temp);
        }
        return inverters;
    }

    class Inverterdata {
        private int pageSize;
        private int pageNumber;
        private int total;
        private List<InverterInfo> infos;

        public List<InverterInfo> getInverters() {
            return this.infos;
        }

        class InverterInfo {
            private String id; // could be an int type
            private String sn;
            private String alias;
            private String gsn;
            private int status;
            private int type;
            private String commTypeName;
            private String custCode;
            private Version version;
            private String model;
            private String equipMode;
            private int pac;
            private double etoday;
            private double etotal;
            private String updateAt;
            private int opened;
            private Plant plant;

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
                return id;
            }

            public double getetoday() {
                return this.etoday;
            }

            public double getetotal() {
                return this.etotal;
            }

            class Plant {
                private int id;
                private String name;
                private int type;
                private String master;
                private String installer;
                private String email;
                private String phone;
                private GatewayVO gatewayVO;
                private boolean sunsynkEquip;
                private int protocolIdentifier;
                private int equipType;

                class GatewayVO {
                    private String gsn;
                    private int status;
                }
            }

            class Version {
                private String masterVer;
                private String softVer;
                private String hardVer;
                private String bmsVer;
            }
        }
    }
    // }
}

/*
 * "{"code":0,
 * "msg":"Success",
 * "data":{
 * "pageSize":10,
 * "pageNumber":1,
 * "total":1,"infos":
 * [
 * {"id":33307,
 * "sn":"2211229948",
 * "alias":"77FB Inverter",
 * "gsn":"E4701229R331",
 * "status":1,
 * "type":2,
 * "commTypeName":"RS485",
 * "custCode":29,
 * "version":
 * {"masterVer":"5.3.8.4",
 * "softVer":"1.5.1.5",
 * "hardVer":"",
 * "hmiVer":"E.4.3.0",
 * "bmsVer":""},
 * "model":"",
 * "equipMode":null,
 * "pac":0,
 * "etoday":0.00,
 * "etotal":0.00,
 * "updateAt":"2024-01-02T14:20:06Z",
 * "opened":1,
 * "plant":
 * {"id":239068,
 * "name":"77FernBrook",
 * "type":2,"master":null,
 * "installer":null,
 * "email":null,
 * "phone":null},
 * "gatewayVO":
 * {"gsn":"E4701229R331",
 * "status":2},
 * "sunsynkEquip":true,
 * "protocolIdentifier":"2",
 * "equipType":2
 * }
 * ]
 * },
 * "success":true
 * }"
 * 
 * 
 * 
 */
