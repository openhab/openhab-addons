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
package org.openhab.binding.mybmw.internal.dto.charge;

import java.util.List;

import org.openhab.binding.mybmw.internal.utils.Constants;

/**
 * The {@link Timer} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 * @author Norbert Truchsess - edit and send of charge profile
 */
public class Timer {
    public int id = -1;// ": 1,
    public String action;// ": "deactivate",
    public Time timeStamp;
    public List<String> timerWeekDays;

    @Override
    public String toString() {
        return id + Constants.COLON + action + Constants.COLON + timeStamp + Constants.COLON + timerWeekDays;
    }
}
