/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.UNAVAILABLE;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;

/**
 * The {@link SomfyTahomaBaseThingHandler} is base thing handler for all things.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public abstract class SomfyTahomaBaseThingHandler extends BaseThingHandler implements SomfyTahomaThingHandler {

    public SomfyTahomaBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    protected SomfyTahomaBridgeHandler getBridgeHandler() {
        return (SomfyTahomaBridgeHandler) this.getBridge().getHandler();
    }

    protected String getURL() {
        return getThing().getConfiguration().get("url").toString();
    }

    public void setAvailable() {
        if (!thing.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public void setUnavailable() {
        if (!thing.getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, UNAVAILABLE);
        }
    }

    public boolean isChannelLinked(Channel channel) {
        return isLinked(channel.getUID().getId());
    }

    public boolean needRefreshCommand() {
        return false;
    }
}
