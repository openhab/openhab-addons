/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upnpcontrolpoint.handler;

import static org.openhab.binding.upnpcontrolpoint.UpnpControlPointBindingConstants.CURRENTTITLE;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpControlPointHandler} is responsible for handling commands sent to the UPnP Renderer.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UpnpServerHandler extends UpnpHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public UpnpServerHandler(Thing thing, UpnpIOService upnpIOService) {
        super(thing, upnpIOService);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for media server device");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CURRENTTITLE:
                    navigateToRoot();
                    break;
            }
        }
    }

    public void navigateToRoot() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("ObjectID", "0");
        inputs.put("BrowseFlag", "BrowseDirectChildren");
        inputs.put("Filter", "");
        inputs.put("StartingIndex", "0");
        inputs.put("RequestedCount", "0");
        inputs.put("SortCriteria", "");

        Map<String, String> result = service.invokeAction(this, "ContentDirectory", "Browse", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "ContentDirectory");
        }

    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        logger.debug("Received variable {} with value {} from service {}", variable, value, service);
        if (variable == null) {
            return;
        }
        switch (variable) {
            case "Result":
                break;
            case "NumberReturned":
                break;
            case "TotalMatches":
                break;
            case "UpdateID":
                break;
            default:
                super.onValueReceived(variable, value, service);
                break;
        }
    }

}
