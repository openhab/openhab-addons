/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.core;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.UserAccounts;
import org.openhab.binding.atv.internal.client.dto.UserAccount;

/**
 * Relay implementation for user account handling.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class UserAccountsRelay extends BaseRelay<UserAccounts> implements UserAccounts {

    /**
     * Creates a new relay user accounts instance.
     *
     * @param guard device guard blocking calls after close
     */
    public UserAccountsRelay(Guard guard) {
        super(new Relayer<>(UserAccounts.class, AppleTVRelay.DEFAULT_PRIORITIES), guard);
    }

    @Override
    public CompletableFuture<List<UserAccount>> accountList() {
        guard.requireNotBlocked("accountList");
        return relayAsync(Capability.ACCOUNTS_ACCOUNT_LIST, UserAccounts::accountList);
    }

    @Override
    public CompletableFuture<Void> switchAccount(String accountId) {
        guard.requireNotBlocked("switchAccount");
        return relayAsync(Capability.ACCOUNTS_SWITCH_ACCOUNT, accounts -> accounts.switchAccount(accountId));
    }
}
