/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upnpcontrol.handler;

import static org.openhab.binding.upnpcontrol.UpnpControlBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.upnpcontrol.internal.UpnpDynamicStateDescriptionProvider;
import org.openhab.binding.upnpcontrol.internal.UpnpEntry;
import org.openhab.binding.upnpcontrol.internal.UpnpXMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpServerHandler} is responsible for handling commands sent to the UPnP Server.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UpnpServerHandler extends UpnpHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConcurrentMap<String, UpnpRendererHandler> upnpRenderers;
    private @Nullable UpnpRendererHandler currentRendererHandler;
    private List<StateOption> rendererStateOptionList = new ArrayList<>();

    @NonNullByDefault(value = {})
    private ChannelUID rendererChannelUID;

    private static final String DIRECTORY_ROOT = "0";
    private static final String UP = "..";

    private String uriMetaData = "";
    private @Nullable List<UpnpEntry> resultList;
    private int numberReturned;
    private int totalMatches;

    private String currentId = DIRECTORY_ROOT;
    private @Nullable String currentSelection;
    private Map<String, String> parentMap = new HashMap<>();

    private String source = "";

    UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider;

    public UpnpServerHandler(Thing thing, UpnpIOService upnpIOService,
            ConcurrentMap<String, UpnpRendererHandler> upnpRenderers,
            UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider) {
        super(thing, upnpIOService);
        this.upnpRenderers = upnpRenderers;
        this.upnpStateDescriptionProvider = upnpStateDescriptionProvider;
        this.parentMap.put(DIRECTORY_ROOT, DIRECTORY_ROOT);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for media server device");

        rendererChannelUID = new ChannelUID(getThing().getUID(), UPNPRENDERER);
        upnpRenderers.forEach((key, value) -> {
            StateOption stateOption = new StateOption(key, value.getThing().getLabel());
            rendererStateOptionList.add(stateOption);
        });
        updateStateDescription(rendererChannelUID, rendererStateOptionList);
        if (service.isRegistered(this)) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} for channel {}", command, channelUID);

        @SuppressWarnings("null")
        ChannelUID currentTitleChannelUID = thing.getChannel(CURRENTTITLE).getUID();

        switch (channelUID.getId()) {
            case UPNPRENDERER:
                if (command instanceof StringType) {
                    currentRendererHandler = (upnpRenderers.get(((StringType) command).toString()));
                }
                break;
            case CURRENTTITLE:
                if (command instanceof RefreshType) {
                    updateTitleSelection(currentId, currentTitleChannelUID);
                } else if (command instanceof StringType) {
                    String commandString = command.toString();
                    if (!commandString.equals(UP)) {
                        parentMap.put(commandString, currentId);
                        logger.debug("{} with parent {} added to parent map", commandString, currentId);
                    }
                    currentSelection = commandString;
                    updateState(SELECT, OnOffType.OFF);
                }
                break;
            case SELECT:
                if (command == OnOffType.ON) {
                    String browseTarget = currentSelection;
                    if (browseTarget != null) {
                        if (browseTarget.equals(UP)) {
                            browseTarget = parentMap.get(currentId);
                        }
                        logger.debug("browse target {}", browseTarget);
                        updateTitleSelection(browseTarget, currentTitleChannelUID);
                    }
                }
                break;
            case SERVE:
                if (command == OnOffType.ON) {
                    serveMedia();
                }
                updateState(channelUID, OnOffType.OFF);
                break;
        }
    }

    public void addRendererOption(String key, UpnpRendererHandler handler) {
        rendererStateOptionList.add(new StateOption(key, upnpRenderers.get(key).getThing().getLabel()));
        updateStateDescription(rendererChannelUID, rendererStateOptionList);
        logger.debug("Renderer option {} added to {}", key, this.getThing().getLabel());
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

    private void updateTitleSelection(String browseTarget, ChannelUID currentTitleChannelUID) {
        browse(browseTarget, "BrowseDirectChildren", "*", "0", "0", "+dc:title");
        currentId = browseTarget;
        logger.debug("Navigating to node {}", currentId);

        List<StateOption> stateOptionList = new ArrayList<>();
        if ((resultList != null) && !(resultList.get(0).getParentId().equals(DIRECTORY_ROOT))) {
            StateOption stateOption = new StateOption(UP, UP);
            stateOptionList.add(stateOption);
        }
        if (resultList != null) {
            resultList.forEach((value) -> {
                StateOption stateOption = new StateOption(value.getId(), value.getTitle());
                stateOptionList.add(stateOption);
            });
        }
        updateStateDescription(currentTitleChannelUID, stateOptionList);

        if (resultList != null) {
            UpnpEntry firstEntry = resultList.get(0);
            String newSelection = firstEntry.getId();
            parentMap.put(newSelection, firstEntry.getParentId());
            logger.debug("{} with parent {} added to parent map", newSelection, firstEntry.getParentId());
            currentSelection = newSelection;
        } else if (!currentId.equals(DIRECTORY_ROOT)) {
            currentSelection = UP;
        } else {
            currentSelection = null;
        }
        updateState(currentTitleChannelUID, new StringType(currentSelection));
    }

    private void updateStateDescription(ChannelUID channelUID, List<StateOption> stateOptionList) {
        StateDescription stateDescription = new StateDescription(null, null, null, null, false, stateOptionList);
        upnpStateDescriptionProvider.setDescription(channelUID, stateDescription);
    }

    public void browse(String objectID, String browseFlag, String filter, String startingIndex, String requestedCount,
            String sortCriteria) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("ObjectID", objectID);
        inputs.put("BrowseFlag", browseFlag);
        inputs.put("Filter", filter);
        inputs.put("StartingIndex", startingIndex);
        inputs.put("RequestedCount", requestedCount);
        inputs.put("SortCriteria", sortCriteria);

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
                if (value != null) {
                    uriMetaData = value;
                }
                resultList = UpnpXMLParser.getEntriesFromString(value);
                break;
            case "NumberReturned":
                numberReturned = Integer.parseInt(value);
                break;
            case "TotalMatches":
                totalMatches = Integer.parseInt(value);
                break;
            case "UpdateID":
                break;
            case "Source":
                if (!((value == null) || (value.isEmpty()))) {
                    source = value;
                }
                break;
            default:
                super.onValueReceived(variable, value, service);
                break;
        }
    }

    public String getURIMetaData() {
        return uriMetaData;
    }

    public String getURI() {
        return getThing().getProperties().get("descriptorURL");
    }

    @Override
    public String getProtocolInfo() {
        Map<String, String> inputs = new HashMap<>();

        Map<String, String> result = service.invokeAction(this, "ConnectionManager", "GetProtocolInfo", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "ConnectionManager");
        }

        return source;
    }

    private void serveMedia() {
        if (currentRendererHandler != null) {
            UpnpRendererHandler handler = currentRendererHandler;
            Queue<UpnpEntry> mediaQueue = new LinkedList<>();
            handler.registerQueue(mediaQueue);
            if (resultList != null) {
                resultList.forEach((entry) -> {
                    mediaQueue.add(entry);
                });
            }
        }
    }
}
