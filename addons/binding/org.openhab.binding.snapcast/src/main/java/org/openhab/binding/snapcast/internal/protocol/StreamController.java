/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snapcast.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.snapcast.internal.data.Params;
import org.openhab.binding.snapcast.internal.data.Stream;

/**
 * Stream protocol handler
 *
 * @author Steffen Brandemann - Initial contribution
 */
@NonNullByDefault
public class StreamController extends AbstractController<Stream, StreamListener> {

    /**
     * @param controller The main snapcast controller
     */
    StreamController(SnapcastController controller) {
        super(controller);
        controller.registerNotifyListener("Stream.OnUpdate", Params.class, this::handleUpdate);
    }

    /**
     * Handle an incoming status update
     *
     * @param params the data structure from a response or notification
     */
    void handleUpdate(Params params) {
        Stream stream = params.getStream();
        if (stream != null) {
            handleUpdate(stream);
        }
    }

    /**
     * Handle an incoming status update
     *
     * @param params the data structure of a stream
     */
    void handleUpdate(Stream params) {
        updateThingState(params);

        final String streamId = params.getId();
        eachListener(streamId, listener -> listener.updateStatus(streamId));
    }

}
