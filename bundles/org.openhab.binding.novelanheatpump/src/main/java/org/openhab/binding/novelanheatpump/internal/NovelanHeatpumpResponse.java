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
package org.openhab.binding.novelanheatpump.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NovelanHeatpumpResponse} is the Java class conatins all Information of the Heatpump
 *
 * @author Jan-Philipp Bolle - Initial contribution
 */

@NonNullByDefault
public class NovelanHeatpumpResponse {
    private final int[] heatpumpValues;
    private final int[] heatpumpParams;

    public NovelanHeatpumpResponse(int[] heatpumpValues, int[] heatpumpParams) {
        this.heatpumpValues = heatpumpValues;
        this.heatpumpParams = heatpumpParams;
    }

    public int[] getHeatpumpValues() {
        return heatpumpValues;
    }

    public int[] getHeatpumpParams() {
        return heatpumpParams;
    }
}
