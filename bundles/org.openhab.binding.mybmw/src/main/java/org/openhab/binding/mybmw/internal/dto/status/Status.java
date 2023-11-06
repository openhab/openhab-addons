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
package org.openhab.binding.mybmw.internal.dto.status;

import java.util.List;

import org.openhab.binding.mybmw.internal.dto.charge.ChargeProfile;

/**
 * The {@link Status} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class Status {
    public String lastUpdatedAt;// ": "2021-12-21T16:46:02Z",
    public Mileage currentMileage;
    public Issues issues;
    public String doorsGeneralState;// ":"Locked",
    public String checkControlMessagesGeneralState;// ":"No Issues",
    public List<DoorWindow> doorsAndWindows;// ":[
    public List<CCMMessage> checkControlMessages;//
    public List<CBSMessage> requiredServices;//
    // "recallMessages":[],
    // "recallExternalUrl":null,
    public List<FuelIndicator> fuelIndicators;
    public String timestampMessage;// ":"Updated from vehicle 12/21/2021 05:46 PM",
    public ChargeProfile chargingProfile;
}
