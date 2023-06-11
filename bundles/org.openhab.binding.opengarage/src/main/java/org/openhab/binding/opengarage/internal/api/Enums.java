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
package org.openhab.binding.opengarage.internal.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container class for enums related to opengarage
 *
 * @author Paul Smedley <paul@smedley.id.au> - Initial contribution
 *
 */
public class Enums {
    public enum OpenGarageCommand {
        OPEN("open"),
        CLOSE("close"),
        CLICK("click");

        private static final Logger LOGGER = LoggerFactory.getLogger(OpenGarageCommand.class);
        private final String value;

        OpenGarageCommand(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
