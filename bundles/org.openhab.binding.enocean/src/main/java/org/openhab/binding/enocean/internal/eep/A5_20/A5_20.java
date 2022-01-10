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
package org.openhab.binding.enocean.internal.eep.A5_20;

import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 * Heating radiator valve actuating drive with feed and room temperature measurement, local set point control and
 * display
 *
 * @author Dominik Vorreiter - Initial contribution
 */
public class A5_20 extends _4BSMessage {

    public A5_20() {
        super();

        this.supportsTeachInVariation3 = true;
    }

    public A5_20(ERP1Message packet) {
        super(packet);

        this.supportsTeachInVariation3 = true;
    }
}
