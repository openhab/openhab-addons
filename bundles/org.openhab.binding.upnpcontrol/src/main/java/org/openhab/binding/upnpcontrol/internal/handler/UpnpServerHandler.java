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
package org.openhab.binding.upnpcontrol.internal.handler;

import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.upnpcontrol.internal.UpnpControlHandlerFactory;
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
 * @author Karel Goderis - Based on UPnP logic in Sonos binding
 */
@NonNullByDefault
public class UpnpServerHandler extends UpnpHandler {

    private final Logger logger = LoggerFactory.getLogger(UpnpServerHandler.class);

    private ConcurrentMap<String, UpnpRendererHandler> upnpRenderers;
    private volatile @Nullable UpnpRendererHandler currentRendererHandler;
    private volatile List<StateOption> rendererStateOptionList = new ArrayList<>();

    @NonNullByDefault({})
    private ChannelUID rendererChannelUID;
    @NonNullByDefault({})
    private ChannelUID currentTitleChannelUID;

    private static final String DIRECTORY_ROOT = "0";
    private static final String UP = "..";

    private volatile List<UpnpEntry> resultList = new ArrayList<UpnpEntry>();
    private volatile int numberReturned;
    private volatile int totalMatches;

    private volatile String currentId = DIRECTORY_ROOT;
    private volatile @Nullable String currentSelection;
    private volatile @Nullable String searchCriteria;
    private volatile Map<String, UpnpEntry> entryMap = new HashMap<>();

    private List<String> source = new ArrayList<>();

    private UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider;

    public UpnpServerHandler(Thing thing, UpnpIOService upnpIOService,
            ConcurrentMap<String, UpnpRendererHandler> upnpRenderers,
            UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider) {
        super(thing, upnpIOService);
        this.upnpRenderers = upnpRenderers;
        this.upnpStateDescriptionProvider = upnpStateDescriptionProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for media server device {}", thing.getLabel());

        rendererChannelUID = thing.getChannel(UPNPRENDERER).getUID();
        currentTitleChannelUID = thing.getChannel(CURRENTTITLE).getUID();

        if (service.isRegistered(this)) {
            initServer();
        }

        super.initialize();
    }

    private void initServer() {
        rendererStateOptionList = new ArrayList<>();
        upnpRenderers.forEach((key, value) -> {
            StateOption stateOption = new StateOption(key, value.getThing().getLabel());
            rendererStateOptionList.add(stateOption);
        });
        updateStateDescription(rendererChannelUID, rendererStateOptionList);

        getProtocolInfo();

        browse(currentId, "BrowseDirectChildren", "*", "0", "0", getConfig().get(SORT_CRITERIA).toString());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} for channel {} on server {}", command, channelUID, thing.getLabel());

        switch (channelUID.getId()) {
            case UPNPRENDERER:
                if (command instanceof StringType) {
                    currentRendererHandler = (upnpRenderers.get(((StringType) command).toString()));
                    if (Boolean.parseBoolean(getConfig().get(CONFIG_FILTER).toString())) {
                        // only refresh title list if filtering by renderer capabilities
                        browse(currentId, "BrowseDirectChildren", "*", "0", "0",
                                getConfig().get(SORT_CRITERIA).toString());
                    }
                } else if ((command instanceof RefreshType) && (currentRendererHandler != null)) {
                    updateState(channelUID, StringType.valueOf(currentRendererHandler.getThing().getLabel()));
                }
                break;
            case CURRENTTITLE:
                if (command instanceof StringType) {
                    currentSelection = command.toString();
                } else if (command instanceof RefreshType) {
                    updateState(channelUID, StringType.valueOf(currentSelection));
                }
                break;
            case SELECT:
                if (command == OnOffType.ON) {
                    String browseTarget = currentSelection;
                    if (browseTarget != null) {
                        if (UP.equals(browseTarget)) {
                            // Move up in tree
                            if (entryMap.get(currentId) != null) {
                                // Parent can be empty
                                browseTarget = entryMap.get(currentId).getParentId();
                            }
                            if (browseTarget.isEmpty() || UP.equals(browseTarget)) {
                                // No parent found, so make it the root directory
                                browseTarget = DIRECTORY_ROOT;
                            }
                        }
                        logger.debug("Browse target {}", browseTarget);
                        currentId = browseTarget;
                        browse(currentId, "BrowseDirectChildren", "*", "0", "0",
                                getConfig().get(SORT_CRITERIA).toString());
                    }
                }
                break;
            case SEARCHCRITERIA:
                if (command instanceof StringType) {
                    searchCriteria = command.toString();
                } else if (command instanceof RefreshType) {
                    updateState(channelUID, StringType.valueOf(searchCriteria));
                }
                break;
            case SEARCH:
                if (command == OnOffType.ON) {
                    String criteria = this.searchCriteria;
                    if (criteria != null) {
                        String searchContainer = "";
                        if (entryMap.get(currentId) != null) {
                            if (entryMap.get(currentId).isContainer()) {
                                searchContainer = currentId;
                            } else {
                                // Parent can be empty
                                searchContainer = entryMap.get(currentId).getParentId();
                            }
                        }
                        if (searchContainer.isEmpty()) {
                            // No parent found, so make it the root directory
                            searchContainer = DIRECTORY_ROOT;
                        }
                        logger.debug("Search container {} for {}", searchContainer, criteria);
                        search(searchContainer, criteria, "*", "0", "0", getConfig().get(SORT_CRITERIA).toString());
                    } else {
                        logger.warn("No search criteria defined.");
                    }
                }
                break;
            case SERVE:
                if (command == OnOffType.ON) {
                    serveMedia();
                }
                break;
        }
    }

    /**
     * Add a renderer to the renderer channel state option list.
     * This method is called from the {@link UpnpControlHandlerFactory} class when creating a renderer handler.
     *
     * @param key
     */
    public void addRendererOption(String key) {
        rendererStateOptionList.add(new StateOption(key, upnpRenderers.get(key).getThing().getLabel()));
        updateStateDescription(rendererChannelUID, rendererStateOptionList);
        logger.debug("Renderer option {} added to {}", key, thing.getLabel());
    }

    /**
     * Remove a renderer from the renderer channel state option list.
     * This method is called from the {@link UpnpControlHandlerFactory} class when removing a renderer handler.
     *
     * @param key
     */
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

    private void updateTitleSelection() {
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
        if ((!list.isEmpty() && !(DIRECTORY_ROOT.equals(list.get(0).getParentId())))
                || (list.isEmpty() && !currentId.equals(DIRECTORY_ROOT))) {
            StateOption stateOption = new StateOption(UP, UP);
            stateOptionList.add(stateOption);
            logger.debug("UP added to selection list on server {}", thing.getLabel());
        }
        list.forEach((value) -> {
            StateOption stateOption = new StateOption(value.getId(), value.getTitle());
            stateOptionList.add(stateOption);
            logger.trace("{} added to selection list on server {}", value.getId(), thing.getLabel());

            // Keep the entries in a map so we can find the parent info to go back up
            entryMap.put(value.getId(), value);
        });
        logger.debug("{} entries added to selection list on server {}", stateOptionList.size(), thing.getLabel());
        updateStateDescription(currentTitleChannelUID, stateOptionList);

        // put the selector to first entry in list if available
        StateOption current = null;
        if (!stateOptionList.isEmpty()) {
            current = stateOptionList.get(0);
            if (UP.equals(current.getLabel()) && (stateOptionList.size() > 1)) {
                current = stateOptionList.get(1);
            }
        }
        currentSelection = (current != null) ? current.getValue() : null;
        logger.debug("Current selection: {}", currentSelection);
        updateState(currentTitleChannelUID, new StringType(currentSelection));
    }

    /**
     * Filter a list of media and only keep the media that are playable on the currently selected renderer.
     *
     * @param resultList
     * @param includeContainers
     * @return
     */
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
        StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withReadOnly(false)
                .withOptions(stateOptionList).build().toStateDescription();
        upnpStateDescriptionProvider.setDescription(channelUID, stateDescription);
    }

    /**
     * Method that does a UPnP browse on a content directory. Results will be retrieved in the {@link onValueReceived}
     * method.
     *
     * @param objectID content directory object
     * @param browseFlag BrowseMetaData or BrowseDirectChildren
     * @param filter properties to be returned
     * @param startingIndex starting index of objects to return
     * @param requestedCount number of objects to return, 0 for all
     * @param sortCriteria sort criteria, example: +dc:title
     */
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

    /**
     * Method that does a UPnP search on a content directory. Results will be retrieved in the {@link onValueReceived}
     * method.
     *
     * @param containerID content directory container
     * @param searchCriteria search criteria, examples:
     *            dc:title contains "song"
     *            dc:creator contains "Springsteen"
     *            upnp:class = "object.item.audioItem"
     *            upnp:album contains "Born in"
     * @param filter properties to be returned
     * @param startingIndex starting index of objects to return
     * @param requestedCount number of objects to return, 0 for all
     * @param sortCriteria sort criteria, example: +dc:title
     */
    public void search(String containerID, String searchCriteria, String filter, String startingIndex,
            String requestedCount, String sortCriteria) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("ContainerID", containerID);
        inputs.put("SearchCriteria", searchCriteria);
        inputs.put("Filter", filter);
        inputs.put("StartingIndex", startingIndex);
        inputs.put("RequestedCount", requestedCount);
        inputs.put("SortCriteria", sortCriteria);

        invokeAction("ContentDirectory", "Search", inputs);
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
        logger.debug("Upnp device {} received variable {} with value {} from service {}", thing.getLabel(), variable,
                value, service);
        if (variable == null) {
            return;
        }
        switch (variable) {
            case "Result":
                if (!((value == null) || (value.isEmpty()))) {
                    resultList = removeDuplicates(UpnpXMLParser.getEntriesFromXML(value));
                } else {
                    resultList = new ArrayList<UpnpEntry>();
                }
                updateTitleSelection();
                break;
            case "NumberReturned":
                if (!((value == null) || (value.isEmpty()))) {
                    numberReturned = Integer.parseInt(value);
                }
                break;
            case "TotalMatches":
                if (!((value == null) || (value.isEmpty()))) {
                    totalMatches = Integer.parseInt(value);
                }
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

    /**
     * Remove double entries by checking the refId if it exists as Id in the list and only keeping the original entry if
     * available. If the original entry is not in the list, only keep one referring entry.
     *
     * @param list
     * @return filtered list
     */
    private List<UpnpEntry> removeDuplicates(List<UpnpEntry> list) {
        List<UpnpEntry> newList = new ArrayList<>();
        List<String> refIdList = new ArrayList<>();
        list.forEach(entry -> {
            String refId = entry.getRefId();
            if (refId.isEmpty()
                    || (list.stream().noneMatch(any -> (refId.equals(any.getId()))) && !refIdList.contains(refId))) {
                newList.add(entry);
            }
            if (!refId.isEmpty()) {
                refIdList.add(refId);
            }
        });
        return newList;
    }

    private void serveMedia() {
        UpnpRendererHandler handler = currentRendererHandler;
        if (handler != null) {
            LinkedList<UpnpEntry> mediaQueue = new LinkedList<>();
            mediaQueue.addAll(filterEntries(resultList, false));
            handler.registerQueue(mediaQueue);
            logger.debug("Serving media queue {} from server {} to renderer {}.", mediaQueue, thing.getLabel(),
                    handler.getThing().getLabel());
        } else {
            logger.warn("Cannot serve media from server {}, no renderer selected.", thing.getLabel());
        }
    }
}
