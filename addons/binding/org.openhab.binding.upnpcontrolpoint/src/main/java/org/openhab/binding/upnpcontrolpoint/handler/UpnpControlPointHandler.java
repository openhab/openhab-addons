/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upnpcontrolpoint.handler;

import static org.openhab.binding.upnpcontrolpoint.UpnpControlPointBindingConstants.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.upnpcontrolpoint.internal.UpnpDynamicStateDescriptionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpControlPointHandler} is responsible for handling commands sent to the UPnP Control Point.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UpnpControlPointHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private UpnpIOService service;

    private ConcurrentMap<String, UpnpRendererHandler> upnpRenderers;
    private ConcurrentMap<String, UpnpServerHandler> upnpServers;

    private @Nullable UpnpServerHandler currentServerHandler;
    private @Nullable UpnpRendererHandler currentRendererHandler;

    private ArrayList<StateOption> serverStateOptionList = new ArrayList<>();
    private ArrayList<StateOption> rendererStateOptionList = new ArrayList<>();

    private UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider;

    @NonNullByDefault(value = {})
    private ChannelUID serverChannelUID;
    @NonNullByDefault(value = {})
    private ChannelUID rendererChannelUID;

    public UpnpControlPointHandler(Thing thing, UpnpIOService upnpIOService,
            ConcurrentMap<String, UpnpRendererHandler> upnpRenderers,
            ConcurrentMap<String, UpnpServerHandler> upnpServers,
            UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider) {
        super(thing);
        service = upnpIOService;
        this.upnpRenderers = upnpRenderers;
        this.upnpServers = upnpServers;
        this.upnpStateDescriptionProvider = upnpStateDescriptionProvider;
    }

    @Override
    public void initialize() {
        serverChannelUID = new ChannelUID(getThing().getUID(), UPNPSERVER);
        upnpServers.forEach((key, value) -> {
            StateOption stateOption = new StateOption(key, value.getThing().getLabel());
            serverStateOptionList.add(stateOption);

        });
        updateStateDescription(serverChannelUID, serverStateOptionList);

        rendererChannelUID = new ChannelUID(getThing().getUID(), UPNPRENDERER);
        upnpRenderers.forEach((key, value) -> {
            StateOption stateOption = new StateOption(key, value.getThing().getLabel());
            rendererStateOptionList.add(stateOption);

        });
        updateStateDescription(rendererChannelUID, rendererStateOptionList);

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof StringType) {
            switch (channelUID.getId()) {
                case "upnpserver":
                    currentServerHandler = (upnpServers.get(((StringType) command).toString()));
                    break;
                case "upnprenderer":
                    currentRendererHandler = (upnpRenderers.get(((StringType) command).toString()));
                    break;
            }
        }
    }

    public void addServerOption(String key, UpnpServerHandler handler) {
        serverStateOptionList.add(new StateOption(key, upnpServers.get(key).getThing().getLabel()));
        updateStateDescription(serverChannelUID, serverStateOptionList);
        logger.debug("Server option {} added to {}", key, this.getThing().getLabel());
    }

    public void addRendererOption(String key, UpnpRendererHandler handler) {
        rendererStateOptionList.add(new StateOption(key, upnpRenderers.get(key).getThing().getLabel()));
        updateStateDescription(rendererChannelUID, rendererStateOptionList);
        logger.debug("Renderer option {} added to {}", key, this.getThing().getLabel());
    }

    public void removeServerOption(String key) {
        if ((currentServerHandler != null) && (currentServerHandler.getThing().getUID().toString().equals(key))) {
            currentServerHandler = null;
            updateState(serverChannelUID, UnDefType.UNDEF);
        }
        serverStateOptionList.removeIf(stateOption -> (stateOption.getValue().equals(key)));
        updateStateDescription(serverChannelUID, serverStateOptionList);
        logger.debug("Server option {} removed from {}", key, this.getThing().getLabel());
    }

    public void removeRendererOption(String key) {
        if ((currentRendererHandler != null) && (currentRendererHandler.getThing().getUID().toString().equals(key))) {
            currentRendererHandler = null;
            updateState(rendererChannelUID, UnDefType.UNDEF);
        }
        rendererStateOptionList.removeIf(stateOption -> (stateOption.getValue().equals(key)));
        updateStateDescription(rendererChannelUID, rendererStateOptionList);
        logger.debug("Renderer option {} removed from {}", key, this.getThing().getLabel());
    }

    private void updateStateDescription(ChannelUID channelUID, ArrayList<StateOption> stateOptionList) {
        StateDescription stateDescription = new StateDescription(null, null, null, null, false, stateOptionList);
        upnpStateDescriptionProvider.setDescription(channelUID, stateDescription);
    }

}
