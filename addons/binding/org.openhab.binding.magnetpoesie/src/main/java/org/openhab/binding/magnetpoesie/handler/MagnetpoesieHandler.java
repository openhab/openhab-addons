/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.magnetpoesie.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.magnetpoesie.MagnetpoesieBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MagnetpoesieHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Yasemin Dogan - Initial contribution
 */
public class MagnetpoesieHandler extends BaseThingHandler {

    private List whos = new ArrayList();
    private List wheres = new ArrayList();
    private List whens = new ArrayList();
    private List whats = new ArrayList();
    private List generals = new ArrayList();

    private Logger logger = LoggerFactory.getLogger(MagnetpoesieHandler.class);

    public MagnetpoesieHandler(Thing thing) {
        super(thing);
    }

    private void addWordsToCategory(Command word, List category) {
        if (word.toString().contains(" ")) {
            String[] words = word.toString().split("\\s+");
            Collections.addAll(category, words);
        } else {
            category.add(word);

        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case MagnetpoesieBindingConstants.CATEGORIE_WHO:
                if (command instanceof RefreshType) {
                    break;
                }
                addWordsToCategory(command, whos);
                logger.debug("WHO ADDED: '{}' in '{}' ", command, channelUID);

                break;
            case MagnetpoesieBindingConstants.CATEGORIE_WHERE:
                if (command instanceof RefreshType) {
                    break;
                }
                addWordsToCategory(command, wheres);
                logger.debug("WHERE ADDED: '{}' in '{}' ", command, channelUID);
                break;
            case MagnetpoesieBindingConstants.CATEGORIE_WHEN:
                if (command instanceof RefreshType) {
                    break;
                }
                addWordsToCategory(command, whens);
                logger.debug("WHEN ADDED: '{}' in '{}' ", command, channelUID);
                break;
            case MagnetpoesieBindingConstants.CATEGORIE_WHAT:
                if (command instanceof RefreshType) {
                    break;
                }
                addWordsToCategory(command, whats);
                logger.debug("WHAT ADDED: '{}' in '{}' ", command, channelUID);
                break;
            case MagnetpoesieBindingConstants.CATEGORIE_GENERAL:
                if (command instanceof RefreshType) {
                    break;
                }
                addWordsToCategory(command, generals);
                logger.debug("GENERAL ADDED: '{}' in '{}' ", command, channelUID);

        }

        if (channelUID.getId().equals(MagnetpoesieBindingConstants.SAVE)) {
            if (command.toString().equals("ON")) {
                // TODO: send lists to classes
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");

        }

    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
