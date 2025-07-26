/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.folding.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.folding.internal.dto.SlotInfo;

/**
 * Interface for callback from Client handler to Slot handler.
 *
 * The client performs refreshes regularly, retrieving information about
 * all slots. It then calls refreshed on all registered SlotUpdateListeners.
 *
 * @author Marius Bjørnstad - Initial contribution
 */
@NonNullByDefault
public interface SlotUpdateListener {
    /**
     * Called when the slot information has been refreshed.
     *
     * @param si the updated {@link SlotInfo} instance containing the refreshed data
     */
    void refreshed(SlotInfo si);
}
