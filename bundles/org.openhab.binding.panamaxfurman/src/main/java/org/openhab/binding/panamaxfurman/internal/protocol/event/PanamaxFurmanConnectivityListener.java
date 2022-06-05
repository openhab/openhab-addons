/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.panamaxfurman.internal.protocol.event;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A listener which is notified when the connection to the Power Conditioner is established or broken
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public interface PanamaxFurmanConnectivityListener {

    /**
     * Called when the connection to the Power Conditioner is established or broken
     *
     */
    public void onConnectivityEvent(PanamaxFurmanConnectivityEvent event);
}
