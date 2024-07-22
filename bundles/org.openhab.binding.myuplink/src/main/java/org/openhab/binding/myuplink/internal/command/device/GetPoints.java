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
package org.openhab.binding.myuplink.internal.command.device;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.myuplink.internal.command.AbstractCommand;
import org.openhab.binding.myuplink.internal.command.JsonResultProcessor;
import org.openhab.binding.myuplink.internal.handler.MyUplinkThingHandler;

/**
 * implements the get points api call of the myUplink API.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GetPoints extends AbstractCommand {
    private final String url;

    public GetPoints(MyUplinkThingHandler handler, String deviceId, JsonResultProcessor resultProcessor) {
        // retry does not make much sense as it is a polling command, command should always succeed therefore update
        // handler on failure.
        super(handler, RetryOnFailure.NO, ProcessFailureResponse.YES, resultProcessor);
        this.url = GET_DEVICE_POINTS.replaceAll("\\{deviceId\\}", deviceId);
    }

    @Override
    protected String getURL() {
        return url;
    }

    @Override
    protected String getChannelGroup() {
        return EMPTY;
    }
}
