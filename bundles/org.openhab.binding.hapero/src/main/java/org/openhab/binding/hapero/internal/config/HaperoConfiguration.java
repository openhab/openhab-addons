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
package org.openhab.binding.hapero.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HaperoConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Daniel Walter - Initial contribution
 */
@NonNullByDefault
public class HaperoConfiguration {
    public String accessMode = "ftp";
    public String ftpServer = "";
    public int port = 21;
    public String userName = "";
    public String password = "";
    public String ftpPath = "";
    public int pollingInterval = 30;
    public String fileStoragePath = "";
    public int refreshTimeout = 300;
}
