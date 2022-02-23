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
package org.openhab.binding.netatmo.internal.handler;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.handler.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.propertyhelper.NHCPropertyHelper;
import org.openhab.binding.netatmo.internal.handler.propertyhelper.PropertyHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.thing.Bridge;

/**
 * {@link ModuleHandler} is the base class for Netatmo weather station devices
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class ModuleHandler extends NetatmoHandler {

    public ModuleHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider, NetatmoServlet webhookServlet) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider, webhookServlet);
    }

    @Override
    protected PropertyHelper getPropertyHelper() {
        return new NHCPropertyHelper(getThing());
    }
}
