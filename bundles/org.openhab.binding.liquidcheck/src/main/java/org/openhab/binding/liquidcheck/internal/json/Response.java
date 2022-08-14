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
package org.openhab.binding.liquidcheck.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
<<<<<<< HEAD
 * The {@link Response} .
=======
 * The {@link Response} class contains the data that the device sends.
>>>>>>> eac3c23fa09d0130ae16dbdc99ddb83d1743b51d
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class Response {

    public Header header = new Header();
    public Payload payload = new Payload();
<<<<<<< HEAD
=======
    public Context context = new Context();
>>>>>>> eac3c23fa09d0130ae16dbdc99ddb83d1743b51d
}
