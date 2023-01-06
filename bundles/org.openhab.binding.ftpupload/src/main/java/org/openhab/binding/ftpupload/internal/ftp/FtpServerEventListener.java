/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
