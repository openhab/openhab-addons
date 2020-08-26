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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.CommandDescription;
import org.eclipse.smarthome.core.types.CommandDescriptionBuilder;
import org.eclipse.smarthome.core.types.CommandOption;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.upnpcontrol.internal.UpnpControlHandlerFactory;
import org.openhab.binding.upnpcontrol.internal.UpnpDynamicCommandDescriptionProvider;
import org.openhab.binding.upnpcontrol.internal.UpnpDynamicStateDescriptionProvider;
import org.openhab.binding.upnpcontrol.internal.UpnpEntry;
import org.openhab.binding.upnpcontrol.internal.UpnpProtocolMatcher;
import org.openhab.binding.upnpcontrol.internal.UpnpXMLParser;
import org.openhab.binding.upnpcontrol.internal.config.UpnpControlServerConfiguration;
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

    private static final String DIRECTORY_ROOT = "0";
    private static final String UP = "..";

    private final Logger logger = LoggerFactory.getLogger(UpnpServerHandler.class);

    private ConcurrentMap<String, UpnpRendererHandler> upnpRenderers;
    private volatile @Nullable UpnpRendererHandler currentRendererHandler;
    private volatile List<StateOption> rendererStateOptionList = Collections.synchronizedList(new ArrayList<>());

    private @NonNullByDefault({}) ChannelUID rendererChannelUID;
    private @NonNullByDefault({}) ChannelUID currentSelectionChannelUID;

    private volatile UpnpEntry currentEntry = new UpnpEntry(DIRECTORY_ROOT, DIRECTORY_ROOT, DIRECTORY_ROOT,
            "object.container");
    private volatile List<UpnpEntry> entries = Collections.synchronizedList(new ArrayList<>()); // current entry list in
                                                                                                // selection
    private volatile Map<String, UpnpEntry> parentMap = new HashMap<>(); // store parents in hierarchy separately to be
                                                                         // able to move up in directory structure

    private UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider;
    private UpnpDynamicCommandDescriptionProvider upnpCommandDescriptionProvider;

    protected @NonNullByDefault({}) UpnpControlServerConfiguration config;

    public UpnpServerHandler(Thing thing, UpnpIOService upnpIOService,
            ConcurrentMap<String, UpnpRendererHandler> upnpRenderers,
            UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider,
            UpnpDynamicCommandDescriptionProvider upnpCommandDescriptionProvider) {
        super(thing, upnpIOService);
        this.upnpRenderers = upnpRenderers;
        this.upnpStateDescriptionProvider = upnpStateDescriptionProvider;
        this.upnpCommandDescriptionProvider = upnpCommandDescriptionProvider;

        // put root as highest level in parent map
        parentMap.put(currentEntry.getId(), currentEntry);
    }

    @Override
    public void initialize() {
        super.initialize();
        config = getConfigAs(UpnpControlServerConfiguration.class);

        logger.debug("Initializing handler for media server device {}", thing.getLabel());

        Channel rendererChannel = thing.getChannel(UPNPRENDERER);
        if (rendererChannel != null) {
            rendererChannelUID = rendererChannel.getUID();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Channel " + UPNPRENDERER + " not defined");
            return;
        }
        Channel selectionChannel = thing.getChannel(BROWSE);
        if (selectionChannel != null) {
            currentSelectionChannelUID = selectionChannel.getUID();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Channel " + BROWSE + " not defined");
            return;
        }
        if (config.udn != null) {
            if (service.isRegistered(this)) {
                initServer();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Communication cannot be established with " + thing.getLabel());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No UDN configured for " + thing.getLabel());
        }
    }

    private void initServer() {
        rendererStateOptionList = Collections.synchronizedList(new ArrayList<>());
        synchronized (rendererStateOptionList) {
            upnpRenderers.forEach((key, value) -> {
                StateOption stateOption = new StateOption(key, value.getThing().getLabel());
                rendererStateOptionList.add(stateOption);
            });
        }
        updateStateDescription(rendererChannelUID, rendererStateOptionList);

        getProtocolInfo();

        browse(currentEntry.getId(), "BrowseDirectChildren", "*", "0", "0", config.sortcriteria);

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} for channel {} on server {}", command, channelUID, thing.getLabel());

        switch (channelUID.getId()) {
            case UPNPRENDERER:
                if (command instanceof StringType) {
                    currentRendererHandler = (upnpRenderers.get(((StringType) command).toString()));
                    if (config.filter) {
                        // only refresh title list if filtering by renderer capabilities
                        browse(currentEntry.getId(), "BrowseDirectChildren", "*", "0", "0", config.sortcriteria);
                    }
                } else if (command instanceof RefreshType) {
                    UpnpRendererHandler renderer = currentRendererHandler;
                    if (renderer != null) {
                        updateState(channelUID, StringType.valueOf(renderer.getThing().getLabel()));
                    }
                }
                break;
            case CURRENTID:
                String currentId = "";
                if (command instanceof StringType) {
                    currentId = String.valueOf(command);
                } else if (command instanceof RefreshType) {
                    currentId = currentEntry.getId();
                    updateState(channelUID, StringType.valueOf(currentId));
                }
                logger.debug("Setting currentId to {}", currentId);
                if (!currentId.isEmpty()) {
                    browse(currentId, "BrowseDirectChildren", "*", "0", "0", config.sortcriteria);
                }
            case BROWSE:
                if (command instanceof StringType) {
                    String browseTarget = command.toString();
                    if (browseTarget != null) {
                        if (!UP.equals(browseTarget)) {
                            final String target = browseTarget;
                            synchronized (entries) {
                                Optional<UpnpEntry> current = entries.stream()
                                        .filter(entry -> target.equals(entry.getId())).findFirst();
                                if (current.isPresent()) {
                                    currentEntry = current.get();
                                } else {
                                    logger.info("Trying to browse invalid target {}", browseTarget);
                                    browseTarget = UP; // move up on invalid target
                                }
                            }
                        }
                        if (UP.equals(browseTarget)) {
                            // Move up in tree
                            browseTarget = currentEntry.getParentId();
                            if (browseTarget.isEmpty()) {
                                // No parent found, so make it the root directory
                                browseTarget = DIRECTORY_ROOT;
                            }
                            currentEntry = parentMap.get(browseTarget);
                        }
                        updateState(CURRENTID, StringType.valueOf(currentEntry.getId()));
                        logger.debug("Browse target {}", browseTarget);
                        browse(browseTarget, "BrowseDirectChildren", "*", "0", "0", config.sortcriteria);
                    }
                }
                break;
            case SEARCH:
                if (command instanceof StringType) {
                    String criteria = command.toString();
                    if (criteria != null) {
                        String searchContainer = "";
                        if (currentEntry.isContainer()) {
                            searchContainer = currentEntry.getId();
                        } else {
                            searchContainer = currentEntry.getParentId();
                        }
                        if (searchContainer.isEmpty()) {
                            // No parent found, so make it the root directory
                            searchContainer = DIRECTORY_ROOT;
                        }
                        updateState(CURRENTID, StringType.valueOf(currentEntry.getId()));
                        logger.debug("Search container {} for {}", searchContainer, criteria);
                        search(searchContainer, criteria, "*", "0", "0", config.sortcriteria);
                    }
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
        synchronized (rendererStateOptionList) {
            rendererStateOptionList.add(new StateOption(key, upnpRenderers.get(key).getThing().getLabel()));
        }
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
        synchronized (rendererStateOptionList) {
            rendererStateOptionList.removeIf(stateOption -> (stateOption.getValue().equals(key)));
        }
        updateStateDescription(rendererChannelUID, rendererStateOptionList);
        logger.debug("Renderer option {} removed from {}", key, thing.getLabel());
    }

    private void updateTitleSelection(List<UpnpEntry> titleList) {
        logger.debug("Navigating to node {} on server {}", currentEntry.getId(), thing.getLabel());

        // Optionally, filter only items that can be played on the renderer
        logger.debug("Filtering content on server {}: {}", thing.getLabel(), config.filter);
        List<UpnpEntry> resultList = config.filter ? filterEntries(titleList, true) : titleList;

        List<CommandOption> commandOptionList = new ArrayList<>();
        // Add a directory up selector if not in the directory root
        if ((!resultList.isEmpty() && !(DIRECTORY_ROOT.equals(resultList.get(0).getParentId())))
                || (resultList.isEmpty() && !DIRECTORY_ROOT.equals(currentEntry.getId()))) {
            CommandOption commandOption = new CommandOption(UP, UP);
            commandOptionList.add(commandOption);
            logger.debug("UP added to selection list on server {}", thing.getLabel());
        }

        synchronized (entries) {
            entries.clear(); // always only keep the current selection in the entry map to keep memory usage down
            resultList.forEach((value) -> {
                CommandOption commandOption = new CommandOption(value.getId(), value.getTitle());
                commandOptionList.add(commandOption);
                logger.trace("{} added to selection list on server {}", value.getId(), thing.getLabel());

                // Keep the entries in a map so we can find the parent and container for the current selection to go
                // back up
                if (value.isContainer()) {
                    parentMap.put(value.getId(), value);
                }
                entries.add(value);
            });
        }

        // Set the currentId to the parent of the first entry in the list
        if (!resultList.isEmpty()) {
            updateState(CURRENTID, StringType.valueOf(resultList.get(0).getId()));
        }

        logger.debug("{} entries added to selection list on server {}", commandOptionList.size(), thing.getLabel());
        updateCommandDescription(currentSelectionChannelUID, commandOptionList);

        serveMedia();
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
        UpnpRendererHandler handler = currentRendererHandler;
        if (handler != null) {
            List<String> sink = handler.getSink();
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

    private void updateCommandDescription(ChannelUID channelUID, List<CommandOption> commandOptionList) {
        CommandDescription commandDescription = CommandDescriptionBuilder.create().withCommandOptions(commandOptionList)
                .build();
        upnpCommandDescriptionProvider.setDescription(channelUID, commandDescription);
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
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication lost with " + thing.getLabel());
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
                    updateTitleSelection(removeDuplicates(UpnpXMLParser.getEntriesFromXML(value)));
                } else {
                    updateTitleSelection(new ArrayList<UpnpEntry>());
                }
                break;
            case "Source":
            case "NumberReturned":
            case "TotalMatches":
            case "UpdateID":
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
        Set<String> refIdSet = new HashSet<>();
        final Set<String> idSet = list.stream().map(UpnpEntry::getId).collect(Collectors.toSet());
        list.forEach(entry -> {
            String refId = entry.getRefId();
            if (refId.isEmpty() || (!idSet.contains(refId)) && !refIdSet.contains(refId)) {
                newList.add(entry);
            }
            if (!refId.isEmpty()) {
                refIdSet.add(refId);
            }
        });
        return newList;
    }

    private void serveMedia() {
        UpnpRendererHandler handler = currentRendererHandler;
        if (handler != null) {
            ArrayList<UpnpEntry> mediaQueue = new ArrayList<>();
            mediaQueue.addAll(filterEntries(entries, false));
            if (mediaQueue.isEmpty() && !currentEntry.isContainer()) {
                mediaQueue.add(currentEntry);
            }
            if (mediaQueue.isEmpty()) {
                logger.debug("Nothing to serve from server {} to renderer {}", thing.getLabel(),
                        handler.getThing().getLabel());
            } else {
                handler.registerQueue(mediaQueue);
                logger.debug("Serving media queue {} from server {} to renderer {}", mediaQueue, thing.getLabel(),
                        handler.getThing().getLabel());
            }
        } else {
            logger.warn("Cannot serve media from server {}, no renderer selected", thing.getLabel());
        }
    }
}
