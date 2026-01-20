/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.handler.matter;

import static org.openhab.binding.dirigera.internal.Constants.*;
import static org.openhab.binding.dirigera.internal.interfaces.Model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;

/**
 * {@link LinkHandler} stores all active connections of a device
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class LinkHandler {
    /*
     * hardlinks initialized with invalid links because the first update shall trigger a link update. If it's declared
     * as empty no link update will be triggered. This is necessary for startup phase.
     */
    private final List<String> linkCandidateTypes = new ArrayList<>();
    private final List<String> hardLinks = new ArrayList<>();
    private final List<String> softLinks = new ArrayList<>();
    private final BaseMatterHandler handler;
    private final String deviceId;

    public LinkHandler(final BaseMatterHandler handler, final String deviceId, final List<String> linkCandidateTypes) {
        this.handler = handler;
        this.deviceId = deviceId;
        this.linkCandidateTypes.addAll(linkCandidateTypes);
    }

    /**
     * Link commands for adding or removing links
     */
    public void handleCommand(ChannelUID channelUID, Command command) {
        String targetChannel = channelUID.getIdWithoutGroup();
        switch (targetChannel) {
            case CHANNEL_LINKS:
                if (command instanceof StringType string) {
                    linkUpdate(string.toFullString(), false);
                }
                break;
            case CHANNEL_LINK_CANDIDATES:
                if (command instanceof StringType string) {
                    linkUpdate(string.toFullString(), true);
                }
                break;
        }
    }

    /**
     * Handle changes in remoteLinks attribute
     *
     * @param update JSON
     */
    public void handleUpdate(JSONObject update) {
        if (update.has(ATTRIBUTES_KEY_REMOTE_LINKS)) {
            JSONArray remoteLinks = update.getJSONArray(ATTRIBUTES_KEY_REMOTE_LINKS);
            List<String> updateList = new ArrayList<>();
            remoteLinks.forEach(link -> {
                updateList.add(link.toString());
            });
            Collections.sort(updateList);
            Collections.sort(hardLinks);
            if (!hardLinks.equals(updateList)) {
                hardLinks.clear();
                hardLinks.addAll(updateList);
                // just update internal link list and let the gateway update do all updates regarding soft links
                handler.gateway().updateLinks();
            }
        }
    }

    /**
     * Update cycle of gateway is starting
     */
    public void updateLinksStart() {
        softLinks.clear();
    }

    /**
     * Get real links from remoteLinks attribute ()hard links)
     *
     * @return links attached to this device
     */
    public List<String> getLinks() {
        return new ArrayList<String>(hardLinks);
    }

    /**
     * Add link towards another device. For controllers or sensors the link is added to the attached device e.g. light
     * or outlet. For other devices the link is added in remoteLinks attribute
     *
     * @param linkedDeviceId id of the device to link to
     * @param add as boolean to add (true) or remove (false) the link
     */
    private void linkUpdate(String linkedDeviceId, boolean add) {
        /**
         * link has to be set to target device like light or outlet, not to the device which triggers an action like
         * lightController or motionSensor
         */
        String targetDevice = "";
        String triggerDevice = "";
        List<String> linksToSend = new ArrayList<>();
        if (handler.isControllerOrSensor()) {
            // request needs to be sent to target device
            targetDevice = linkedDeviceId;
            triggerDevice = deviceId;
            // get current links
            JSONObject deviceData = handler.gateway().model().getAllFor(targetDevice, MODEL_KEY_DEVICES);
            if (deviceData.has(ATTRIBUTES_KEY_REMOTE_LINKS)) {
                JSONArray jsonLinks = deviceData.getJSONArray(ATTRIBUTES_KEY_REMOTE_LINKS);
                jsonLinks.forEach(link -> {
                    linksToSend.add(link.toString());
                });
                // this is sensor branch so add link of sensor
                if (add) {
                    if (!linksToSend.contains(triggerDevice)) {
                        linksToSend.add(triggerDevice);
                    }
                } else {
                    if (linksToSend.contains(triggerDevice)) {
                        linksToSend.remove(triggerDevice);
                    }
                }
            }
        } else {
            // send update to this device
            targetDevice = deviceId;
            triggerDevice = linkedDeviceId;
            if (add) {
                hardLinks.add(triggerDevice);
            } else {
                hardLinks.remove(triggerDevice);
            }
            linksToSend.addAll(hardLinks);
        }
        JSONArray newLinks = new JSONArray(linksToSend);
        JSONObject attributes = new JSONObject();
        attributes.put(ATTRIBUTES_KEY_REMOTE_LINKS, newLinks);
        handler.gateway().api().sendPatch(targetDevice, attributes);
        // after api command remoteLinks property will be updated and trigger new linkUpadte
    }

    /**
     * Adds a soft link towards the device which has the link stored in remoteLinks attributes
     *
     * @param device id of the device which contains this link
     */
    public void addSoftlink(String id) {
        if (!softLinks.contains(id) && !handler.getDeviceId().equals(id)) {
            softLinks.add(id);
        }
    }

    /**
     * Update link and candidates channels with current links
     */
    public List<CommandOption> getLinkOptions() {
        List<String> allLinks = new ArrayList<>();
        allLinks.addAll(hardLinks);
        allLinks.addAll(softLinks);
        return getOptions(allLinks);
    }

    public List<CommandOption> getCandidateOptions() {
        List<String> possibleCandidates = handler.gateway().model().getDevicesForTypes(linkCandidateTypes);
        List<String> candidates = new ArrayList<>();
        possibleCandidates.forEach(entry -> {
            if (!hardLinks.contains(entry) && !softLinks.contains(entry)) {
                candidates.add(entry);
            }
        });
        return getOptions(candidates);
    }

    private List<CommandOption> getOptions(List<String> entries) {
        List<CommandOption> options = new ArrayList<>();
        Collections.sort(entries);
        entries.forEach(entry -> {
            String customName = handler.gateway().model().getCustonNameFor(entry);
            if (!handler.gateway().isKnownDevice(entry)) {
                // if device isn't present in OH attach this suffix
                customName += " (!)";
            }
            options.add(new CommandOption(entry, customName));
        });
        return options;
    }
}
