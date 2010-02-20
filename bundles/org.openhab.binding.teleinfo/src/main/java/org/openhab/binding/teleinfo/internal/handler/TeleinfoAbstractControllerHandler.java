/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.teleinfo.internal.TeleinfoDiscoveryService;
import org.openhab.binding.teleinfo.internal.dto.Frame;

/**
 * The {@link TeleinfoAbstractControllerHandler} class defines a skeleton for controller handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public abstract class TeleinfoAbstractControllerHandler extends BaseBridgeHandler {

    private List<TeleinfoControllerHandlerListener> listeners = Collections.synchronizedList(new ArrayList<>());

    public TeleinfoAbstractControllerHandler(Bridge bridge) {
        super(bridge);
    }

    public void addListener(final TeleinfoControllerHandlerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final TeleinfoControllerHandlerListener listener) {
        listeners.remove(listener);
    }

    protected void fireOnFrameReceivedEvent(final Frame frame) {
        listeners.forEach(l -> l.onFrameReceived(this, frame));
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(TeleinfoDiscoveryService.class);
    }
}
