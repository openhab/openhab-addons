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

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * The {@link GenericThingState} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class ChannelStringType extends GenericChannelState {

    protected ChannelStringType(ChannelUID channelUID, GenericThingState thing,
            ChannelStateChangeSubscriber subscriber) {
        super(channelUID, thing, subscriber);
    }

    @Override
    protected State convert(Object _state) throws UnsupportedDataTypeException {
        State newState = UnDefType.UNDEF;

        if (_state instanceof String) {
            newState = new StringType((String) _state);
        } else if (_state instanceof StringType) {
            newState = (StringType) _state;

        } else if (_state instanceof UnDefType) {
            newState = (UnDefType) _state;
        } else {
            throw new UnsupportedDataTypeException();
        }
        return newState;
    }

}
