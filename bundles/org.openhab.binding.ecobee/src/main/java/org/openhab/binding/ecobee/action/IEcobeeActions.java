/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.ecobee.action;

import java.util.Date;
import java.util.Map;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;

/**
 * The {@link IEcobeeActions} defines the interface for all thing actions supported by the binding.
 * These methods, parameters, and return types are explained in {@link EcobeeActions}.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public interface IEcobeeActions {

    public Boolean acknowledge(@Nullable String ackRef, @Nullable String ackType, @Nullable Boolean remindMeLater);

    public Boolean controlPlug(@Nullable String plugName, @Nullable String plugState, @Nullable Date startDateTime,
            @Nullable Date endDateTime, @Nullable String holdType, @Nullable Number holdHours);

    public Boolean sendMessage(@Nullable String text);

    public Boolean createVacation(@Nullable String name, @Nullable QuantityType<Temperature> coolHoldTemp,
            @Nullable QuantityType<Temperature> heatHoldTemp, @Nullable Date startDateTime, @Nullable Date endDateTime,
            @Nullable String fan, @Nullable Number fanMinOnTime);

    public Boolean deleteVacation(@Nullable String name);

    public Boolean resetPreferences();

    public Boolean resumeProgram(@Nullable Boolean resumeAll);

    public Boolean setHold(@Nullable QuantityType<Temperature> coolHoldTemp,
            @Nullable QuantityType<Temperature> heatHoldTemp);

    public Boolean setHold(@Nullable QuantityType<Temperature> coolHoldTemp,
            @Nullable QuantityType<Temperature> heatHoldTemp, @Nullable Number holdHours);

    public Boolean setHold(@Nullable String holdClimateRef);

    public Boolean setHold(@Nullable String holdClimateRef, @Nullable Number holdHours);

    public Boolean setHold(@Nullable QuantityType<Temperature> coolHoldTemp,
            @Nullable QuantityType<Temperature> heatHoldTemp, @Nullable String holdClimateRef,
            @Nullable Date startDateTime, @Nullable Date endDateTime, @Nullable String holdType,
            @Nullable Number holdHours);

    public Boolean setHold(@Nullable Map<String, Object> params, @Nullable String holdType, @Nullable Number holdHours,
            @Nullable Date startDateTime, @Nullable Date endDateTime);

    public Boolean setOccupied(@Nullable Boolean occupied, @Nullable Date startDateTime, @Nullable Date endDateTime,
            @Nullable String holdType, @Nullable Number holdHours);

    public Boolean updateSensor(@Nullable String name, @Nullable String deviceId, @Nullable String sensorId);

    public @Nullable String getAlerts();

    public @Nullable String getEvents();

    public @Nullable String getClimates();
}
