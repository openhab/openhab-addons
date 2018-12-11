/*
 * Copyright (c) 2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.http.internal.model;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;

/**
 * An interface describing an error listener.
 *
 * @author Brian J. Tarricone
 */
@FunctionalInterface
public interface ErrorListener {
    void accept(ChannelUID channelUID, ThingStatusDetail errorDetail, String errorDescription);
}
