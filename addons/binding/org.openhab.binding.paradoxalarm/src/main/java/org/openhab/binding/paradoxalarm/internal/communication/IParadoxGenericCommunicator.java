/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.io.IOException;

/**
 * The {@link IParadoxGenericCommunicator} is representing the functionality of generic communication. Only login/logout
 * sequence which is used to determine the Panel type.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public interface IParadoxGenericCommunicator {
    void close();

    void logoutSequence() throws IOException;

    void loginSequence() throws IOException, InterruptedException;

    byte[] getPanelInfoBytes();

    boolean isOnline();

}
