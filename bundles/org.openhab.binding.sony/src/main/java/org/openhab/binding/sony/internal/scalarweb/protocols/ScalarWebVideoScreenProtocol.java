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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sony.internal.SonyUtil;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannel;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelDescriptor;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebChannelTracker;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebContext;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebMethod;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebService;
import org.openhab.binding.sony.internal.scalarweb.models.api.CurrentValue;
import org.openhab.binding.sony.internal.scalarweb.models.api.Mode;
import org.openhab.binding.sony.internal.scalarweb.models.api.Position;
import org.openhab.binding.sony.internal.scalarweb.models.api.Screen;
import org.openhab.binding.sony.internal.scalarweb.models.api.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the protocol handles the Video Screen service
 *
 * @author Tim Roberts - Initial contribution
 * @param <T> the generic type for the callback
 */
@NonNullByDefault
class ScalarWebVideoScreenProtocol<T extends ThingCallback<String>> extends AbstractScalarWebProtocol<T> {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(ScalarWebVideoScreenProtocol.class);

    // Constants used by the protocol
    private static final String AUDIOSOURCE = "audiosource";
    private static final String BANNERMODE = "bannermode";
    private static final String MULTISCREENMODE = "multiscreenmode";
    private static final String PIPSUBSCREENPOSITION = "pipsubscreenposition";
    private static final String SCENESETTING = "scenesetting";

    /**
     * Instantiates a new scalar web video screen protocol.
     *
     * @param context the non-null context
     * @param service the non-null service
     * @param callback the non-null callback
     */
    ScalarWebVideoScreenProtocol(final ScalarWebProtocolFactory<T> factory, final ScalarWebContext context,
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

        try {
            execute(ScalarWebMethod.GETAUDIOSOURCESCREEN);
            descriptors.add(createDescriptor(createChannel(AUDIOSOURCE), "String", "scalarvideoscreenaudiosource"));
        } catch (final IOException e) {
            logger.debug("Exception getting audio source screen: {}", e.getMessage());
        }

        try {
            execute(ScalarWebMethod.GETBANNERMODE);
            descriptors.add(createDescriptor(createChannel(BANNERMODE), "String", "scalarvideoscreenbannermode"));
        } catch (final IOException e) {
            logger.debug("Exception getting banner mode: {}", e.getMessage());
        }

        try {
            execute(ScalarWebMethod.GETMULTISCREENMODE);
            descriptors.add(
                    createDescriptor(createChannel(MULTISCREENMODE), "String", "scalarvideoscreenmultiscreenmode"));
        } catch (final IOException e) {
            logger.debug("Exception getting multiscreen mode: {}", e.getMessage());
        }

        try {
            execute(ScalarWebMethod.GETPIPSUBSCREENPOSITION);
            descriptors.add(createDescriptor(createChannel(PIPSUBSCREENPOSITION), "String",
                    "scalarvideoscreenpipsubscreenposition"));
        } catch (final IOException e) {
            logger.debug("Exception getting pip subscreen position: {}", e.getMessage());
        }

        // scenesetting shows notimplemented when not in TWIN view mode - so just assume it has it.
        descriptors.add(createDescriptor(createChannel(SCENESETTING), "String", "scalarvideoscreenscenesetting"));

        return descriptors;
    }

    @Override
    public void refreshState(boolean initial) {
        final ScalarWebChannelTracker tracker = getChannelTracker();
        if (tracker.isCategoryLinked(AUDIOSOURCE)) {
            refreshAudioSource();
        }

        if (tracker.isCategoryLinked(BANNERMODE)) {
            refreshBannerMode();
        }

        if (tracker.isCategoryLinked(MULTISCREENMODE)) {
            refreshMultiScreenMode();
        }

        if (tracker.isCategoryLinked(PIPSUBSCREENPOSITION)) {
            refreshPipPosition();
        }

        if (tracker.isCategoryLinked(SCENESETTING)) {
            refreshSceneSetting();
        }
    }

    @Override
    public void refreshChannel(final ScalarWebChannel channel) {
        Objects.requireNonNull(channel, "channel cannot be null");

        switch (channel.getCategory()) {
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
     * Refresh audio source
     */
    private void refreshAudioSource() {
        try {
            final Screen screen = execute(ScalarWebMethod.GETAUDIOSOURCESCREEN).as(Screen.class);
            stateChanged(AUDIOSOURCE, SonyUtil.newStringType(screen.getScreen()));
        } catch (final IOException e) {
            logger.debug("Exception getting the audio source screen: {}", e.getMessage());
        }
    }

    /**
     * Refresh banner mode
     */
    private void refreshBannerMode() {
        try {
            final CurrentValue cv = execute(ScalarWebMethod.GETBANNERMODE).as(CurrentValue.class);
            stateChanged(BANNERMODE, SonyUtil.newStringType(cv.getCurrentValue()));
        } catch (final IOException e) {
            logger.debug("Exception getting the banner mode: {}", e.getMessage());
        }
    }

    /**
     * Refresh multi screen mode
     */
    private void refreshMultiScreenMode() {
        try {
            final Mode mode = execute(ScalarWebMethod.GETMULTISCREENMODE).as(Mode.class);
            stateChanged(MULTISCREENMODE, SonyUtil.newStringType(mode.getMode()));
        } catch (final IOException e) {
            logger.debug("Exception getting the multi screen mode: {}", e.getMessage());
        }
    }

    /**
     * Refresh pip position
     */
    private void refreshPipPosition() {
        try {
            final Position position = execute(ScalarWebMethod.GETPIPSUBSCREENPOSITION).as(Position.class);
            stateChanged(PIPSUBSCREENPOSITION, SonyUtil.newStringType(position.getPosition()));
        } catch (final IOException e) {
            logger.debug("Exception getting the PIP position: {}", e.getMessage());
        }
    }

    /**
     * Refresh scene setting.
     */
    private void refreshSceneSetting() {
        try {
            final CurrentValue cv = execute(ScalarWebMethod.GETSCENESETTING).as(CurrentValue.class);
            stateChanged(SCENESETTING, SonyUtil.newStringType(cv.getCurrentValue()));
        } catch (final IOException e) {
            logger.debug("Exception getting the scene screen: {}", e.getMessage());
        }
    }

    @Override
    public void setChannel(final ScalarWebChannel channel, final Command command) {
        Objects.requireNonNull(channel, "channel cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        switch (channel.getCategory()) {
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
     * @param audioSource the new non-null, non-empty audio source
     */
    private void setAudioSource(final String audioSource) {
        Validate.notEmpty(audioSource, "audioSource cannot be empty");
        handleExecute(ScalarWebMethod.SETAUDIOSOURCESCREEN, new Screen(audioSource));
    }

    /**
     * Sets the banner mode
     *
     * @param bannerMode the new non-null, non-empty banner mode
     */
    private void setBannerMode(final String bannerMode) {
        Validate.notEmpty(bannerMode, "bannerMode cannot be empty");
        handleExecute(ScalarWebMethod.SETBANNERMODE, new Value(bannerMode));
    }

    /**
     * Sets the multi screen mode
     *
     * @param multiScreenMode the new non-null, non-empty multi screen mode
     */
    private void setMultiScreenMode(final String multiScreenMode) {
        Validate.notEmpty(multiScreenMode, "multiScreenMode cannot be empty");
        handleExecute(ScalarWebMethod.SETMULTISCREENMODE, new Mode(multiScreenMode));
    }

    /**
     * Sets the pip sub screen position
     *
     * @param pipPosition the new non-null, non-empty pip sub screen position
     */
    private void setPipSubScreenPosition(final String pipPosition) {
        Validate.notEmpty(pipPosition, "pipPosition cannot be empty");
        handleExecute(ScalarWebMethod.SETPIPSUBSCREENPOSITION, new Position(pipPosition));
    }

    /**
     * Sets the scene setting
     *
     * @param sceneSetting the new scene setting
     */
    private void setSceneSetting(final String sceneSetting) {
        Validate.notEmpty(sceneSetting, "sceneSetting cannot be empty");
        handleExecute(ScalarWebMethod.SETSCENESETTING, new Value(sceneSetting));
    }
}
