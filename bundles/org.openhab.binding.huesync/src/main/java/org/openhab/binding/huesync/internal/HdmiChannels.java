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

/**
 * 
 * @author Patrik Gfeller - Initial contribution
 */
public class HdmiChannels {
    public String NAME;
    public String TYPE;
    public String MODE;
    public String STATUS;

    public HdmiChannels(String name, String type, String mode, String status) {
        NAME = name;
        TYPE = type;
        MODE = mode;
        STATUS = status;
    }
}
