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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.upnpcontrol.internal.UpnpDynamicStateDescriptionProvider;
import org.openhab.binding.upnpcontrol.internal.UpnpEntry;
import org.openhab.binding.upnpcontrol.internal.UpnpProtocolMatcher;
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
    private volatile @Nullable UpnpRendererHandler currentRendererHandler;
    private volatile List<StateOption> rendererStateOptionList = new ArrayList<>();

    @NonNullByDefault({})
    private ChannelUID rendererChannelUID;
    @NonNullByDefault({})
    private ChannelUID currentTitleChannelUID;

    private static final String DIRECTORY_ROOT = "0";
    private static final String UP = "..";

    private volatile String uriMetaData = "";
    private volatile List<UpnpEntry> resultList = new ArrayList<UpnpEntry>();
    private volatile int numberReturned;
    private volatile int totalMatches;

    private volatile String currentId = DIRECTORY_ROOT;
    private volatile @Nullable String currentSelection;
    private volatile Map<String, String> parentMap = new HashMap<>();

    private List<String> source = new ArrayList<>();

    private UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider;

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
        logger.debug("Initializing handler for media server device {}", thing.getLabel());

        // rendererChannelUID = new ChannelUID(thing.getUID(), UPNPRENDERER);
        rendererChannelUID = thing.getChannel(UPNPRENDERER).getUID();
        currentTitleChannelUID = thing.getChannel(CURRENTTITLE).getUID();

        if (service.isRegistered(this)) {
            initServer();
        }

        super.initialize();
    }

    private void initServer() {
        upnpRenderers.forEach((key, value) -> {
            StateOption stateOption = new StateOption(key, value.getThing().getLabel());
            rendererStateOptionList.add(stateOption);
        });
        updateStateDescription(rendererChannelUID, rendererStateOptionList);

        getProtocolInfo();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} for channel {} on server {}", command, channelUID, thing.getLabel());

        switch (channelUID.getId()) {
            case UPNPRENDERER:
                if (command instanceof StringType) {
                    currentRendererHandler = (upnpRenderers.get(((StringType) command).toString()));
                    updateTitleSelection(currentId, currentTitleChannelUID);
                }
                updateState(SELECT, OnOffType.OFF);
                updateState(SERVE, OnOffType.OFF);
                break;
            case CURRENTTITLE:
                if (command instanceof StringType) {
                    currentSelection = command.toString();
                }
                updateState(SELECT, OnOffType.OFF);
                updateState(SERVE, OnOffType.OFF);
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
                updateState(SERVE, OnOffType.OFF);
                break;
            case SERVE:
                if (command == OnOffType.ON) {
                    serveMedia();
                }
                updateState(SELECT, OnOffType.OFF);
                break;
        }
    }

    public void addRendererOption(String key) {
        rendererStateOptionList.add(new StateOption(key, upnpRenderers.get(key).getThing().getLabel()));
        updateStateDescription(rendererChannelUID, rendererStateOptionList);
        logger.debug("Renderer option {} added to {}", key, thing.getLabel());
    }

    public void removeRendererOption(String key) {
        UpnpRendererHandler handler = currentRendererHandler;
        if ((handler != null) && (handler.getThing().getUID().toString().equals(key))) {
            currentRendererHandler = null;
            updateState(rendererChannelUID, UnDefType.UNDEF);
        }
        rendererStateOptionList.removeIf(stateOption -> (stateOption.getValue().equals(key)));
        updateStateDescription(rendererChannelUID, rendererStateOptionList);
        logger.debug("Renderer option {} removed from {}", key, thing.getLabel());
    }

    private void updateTitleSelection(String browseTarget, ChannelUID currentTitleChannelUID) {
        browse(browseTarget, "BrowseDirectChildren", "*", "0", "0", "+dc:title");
        currentId = browseTarget;
        logger.debug("Navigating to node {} on server {}", currentId, thing.getLabel());

        List<UpnpEntry> list;

        // Optionally, filter only items that can be played on the renderer
        String filter = getConfig().get(CONFIG_FILTER).toString();
        logger.debug("Filtering content on server {}: {}", thing.getLabel(), filter);
        if (Boolean.parseBoolean(filter)) {
            list = filterEntries(resultList, true);
        } else {
            list = resultList;
        }

        List<StateOption> stateOptionList = new ArrayList<>();
        // Add a directory up selector if not in the directory root
        if ((!list.isEmpty() && !(list.get(0).getParentId().equals(DIRECTORY_ROOT)))
                || (list.isEmpty() && !currentId.equals(DIRECTORY_ROOT))) {
            StateOption stateOption = new StateOption(UP, UP);
            stateOptionList.add(stateOption);
            logger.debug("UP added to selection list");
        }
        list.forEach((value) -> {
            StateOption stateOption = new StateOption(value.getId(), value.getTitle());
            stateOptionList.add(stateOption);
            logger.debug("{} added to selection list", value.getId());

            // update the parentMap, so we can retract when UP gets selected
            String newSelection = value.getId();
            String parentId = value.getParentId();
            parentMap.put(newSelection, parentId);
            logger.debug("{} with parent {} added to parent map", newSelection, parentId);
        });
        updateStateDescription(currentTitleChannelUID, stateOptionList);

        // put the selector to first entry in list if available
        String current = null;
        if (!stateOptionList.isEmpty()) {
            current = stateOptionList.get(0).getLabel();
            if (current.equals(UP) && (stateOptionList.size() > 1)) {
                current = stateOptionList.get(1).getLabel();
            }
        }
        currentSelection = current;
        logger.debug("Current selection: {}", currentSelection);
        updateState(currentTitleChannelUID, new StringType(currentSelection));
    }

    private List<UpnpEntry> filterEntries(List<UpnpEntry> resultList, boolean includeContainers) {
        logger.debug("Raw result list {}", resultList);
        List<UpnpEntry> list = new ArrayList<>();
        if (currentRendererHandler != null) {
            List<String> sink = currentRendererHandler.getSink();
            list = resultList.stream()
                    .filter(entry -> (includeContainers && entry.isContainer())
                            || UpnpProtocolMatcher.testProtocolList(entry.getProtocolList(), sink))
                    .collect(Collectors.toList());
        }
        logger.debug("Filtered result list {}", list);
        return list;
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

        invokeAction("ContentDirectory", "Browse", inputs);
    }

    @Override
    public void onStatusChanged(boolean status) {
        logger.debug("Server status changed to {}", status);
        if (status) {
            initServer();
        }
        super.onStatusChanged(status);
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
                resultList.clear();
                resultList.addAll(UpnpXMLParser.getEntriesFromXML(value));
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
                    source.clear();
                    source.addAll(Arrays.asList(value.split(",")));
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

    private void serveMedia() {
        UpnpRendererHandler handler = currentRendererHandler;
        if (handler != null) {
            Queue<UpnpEntry> mediaQueue = new LinkedList<>();
            mediaQueue.addAll(filterEntries(resultList, false));
            handler.registerQueue(mediaQueue);
            logger.debug("Serving media queue {} from server {} to renderer {}.", mediaQueue, thing.getLabel(),
                    handler.getThing().getLabel());
        } else {
            logger.debug("Cannot serve media from server {}, no renderer selected.", thing.getLabel());
        }
    }
}
