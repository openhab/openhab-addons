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
package org.openhab.binding.lutron.internal.protocol.leap;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.protocol.leap.dto.Area;
import org.openhab.binding.lutron.internal.protocol.leap.dto.ButtonGroup;
import org.openhab.binding.lutron.internal.protocol.leap.dto.Device;
import org.openhab.binding.lutron.internal.protocol.leap.dto.OccupancyGroup;
import org.openhab.binding.lutron.internal.protocol.leap.dto.ZoneStatus;

/**
 * Interface defining callback routines used by LeapMessageParser
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public interface LeapMessageParserCallbacks {

    public void validMessageReceived(String communiqueType);

    public void handleEmptyButtonGroupDefinition();

    public void handleZoneUpdate(ZoneStatus zoneStatus);

    public void handleGroupUpdate(int groupNumber, String occupancyStatus);

    public void handleMultipleButtonGroupDefinition(List<ButtonGroup> buttonGroupList);

    public void handleMultipleDeviceDefintion(List<Device> deviceList);

    public void handleMultipleAreaDefinition(List<Area> areaList);

    public void handleMultipleOccupancyGroupDefinition(List<OccupancyGroup> oGroupList);
}
