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

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebState;
import org.openhab.binding.sony.internal.scalarweb.models.api.CurrentValue;
import org.openhab.binding.sony.internal.scalarweb.models.api.Mode;
import org.openhab.binding.sony.internal.scalarweb.models.api.Position;
import org.openhab.binding.sony.internal.scalarweb.models.api.Screen;
import org.openhab.binding.sony.internal.scalarweb.models.api.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ScalarWebVideoScreenProtocol.
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type
 */
class ScalarWebVideoScreenProtocol<T extends ThingCallback<ScalarWebChannel>> extends AbstractScalarWebProtocol<T> {

    /** The logger. */
    private Logger logger = LoggerFactory.getLogger(ScalarWebSystemProtocol.class);

    /** The Constant AUDIOSOURCE. */
    private final static String AUDIOSOURCE = "audiosource";

    /** The Constant BANNERMODE. */
    private final static String BANNERMODE = "bannermode";

    /** The Constant MULTISCREENMODE. */
    private final static String MULTISCREENMODE = "multiscreenmode";

    /** The Constant PIPSUBSCREENPOSITION. */
    private final static String PIPSUBSCREENPOSITION = "pipsubscreenposition";

    /** The Constant SCENESETTING. */
    private final static String SCENESETTING = "scenesetting";

    /**
     * Instantiates a new scalar web video screen protocol.
     *
     * @param tracker the tracker
     * @param state the state
     * @param service the service
     * @param callback the callback
     */
    ScalarWebVideoScreenProtocol(ScalarWebChannelTracker tracker, ScalarWebState state, ScalarWebService service,
            T callback) {
        super(tracker, state, service, callback);
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
            execute(ScalarWebMethod.GetAudioSourceScreen);
            descriptors.add(createDescriptor(createChannel(AUDIOSOURCE), "String", "scalarvideoscreenaudiosource"));
        } catch (IOException e) {
            // not implemented
        }

        try {
            execute(ScalarWebMethod.GetBannerMode);
            descriptors.add(createDescriptor(createChannel(BANNERMODE), "String", "scalarvideoscreenbannermode"));
        } catch (IOException e) {
            // not implemented
        }

        try {
            execute(ScalarWebMethod.GetMultiScreenMode);
            descriptors.add(
                    createDescriptor(createChannel(MULTISCREENMODE), "String", "scalarvideoscreenmultiscreenmode"));
        } catch (IOException e) {
            // not implemented
        }

        try {
            execute(ScalarWebMethod.GetPipSubScreenPosition);
            descriptors.add(createDescriptor(createChannel(PIPSUBSCREENPOSITION), "String",
                    "scalarvideoscreenpipsubscreenposition"));
        } catch (IOException e) {
            // not implemented
        }

        // scenesetting shows notimplemented when not in TWIN view mode - so just assume it has it.
        descriptors.add(createDescriptor(createChannel(SCENESETTING), "String", "scalarvideoscreenscenesetting"));

        return descriptors;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#refreshState()
     */
    @Override
    public void refreshState() {
        if (isLinked(AUDIOSOURCE)) {
            refreshAudioSource();
        }

        if (isLinked(BANNERMODE)) {
            refreshBannerMode();
        }

        if (isLinked(MULTISCREENMODE)) {
            refreshMultiScreenMode();
        }

        if (isLinked(PIPSUBSCREENPOSITION)) {
            refreshPipPosition();
        }

        if (isLinked(SCENESETTING)) {
            refreshSceneSetting();
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
        switch (channel.getId()) {
            case AUDIOSOURCE:
                refreshAudioSource();
                break;

            case BANNERMODE:
                refreshBannerMode();
                break;

            case MULTISCREENMODE:
                refreshMultiScreenMode();
                break;

            case PIPSUBSCREENPOSITION:
                refreshPipPosition();
                break;

            case SCENESETTING:
                refreshSceneSetting();
                break;

            default:
                logger.debug("Unknown refresh channel: {}", channel);
                break;
        }

    }

    /**
     * Refresh audio source.
     */
    private void refreshAudioSource() {
        try {
            final Screen screen = execute(ScalarWebMethod.GetAudioSourceScreen).as(Screen.class);
            callback.stateChanged(createChannel(AUDIOSOURCE), new StringType(screen.getScreen()));
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Refresh banner mode.
     */
    private void refreshBannerMode() {
        try {
            final CurrentValue cv = execute(ScalarWebMethod.GetBannerMode).as(CurrentValue.class);
            callback.stateChanged(createChannel(BANNERMODE), new StringType(cv.getCurrentValue()));
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Refresh multi screen mode.
     */
    private void refreshMultiScreenMode() {
        try {
            final Mode mode = execute(ScalarWebMethod.GetMultiScreenMode).as(Mode.class);
            callback.stateChanged(createChannel(MULTISCREENMODE), new StringType(mode.getMode()));
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Refresh pip position.
     */
    private void refreshPipPosition() {
        try {
            final Position position = execute(ScalarWebMethod.GetPipSubScreenPosition).as(Position.class);
            callback.stateChanged(createChannel(PIPSUBSCREENPOSITION), new StringType(position.getPosition()));
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * Refresh scene setting.
     */
    private void refreshSceneSetting() {
        try {
            final CurrentValue cv = execute(ScalarWebMethod.GetSceneSetting).as(CurrentValue.class);
            callback.stateChanged(createChannel(SCENESETTING), new StringType(cv.getCurrentValue()));
        } catch (IOException e) {
            // do nothing
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openhab.binding.sony.internal.scalarweb.protocols.ScalarWebProtocol#setChannel(org.openhab.binding.sony.
     * internal.scalarweb.ScalarWebChannel, org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void setChannel(ScalarWebChannel channel, Command command) {
        switch (channel.getId()) {
            case AUDIOSOURCE:
                setAudioSource(command.toString());
                break;

            case BANNERMODE:
                setBannerMode(command.toString());
                break;

            case MULTISCREENMODE:
                setMultiScreenMode(command.toString());
                break;

            case PIPSUBSCREENPOSITION:
                setPipSubScreenPosition(command.toString());
                break;

            case SCENESETTING:
                setSceneSetting(command.toString());
                break;

            default:
                logger.debug("Unhandled channel command: {} - {}", channel, command);
                break;
        }
    }

    /**
     * Sets the audio source.
     *
     * @param audioSource the new audio source
     */
    private void setAudioSource(String audioSource) {
        handleExecute(ScalarWebMethod.setAudioSourceScreen, new Screen(audioSource));
    }

    /**
     * Sets the banner mode.
     *
     * @param bannerMode the new banner mode
     */
    private void setBannerMode(String bannerMode) {
        handleExecute(ScalarWebMethod.setBannerMode, new Value(bannerMode));
    }

    /**
     * Sets the multi screen mode.
     *
     * @param multiScreenMode the new multi screen mode
     */
    private void setMultiScreenMode(String multiScreenMode) {
        handleExecute(ScalarWebMethod.setMultiScreenMode, new Mode(multiScreenMode));
    }

    /**
     * Sets the pip sub screen position.
     *
     * @param pipPosition the new pip sub screen position
     */
    private void setPipSubScreenPosition(String pipPosition) {
        handleExecute(ScalarWebMethod.setPipSubScreenPosition, new Position(pipPosition));
    }

    /**
     * Sets the scene setting.
     *
     * @param sceneSetting the new scene setting
     */
    private void setSceneSetting(String sceneSetting) {
        handleExecute(ScalarWebMethod.setSceneSetting, new Value(sceneSetting));
    }

}
