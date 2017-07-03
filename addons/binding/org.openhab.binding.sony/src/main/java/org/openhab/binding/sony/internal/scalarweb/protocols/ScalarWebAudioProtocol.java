/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.protocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebState;
import org.openhab.binding.sony.internal.scalarweb.models.api.AudioMute;
import org.openhab.binding.sony.internal.scalarweb.models.api.AudioVolume;
import org.openhab.binding.sony.internal.scalarweb.models.api.VolumeInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebAudioProtocol.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
class ScalarWebAudioProtocol<T extends ThingCallback<ScalarWebChannel>> extends AbstractScalarWebProtocol<T> {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebAudioProtocol.class);

    /** The Constant MUTE. */
    private final static String MUTE = "mute";

    /** The Constant VOLUME. */
    private final static String VOLUME = "volume";

    /**
     * Instantiates a new scalar web audio protocol.
     *
     * @param tracker the tracker
     * @param state the state
     * @param audioService the audio service
     * @param callback the callback
     */
    ScalarWebAudioProtocol(ScalarWebChannelTracker tracker, ScalarWebState state, ScalarWebService audioService,
            T callback) {
        super(tracker, state, audioService, callback);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#getChannelDescriptors()
     */
    @Override
    public Collection<ScalarWebChannelDescriptor> getChannelDescriptors() {

        final List<ScalarWebChannelDescriptor> descriptors = new ArrayList<ScalarWebChannelDescriptor>();
        try {
            final List<VolumeInformation> vols = execute(ScalarWebMethod.GetVolumeInformation)
                    .asArray(VolumeInformation.class);

            for (VolumeInformation vi : vols) {
                final String title = WordUtils.capitalize(vi.getTarget());
                descriptors.add(createDescriptor(createChannel(VOLUME, vi.getTarget()), "Dimmer", "scalaraudiovolume",
                        "Volume " + title, "Volume for " + title));

                descriptors.add(createDescriptor(createChannel(MUTE, vi.getTarget()), "Switch", "scalaraudiomute",
                        "Mute " + title, "Mute " + title));

            }
        } catch (IOException e) {
            // do nothing more than what execute already did
        }

        return descriptors;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshState()
     */
    @Override
    public void refreshState() {
        try {
            if (isIdLinked(VOLUME, MUTE)) {
                final List<VolumeInformation> vols = execute(ScalarWebMethod.GetVolumeInformation)
                        .asArray(VolumeInformation.class);

                for (VolumeInformation vi : vols) {
                    final ScalarWebChannel volChannel = createChannel(VOLUME, vi.getTarget());
                    if (isLinked(volChannel)) {
                        callback.stateChanged(volChannel, new PercentType(vi.getVolume()));
                    }

                    final ScalarWebChannel volMute = createChannel(MUTE, vi.getTarget());
                    if (isLinked(volMute)) {
                        callback.stateChanged(volMute, vi.isMute() ? OnOffType.ON : OnOffType.OFF);
                    }
                }
            }
        } catch (IOException e) {
            // do nothing more than what execute already did
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel)
     */
    @Override
    public void refreshChannel(ScalarWebChannel channel) {
        refreshState();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#setChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel, org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void setChannel(ScalarWebChannel channel, Command command) {
        final String[] paths = channel.getPaths();
        if (paths.length != 1) {
            logger.debug("Channel path invalid: {}", channel);
        } else {
            final String target = paths[0];

            switch (channel.getId()) {
                case MUTE:
                    if (command instanceof OnOffType) {
                        setMute(target, command == OnOffType.ON);
                    } else {
                        logger.debug("Mute command not an OnOffType: {}", command);
                    }

                    break;

                case VOLUME:
                    if (command instanceof PercentType) {
                        setVolume(paths[0], ((PercentType) command).intValue());
                    } else if (command instanceof OnOffType) {
                        setMute(target, command == OnOffType.ON);
                    } else if (command instanceof IncreaseDecreaseType) {
                        adjustVolume(target, ((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE);
                    } else {
                        logger.debug("Volume command not an PercentType/OnOffType/IncreaseDecreaseType: {}", command);
                    }

                    break;

                default:
                    logger.debug("Unhandled channel command: {} - {}", channel, command);
                    break;
            }
        }

    }

    /**
     * Sets the mute.
     *
     * @param target the target
     * @param muted the muted
     */
    private void setMute(String target, boolean muted) {
        handleExecute(ScalarWebMethod.SetAudioMute, new AudioMute(target, muted));
    }

    /**
     * Sets the volume.
     *
     * @param target the target
     * @param volume the volume
     */
    private void setVolume(String target, int volume) {
        handleExecute(ScalarWebMethod.SetAudioVolume, new AudioVolume(target, volume));
    }

    /**
     * Adjust volume.
     *
     * @param target the target
     * @param up the up
     */
    private void adjustVolume(String target, boolean up) {
        try {
            final List<VolumeInformation> vols = execute(ScalarWebMethod.GetVolumeInformation)
                    .asArray(VolumeInformation.class);

            for (VolumeInformation vi : vols) {
                if (StringUtils.equalsIgnoreCase(target, vi.getTarget())) {
                    int newVol = vi.getVolume() + (up ? 1 : -1);
                    if (newVol < vi.getMinVolume()) {
                        newVol = vi.getMinVolume();
                    } else if (newVol > vi.getMaxVolume()) {
                        newVol = vi.getMaxVolume();
                    }
                    setVolume(target, newVol);
                    break;
                }
            }
        } catch (IOException e) {

        }
    }
}
