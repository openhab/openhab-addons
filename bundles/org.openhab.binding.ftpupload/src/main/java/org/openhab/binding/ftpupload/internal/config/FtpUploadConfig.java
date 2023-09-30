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
package org.openhab.binding.ftpupload.internal.config;

/**
 * Configuration class for FtpUpload device.
 *
 * @author Pauli Anttila - Initial contribution
 */

public class FtpUploadConfig {

    public String userName;
    public String password;

    @Override
    public String toString() {
        String str = "";

        str += "userName = " + userName;
        str += ", password = *****";

        return str;
    }
}
