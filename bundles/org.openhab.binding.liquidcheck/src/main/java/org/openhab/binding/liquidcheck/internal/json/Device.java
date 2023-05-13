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
package org.openhab.binding.liquidcheck.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Device} is used for serializing and deserializing of JSONs.
 * It contains the device related data like firmware, hardware, name, the model class,
 * manufacturer, uuid and the security class.
 * 
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class Device {
    public String firmware = "";
    public String hardware = "";
    public String name = "";
    public Model model = new Model();
    public String manufacturer = "";
    public String uuid = "";
    public Security security = new Security();
}
