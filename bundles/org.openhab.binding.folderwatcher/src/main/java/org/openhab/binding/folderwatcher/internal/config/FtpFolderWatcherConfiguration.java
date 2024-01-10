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
package org.openhab.binding.folderwatcher.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.folderwatcher.internal.SecureMode;

/**
 * The {@link FtpFolderWatcherConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Alexandr Salamatov - Initial contribution
 */
@NonNullByDefault
public class FtpFolderWatcherConfiguration {
    public String ftpAddress = "";
    public int ftpPort;
    public String ftpUsername = "";
    public String ftpPassword = "";
    public String ftpDir = "";
    public int pollInterval;
    public int connectionTimeout;
    public boolean listHidden;
    public int diffHours;
    public boolean listRecursiveFtp;
    public SecureMode secureMode = SecureMode.NONE;
}
