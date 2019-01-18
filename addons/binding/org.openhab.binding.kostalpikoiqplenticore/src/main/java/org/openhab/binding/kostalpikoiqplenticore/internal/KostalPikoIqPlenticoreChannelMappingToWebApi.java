/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kostalpikoiqplenticore.internal;

/***
 * The {@link KostalPikoIqPlenticoreChannelMappingToWebApi}} is used to map the channel name to the web API commands
 *
 * @author René Stakemeier - Initial contribution
 *
 */
class KostalPikoIqPlenticoreChannelMappingToWebApi {

    /**
     * Constructor of {@link KostalPikoIqPlenticoreChannelMappingToWebApi}
     *
     * @param channelUID    The channel UUID
     * @param moduleId      module id (as defined by the web api)
     * @param processdataId process data id (as defined by the web api)
     * @param dataType      data type of this channel
     */
    KostalPikoIqPlenticoreChannelMappingToWebApi(String channelUID, String moduleId, String processdataId,
            KostalPikoIqPlenticoreChannelDatatypes dataType) {
        this.channelUID = channelUID;
        this.moduleId = moduleId;
        this.processdataId = processdataId;
        this.dataType = dataType;
    }

    String channelUID;
    String moduleId;
    String processdataId;
    KostalPikoIqPlenticoreChannelDatatypes dataType;
}
