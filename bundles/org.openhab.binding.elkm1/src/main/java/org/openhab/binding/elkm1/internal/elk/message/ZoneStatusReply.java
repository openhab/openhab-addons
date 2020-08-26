/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;
import org.openhab.binding.elkm1.internal.elk.ElkMessageFactory;
import org.openhab.binding.elkm1.internal.elk.ElkZoneConfig;
import org.openhab.binding.elkm1.internal.elk.ElkZoneStatus;

/**
 * The reply to a zone status request with all the zones we have status on.
 *
 * @author David Bennett - Initial Contribution
 */
public class ZoneStatusReply extends ElkMessage {
    private final ElkZoneConfig[] config;
    private final ElkZoneStatus[] status;

    public ZoneStatusReply(String input) {
        super(ElkCommand.ZoneStatusReply);
        config = new ElkZoneConfig[input.length()];
        status = new ElkZoneStatus[input.length()];
        for (int i = 0; i < input.length() && i < ElkMessageFactory.MAX_ZONES; i++) {
            int val = Integer.valueOf(input.charAt(i));
            switch (val & 0x3) {
                case 0:
                    config[i] = ElkZoneConfig.Unconfigured;
                    break;
                case 1:
                    config[i] = ElkZoneConfig.Open;
                    break;
                case 2:
                    config[i] = ElkZoneConfig.EOL;
                    break;
                case 3:
                    config[i] = ElkZoneConfig.Short;
                    break;
                default:
                    config[i] = ElkZoneConfig.Invalid;
                    break;
            }
            switch ((val & 0x0e) >> 2) {
                case 0:
                    status[i] = ElkZoneStatus.Normal;
                    break;
                case 1:
                    status[i] = ElkZoneStatus.Trouble;
                    if (config[i] == ElkZoneConfig.Unconfigured) {
                        config[i] = ElkZoneConfig.Invalid;
                        status[i] = ElkZoneStatus.Invalid;
                    }
                    break;
                case 2:
                    status[i] = ElkZoneStatus.Violated;
                    if (config[i] == ElkZoneConfig.Unconfigured) {
                        config[i] = ElkZoneConfig.Invalid;
                        status[i] = ElkZoneStatus.Invalid;
                    }
                    break;
                case 3:
                    status[i] = ElkZoneStatus.Bypassed;
                    break;
                default:
                    status[i] = ElkZoneStatus.Invalid;
                    break;
            }
        }
    }

    public ElkZoneConfig[] getConfig() {
        return config;
    }

    public ElkZoneStatus[] getStatus() {
        return status;
    }

    @Override
    protected String getData() {
        return null;
    }
}
