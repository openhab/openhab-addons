/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.openhab.binding.teleinfo.internal.reader.Frame;

/**
 * The {@link TeleinfoAbstractControllerHandler} class defines a skeleton for controller handlers.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public abstract class TeleinfoAbstractControllerHandler extends BaseBridgeHandler {

    private List<TeleinfoControllerHandlerListener> listeners = new ArrayList<>();

    public TeleinfoAbstractControllerHandler(@NonNull Bridge bridge) {
        super(bridge);
    }

    public void addListener(final TeleinfoControllerHandlerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final TeleinfoControllerHandlerListener listener) {
        listeners.remove(listener);
    }

    protected void fireOnFrameReceivedEvent(final Frame frame) {
        for (int i = 0; i < listeners.size(); i++) {
            TeleinfoControllerHandlerListener listener = listeners.get(i);
            listener.onFrameReceived(this, frame);
        }
    }
}
