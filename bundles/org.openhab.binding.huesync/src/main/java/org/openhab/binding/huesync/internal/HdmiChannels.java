/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.huesync.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * 
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class HdmiChannels {
    public String name;
    public String type;
    public String mode;
    public String status;

    public HdmiChannels(String name, String type, String mode, String status) {
        this.name = name;
        this.type = type;
        this.mode = mode;
        this.status = status;
    }
}
