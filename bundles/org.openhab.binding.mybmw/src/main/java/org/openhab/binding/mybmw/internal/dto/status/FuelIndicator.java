/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

/**
 * The {@link FuelIndicator} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class FuelIndicator {
    public int mainBarValue;// ": 74,
    public String rangeUnits;// ": "km",
    public String rangeValue;// ": "76",
    public String levelUnits;// ": "%",
    public String levelValue;// ": "74",

    /**
     * not used
     * "secondaryBarValue": 0,
     * "infoIconId": 59694,
     * "rangeIconId": 59683,
     * "levelIconId": 59694,
     * "showsBar": true,
     * "showBarGoal": false,
     * "barType": null,
     * "infoLabel": "State of Charge",
     * "isInaccurate": false,
     * "isCircleIcon": false,
     * "iconOpacity": "high",
     * "chargingType": null,
     * "chargingStatusType": "DEFAULT",
     * "chargingStatusIndicatorType": "DEFAULT"
     **/
}
