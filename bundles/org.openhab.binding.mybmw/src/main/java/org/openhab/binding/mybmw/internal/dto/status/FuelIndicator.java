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

    public int secondaryBarValue;// ": 0,
    public int infoIconId;// ": 59694,
    public int rangeIconId;// ": 59683,
    public int levelIconId;// ": 59694,
    public boolean showsBar;// ": true,
    public boolean showBarGoal;// ": false,
    public String barType;// ": null,
    public String infoLabel;// ": "State of Charge",
    public boolean isInaccurate;// ": false,
    public boolean isCircleIcon;// ": false,
    public String iconOpacity;// ": "high",
    public String chargingType;// ": null,
    public String chargingStatusType;// ": "DEFAULT",
    public String chargingStatusIndicatorType;// ": "DEFAULT"
}
