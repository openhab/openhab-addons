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
package org.openhab.binding.mybmw.internal.dto.status;

import org.openhab.binding.mybmw.internal.utils.Constants;

/**
 * The {@link CCMMessage} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class CCMMessage {
    public String criticalness;// ": "semiCritical",
    public int iconId;// ": 60217,
    public String state = Constants.NO_ENTRIES;// ": "Medium",
    public String title = Constants.NO_ENTRIES;// ": "Battery discharged: Start engine"
    public String id;// ": "229",
    public String longDescription = Constants.NO_ENTRIES;// ": "Charge by driving for longer periods or use external
                                                         // charger. Functions requiring battery will be switched off.
}
