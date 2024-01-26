/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.structures;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Tapo-Child Structure Class
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class TapoChildData {
    private int startIndex = 0;
    private int sum = 0;
    private List<TapoChild> childDeviceList = List.of();

    public int getStartIndex() {
        return startIndex;
    }

    public int getSum() {
        return sum;
    }

    public List<TapoChild> getChildDeviceList() {
        return childDeviceList;
    }
}
