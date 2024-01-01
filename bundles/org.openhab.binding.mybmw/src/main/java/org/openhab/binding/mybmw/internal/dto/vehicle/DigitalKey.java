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
package org.openhab.binding.mybmw.internal.dto.vehicle;

/**
 * 
 * derived from the API responses
 * 
 * @author Martin Grassl - initial contribution
 */
public class DigitalKey {
    private String bookedServicePackage = ""; // NONE,
    private String readerGraphics = "";
    private String state = ""; // NOT_AVAILABLE

    public String getBookedServicePackage() {
        return bookedServicePackage;
    }

    public void setBookedServicePackage(String bookedServicePackage) {
        this.bookedServicePackage = bookedServicePackage;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReaderGraphics() {
        return readerGraphics;
    }

    public void setReaderGraphics(String readerGraphics) {
        this.readerGraphics = readerGraphics;
    }

    @Override
    public String toString() {
        return "DigitalKey [bookedServicePackage=" + bookedServicePackage + ", readerGraphics=" + readerGraphics
                + ", state=" + state + "]";
    }
}
