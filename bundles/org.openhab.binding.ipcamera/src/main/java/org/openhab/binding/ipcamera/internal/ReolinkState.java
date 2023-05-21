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
package org.openhab.binding.ipcamera.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ReolinkState} class holds the state and GSON parsed replies for a single Reolink Camera.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class ReolinkState {
    public class GetAiStateResponse {
        public class Value {
            public class Alarm {
                public int alarm_state = 0;
                public int support = 0;
            }

            public int channel = 0;
            public Alarm dog_cat = new Alarm();
            public Alarm face = new Alarm();
            public Alarm people = new Alarm();
            public Alarm vehicle = new Alarm();
        }

        public String cmd = "";
        public int code = 0;
        public Value value = new Value();
    }
}
