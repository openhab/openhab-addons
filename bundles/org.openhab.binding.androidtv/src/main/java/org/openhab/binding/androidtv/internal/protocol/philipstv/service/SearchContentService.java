/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.api.PhilipsTVService;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.ComponentDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.ExtrasDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.IntentDTO;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application.LaunchAppDTO;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for toggling the Google Assistant on the Philips TV
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public class SearchContentService implements PhilipsTVService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PhilipsTVConnectionManager handler;

    private final ConnectionManager connectionManager;

    public SearchContentService(PhilipsTVConnectionManager handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        if (command instanceof StringType) {
            try {
                searchForContentOnTv(command.toString());
            } catch (Exception e) {
                if (isTvOfflineException(e)) {
                    logger.warn("Could not search content on Philips TV: TV is offline.");
                    handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
                } else if (isTvNotListeningException(e)) {
                    handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            TV_NOT_LISTENING_MSG);
                } else {
                    logger.warn("Error during the launch of search content on Philips TV: {}", e.getMessage(), e);
                }
            }
        } else if (!(command instanceof RefreshType)) {
            logger.warn("Unknown command: {} for Channel {}", command, channel);
        }
    }

    private void searchForContentOnTv(String searchContent) throws IOException {
        ExtrasDTO extrasDTO = new ExtrasDTO();
        extrasDTO.setQuery(searchContent);

        IntentDTO intentDTO = new IntentDTO(new ComponentDTO(), extrasDTO);
        intentDTO.setAction("android.search.action.GLOBAL_SEARCH");
        LaunchAppDTO launchAppDTO = new LaunchAppDTO(intentDTO);

        String searchContentLaunch = OBJECT_MAPPER.writeValueAsString(launchAppDTO);

        logger.debug("Search Content Launch json: {}", searchContentLaunch);
        connectionManager.doHttpsPost(LAUNCH_APP_PATH, searchContentLaunch);
    }
}
