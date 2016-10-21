/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.command.http;

import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderMessage.ZoneMinderRequestType;

/**
 * Class that contains requests to a ZoneMinder Server.
 *
 * @author Martin S. Eskildsen
 */
public class ZoneMinderHttpServerRequest extends ZoneMinderHttpRequest {

    ZoneMinderHttpServerRequest(ZoneMinderRequestType _requestType, String id) {
        super(_requestType, ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER, id);
        // TODO Auto-generated constructor stub
    }

}
