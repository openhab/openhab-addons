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
package org.openhab.binding.boschshc.internal;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Response of the Controller for a Long Poll API call.
 *
 * The result field will contain the subscription ID needed for further API calls (e.g. the long polling call)
 *
 * @author Stefan Kästle - Initial contribution
 */
public class SubscribeResult {
    private @Nullable String result;
    private @Nullable String jsonrpc;

    public @Nullable String getResult() {
        return this.result;
    }

    public @Nullable String getJsonrpc() {
        return this.jsonrpc;
    }

}
