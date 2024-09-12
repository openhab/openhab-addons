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
package org.openhab.binding.folding.internal.handler;

/**
 * Interface for callback from Client handler to Slot handler.
 *
 * The client performs refreshes regularly, retrieving information about
 * all slots. It then calls refreshed on all registered SlotUpdateListeners.
 *
 * @author Marius Bjørnstad - Initial contribution
 */
public interface SlotUpdateListener {
    void refreshed(SlotInfo si);
}
