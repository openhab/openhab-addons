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
package org.openhab.binding.myuplink.internal.command;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.myuplink.internal.handler.MyUplinkThingHandler;
import org.openhab.binding.myuplink.internal.model.ValidationException;

/**
 * base class for all commands that support paging. common logic should be implemented here
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public abstract class AbstractPagingCommand extends AbstractCommand {

    public AbstractPagingCommand(MyUplinkThingHandler handler, RetryOnFailure retryOnFailure,
            ProcessFailureResponse processFailureResponse, JsonResultProcessor resultProcessor) {
        super(handler, retryOnFailure, processFailureResponse, resultProcessor);
    }

    @Override
    protected Request prepareRequest(Request requestToPrepare) throws ValidationException {
        requestToPrepare.param(WEB_REQUEST_PARAM_PAGE_SIZE_KEY, String.valueOf(WEB_REQUEST_PARAM_PAGE_SIZE_VALUE));
        requestToPrepare.method(HttpMethod.GET);
        return requestToPrepare;
    }
}
