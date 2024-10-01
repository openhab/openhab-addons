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
package org.openhab.binding.velbus.internal.packets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velbus.internal.VelbusBindingConstants;

/**
 * The {@link VelbusSetScenePacket} represents a Velbus packet that can be used to
 * set the scene of a channel on the DALI module.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusSetScenePacket extends VelbusPacket {
    private byte[] data;

    public VelbusSetScenePacket(byte address, byte channel, byte sceneNumber) {
        super(address, PRIO_HI, false);

        this.data = new byte[] { VelbusBindingConstants.COMMAND_SET_DIMSCENE, channel, sceneNumber };
    }

    public void GoToScene(byte sceneNumber) {
        data[2] = sceneNumber;
    }

    @Override
    protected byte[] getDataBytes() {
        return data;
    }
}
