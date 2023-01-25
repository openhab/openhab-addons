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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NhcAccessEvent} interface is used to pass door lock events received from the Niko Home Control
 * controller to the consuming client. It is designed to pass events to openHAB handlers that implement this interface.
 * Because of the design, the org.openhab.binding.nikohomecontrol.internal.protocol package can be extracted and used
 * independent of openHAB.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public interface NhcAccessEvent extends NhcBaseEvent {

    /**
     * This method is called when a door bell event is received from the Niko Home Control controller.
     *
     * @param state true for bell ringing
     */
    public void accessBellEvent(boolean state);

    /**
     * This method is called a ring and come in setting event is received from the Niko Home Control controller.
     *
     * @param state true for bell ringing
     */
    public void accessRingAndComeInEvent(boolean state);

    /**
     * This method is called when a door lock event is received from the Niko Home Control controller.
     *
     * @param state true for locked
     */
    public void accessDoorLockEvent(boolean state);

    /**
     * Update video device properties.
     */
    public void updateVideoDeviceProperties();
}
