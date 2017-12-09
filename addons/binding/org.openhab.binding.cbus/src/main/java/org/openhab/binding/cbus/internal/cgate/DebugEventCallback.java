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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class DebugEventCallback extends EventCallback {
    private Logger logger = LoggerFactory.getLogger(DebugEventCallback.class);

    @Override
    public boolean acceptEvent(int event_code) {
        return logger.isDebugEnabled(); // Accept all events if debug enabled
    }

    @Override
    public void processEvent(CGateSession cgate_session, int event_code, GregorianCalendar event_time, String event) {
        // logger.debug("event_code: " + event_code + ", event_time: " + event_time.toString() + ", event: " + event);
    }

}
