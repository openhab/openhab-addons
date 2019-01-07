/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.transceiver;

import org.openhab.binding.enocean.internal.messages.ESP3Packet;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public interface ESP3PacketListener {

    public void espPacketReceived(ESP3Packet packet);

    public long getSenderIdToListenTo();
}
