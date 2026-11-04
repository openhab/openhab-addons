/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.robonect.internal.model.BatteryInfo;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.HourInfo;
import org.openhab.binding.robonect.internal.model.ModelParser;
import org.openhab.binding.robonect.internal.model.MotorInfo;
import org.openhab.binding.robonect.internal.model.PortalInfo;
import org.openhab.binding.robonect.internal.model.VersionInfo;
import org.openhab.binding.robonect.internal.model.WireInfo;
import org.openhab.binding.robonect.internal.model.WlanInfo;
import org.openhab.binding.robonect.internal.model.cmd.BatteryCommand;
import org.openhab.binding.robonect.internal.model.cmd.ErrorCommand;
import org.openhab.binding.robonect.internal.model.cmd.HourCommand;
import org.openhab.binding.robonect.internal.model.cmd.MotorCommand;
import org.openhab.binding.robonect.internal.model.cmd.PortalCommand;
import org.openhab.binding.robonect.internal.model.cmd.VersionCommand;
import org.openhab.binding.robonect.internal.model.cmd.WireCommand;
import org.openhab.binding.robonect.internal.model.cmd.WlanCommand;

/**
 * API operations for diagnostic and telemetry endpoints.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public final class RobonectDiagnosticsApi {
    private final RobonectTransport transport;

    /**
     * Creates an endpoint API using the shared transport.
     *
     * @param transport the transport used for requests and parsing
     */
    RobonectDiagnosticsApi(RobonectTransport transport) {
        this.transport = transport;
    }

    /**
     * Returns the error list reported by the mower.
     *
     * @return - the list of errors.
     */
    public ErrorList errorList() {
        String responseString = transport.sendCommand(new ErrorCommand());
        return ModelParser.parse(responseString, ErrorList.class);
    }

    /**
     * Returns detailed battery information reported by the mower.
     *
     * @return the battery information including its success status
     */
    public BatteryInfo getBatteryInfo() {
        return ModelParser.parse(transport.sendCommand(new BatteryCommand()), BatteryInfo.class);
    }

    /**
     * Returns detailed motor information reported by the mower.
     *
     * @return the motor information including its success status
     */
    public MotorInfo getMotorInfo() {
        return ModelParser.parse(transport.sendCommand(new MotorCommand()), MotorInfo.class);
    }

    /**
     * Returns aggregate operating statistics reported by the mower.
     *
     * @return the operating statistics including their success status
     */
    public HourInfo getHourInfo() {
        return ModelParser.parse(transport.sendCommand(new HourCommand()), HourInfo.class);
    }

    /**
     * Returns boundary-wire sensor information.
     *
     * @return the sensor information including its success status
     */
    public WireInfo getWireInfo() {
        return ModelParser.parse(transport.sendCommand(new WireCommand()), WireInfo.class);
    }

    /**
     * Reads WLAN, access-point, and DHCP state.
     *
     * @return the WLAN information and its success status
     */
    public WlanInfo getWlanInfo() {
        return ModelParser.parse(transport.sendCommand(new WlanCommand()), WlanInfo.class);
    }

    /**
     * Returns the remote portal connection status.
     *
     * @return the portal information including its success status
     */
    public PortalInfo getPortalInfo() {
        return ModelParser.parse(transport.sendCommand(new PortalCommand()), PortalInfo.class);
    }

    /**
     * Retrieve the version information of the mower and module. See {@link VersionInfo} for details.
     *
     * @return - the Version Information including the successful status.
     */
    public VersionInfo getVersionInfo() {
        String versionResponse = transport.sendCommand(new VersionCommand());
        return ModelParser.parse(versionResponse, VersionInfo.class);
    }
}
