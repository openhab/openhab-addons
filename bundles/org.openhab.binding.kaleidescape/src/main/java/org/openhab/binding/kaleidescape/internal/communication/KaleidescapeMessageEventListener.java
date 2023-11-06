/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.kaleidescape.internal.communication;

import java.util.EventListener;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Kaleidescape Event Listener interface. Handles incoming Kaleidescape message events
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public interface KaleidescapeMessageEventListener extends EventListener {
    /**
     * Event handler method for incoming Kaleidescape message events
     *
     * @param event the KaleidescapeMessageEvent object
     */
    void onNewMessageEvent(KaleidescapeMessageEvent event);
}
