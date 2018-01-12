/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal.listeners;

import java.util.Map;

import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.StateChangeListener;
import org.eclipse.smarthome.core.types.State;
import org.openhab.ui.cometvisu.internal.backend.EventBroadcaster;
import org.openhab.ui.cometvisu.internal.backend.beans.StateBean;

/**
 * listens to state changes on items and send them to an EventBroadcaster
 * 
 * @author Tobias Bräutigam
 * @since 2.0.0
 */
public class StateEventListener implements StateChangeListener {

    private EventBroadcaster eventBroadcaster;

    public void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = eventBroadcaster;
    }

    protected void unsetEventBroadcaster(EventBroadcaster eventBroadcaster) {
        this.eventBroadcaster = null;
    }

    @Override
    public void stateChanged(Item item, State oldState, State newState) {
        Map<String, Class<? extends State>> clientItems = eventBroadcaster.getClientItems(item);
        if (clientItems != null && clientItems.size() > 0) {
            for (String cvItemName : clientItems.keySet()) {
                Class<? extends State> stateClass = clientItems.get(cvItemName);
                StateBean stateBean = new StateBean();
                stateBean.name = cvItemName;
                if (stateClass != null)
                    stateBean.state = item.getStateAs(stateClass).toString();
                else
                    stateBean.state = item.getState().toString();
                eventBroadcaster.broadcastEvent(stateBean);
            }
        } else {
            StateBean stateBean = new StateBean();
            stateBean.name = item.getName();
            stateBean.state = newState.toString();
            eventBroadcaster.broadcastEvent(stateBean);
        }

    }

    @Override
    public void stateUpdated(Item item, State state) {
        if (item instanceof GroupItem) {

            // group item update could be relevant for the client, although the state of switch group does not change
            // wenn more the one are on, the number-groupFunction changes
            Map<String, Class<? extends State>> clientItems = eventBroadcaster.getClientItems(item);
            if (clientItems != null && clientItems.size() > 0) {
                for (String cvItemName : clientItems.keySet()) {
                    Class<? extends State> stateClass = clientItems.get(cvItemName);
                    if (stateClass != null) {
                        StateBean stateBean = new StateBean();
                        stateBean.name = cvItemName;
                        stateBean.state = item.getStateAs(stateClass).toString();

                        eventBroadcaster.broadcastEvent(stateBean);
                    }
                }
            }
        }
    }
}
