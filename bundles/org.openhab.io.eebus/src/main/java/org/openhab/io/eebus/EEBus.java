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
package org.openhab.io.eebus;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * EEBus integration API.
 *
 * @author openHAB EEBus Add-on Contributors - Initial contribution
 */
@NonNullByDefault
public interface EEBus {

    /**
     * @return this node's own SHIP Subject Key Identifier, to give to a pairing partner's
     *         installer to pre-trust it, or {@code null} if the node hasn't started yet.
     */
    @Nullable
    String getOwnSki();
}
