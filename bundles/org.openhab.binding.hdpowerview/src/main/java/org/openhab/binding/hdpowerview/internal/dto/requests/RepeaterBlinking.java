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
package org.openhab.binding.hdpowerview.internal.dto.requests;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * State of a single Repeater for being updated by an HD PowerView Hub
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class RepeaterBlinking {
    public Repeater repeater;

    public class Repeater {
        public int id;
        public boolean blinkEnabled;

        public Repeater(int id, boolean enable) {
            this.id = id;
            this.blinkEnabled = enable;
        }
    }

    public RepeaterBlinking(int id, boolean enable) {
        repeater = new Repeater(id, enable);
    }
}
