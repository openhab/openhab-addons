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
package org.openhab.binding.tplinkbridge;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A TP-Link account shared by Kasa and Tapo Things. The handler of a {@code tplinkbridge:account} bridge implements
 * this interface, so a child Thing in another binding gets the credentials from
 * {@code bridge.getHandler() instanceof TPLinkAccount account}.
 *
 * <p>
 * When the credentials change, the bridge is reinitialized and its children receive a bridge status change, so
 * they should read the credentials again on every login rather than caching them.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public interface TPLinkAccount {

    /**
     * @return the configured account credentials
     */
    TPLinkCredentials getCredentials();
}
