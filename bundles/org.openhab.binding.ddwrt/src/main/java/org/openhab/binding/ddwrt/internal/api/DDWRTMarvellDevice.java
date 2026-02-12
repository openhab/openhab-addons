/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;

/**
 * DD-WRT device with Marvell chipset. Uses {@code iwinfo} commands.
 * Shares the iwinfo assoclist parser with {@link DDWRTOpenWrtDevice}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTMarvellDevice extends DDWRTBaseDevice {

    public DDWRTMarvellDevice(DDWRTDeviceConfiguration cfg) {
        super(cfg);
    }

    @Override
    protected List<DDWRTWirelessClient> getAssociatedClients(SshRunner runner, String iface) {
        return IwinfoParser.parseAssoclist(runner, iface, mac);
    }

    @Override
    protected List<DDWRTRadio> enumerateRadios(SshRunner runner) {
        return IwinfoParser.enumerateRadios(runner, mac);
    }

    @Override
    protected void refreshIdentity(SshRunner runner) {
        model = safeTrim(runner.execStdout("grep -i 'Board:' /tmp/loginprompt 2>/dev/null | cut -d' ' -f 2-"));
        firmware = safeTrim(runner.execStdout("grep -i DD-WRT /tmp/loginprompt 2>/dev/null | cut -d' ' -f-2"));
    }

    @Override
    protected void setRadioEnabled(SshRunner runner, String iface, boolean enabled) {
        if (enabled) {
            runner.execStdout("ifconfig " + iface + " up");
        } else {
            runner.execStdout("ifconfig " + iface + " down");
        }
    }
}
