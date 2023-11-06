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
package org.openhab.binding.paradoxalarm.internal.handlers;

import java.time.LocalDateTime;

/**
 * The {@link PanelConfiguration} Paradox Panel handler configuration.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class PanelConfiguration extends EntityConfiguration {
    private double vdcVoltage;
    private double dcVoltage;
    private double batteryVoltage;
    private LocalDateTime panelTime;

    public double getVdcVoltage() {
        return vdcVoltage;
    }

    public void setAcVoltage(double vdcVoltage) {
        this.vdcVoltage = vdcVoltage;
    }

    public double getDcVoltage() {
        return dcVoltage;
    }

    public void setDcVoltage(double dcVoltage) {
        this.dcVoltage = dcVoltage;
    }

    public double getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(double batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public LocalDateTime getPanelTime() {
        return panelTime;
    }

    public void setPanelTime(LocalDateTime panelTime) {
        this.panelTime = panelTime;
    }
}
