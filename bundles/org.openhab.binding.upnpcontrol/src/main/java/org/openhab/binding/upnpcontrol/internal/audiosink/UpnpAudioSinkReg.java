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
package org.openhab.binding.upnpcontrol.internal.audiosink;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.upnpcontrol.internal.UpnpControlHandlerFactory;
import org.openhab.binding.upnpcontrol.internal.handler.UpnpRendererHandler;

/**
 * Interface class to be implemented in {@link UpnpControlHandlerFactory}, allows a {UpnpRendererHandler} to register
 * itself as an audio sink when it supports audio. If it supports audio is only known after the communication with the
 * renderer is established.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public interface UpnpAudioSinkReg {

    /**
     * Implemented method should create a new {@link UpnpAudioSink} and register the handler parameter as an audio sink.
     *
     * @param handler
     */
    void registerAudioSink(UpnpRendererHandler handler);
}
