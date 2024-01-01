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
 * Base class for results
 *
 * @author Wolfgang Klimt - initial contribution
 */
@NonNullByDefault
public abstract class AwattarBestPriceResult {

    private long start;
    private long end;

    public AwattarBestPriceResult() {
    }

    public long getStart() {
        return start;
    }

    public void updateStart(long start) {
        if (this.start == 0 || this.start > start) {
            this.start = start;
        }
    }

    public long getEnd() {
        return end;
    }

    public void updateEnd(long end) {
        if (this.end == 0 || this.end < end) {
            this.end = end;
        }
    }

    public abstract boolean isActive();

    public abstract String getHours();
}
