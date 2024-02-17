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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NhcActionEvent} interface is used to pass action events received from the Niko Home Control controller to
 * the consuming client. It is designed to pass events to openHAB handlers that implement this interface. Because of
 * the design, the org.openhab.binding.nikohomecontrol.internal.protocol package can be extracted and used independent
 * of openHAB.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public interface NhcActionEvent {

    /**
     * This method is called when an action event is received from the Niko Home Control controller.
     *
     * @param state
     */
    void actionEvent(int state);

    /**
     * Called to indicate the action has been initialized.
     *
     */
    void actionInitialized();

    /**
     * Called to indicate the action has been removed from the Niko Home Control controller.
     *
     */
    void actionRemoved();
}
