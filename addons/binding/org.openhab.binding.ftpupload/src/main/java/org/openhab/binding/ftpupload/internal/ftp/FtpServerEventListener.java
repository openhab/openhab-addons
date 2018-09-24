/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ftpupload.internal.ftp;

import org.eclipse.jdt.annotation.NonNull;

/**
 * This interface defines interface to receive data from FTP server.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface FtpServerEventListener {

    /**
     * Procedure for receive raw data from FTP server.
     *
     * @param userName User name.
     * @param filename Received filename.
     * @param data Received raw data.
     */
    void fileReceived(@NonNull String userName, @NonNull String filename, byte[] data);

}
