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
package org.openhab.binding.tr064.internal.config;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;

/**
 * The {@link Tr064RootConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Tr064RootConfiguration extends Tr064BaseThingConfiguration {
    public static final int DEFAULT_HTTP_TIMEOUT = 5; // in s

    public String host = "";
    public String user = "dslf-config";
    public String password = "";
    public int timeout = DEFAULT_HTTP_TIMEOUT;

    /* following parameters only available in fritzbox thing */
    public List<String> tamIndices = List.of();
    public List<String> callDeflectionIndices = List.of();
    public List<String> missedCallDays = List.of();
    public List<String> rejectedCallDays = List.of();
    public List<String> inboundCallDays = List.of();
    public List<String> outboundCallDays = List.of();
    public List<String> callListDays = List.of();
    public List<String> wanBlockIPs = List.of();
    public int phonebookInterval = 600;

    // Backup data
    public String backupDirectory = OpenHAB.getUserDataFolder();
    public @Nullable String backupPassword;

    public boolean isValid() {
        return !host.isEmpty() && !user.isEmpty() && !password.isEmpty();
    }
}
