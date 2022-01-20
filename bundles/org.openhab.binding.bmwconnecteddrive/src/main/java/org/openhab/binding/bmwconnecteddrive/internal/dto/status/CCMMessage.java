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
package org.openhab.binding.bmwconnecteddrive.internal.dto.status;

import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;

/**
 * The {@link CCMMessage} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class CCMMessage {
    // if necessary. Perform reset after adjustment. See Owner's Handbook for further
    // information.",
    public String ccmDescriptionShort = Constants.INVALID;// ": "Tyre pressure notification",
    public String ccmDescriptionLong = Constants.INVALID;// ": "You can continue driving. Check tyre pressure when tyres
    // are cold and adjust
    public int ccmId = -1;// ": 955,
    public int ccmMileage = -1;// ": 41544
}
