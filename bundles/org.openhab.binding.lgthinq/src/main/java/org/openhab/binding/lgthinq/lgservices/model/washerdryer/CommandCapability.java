/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.washerdryer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.LGAPIVerion;
import org.openhab.binding.lgthinq.lgservices.model.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * The {@link CommandCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class CommandCapability {
    private boolean isPowerCommandsAvailable = false;
    private String powerOffCommand = "";
    private String stopCommand = "";
    private String wakeUpCommand = "";
    private static final Logger logger = LoggerFactory.getLogger(CommandCapability.class);

    public void loadCommands(JsonNode rootNode) throws LGThinqApiException {
        LGAPIVerion version = ModelUtils.discoveryAPIVersion(rootNode);
        switch (version) {
            case V1_0:
                logger.warn("Version {} for commands of Dryer/Washers not supported for this binding.",
                        version.getValue());
                return;
            case V2_0:
                JsonNode wifiNode = rootNode.path("ControlWifi");
                if (wifiNode.isMissingNode()) {
                    logger.warn(
                            "Dryer/Washer is missing ControlWifi node in the model. Commands are not supported for this model.");
                    return;
                }
                JsonNode wmOffNode = wifiNode.path("WMOff");
                JsonNode wmStopNode = wifiNode.path("WMStop");
                JsonNode wmWakeUpNode = wifiNode.path("WMWakeup");
                boolean isOffPresent = !wmOffNode.isMissingNode();
                boolean isStopPresent = !wmStopNode.isMissingNode();
                boolean isWakeUpPresent = !wmWakeUpNode.isMissingNode();
                if (isOffPresent || isStopPresent || isWakeUpPresent) {
                    isPowerCommandsAvailable = true;
                    powerOffCommand = isOffPresent
                            ? wmOffNode.path("data").path("washerDryer").path("controlDataType").textValue()
                            : "";
                    stopCommand = isStopPresent
                            ? wmStopNode.path("data").path("washerDryer").path("controlDataType").textValue()
                            : "";
                    wakeUpCommand = isWakeUpPresent
                            ? wmWakeUpNode.path("data").path("washerDryer").path("controlDataType").textValue()
                            : "";
                }
        }
    }

    public boolean isPowerCommandsAvailable() {
        return isPowerCommandsAvailable;
    }

    public void setPowerCommandsAvailable(boolean powerCommandsAvailable) {
        isPowerCommandsAvailable = powerCommandsAvailable;
    }

    public String getPowerOffCommand() {
        return powerOffCommand;
    }

    public String getStopCommand() {
        return stopCommand;
    }

    public String getWakeUpCommand() {
        return wakeUpCommand;
    }
}
