/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.internal;

/**
 * The {@link VelbusPacketListener} is notified when a Velbus packet for
 * the listener's address is sent on the bus.
 *
 * @author Cedric Boon - Initial contribution
 */
public interface VelbusPacketListener {
    /**
     * This method is called whenever the state of the given relay has changed.
     *
     * @param packet The bytes of the received packet.
     */
    void onPacketReceived(byte[] packet);
}
