/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.controls;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.loxone.internal.types.LxUuid;

/**
 * A timed switch type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a switch control is:
 * <ul>
 * <li>a virtual input of switch type
 * <li>a push button function block
 * </ul>
 *
 * @author Stephan Brunner - initial contribution
 *
 */
class LxControlTimedSwitch extends LxControlPushbutton {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlTimedSwitch(uuid);
        }

        @Override
        String getType() {
            return "timedswitch";
        }
    }

    /**
     * deactivationDelay - countdown until the output is deactivated.
     * 0 = the output is turned off
     * -1 = the output is permanently on
     * otherwise it will count down from deactivationDelayTotal
     */
    private static final String STATE_DEACTIVATION_DELAY = "deactivationdelay";

    private LxControlTimedSwitch(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        ChannelUID id = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_NUMBER),
                defaultChannelLabel + " / Deactivation Delay", "Deactivation Delay", null, null,
                this::getDeactivationState);
        addChannelStateDescription(id, new StateDescription(new BigDecimal(-1), null, null, null, true, null));
    }

    private State getDeactivationState() {
        Double deactivationValue = getStateDoubleValue(STATE_DEACTIVATION_DELAY);
        if (deactivationValue != null) {
            if (deactivationValue.equals(-1.0)) {
                // we don't show the special value of -1 to the user, this means switch is on and delay is zero
                deactivationValue = 0.0;
            }
            return new DecimalType(deactivationValue);
        }
        return null;
    }

    @Override
    OnOffType getSwitchState() {
        /**
         * 0 = the output is turned off
         * -1 = the output is permanently on
         * otherwise it will count down from deactivationDelayTotal
         **/
        Double value = getStateDoubleValue(STATE_DEACTIVATION_DELAY);
        if (value != null) {
            if (value == -1.0 || value > 0.0) { // mapping
                return OnOffType.ON;
            } else if (value == 0) {
                return OnOffType.OFF;
            }
        }
        return null;
    }
}
