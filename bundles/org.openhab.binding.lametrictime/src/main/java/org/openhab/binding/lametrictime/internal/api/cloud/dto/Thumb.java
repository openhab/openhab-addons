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
package org.openhab.binding.lametrictime.internal.api.cloud.dto;

/**
 * Pojo for thumb.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Thumb {
    private String original;
    private String small;
    private String large;
    private String xlarge;

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public Thumb withOriginal(String original) {
        this.original = original;
        return this;
    }

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public Thumb withSmall(String small) {
        this.small = small;
        return this;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public Thumb withLarge(String large) {
        this.large = large;
        return this;
    }

    public String getXlarge() {
        return xlarge;
    }

    public void setXlarge(String xlarge) {
        this.xlarge = xlarge;
    }

    public Thumb withXlarge(String xlarge) {
        this.xlarge = xlarge;
        return this;
    }
}
