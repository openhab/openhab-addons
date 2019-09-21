/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The Alarm state of the Verisure System.
 *
 * @author Jarle Hjortland - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureAlarmJSON extends VerisureBaseThingJSON {

    private @Nullable String date;
    private @Nullable String notAllowedReason;
    private @Nullable Boolean changeAllowed;
    private @Nullable String label;
    private @Nullable String type;

    @Override
    public String getDeviceId() {
        if ("ARM_STATE".equals(type)) {
            return "alarm" + deviceId;
        } else {
            return deviceId;
        }
    }

    /**
     * @return the date
     */
    public @Nullable String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the notAllowedReason
     */
    public @Nullable String getNotAllowedReason() {
        return notAllowedReason;
    }

    /**
     * @param notAllowedReason the notAllowedReason to set
     */
    public void setNotAllowedReason(String notAllowedReason) {
        this.notAllowedReason = notAllowedReason;
    }

    /**
     * @return the changeAllowed
     */
    public @Nullable Boolean getChangeAllowed() {
        return changeAllowed;
    }

    /**
     * @param changeAllowed the changeAllowed to set
     */
    public void setChangeAllowed(Boolean changeAllowed) {
        this.changeAllowed = changeAllowed;
    }

    /**
     * @return the label
     */
    public @Nullable String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the type
     */
    public @Nullable String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    @SuppressWarnings("null")
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((changeAllowed == null) ? 0 : changeAllowed.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((notAllowedReason == null) ? 0 : notAllowedReason.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /*
     *
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        VerisureAlarmJSON other = (VerisureAlarmJSON) obj;
        if (changeAllowed == null) {
            if (other != null && other.changeAllowed != null) {
                return false;
            }
        } else if (changeAllowed != null && other != null && !changeAllowed.equals(other.changeAllowed)) {
            return false;
        }
        if (date == null) {
            if (other != null && other.date != null) {
                return false;
            }
        } else if (date != null && other != null && !date.equals(other.date)) {
            return false;
        }
        if (label == null) {
            if (other != null && other.label != null) {
                return false;
            }
        } else if (label != null && other != null && !label.equals(other.label)) {
            return false;
        }
        if (notAllowedReason == null) {
            if (other != null && other.notAllowedReason != null) {
                return false;
            }
        } else if (notAllowedReason != null && other != null && !notAllowedReason.equals(other.notAllowedReason)) {
            return false;
        }
        if (type == null) {
            if (other != null && other.type != null) {
                return false;
            }
        } else if (type != null && other != null && !type.equals(other.type)) {
            return false;
        }

        return true;
    }

    /*
     *
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("VerisureAlarmJSON [");
        if (date != null) {
            builder.append("date=");
            builder.append(date);
            builder.append(", ");
        }
        if (label != null) {
            builder.append("label=");
            builder.append(label);
            builder.append(", ");
        }
        if (type != null) {
            builder.append("type=");
            builder.append(type);
            builder.append(", ");
        }
        builder.append("]");
        return super.toString() + "\n" + builder.toString();
    }

}
