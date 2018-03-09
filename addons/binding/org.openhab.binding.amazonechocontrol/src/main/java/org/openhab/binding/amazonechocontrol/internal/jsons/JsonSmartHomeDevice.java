/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.internal.jsons;

/**
 * The {@link JsonSmartHomeDevice} encapsulate the GSON-part data of a network query
 *
 * @author Michael Geramb - Initial contribution
 */
public class JsonSmartHomeDevice {
    public String entityId;
    public String friendlyName;
    public String[] actions;
    public String manufacturerName;
}
