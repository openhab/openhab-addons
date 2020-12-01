/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebContext;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the protocol handles the Video service
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type for the callback
 */
@NonNullByDefault
class ScalarWebVideoProtocol<T extends ThingCallback<String>> extends AbstractScalarWebProtocol<T> {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(ScalarWebVideoProtocol.class);

    // Constants used by the protocol
    private static final String PICTUREQUALITYSETTINGS = "picturequalitysettings";

    /**
     * Instantiates a new scalar web video protocol.
     *
     * @param context the non-null context
     * @param service the non-null service
     * @param callback the non-null callback
     */
    ScalarWebVideoProtocol(final ScalarWebProtocolFactory<T> factory, final ScalarWebContext context,
            final ScalarWebService service, final T callback) {
        super(factory, context, service, callback);
    }

    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors(final boolean dynamicOnly) {
        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();

        // no dynamic channels
        if (dynamicOnly) {
            return descriptors;
        }

        if (service.hasMethod(ScalarWebMethod.GETPICTUREQUALITYSETTINGS)) {
            addGeneralSettingsDescriptor(descriptors, ScalarWebMethod.GETPICTUREQUALITYSETTINGS, PICTUREQUALITYSETTINGS,
                    "Picture Quality Setting");
        }

        return descriptors;
    }

    @Override
    public void refreshState(boolean initial) {
        final ScalarWebChannelTracker tracker = getChannelTracker();

        if (tracker.isCategoryLinked(PICTUREQUALITYSETTINGS)) {
            refreshGeneralSettings(tracker.getLinkedChannelsForCategory(PICTUREQUALITYSETTINGS),
                    ScalarWebMethod.GETPICTUREQUALITYSETTINGS);
        }
    }

    @Override
    public void refreshChannel(final ScalarWebChannel channel) {
        Objects.requireNonNull(channel, "channel cannot be null");

        switch (channel.getCategory()) {
            case PICTUREQUALITYSETTINGS:
                refreshGeneralSettings(Collections.singleton(channel), ScalarWebMethod.GETPICTUREQUALITYSETTINGS);
                break;

            default:
                logger.debug("Unknown refresh channel: {}", channel);
                break;
        }
    }

    @Override
    public void setChannel(final ScalarWebChannel channel, final Command command) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        switch (channel.getCategory()) {
            case PICTUREQUALITYSETTINGS:
                setGeneralSetting(ScalarWebMethod.SETPICTUREQUALITYSETTINGS, channel, command);
                break;

            default:
                logger.debug("Unhandled channel command: {} - {}", channel, command);
                break;
        }
    }
}
