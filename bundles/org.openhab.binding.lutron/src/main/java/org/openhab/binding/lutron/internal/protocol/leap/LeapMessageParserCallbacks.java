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
package org.openhab.binding.lutron.internal.protocol.leap;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.protocol.leap.dto.Area;
import org.openhab.binding.lutron.internal.protocol.leap.dto.ButtonGroup;
import org.openhab.binding.lutron.internal.protocol.leap.dto.Device;
import org.openhab.binding.lutron.internal.protocol.leap.dto.OccupancyGroup;
import org.openhab.binding.lutron.internal.protocol.leap.dto.Project;
import org.openhab.binding.lutron.internal.protocol.leap.dto.ZoneStatus;

/**
 * Interface defining callback routines used by LeapMessageParser
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public interface LeapMessageParserCallbacks {

    void handleProjectDefinition(Project project);

    void handleDeviceDefinition(Device device);

    void validMessageReceived(String communiqueType);

    void handleEmptyButtonGroupDefinition();

    void handleZoneUpdate(ZoneStatus zoneStatus);

    void handleGroupUpdate(int groupNumber, String occupancyStatus);

    void handleMultipleButtonGroupDefinition(List<ButtonGroup> buttonGroupList);

    void handleMultipleDeviceDefinition(List<Device> deviceList);

    void handleMultipleAreaDefinition(List<Area> areaList);

    void handleMultipleOccupancyGroupDefinition(List<OccupancyGroup> oGroupList);
}
