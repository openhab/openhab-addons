/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.util.Collection;

import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;

// TODO: Auto-generated Javadoc
/**
 * The Interface ScalarWebProtocol.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
public interface ScalarWebProtocol<T extends ThingCallback<ScalarWebChannel>> extends AutoCloseable {

    /**
     * Gets the channel descriptors.
     *
     * @return the channel descriptors
     */
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors();

    /**
     * Refresh state.
     */
    public void refreshState();

    /**
     * Refresh channel.
     *
     * @param channel the channel
     */
    public void refreshChannel(ScalarWebChannel channel);

    /**
     * Sets the channel.
     *
     * @param channel the channel
     * @param command the command
     */
    public void setChannel(ScalarWebChannel channel, Command command);
}
