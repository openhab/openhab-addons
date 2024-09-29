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
package org.openhab.binding.sleepiq.internal.api.dto;

/**
 * The {@link BedStatus} holds the BedStatus response from the sleepiq API.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class BedStatus {
    private Long status;
    private String bedId;
    private BedSideStatus leftSide;
    private BedSideStatus rightSide;

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public BedStatus withStatus(Long status) {
        setStatus(status);
        return this;
    }

    public String getBedId() {
        return bedId;
    }

    public void setBedId(String bedId) {
        this.bedId = bedId;
    }

    public BedStatus withBedId(String bedId) {
        setBedId(bedId);
        return this;
    }

    public BedSideStatus getLeftSide() {
        return leftSide;
    }

    public void setLeftSide(BedSideStatus leftSide) {
        this.leftSide = leftSide;
    }

    public BedStatus withLeftSide(BedSideStatus leftSide) {
        setLeftSide(leftSide);
        return this;
    }

    public BedSideStatus getRightSide() {
        return rightSide;
    }

    public void setRightSide(BedSideStatus rightSide) {
        this.rightSide = rightSide;
    }

    public BedStatus withRightSide(BedSideStatus rightSide) {
        setRightSide(rightSide);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bedId == null) ? 0 : bedId.hashCode());
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
        if (!(obj instanceof BedStatus)) {
            return false;
        }
        BedStatus other = (BedStatus) obj;
        if (bedId == null) {
            if (other.bedId != null) {
                return false;
            }
        } else if (!bedId.equals(other.bedId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BedStatus [status=");
        builder.append(status);
        builder.append(", bedId=");
        builder.append(bedId);
        builder.append(", leftSide=");
        builder.append(leftSide);
        builder.append(", rightSide=");
        builder.append(rightSide);
        builder.append("]");
        return builder.toString();
    }
}
