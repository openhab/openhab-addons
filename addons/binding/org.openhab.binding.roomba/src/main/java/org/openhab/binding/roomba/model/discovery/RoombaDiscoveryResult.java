/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roomba.model.discovery;

/**
 * POJO Representation of a discovery response from a Roomba, model 980 or up.
 *
 * Example Json:
 *
 * {
 * "hostname": "Roomba-XXXXX",
 * "ip": "192.168.1.70",
 * "mac": "XX:XX:XX:XX:XX:XX",
 * "nc": 0,
 * "proto": "http",
 * "robotname": "Default",
 * "sku": "R98----",
 * "sw": "v1.6.6",
 * "ver": "2"
 * }
 *
 * @author Stephen Liang
 *
 */
public class RoombaDiscoveryResult {
    public String ver;
    public String hostname;
    public String robotname;
    public String ip;
    public String mac;
    public String sw;
    public String sku;
    public String nc;
    public String proto;

    public RoombaDiscoveryResult() {
        // no-arg constructor
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getRobotname() {
        return robotname;
    }

    public void setRobotname(String robotname) {
        this.robotname = robotname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getSw() {
        return sw;
    }

    public void setSw(String sw) {
        this.sw = sw;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNc() {
        return nc;
    }

    public void setNc(String nc) {
        this.nc = nc;
    }

    public String getProto() {
        return proto;
    }

    public void setProto(String proto) {
        this.proto = proto;
    }
}
