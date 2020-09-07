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
package org.openhab.binding.bmwconnecteddrive.internal.dto.status;

import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;

/**
 * The {@link CBSMessage} Data Transfer Object
 *
 * @author Bernd Weymann - Initial contribution
 */
public class CBSMessage {
    public String cbsType;// ": "BRAKE_FLUID",
    public String cbsState;// ": "OK",
    public String cbsDueDate;// ": "2021-11",
    public String cbsDescription;// ": "Next change due at the latest by the stated date."
    public int cbsRemainingMileage; // 46000

    public String getDueDate() {
        if (cbsDueDate == null) {
            return Constants.INVALID;
        } else {
            return cbsDueDate;
        }
    }

    public String getType() {
        if (cbsType == null) {
            return Constants.INVALID;
        } else {
            return cbsType;
        }
    }

    @Override
    public String toString() {
        return new StringBuffer(cbsDueDate).append(Constants.HYPHEN).append(cbsRemainingMileage)
                .append(Constants.HYPHEN).append(cbsType).toString();
    }
}
