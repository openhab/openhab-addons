/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.myq.internal.dto;

/**
 * The {@link DeviceStateDTO} entity from the MyQ API
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DeviceStateDTO {

    public Boolean gdoLockConnected;
    public Boolean attachedWorkLightErrorPresent;
    public String learnStatus;
    public Boolean hasCamera;
    public String lampState;
    public String batteryBackupState;
    public String doorState;
    public String lastUpdate;
    public Boolean isUnattendedOpenAllowed;
    public Boolean isUnattendedCloseAllowed;
    public Integer serviceCycleCount;
    public Integer absoluteCycleCount;
    public Boolean online;
    public String lastStatus;
}
