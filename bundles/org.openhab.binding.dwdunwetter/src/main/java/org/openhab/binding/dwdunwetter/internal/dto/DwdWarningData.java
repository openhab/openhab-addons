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
package org.openhab.binding.dwdunwetter.internal.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data for one warning.
 *
 * @author Martin Koehler - Initial contribution
 */
public class DwdWarningData {

    private String id;

    private Severity severity;
    private String description;
    private Instant effective;
    private Instant expires;
    private Instant onset;
    private String event;
    private String status;
    private String msgType;
    private String headline;
    private BigDecimal altitude;
    private BigDecimal ceiling;
    private String instruction;
    private Urgency urgency;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Severity getSeverity() {
        return severity == null ? Severity.UNKNOWN : severity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setEffective(Instant effective) {
        this.effective = effective;
    }

    public Instant getEffective() {
        return effective;
    }

    public void setExpires(Instant expires) {
        this.expires = expires;
    }

    public Instant getExpires() {
        return expires;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isTest() {
        return "Test".equalsIgnoreCase(status);
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public boolean isCancel() {
        return "Cancel".equalsIgnoreCase(msgType);
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getHeadline() {
        return headline;
    }

    public Instant getOnset() {
        return onset;
    }

    public void setOnset(Instant onset) {
        this.onset = onset;
    }

    public void setAltitude(BigDecimal altitude) {
        this.altitude = altitude;
    }

    public BigDecimal getAltitude() {
        return altitude;
    }

    public void setCeiling(BigDecimal ceiling) {
        this.ceiling = ceiling;
    }

    public BigDecimal getCeiling() {
        return ceiling;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DwdWarningData other = (DwdWarningData) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }
}
