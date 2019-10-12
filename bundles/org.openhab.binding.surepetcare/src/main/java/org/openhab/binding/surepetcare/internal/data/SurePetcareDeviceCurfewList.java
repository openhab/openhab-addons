/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal.data;

import java.util.ArrayList;

/**
 * The {@link SurePetcareDeviceCurfewList} class is used to serialize a list of curfew parameters.
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareDeviceCurfewList extends ArrayList<SurePetcareDeviceCurfew> {

    private static final long serialVersionUID = -6947992959305282143L;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(", curfew=[");
        for (SurePetcareDeviceCurfew c : this) {
            builder.append("(").append(c).append(")");
        }
        builder.append("]]");
        return builder.toString();
    }

}