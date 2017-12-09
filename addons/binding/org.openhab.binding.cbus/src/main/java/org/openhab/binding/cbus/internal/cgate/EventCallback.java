/*
 * CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *
 * Copyright 2008, 2009, 2012, 2017 Dave Oxley <dave@daveoxley.co.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.cbus.internal.cgate;

import java.util.GregorianCalendar;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public abstract class EventCallback {
    /**
     *
     * @param event_code
     * @return
     */
    public abstract boolean acceptEvent(int event_code);

    /**
     *
     * @param cgate_session
     * @param event_time
     * @param event_code
     * @param event
     */
    public abstract void processEvent(CGateSession cgate_session, int event_code, GregorianCalendar event_time,
            String event);
}
