/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.smappee.service;

/**
 * Where is the Smappee located ?
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SmappeeServiceLocation {
    public int serviceLocationId;
    public String name;
}

// Example JSON received from the Smappee API :
// {
// "appName": "MyFirstApp",
// "serviceLocations": [
// {
// "serviceLocationId": 12345,
// "name": "Home"
// }
// ]
// }
