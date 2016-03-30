/**
 *  CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *  Copyright (C) 2008,2009,2012  Dave Oxley <dave@daveoxley.co.uk>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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
