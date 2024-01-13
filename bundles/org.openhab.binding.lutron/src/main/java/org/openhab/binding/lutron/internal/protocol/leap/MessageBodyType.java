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
package org.openhab.binding.lutron.internal.protocol.leap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * LEAP MessageBodyType enum
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public enum MessageBodyType {
    ExceptionDetail("ExceptionDetail"),
    MultipleAffectedZoneDefinition("MultipleAffectedZoneDefinition"),
    MultipleAreaDefinition("MultipleAreaDefinition"),
    MultipleButtonDefinition("MultipleButtonDefinition"),
    MultipleButtonGroupDefinition("MultipleButtonGroupDefinition"),
    MultipleDeviceDefinition("MultipleDeviceDefinition"),
    MultipleDeviceStatus("MultipleDeviceStatus"),
    MultipleOccupancyGroupDefinition("MultipleOccupancyGroupDefinition"),
    MultipleOccupancyGroupStatus("MultipleOccupancyGroupStatus"),
    MultiplePresetAssignmentDefinition("MultiplePresetAssignmentDefinition"),
    MultipleProgrammingModelDefinition("MultipleProgrammingModelDefinition"),
    MultipleServerDefinition("MultipleServerDefinition"),
    MultipleServiceDefinition("MultipleServiceDefinition"),
    MultipleTimeclockDefinition("MultipleTimeclockDefinition"),
    MultipleVirtualButtonDefinition("MultipleVirtualButtonDefinition"),
    MultipleZoneDefinition("MultipleZoneDefinition"),
    MultipleZoneStatus("MultipleZoneStatus"),
    OneAffectedZoneDefinition("OneAffectedZoneDefinition"),
    OneAlexaDataSummaryDefinition("OneAlexaDataSummaryDefinition"),
    OneAreaDefinition("OneAreaDefinition"),
    OneAreaLoadSheddingDefinition("OneAreaLoadSheddingDefinition"),
    OneButtonDefinition("OneButtonDefinition"),
    OneButtonGroupDefinition("OneButtonGroupDefinition"),
    OneDeviceDefinition("OneDeviceDefinition"),
    OneDeviceRulesDefinition("OneDeviceRulesDefinition"),
    OneDeviceStatus("OneDeviceStatus"),
    OneGoogleHomeDataSummaryDefinition("OneGoogleHomeDataSummaryDefinition"),
    OneLinkNodeDefinition("OneLinkNodeDefinition"),
    OneLIPIdListDefinition("OneLIPIdListDefinition"),
    OneNetworkInterfaceDefinition("OneNetworkInterfaceDefinition"),
    OneOccupancyGroupDefinition("OneOccupancyGroupDefinition"),
    OnePairingListDefinition("OnePairingListDefinition"),
    OnePingResponse("OnePingResponse"),
    OnePresetAssignmentDefinition("OnePresetAssignmentDefinition"),
    OnePresetDefinition("OnePresetDefinition"),
    OneProgrammingModelDefinition("OneProgrammingModelDefinition"),
    OneProjectDefinition("OneProjectDefinition"),
    OneServerDefinition("OneServerDefinition"),
    OneServiceDefinition("OneServiceDefinition"),
    OneSystemDefinition("OneSystemDefinition"),
    OneTimeclockEventRulesDefinition("OneTimeclockEventRulesDefinition"),
    OneVirtualButtonDefinition("OneVirtualButtonDefinition"),
    OneZoneDefinition("OneZoneDefinition"),
    OneZoneStatus("OneZoneStatus");

    private final transient String string;

    MessageBodyType(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
