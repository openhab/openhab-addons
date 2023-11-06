/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tplinksmarthome.internal.model;

import org.openhab.core.library.types.OnOffType;

import com.google.gson.annotations.Expose;

/**
 * Data class to set the led of a Smart Home device on or off.
 * Only setter methods as the object is only used to send values.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class SetLedOff extends ContextState implements HasErrorResponse {

    public static class LedOff extends ErrorResponse {
        @Expose
        private int off;

        public void setOff(int off) {
            this.off = off;
        }

        @Override
        public String toString() {
            return "off:" + off + super.toString();
        }
    }

    public static class System {
        @Expose
        private LedOff setLedOff = new LedOff();

        @Override
        public String toString() {
            return "set_led_off:{" + setLedOff + "}";
        }
    }

    @Expose
    private System system = new System();

    @Override
    public ErrorResponse getErrorResponse() {
        return system.setLedOff;
    }

    public void setLed(OnOffType onOff) {
        system.setLedOff.setOff(onOff == OnOffType.OFF ? 1 : 0);
    }

    @Override
    public String toString() {
        return "SetLedOff {system:{" + system + "}";
    }
}
