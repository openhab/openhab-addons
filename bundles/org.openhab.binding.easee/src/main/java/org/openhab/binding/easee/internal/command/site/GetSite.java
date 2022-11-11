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
package org.openhab.binding.easee.internal.command.site;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.easee.internal.command.AbstractCommand;
import org.openhab.binding.easee.internal.command.JsonResultProcessor;
import org.openhab.binding.easee.internal.handler.EaseeBridgeHandler;

/**
 * implements the get sites api call of the site.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class GetSite extends AbstractCommand {

    public GetSite(EaseeBridgeHandler handler, JsonResultProcessor resultProcessor) {
        // retry does not make much sense as it is a polling command, command should always succeed therefore update
        // handler on failure.
        super(handler, RetryOnFailure.NO, ProcessFailureResponse.YES, resultProcessor);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) {
        requestToPrepare.method(HttpMethod.GET);
        return requestToPrepare;
    }

    @Override
    protected String getURL() {
        String url = GET_SITE_URL;
        url = url.replaceAll("\\{siteId\\}", handler.getBridgeConfiguration().getSiteId());
        return url;
    }

    @Override
    protected String getChannelGroup() {
        return CHANNEL_GROUP_SITE_INFO;
    }
}
