/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.zoneminder.internal.state;

import javax.activation.UnsupportedDataTypeException;

import org.eclipse.smarthome.core.types.State;

/**
 * The {@link ChannelState} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
interface ChannelState {

    public void subscribe();

    public void unsubscribe();

    public State getState();

    // private State statePublished = UnDefType.NULL;
    // private Type dataType = UnDefType.class;

    public void setState(Object state) throws UnsupportedDataTypeException;

    public void setState(Object state, boolean update) throws UnsupportedDataTypeException;

}
