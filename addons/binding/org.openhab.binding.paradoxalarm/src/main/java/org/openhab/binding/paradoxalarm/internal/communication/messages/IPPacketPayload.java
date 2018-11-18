/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.communication.messages;

import java.io.IOException;

/**
 * Interface representing what we need to add IPPacketPayload.
 * Not sure if we need it as it needs only getBytes() method so far.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public interface IPPacketPayload {

    public byte[] getBytes() throws IOException;
}
