/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;

/**
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SpecialCommand {
    public enum Direction {
        OUT,
        IN;
    }

    private final String PARAM_COMMAND_TO_REACT = "commandToReact";
    private final String PARAM_RESOURCE_ID = "resourceId";
    private final String PARAM_COMMAND_TO_SEND = "commandToSend";
    private final String PARAM_PULSE = "pulseWidth";

    private Direction direction;
    private String commandToReact;
    private int resourceId;
    private Command commandToSend;
    private int pulseWidth;

    public SpecialCommand(String item) {
        parseItem(item);
    }

    public Direction getDirection() {
        return direction;
    }

    public String getCommandToReact() {
        return commandToReact;
    }

    public int getResourceId() {
        return resourceId;
    }

    public Command getCommandToSend() {
        return commandToSend;
    }

    public int getPulseWidth() {
        return pulseWidth;
    }

    private void parseItem(String item) throws IllegalArgumentException {
        if (item.startsWith(">")) {
            direction = Direction.OUT;
            String[] items = item.replace(">(", "").replace(")", "").split(",");
            if (items.length == 0) {
                throw new IllegalArgumentException(String.format("Illegal parameter count"));
            }
            for (int i = 0; i < items.length; i++) {
                String[] keyValue = items[i].split("=");
                if (keyValue.length == 2) {
                    switch (keyValue[0].trim()) {
                        case PARAM_RESOURCE_ID:
                            try {
                                resourceId = Integer.parseInt(keyValue[1].trim());
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException(
                                        String.format("Illegal value in parameter %s", PARAM_RESOURCE_ID));
                            }
                            break;
                        case PARAM_PULSE:
                            try {
                                pulseWidth = Integer.parseInt(keyValue[1].trim().replace("ms", ""));
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException(
                                        String.format("Illegal value in parameter %s", PARAM_PULSE));
                            }
                            if (pulseWidth < 0) {
                                throw new IllegalArgumentException(
                                        String.format("Illegal value in parameter %s", PARAM_PULSE));
                            }
                            break;
                        case PARAM_COMMAND_TO_REACT:
                            commandToReact = keyValue[1].trim();
                            if ("".equals(commandToReact)) {
                                throw new IllegalArgumentException(
                                        String.format("Illegal value in parameter %s", PARAM_COMMAND_TO_REACT));
                            }
                            break;
                        case PARAM_COMMAND_TO_SEND:
                            switch (keyValue[1].trim()) {
                                case "ON":
                                    commandToSend = OnOffType.ON;
                                    break;

                                case "OFF":
                                    commandToSend = OnOffType.OFF;
                                    break;

                                default:
                                    throw new IllegalArgumentException(
                                            String.format("Illegal value in parameter %s", PARAM_COMMAND_TO_SEND));
                            }
                    }
                }
            }
        } else if (item.startsWith("<")) {
            direction = Direction.IN;
            String[] items = item.replace("<(", "").replace(")", "").split(",");
            if (items.length == 0) {
                throw new IllegalArgumentException(String.format("Illegal parameter count"));
            }
            for (int i = 0; i < items.length; i++) {
                String[] keyValue = items[i].split("=");
                if (keyValue.length == 2) {
                    switch (keyValue[0].trim()) {
                        case PARAM_RESOURCE_ID:
                            try {
                                resourceId = Integer.parseInt(keyValue[1].trim());
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException(
                                        String.format("Illegal value in parameter %s", PARAM_RESOURCE_ID));
                            }
                            break;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException(String.format("Illegal direction"));
        }
    }

    @Override
    public String toString() {
        String str = "";

        str += "[ direction=" + direction;
        str += ", commandToReact=" + commandToReact;
        str += ", resourceId=" + resourceId;
        str += ", commandToSend=" + commandToSend;
        str += ", pulseWidth=" + pulseWidth;
        str += " ]";

        return str;
    }
}
