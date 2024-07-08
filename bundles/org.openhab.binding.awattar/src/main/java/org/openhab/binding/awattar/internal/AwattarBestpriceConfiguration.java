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
package org.openhab.binding.awattar.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Stores the bestprice config
 *
 * @author Wolfgang Klimt - initial contribution
 */
@NonNullByDefault
public class AwattarBestpriceConfiguration {
    public int rangeStart = 0;
    public int rangeDuration = 24;
    public int length = 1;
    public boolean consecutive = true;

    @Override
    public String toString() {
        return String.format("{ s: %d, d: %d, l: %d, c: %b )", rangeStart, rangeDuration, length, consecutive);
    }
}
