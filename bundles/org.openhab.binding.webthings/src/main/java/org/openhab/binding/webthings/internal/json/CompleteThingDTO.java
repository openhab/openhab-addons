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
package org.openhab.binding.webthings.internal.json;

import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.core.thing.firmware.dto.FirmwareStatusDTO;
import org.eclipse.smarthome.io.rest.core.thing.EnrichedChannelDTO;

/**
 * The {@link CompleteThingDTO}
 *
 * @author Sven Schneider - Initial contribution
 */
public class CompleteThingDTO{
    //EnrichedChannelDTO
    public String label;
    public String bridgeUID;
    public Map<String, Object> configuration;
    public Map<String, String> properties;
    public String UID;
    public String thingTypeUID;
    public List<EnrichedChannelDTO> channels;
    public String location;
    public ThingStatusInfo statusInfo;
    public final FirmwareStatusDTO firmwareStatus;
    public boolean editable;

    /**
     * Creates an enriched thing data transfer object.
     *
     * @param thingDTO the base {@link ThingDTO}
     * @param channels the list of {@link EnrichedChannelDTO} for this thing
     * @param statusInfo {@link ThingStatusInfo} for this thing
     * @param firmwareStatus {@link FirmwareStatusDTO} for this thing
     * @param editable true if this thing can be edited
     */
    protected CompleteThingDTO( String thingTypeUID, String UID, String label, String bridgeUID, List<EnrichedChannelDTO> channels, 
                            Map<String, Object> configuration, Map<String, String> properties, String location, 
                            ThingStatusInfo statusInfo, FirmwareStatusDTO firmwareStatus, boolean editable) {
        this.thingTypeUID = thingTypeUID;
        this.UID = UID;
        this.label = label;
        this.bridgeUID = bridgeUID;
        this.channels = channels;
        this.configuration = configuration;
        this.properties = properties;
        this.location = location;
        this.statusInfo = statusInfo;
        this.firmwareStatus = firmwareStatus;
        this.editable = editable;
    }
}
