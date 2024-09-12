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
package org.openhab.binding.unifi.internal.api.cache;

import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.ID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.dto.UniFiVoucher;

/**
 * The {@link UniFiVoucherCache} is a specific implementation of {@link UniFiCache} for the purpose of caching
 * {@link UniFiVoucher} instances.
 *
 * The cache uses the following prefixes: <code>id</code>
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
class UniFiVoucherCache extends UniFiCache<UniFiVoucher> {

    public UniFiVoucherCache() {
        super(ID);
    }

    @Override
    protected @Nullable String getSuffix(final UniFiVoucher voucher, final Prefix prefix) {
        switch (prefix) {
            case ID:
                return voucher.getId();
            default:
                return null;
        }
    }
}
