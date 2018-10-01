/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol.message;

import org.openhab.binding.domintell.internal.protocol.model.ItemKey;
import org.openhab.binding.domintell.internal.protocol.model.SerialNumber;
import org.openhab.binding.domintell.internal.protocol.model.module.ModuleKey;
import org.openhab.binding.domintell.internal.protocol.model.type.ActionType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;
import org.openhab.binding.domintell.internal.protocol.model.type.SelectionProvider;

import java.text.NumberFormat;
import java.util.Locale;

/**
* The {@link ActionMessageBuilder} class is message builder for outgoing Domintell messages
*
* @author Gabor Bicskei - Initial contribution
*/
public class ActionMessageBuilder {
    /**
     * Temperature format
     */
    private static NumberFormat temperatureFormat;

    /**
     * Tuner frequency format
     */
    private static NumberFormat frequencyFormat;

    //initialize formats
    static {
        temperatureFormat = NumberFormat.getInstance(Locale.US);
        temperatureFormat.setMinimumIntegerDigits(2);
        temperatureFormat.setMaximumIntegerDigits(2);
        temperatureFormat.setMinimumFractionDigits(1);
        temperatureFormat.setMaximumFractionDigits(1);

        frequencyFormat = NumberFormat.getInstance(Locale.US);
        frequencyFormat.setMinimumIntegerDigits(3);
        frequencyFormat.setMaximumIntegerDigits(3);
        frequencyFormat.setMinimumFractionDigits(4);
        frequencyFormat.setMaximumFractionDigits(4);
    }

    /**
     * Module type
     */
    private ModuleType moduleType;

    /**
     * Serial number
     */
    private SerialNumber serialNumber;

    /**
     * IO number
     */
    private Integer ioNumber;

    /**
     * Requested action
     */
    private ActionType action;

    /**
     * Value to send
     */
    private Double numValue;

    private ActionMessageBuilder() {
    }

    public static ActionMessageBuilder create() {
         return new ActionMessageBuilder();
    }

    public ActionMessageBuilder withItemKey(ItemKey key) {
        this.moduleType = key.getModuleKey().getModuleType();
        this.serialNumber =key.getModuleKey().getSerialNumber();
        this.ioNumber = key.getIoNumber();

        return this;
    }

    public ActionMessageBuilder withModuleKey(ModuleKey key) {
        this.moduleType = key.getModuleType();
        this.serialNumber = key.getSerialNumber();
        return this;
    }

    public ActionMessageBuilder withIONumber(Integer ioNumber) {
        this.ioNumber = ioNumber;
        return this;
    }

    public ActionMessageBuilder withAction(ActionType action) {
        this.action = action;
        return this;
    }

    public ActionMessageBuilder withValue(Double numValue) {
        this.numValue = numValue;
        return this;
    }

    public ActionMessageBuilder withSelection(SelectionProvider selection) {
        this.numValue = (double)selection.getValue();
        return this;
    }

    /**
     * Format the message
     *
     * @return Message string
     */
    public String build() {
        StringBuilder sb = new StringBuilder(ItemKey.toLabel(moduleType, serialNumber, ioNumber));
        String[] actionString = action.getActionString();
        if (actionString.length == 1) {
            sb.append(actionString[0]);
            if (action.isDecimalValueNeeded()
                    || action.isFrequencyValueNeeded()
                    || action.isTemperatureValueNeeded()
                    || action.isSelectionValueNeeded()) {
                if (numValue != null) {
                    if (action.isSelectionValueNeeded() || action.isDecimalValueNeeded()) {
                        sb.append(numValue.intValue());
                    } else if (action.isTemperatureValueNeeded()) {
                        sb.append(temperatureFormat.format(numValue));
                    } else {
                        sb.append(frequencyFormat.format(numValue));
                    }
                } else {
                    throw new IllegalArgumentException("Missing value for action message: " + action);
                }
            }
        } else {
            String base = sb.toString();
            sb = new StringBuilder();
            for (String action: actionString) {
                sb.append("&").append(base).append(action);
            }
        }
        return sb.toString();
    }
}
