/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vallox;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ValloxBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hauke Fuhrmann - Initial contribution
 */
public class ValloxBindingConstants {

    public static final String BINDING_ID = "vallox";
    public static final ThingTypeUID THING_TYPE_VALLOX_SERIAL = new ThingTypeUID(BINDING_ID, "kwl90se");

    public static final String PARAMETER_HOST = "host";
    public static final String PARAMETER_PORT = "port";

}
