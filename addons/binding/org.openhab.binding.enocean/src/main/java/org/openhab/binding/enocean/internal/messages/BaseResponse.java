/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.enocean.internal.messages;

import org.openhab.binding.enocean.internal.transceiver.Helper;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class BaseResponse extends Response {

    public BaseResponse(Response response) {
        super(response.getPayload().length + response.getOptionalPayload().length, 0,
                Helper.concatAll(response.getPayload(), response.getOptionalPayload()));
    }

}
