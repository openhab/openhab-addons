/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal.dto;

/**
 * Common fields in vehicle status API responses.
 *
 * @author Marcus Better - Initial contribution
 */
public interface CommonVehicleStatus {
    boolean engine();

    boolean doorLock();

    DoorStatus doorOpen();

    DoorStatus windowOpen();

    boolean trunkOpen();

    boolean hoodOpen();

    boolean airCtrlOn();

    TemperatureValue airTemp();

    boolean defrost();

    int steerWheelHeat();

    int sideBackWindowHeat();

    int sideMirrorHeat();

    SeatHeaterState seatHeaterVentState();

    BatteryStatus battery();

    EvStatus evStatus();

    DrivingRange dte();

    int fuelLevel();

    boolean lowFuelLight();

    boolean washerFluidStatus();

    boolean brakeOilStatus();

    TirePressureWarnings tirePressureWarning();
}
