/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal;

import java.util.Map;

import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;

/**
 * Extends the ChannelTypeProvider to provide the list of available channels for the current zone.
 *
 * @author David Gräff
 */
public interface YamahaChannelsTypeProvider extends ChannelTypeProvider {
    /**
     * Changes the available inputs
     */
    void changeAvailableInputs(Map<String, String> availableInputs);

    /**
     * Changes the available inputs
     */
    void changePresetNames(String presetNames[]);

    /**
     * We need to call this method after the XML type files are read and available in the type registry.
     * This will provide us with a default input channels channel type which lists all existing inputs.
     * As soon as changeAvailableInputs is called, that channel type will be replaced.
     */
    void init();
}
