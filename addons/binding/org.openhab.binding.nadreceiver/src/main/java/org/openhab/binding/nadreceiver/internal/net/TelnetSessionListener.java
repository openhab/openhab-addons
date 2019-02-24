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
package org.openhab.binding.nadreceiver.internal.net;

import java.io.IOException;

/**
 * Class copied from binding project Lutron and his author Allan Tong
 *
 * Listener for Telnet session events
 *
 * @author Marc Chételat - Initial contribution
 */
public interface TelnetSessionListener {

    void incomingMessageAvailable();

    void errorHandling(IOException exception);
}
