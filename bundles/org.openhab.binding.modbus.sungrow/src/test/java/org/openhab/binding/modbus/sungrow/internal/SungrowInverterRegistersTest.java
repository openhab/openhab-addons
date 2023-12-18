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
package org.openhab.binding.modbus.sungrow.internal;

import org.junit.jupiter.api.Test;

/**
 * Tests
 *
 * @author Sönke Küper - Initial contribution
 */
class SungrowInverterRegistersTest {

    @Test
    public void test() {
        for (SungrowInverterRegisters register : SungrowInverterRegisters.values()) {
            System.out.printf("\t<channel id=\"%s\" typeId=\"%s\"/>\n", register.name().toLowerCase(),
                    register.name().toLowerCase());

        }

        for (SungrowInverterRegisters register : SungrowInverterRegisters.values()) {
            System.out.printf(
                    "\t<channel-type id=\"%s\">\n" + "\t\t<item-type>%s</item-type>\n" + "\t\t<label>%s</label>\n"
                            + "\t\t<state pattern=\"%%.2f %%unit%%\" readOnly=\"true\"/>\n" + "\t</channel-type>\n",
                    register.name().toLowerCase(), register.getUnit(), register.name());

        }
    }
}
