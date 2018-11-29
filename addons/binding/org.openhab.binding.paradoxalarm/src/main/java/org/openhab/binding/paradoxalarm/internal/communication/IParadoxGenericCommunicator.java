/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.communication;

import java.io.IOException;

/**
 * The {@link IParadoxGenericCommunicator} is representing the functionality of generic communication. Only login/logout
 * sequence which is used to determine the Panel type.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public interface IParadoxGenericCommunicator {
    void close() throws IOException;

    void logoutSequence() throws IOException;

    void loginSequence() throws IOException, InterruptedException;

    public byte[] getPanelInfoBytes();

}
