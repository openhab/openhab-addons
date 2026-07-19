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
package org.openhab.binding.atv.internal.client.capability;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.UserAccount;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * API for user account handling.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface UserAccounts {

    /**
     * Fetches a list of user accounts that can be switched.
     *
     * @return future completing with available user accounts
     */
    default CompletableFuture<List<UserAccount>> accountList() {
        return CompletableFuture.failedFuture(new NotSupportedError("accountList is not supported"));
    }

    /**
     * Switches user account by account ID.
     *
     * @param accountId identifier of account to switch to
     * @return future completing when account has been switched
     */
    default CompletableFuture<Void> switchAccount(String accountId) {
        return CompletableFuture.failedFuture(new NotSupportedError("switchAccount is not supported"));
    }
}
