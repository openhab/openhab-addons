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
import org.openhab.binding.myuplink.internal.model.SmartHomeModeResponseTransformer;

/**
 * implements the get sites api call of the site.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GetSmartHomeMode extends AbstractCommand {
    private String url;

    public GetSmartHomeMode(MyUplinkThingHandler handler, String systemId, JsonResultProcessor resultProcessor) {
        // retry does not make much sense as it is a polling command, command should always succeed therefore update
        // handler on failure.
        super(handler, new SmartHomeModeResponseTransformer(handler), RetryOnFailure.NO, ProcessFailureResponse.YES,
                resultProcessor);
        this.url = GET_SMART_HOME_MODE_URL.replaceAll("\\{systemId\\}", systemId);
    }

    @Override
    protected String getURL() {
        return this.url;
    }

    @Override
    protected String getChannelGroup() {
        return EMPTY;
    }
}
