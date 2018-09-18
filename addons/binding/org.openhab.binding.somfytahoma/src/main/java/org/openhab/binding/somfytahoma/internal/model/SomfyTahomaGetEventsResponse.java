/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.internal.model;

import java.util.Collection;
import java.util.Collections;

/**
 * The {@link SomfyTahomaGetEventsResponse} holds information about response to getEvents
 * command of your TahomaLink account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaGetEventsResponse {

    Collection<SomfyTahomaEvent> events = Collections.emptyList();

    public Collection<SomfyTahomaEvent> getEvents() {
        return events;
    }
}
