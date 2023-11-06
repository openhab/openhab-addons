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
 * The {@link OpeningTime} class is representing single Opening Time entry from the api request (i.e one setting like
 * "Montag" "09:00" "18:00")
 *
 * @author Jürgen Baginski - Initial contribution
 */
public class OpeningTime {

    private String text;
    private String start;
    private String end;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append(this.getText()).append(" Open: ").append(this.getStart()).append("  Close: ")
                .append(this.getEnd());
        return sb.toString();
    }
}
