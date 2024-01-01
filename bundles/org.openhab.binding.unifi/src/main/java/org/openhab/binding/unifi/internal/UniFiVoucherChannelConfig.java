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
package org.openhab.binding.unifi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link UniFiVoucherChannelConfig} encapsulates all the configuration options for the guestVouchersGenerate
 * channel on the UniFi Site thing.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UniFiVoucherChannelConfig {

    private int voucherCount;
    private int voucherExpiration;
    private int voucherUsers;
    private @Nullable Integer voucherUpLimit;
    private @Nullable Integer voucherDownLimit;
    private @Nullable Integer voucherDataQuota;

    public int getCount() {
        return voucherCount;
    }

    public int getExpiration() {
        return voucherExpiration;
    }

    public int getVoucherUsers() {
        return voucherUsers;
    }

    public @Nullable Integer getUpLimit() {
        return voucherUpLimit;
    }

    public @Nullable Integer getDownLimit() {
        return voucherDownLimit;
    }

    public @Nullable Integer getDataQuota() {
        return voucherDataQuota;
    }
}
