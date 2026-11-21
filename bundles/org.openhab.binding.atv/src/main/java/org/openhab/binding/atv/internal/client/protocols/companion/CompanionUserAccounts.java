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
package org.openhab.binding.atv.internal.client.protocols.companion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.UserAccounts;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.dto.UserAccount;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;

/**
 * Implementation of the user account handling API for Companion.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CompanionUserAccounts implements UserAccounts, CapabilitySource {

    private final CompanionApi api;

    /**
     * Creates a new instance.
     *
     * @param api Companion API
     */
    public CompanionUserAccounts(CompanionApi api) {
        this.api = api;
    }

    @Override
    public CompletableFuture<List<UserAccount>> accountList() {
        return api.accountList().thenApply(response -> {
            Map<String, Object> content = CompanionApi.content(response);
            if (content == null) {
                throw new ProtocolError("missing content in response");
            }
            List<UserAccount> accounts = new ArrayList<>();
            for (Map.Entry<String, Object> entry : content.entrySet()) {
                accounts.add(new UserAccount((String) entry.getValue(), entry.getKey()));
            }
            return accounts;
        });
    }

    @Override
    public CompletableFuture<Void> switchAccount(String accountId) {
        return api.switchAccount(accountId);
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.ACCOUNTS_ACCOUNT_LIST, Capability.ACCOUNTS_SWITCH_ACCOUNT);
    }
}
