/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.phc.handler;

import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.phc.PHCBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PHCHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Hohaus - Initial contribution
 *
 */
public class PHCHandler extends BaseThingHandler {

  private Logger logger = LoggerFactory.getLogger(PHCHandler.class);

  String moduleAddress; // like DIP switches
  PHCBridgeHandler bridgeHandler = null;

  public PHCHandler(Thing thing) {
    super(thing);

  }

  @Override
  public void initialize() {

    logger.debug("Initializing PHC thing.");
    updateStatus(ThingStatus.INITIALIZING);
    try {

      moduleAddress = getConfig().get(PHCBindingConstants.ADDRESS).toString();

      if (getPHCBridgeHandler() == null) {
        return;
      }
      if (getBridge().getStatus() == ThingStatus.ONLINE) {
        updateStatus(ThingStatus.ONLINE);
      } else {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
      }

    } catch (Exception e) {
      logger.error("Exception while initializing. ", e);
    }
  }

  public void handleIncoming(String type, OnOffType state) {
    postCommand(type, state);
  }

  @Override
  public void handleCommand(ChannelUID channelUID, Command command) {
    if (!channelUID.getGroupId().equals(PHCBindingConstants.CHANNELS_EM)) {
      getPHCBridgeHandler().send(channelUID.getGroupId(), new StringBuilder(moduleAddress).reverse().toString(),
          channelUID.getIdWithoutGroup(), command);
    }
  }

  @Override
  public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
    if (configurationParameters.containsKey(PHCBindingConstants.UP_DOWN_TIME)) {
      if (configurationParameters.containsKey(PHCBindingConstants.ADDRESS)) {
        configurationParameters.remove(PHCBindingConstants.ADDRESS);
        super.handleConfigurationUpdate(configurationParameters);
      }
    }
  }

  private PHCBridgeHandler getPHCBridgeHandler() {
    if (bridgeHandler == null) {
      Bridge bridge = getBridge();
      if (bridge == null) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
            "The Thing requires to select a Bridge");
        return null;
      }

      ThingHandler handler = bridge.getHandler();
      if (handler instanceof PHCBridgeHandler) {
        bridgeHandler = (PHCBridgeHandler) handler;
      } else {
        logger.debug("No available bridge handler for {}.", bridge.getUID());
        return null;
      }
    }

    return bridgeHandler;
  }
}
