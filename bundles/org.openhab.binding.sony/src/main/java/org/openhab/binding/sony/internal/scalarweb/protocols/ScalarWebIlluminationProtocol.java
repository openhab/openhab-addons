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

import org.apache.commons.lang.StringUtils;
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
 * The implementation of the protocol handles the illumination service
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type for the callback
 */
@NonNullByDefault
class ScalarWebIlluminationProtocol<T extends ThingCallback<String>> extends AbstractScalarWebProtocol<T> {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(ScalarWebIlluminationProtocol.class);

    // Constants used by this protocol
    private static final String ILLUMINATIONSETTINGS = "illuminationsettings";

    /**
     * Instantiates a new scalar web audio protocol.
     *
     * @param factory the non-null factory to use
     * @param context the non-null context to use
     * @param service the non-null service to use
     * @param callback the non-null callback to use
     */
    ScalarWebIlluminationProtocol(final ScalarWebProtocolFactory<T> factory, final ScalarWebContext context,
            final ScalarWebService audioService, final T callback) {
        super(factory, context, audioService, callback);
    }

    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors(final boolean dynamicOnly) {
        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();

        // no dynamic channels
        if (dynamicOnly) {
            return descriptors;
        }

        if (service.hasMethod(ScalarWebMethod.GETILLUMNATIONSETTING)) {
            addGeneralSettingsDescriptor(descriptors, ScalarWebMethod.GETILLUMNATIONSETTING, ILLUMINATIONSETTINGS,
                    "Illumination Setting");
        }

        return descriptors;
    }

    @Override
    public void refreshState(boolean initial) {
        final ScalarWebChannelTracker tracker = getContext().getTracker();
        if (tracker.isCategoryLinked(ILLUMINATIONSETTINGS)) {
            refreshGeneralSettings(tracker.getLinkedChannelsForCategory(ILLUMINATIONSETTINGS),
                    ScalarWebMethod.GETILLUMNATIONSETTING);
        }
    }

    @Override
    public void refreshChannel(final ScalarWebChannel channel) {
        Objects.requireNonNull(channel, "channel cannot be null");

        final String ctgy = channel.getCategory();
        if (StringUtils.equalsIgnoreCase(ctgy, ILLUMINATIONSETTINGS)) {
            refreshGeneralSettings(Collections.singleton(channel), ScalarWebMethod.GETILLUMNATIONSETTING);
        }
    }

    @Override
    public void setChannel(final ScalarWebChannel channel, final Command command) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        switch (channel.getCategory()) {
            case ILLUMINATIONSETTINGS:
                setGeneralSetting(ScalarWebMethod.SETILLUMNATIONSETTING, channel, command);
                break;

            default:
                logger.debug("Unhandled channel command: {} - {}", channel, command);
                break;
        }
    }
}
