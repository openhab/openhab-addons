/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.handler;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface for connections to JeeLink USB Receivers.
 *
 * @author Volker Bier - Initial contribution
 */
public interface JeeLinkConnection {
    void addReadingConverter(JeeLinkReadingConverter<?> listener);

    void removeReadingConverters();

    void closeConnection();

    void openConnection();

    OutputStream getInitStream() throws IOException;

    String getPort();

    void setInitCommands(String initCommands);

    void addConnectionListener(ConnectionListener listener);

    void removeConnectionListener(ConnectionListener listener);
}
