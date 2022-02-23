/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.ftp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.ftp.FtpConfig.FtpConfigResponse;
import org.openhab.binding.freeboxos.internal.api.rest.ActivableRest;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;

/**
 * The {@link FtpManager} is the Java class used to handle api requests
 * related to ftp
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FtpManager extends ActivableRest<FtpConfig, FtpConfigResponse> {
    private static final String FTP_PATH = "ftp";

    public FtpManager(FreeboxOsSession session) {
        super(session, FtpConfigResponse.class, FTP_PATH, CONFIG_SUB_PATH);
    }
}
