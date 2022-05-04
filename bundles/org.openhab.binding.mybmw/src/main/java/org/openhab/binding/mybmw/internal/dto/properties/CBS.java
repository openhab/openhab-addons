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
package org.openhab.binding.mybmw.internal.dto.properties;

import org.openhab.binding.mybmw.internal.utils.Constants;

/**
 * The {@link CBS} Data Transfer Object ConditionBasedService
 *
 * @author Bernd Weymann - Initial contribution
 */
public class CBS {
    public String type = Constants.NO_ENTRIES;// ": "BRAKE_FLUID",
    public String status = Constants.NO_ENTRIES;// ": "OK",
    public String dateTime;// ": "2023-11-01T00:00:00.000Z"
    public Distance distance;
}
