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
package org.openhab.binding.paradoxalarm.internal.communication;

import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The {@link IParadoxInitialLoginCommunicator} is representing the functionality of generic communication. Only
 * login/logout
 * sequence which is used to determine the Panel type.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public interface IParadoxInitialLoginCommunicator extends IConnectionHandler {

    void startLoginSequence();

    byte[] getPanelInfoBytes();

    void setPanelInfoBytes(byte[] panelInfoBytes);

    /**
     * @return IP150 connection password
     */
    String getPassword();

    byte[] getPcPasswordBytes();

    ScheduledExecutorService getScheduler();

    void setListeners(Collection<IDataUpdateListener> listeners);

    void updateListeners();
}
