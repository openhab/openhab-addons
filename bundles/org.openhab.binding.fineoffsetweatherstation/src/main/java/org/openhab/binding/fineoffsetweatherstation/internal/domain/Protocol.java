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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.handler.ThingStatusListener;
import org.openhab.binding.fineoffsetweatherstation.internal.service.ELVGatewayQueryService;
import org.openhab.binding.fineoffsetweatherstation.internal.service.FineOffsetGatewayQueryService;
import org.openhab.binding.fineoffsetweatherstation.internal.service.GatewayQueryService;

/**
 * The protocol defining the way the data is parsed
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public enum Protocol {
    DEFAULT(FineOffsetGatewayQueryService::new),
    ELV(ELVGatewayQueryService::new);

    private final GatewayQueryServiceFactory queryServiceFactory;

    Protocol(GatewayQueryServiceFactory queryServiceFactory) {
        this.queryServiceFactory = queryServiceFactory;
    }

    public GatewayQueryService getGatewayQueryService(FineOffsetGatewayConfiguration config,
            @Nullable ThingStatusListener thingStatusListener, ConversionContext conversionContext) {
        return queryServiceFactory.newInstance(config, thingStatusListener, conversionContext);
    }

    private interface GatewayQueryServiceFactory {
        GatewayQueryService newInstance(FineOffsetGatewayConfiguration config,
                @Nullable ThingStatusListener thingStatusListener, ConversionContext conversionContext);
    }
}
