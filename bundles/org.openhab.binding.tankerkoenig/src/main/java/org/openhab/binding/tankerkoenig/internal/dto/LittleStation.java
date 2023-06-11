/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tankerkoenig.internal.dto;

/**
 * The {@link LittleStation} class is the representing java model for the station specific json result of the prices
 * request. (LittleStation because it has nearly no other information than gas prices. Stripped down version of
 * Station.java)
 *
 * @author Dennis Dollinger - Initial contribution
 */
public class LittleStation {

    private String e5;
    private String e10;
    private String diesel;
    private String status;
    private String id;
    private Boolean open;

    public String getE5() {
        return e5;
    }

    public void setE5(String e5) {
        this.e5 = e5;
    }

    public String getE10() {
        return e10;
    }

    public void setE10(String e10) {
        this.e10 = e10;
    }

    public String getDiesel() {
        return diesel;
    }

    public void setDiesel(String diesel) {
        this.diesel = diesel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public Boolean isOpen() {
        return open;
    }

    public void setOpen(Boolean isOpen) {
        this.open = isOpen;
    }
}
