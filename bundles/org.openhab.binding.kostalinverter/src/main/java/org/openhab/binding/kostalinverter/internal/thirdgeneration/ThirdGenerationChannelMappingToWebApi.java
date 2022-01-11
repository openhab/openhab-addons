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
package org.openhab.binding.kostalinverter.internal.thirdgeneration;

/***
 * The {@link ThirdGenerationChannelMappingToWebApi}} is used to map the channel name to the web API commands
 *
 * @author René Stakemeier - Initial contribution
 *
 */
class ThirdGenerationChannelMappingToWebApi {

    String channelUID;
    String moduleId;
    String processdataId;
    ThirdGenerationChannelDatatypes dataType;

    /**
     * Constructor of {@link ThirdGenerationChannelMappingToWebApi}
     *
     * @param channelUID The channel UUID
     * @param moduleId module id (as defined by the web api)
     * @param processdataId process data id (as defined by the web api)
     * @param dataType data type of this channel
     */
    ThirdGenerationChannelMappingToWebApi(String channelUID, String moduleId, String processdataId,
            ThirdGenerationChannelDatatypes dataType) {
        this.channelUID = channelUID;
        this.moduleId = moduleId;
        this.processdataId = processdataId;
        this.dataType = dataType;
    }
}
