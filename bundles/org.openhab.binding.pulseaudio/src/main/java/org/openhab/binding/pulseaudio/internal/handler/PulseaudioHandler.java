/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pulseaudio.internal.handler;

import static org.openhab.binding.pulseaudio.internal.PulseaudioBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.PulseAudioAudioSink;
import org.openhab.binding.pulseaudio.internal.PulseAudioAudioSource;
import org.openhab.binding.pulseaudio.internal.PulseaudioBindingConstants;
import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig;
import org.openhab.binding.pulseaudio.internal.items.SimpleProtocolTCPModule;
import org.openhab.binding.pulseaudio.internal.items.Sink;
import org.openhab.binding.pulseaudio.internal.items.SinkInput;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioSource;
import org.openhab.core.audio.utils.AudioSinkUtils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PulseaudioHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tobias Bräutigam - Initial contribution
 * @author Miguel Álvarez - Register audio source and refactor
 */
@NonNullByDefault
public class PulseaudioHandler extends BaseThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(SINK_THING_TYPE, COMBINED_SINK_THING_TYPE, SINK_INPUT_THING_TYPE,
                    SOURCE_THING_TYPE, SOURCE_OUTPUT_THING_TYPE).collect(Collectors.toSet()));
    private final Logger logger = LoggerFactory.getLogger(PulseaudioHandler.class);

    private @Nullable DeviceIdentifier deviceIdentifier;
    private @Nullable PulseAudioAudioSink audioSink;
    private @Nullable PulseAudioAudioSource audioSource;
    private int simpleProtocolMinPort;
    private int simpleProtocolMaxPort;
    private @Nullable Integer savedVolume;

    private final Map<String, ServiceRegistration<AudioSink>> audioSinkRegistrations = new ConcurrentHashMap<>();
    private final Map<String, ServiceRegistration<AudioSource>> audioSourceRegistrations = new ConcurrentHashMap<>();

    private final BundleContext bundleContext;

    private AudioSinkUtils audioSinkUtils;

    public PulseaudioHandler(Thing thing, BundleContext bundleContext, AudioSinkUtils audioSinkUtils) {
        super(thing);
        this.bundleContext = bundleContext;
        this.audioSinkUtils = audioSinkUtils;
    }

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        try {
            deviceIdentifier = new DeviceIdentifier((String) config.get(DEVICE_PARAMETER_NAME_OR_DESCRIPTION),
                    (String) config.get(DEVICE_PARAMETER_ADDITIONAL_FILTERS));
            simpleProtocolMinPort = (int) (config.containsKey(DEVICE_PARAMETER_MIN_PORT)
                    ? config.get(DEVICE_PARAMETER_MIN_PORT)
                    : 1024);
            simpleProtocolMaxPort = (int) (config.containsKey(DEVICE_PARAMETER_MAX_PORT)
                    ? config.get(DEVICE_PARAMETER_MAX_PORT)
                    : 64512);
        } catch (PatternSyntaxException p) {
            deviceIdentifier = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Incorrect regular expression: " + (String) config.get(DEVICE_PARAMETER_ADDITIONAL_FILTERS));
            return;
        }
        initializeWithTheBridge();
    }

    public @Nullable DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public int getMaxIdleModules() {
        return ((BigDecimal) thing.getConfiguration().get(DEVICE_PARAMETER_IDLE_MODULES)).intValue();
    }

    private synchronized void audioSinkSetup() {
        if (audioSink != null) {
            // Audio sink is already setup
            return;
        }
        if (!SINK_THING_TYPE.equals(thing.getThingTypeUID())) {
            return;
        }
        // check the property to see if it's enabled :
        Boolean sinkActivated = (Boolean) thing.getConfiguration().get(DEVICE_PARAMETER_AUDIO_SINK_ACTIVATION);
        if (sinkActivated == null || !sinkActivated.booleanValue()) {
            return;
        }
        this.audioSink = new PulseAudioAudioSink(this, scheduler, audioSinkUtils);
        // Register the sink as an audio sink in openhab
        logger.trace("Registering an audio sink for pulse audio sink thing {}", thing.getUID());
        @SuppressWarnings("unchecked")
        ServiceRegistration<AudioSink> reg = (ServiceRegistration<AudioSink>) bundleContext
                .registerService(AudioSink.class.getName(), audioSink, new Hashtable<>());
        audioSinkRegistrations.put(thing.getUID().toString(), reg);
    }

    private synchronized void audioSinkUnsetup() {
        PulseAudioAudioSink sink = audioSink;
        if (sink != null) {
            sink.close();
            audioSink = null;
        }
        // Unregister the potential pulse audio sink's audio sink
        ServiceRegistration<AudioSink> sinkReg = audioSinkRegistrations.remove(getThing().getUID().toString());
        if (sinkReg != null) {
            logger.trace("Unregistering the audio sync service for pulse audio sink thing {}", getThing().getUID());
            sinkReg.unregister();
        }
    }

    private synchronized void audioSourceSetup() {
        if (audioSource != null) {
            // Audio source is already setup
            return;
        }
        if (!SOURCE_THING_TYPE.equals(thing.getThingTypeUID())) {
            return;
        }
        // check the property to see if it's enabled :
        Boolean sourceActivated = (Boolean) thing.getConfiguration().get(DEVICE_PARAMETER_AUDIO_SOURCE_ACTIVATION);
        if (sourceActivated == null || !sourceActivated.booleanValue()) {
            return;
        }
        audioSource = new PulseAudioAudioSource(this, scheduler);
        // Register the source as an audio source in openhab
        logger.trace("Registering an audio source for pulse audio source thing {}", thing.getUID());
        @SuppressWarnings("unchecked")
        ServiceRegistration<AudioSource> reg = (ServiceRegistration<AudioSource>) bundleContext
                .registerService(AudioSource.class.getName(), audioSource, new Hashtable<>());
        audioSourceRegistrations.put(thing.getUID().toString(), reg);
    }

    public synchronized void audioSourceUnsetup() {
        PulseAudioAudioSource source = audioSource;
        if (source != null) {
            source.close();
            audioSource = null;
        }
        // Unregister the potential pulse audio source's audio sources
        ServiceRegistration<AudioSource> sourceReg = audioSourceRegistrations.remove(getThing().getUID().toString());
        if (sourceReg != null) {
            logger.trace("Unregistering the audio sync service for pulse audio source thing {}", getThing().getUID());
            sourceReg.unregister();
        }
    }

    @Override
    public void dispose() {
        logger.trace("Thing {} {} disposed.", getThing().getUID(), safeGetDeviceNameOrDescription());
        super.dispose();
        audioSinkUnsetup();
        audioSourceUnsetup();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        initializeWithTheBridge();
    }

    private void initializeWithTheBridge() {
        PulseaudioBridgeHandler pulseaudioBridgeHandler = getPulseaudioBridgeHandler();
        if (pulseaudioBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        } else if (pulseaudioBridgeHandler.getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else {
            deviceUpdate(pulseaudioBridgeHandler.getDevice(deviceIdentifier));
        }
    }

    private synchronized @Nullable PulseaudioBridgeHandler getPulseaudioBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for device {}.", safeGetDeviceNameOrDescription());
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof PulseaudioBridgeHandler pulseaudioBridgeHandler) {
            return pulseaudioBridgeHandler;
        } else {
            logger.debug("No available bridge handler found for device {} bridge {} .",
                    safeGetDeviceNameOrDescription(), bridge.getUID());
            return null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        PulseaudioBridgeHandler briHandler = getPulseaudioBridgeHandler();
        if (briHandler == null) {
            logger.debug("pulseaudio server bridge handler not found. Cannot handle command without bridge.");
            return;
        }
        if (command instanceof RefreshType) {
            briHandler.handleCommand(channelUID, command);
            return;
        }

        AbstractAudioDeviceConfig device = briHandler.getDevice(deviceIdentifier);
        if (device == null) {
            logger.warn("device {} not found", safeGetDeviceNameOrDescription());
            deviceUpdate(null);
            return;
        } else {
            State updateState = UnDefType.UNDEF;
            if (channelUID.getId().equals(VOLUME_CHANNEL)) {
                if (command instanceof IncreaseDecreaseType) {
                    // refresh to get the current volume level
                    briHandler.getClient().update();
                    device = briHandler.getDevice(deviceIdentifier);
                    if (device == null) {
                        logger.warn("missing device info, aborting");
                        return;
                    }
                    int oldVolume = device.getVolume();
                    int newVolume = oldVolume;
                    if (command.equals(IncreaseDecreaseType.INCREASE)) {
                        newVolume = Math.min(100, oldVolume + 5);
                    }
                    if (command.equals(IncreaseDecreaseType.DECREASE)) {
                        newVolume = Math.max(0, oldVolume - 5);
                    }
                    briHandler.getClient().setVolumePercent(device, newVolume);
                    updateState = new PercentType(newVolume);
                    savedVolume = newVolume;
                } else if (command instanceof PercentType volume) {
                    briHandler.getClient().setVolumePercent(device, volume.intValue());
                    updateState = volume;
                    savedVolume = volume.intValue();
                } else if (command instanceof DecimalType volume) {
                    briHandler.getClient().setVolume(device, volume.intValue());
                    updateState = volume;
                    savedVolume = volume.intValue();
                }
            } else if (channelUID.getId().equals(MUTE_CHANNEL)) {
                if (command instanceof OnOffType onOffCommand) {
                    briHandler.getClient().setMute(device, OnOffType.ON.equals(command));
                    updateState = onOffCommand;
                }
            } else if (channelUID.getId().equals(SLAVES_CHANNEL)) {
                if (device instanceof Sink sink && sink.isCombinedSink()) {
                    if (command instanceof StringType) {
                        List<Sink> slaves = new ArrayList<>();
                        for (String slaveName : command.toString().split(",")) {
                            Sink slave = briHandler.getClient().getSink(slaveName.trim());
                            if (slave != null) {
                                slaves.add(slave);
                            }
                        }
                        if (!slaves.isEmpty()) {
                            briHandler.getClient().setCombinedSinkSlaves(((Sink) device), slaves);
                        }
                    }
                } else {
                    logger.warn("{} is no combined sink", device);
                }
            } else if (channelUID.getId().equals(ROUTE_TO_SINK_CHANNEL)) {
                if (device instanceof SinkInput input) {
                    Sink newSink = null;
                    if (command instanceof DecimalType decimalCommand) {
                        newSink = briHandler.getClient().getSink(decimalCommand.intValue());
                    } else {
                        newSink = briHandler.getClient().getSink(command.toString());
                    }
                    if (newSink != null) {
                        logger.debug("rerouting {} to {}", device, newSink);
                        briHandler.getClient().moveSinkInput(input, newSink);
                        updateState = new StringType(newSink.getPaName());
                    } else {
                        logger.warn("no sink {} found", command.toString());
                    }
                }
            }
            logger.trace("updating {} to {}", channelUID, updateState);
            if (!updateState.equals(UnDefType.UNDEF)) {
                updateState(channelUID, updateState);
            }
        }
    }

    /**
     * Use last checked volume for faster access
     *
     * @return
     */
    public Integer getLastVolume() {
        Integer savedVolumeFinal = savedVolume;
        if (savedVolumeFinal == null) {
            PulseaudioBridgeHandler briHandler = getPulseaudioBridgeHandler();
            if (briHandler != null) {
                // refresh to get the current volume level
                briHandler.getClient().update();
                AbstractAudioDeviceConfig device = briHandler.getDevice(deviceIdentifier);
                if (device != null) {
                    savedVolume = savedVolumeFinal = device.getVolume();
                }
            }
        }
        return savedVolumeFinal == null ? 50 : savedVolumeFinal;
    }

    public void setVolume(int volume) {
        PulseaudioBridgeHandler briHandler = getPulseaudioBridgeHandler();
        if (briHandler == null) {
            logger.warn("bridge is not ready");
            return;
        }
        AbstractAudioDeviceConfig device = briHandler.getDevice(deviceIdentifier);
        if (device == null) {
            logger.warn("missing device info, aborting");
            return;
        }
        briHandler.getClient().setVolumePercent(device, volume);
        updateState(VOLUME_CHANNEL, new PercentType(volume));
        savedVolume = volume;
    }

    public void deviceUpdate(@Nullable AbstractAudioDeviceConfig device) {
        if (device != null) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            logger.debug("Updating states of {} id: {}", device, VOLUME_CHANNEL);
            int actualVolume = device.getVolume();
            savedVolume = actualVolume;
            updateState(VOLUME_CHANNEL, new PercentType(actualVolume));
            updateState(MUTE_CHANNEL, OnOffType.from(device.isMuted()));
            org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig.State state = device.getState();
            updateState(STATE_CHANNEL, state != null ? new StringType(state.toString()) : new StringType("-"));
            if (device instanceof SinkInput input) {
                updateState(ROUTE_TO_SINK_CHANNEL,
                        new StringType(Optional.ofNullable(input.getSink()).map(Sink::getPaName).orElse("-")));
            }
            if (device instanceof Sink sink && sink.isCombinedSink()) {
                updateState(SLAVES_CHANNEL, new StringType(String.join(",", sink.getCombinedSinkNames())));
            }
            audioSinkSetup();
            audioSourceSetup();
        } else {
            updateState(VOLUME_CHANNEL, UnDefType.UNDEF);
            updateState(MUTE_CHANNEL, UnDefType.UNDEF);
            updateState(STATE_CHANNEL, UnDefType.UNDEF);
            if (SINK_INPUT_THING_TYPE.equals(thing.getThingTypeUID())) {
                updateState(ROUTE_TO_SINK_CHANNEL, UnDefType.UNDEF);
            }
            if (COMBINED_SINK_THING_TYPE.equals(thing.getThingTypeUID())) {
                updateState(SLAVES_CHANNEL, UnDefType.UNDEF);
            }
            audioSinkUnsetup();
            audioSourceUnsetup();
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    public String getHost() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return (String) bridge.getConfiguration().get(PulseaudioBindingConstants.BRIDGE_PARAMETER_HOST);
        } else {
            logger.warn("A bridge must be configured for this pulseaudio thing");
            return "null";
        }
    }

    /**
     * Unload existing Simple Protocol TCP modules for this device on the remote pulseaudio.
     *
     * @throws IOException if unable to load device config
     */
    public void clearSimpleProtocolTCPModules() throws IOException {
        var briHandler = getPulseaudioBridgeHandler();
        if (briHandler == null) {
            throw new IOException("bridge is not ready");
        }
        AbstractAudioDeviceConfig device = briHandler.getDevice(deviceIdentifier);
        if (device == null) {
            throw new IOException(
                    "missing device info, device " + safeGetDeviceNameOrDescription() + " appears to be offline");
        }
        briHandler.getClient().getSimpleProtocolTCPModulesByDevice(device, simpleProtocolMinPort, simpleProtocolMaxPort)
                .forEach(spModule -> {
                    try {
                        unloadModule(spModule);
                    } catch (IOException e) {
                        logger.warn("IOException unloading module {}: ", spModule.getId(), e);
                    }
                });
    }

    /**
     * Creates a new Simple Protocol TCP module instance on the server or reuse an idle one if still available.
     *
     * @return the Simple Protocol module instance
     * @throws IOException when device info is not available
     * @throws InterruptedException when interrupted during the loading module wait
     */
    public Optional<SimpleProtocolTCPModule> loadSimpleProtocolModule(AudioFormat audioFormat,
            @Nullable SimpleProtocolTCPModule module) throws IOException, InterruptedException {
        var briHandler = getPulseaudioBridgeHandler();
        if (briHandler == null) {
            throw new IOException("bridge is not ready");
        }
        AbstractAudioDeviceConfig device = briHandler.getDevice(deviceIdentifier);
        if (device == null) {
            throw new IOException(
                    "missing device info, device " + safeGetDeviceNameOrDescription() + " appears to be offline");
        }
        return briHandler.getClient().loadModuleSimpleProtocolTcpIfNeeded(device, audioFormat, simpleProtocolMinPort,
                simpleProtocolMaxPort, module);
    }

    public AudioFormat getSourceAudioFormat() {
        String simpleFormat = ((String) getThing().getConfiguration().get(DEVICE_PARAMETER_AUDIO_SOURCE_FORMAT));
        BigDecimal simpleRate = ((BigDecimal) getThing().getConfiguration().get(DEVICE_PARAMETER_AUDIO_SOURCE_RATE));
        BigDecimal simpleChannels = ((BigDecimal) getThing().getConfiguration()
                .get(DEVICE_PARAMETER_AUDIO_SOURCE_CHANNELS));
        AudioFormat fallback = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, false, 16,
                16 * 16000, 16000L, 1);
        if (simpleFormat == null || simpleRate == null || simpleChannels == null) {
            return fallback;
        }
        int sampleRateAllChannels = simpleRate.intValue() * simpleChannels.intValue();
        switch (simpleFormat) {
            case "u8" -> {
                return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_UNSIGNED, null, 8,
                        8 * sampleRateAllChannels, simpleRate.longValue(), simpleChannels.intValue());
            }
            case "s16le" -> {
                return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, false, 16,
                        16 * sampleRateAllChannels, simpleRate.longValue(), simpleChannels.intValue());
            }
            case "s16be" -> {
                return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, true, 16,
                        16 * sampleRateAllChannels, simpleRate.longValue(), simpleChannels.intValue());
            }
            case "s24le" -> {
                return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, false, 24,
                        24 * sampleRateAllChannels, simpleRate.longValue(), simpleChannels.intValue());
            }
            case "s24be" -> {
                return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, true, 24,
                        24 * sampleRateAllChannels, simpleRate.longValue(), simpleChannels.intValue());
            }
            case "s32le" -> {
                return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, false, 32,
                        32 * sampleRateAllChannels, simpleRate.longValue(), simpleChannels.intValue());
            }
            case "s32be" -> {
                return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED, true, 32,
                        32 * sampleRateAllChannels, simpleRate.longValue(), simpleChannels.intValue());
            }
            default -> {
                logger.warn("unsupported format {}", simpleFormat);
                return fallback;
            }
        }
    }

    public void unloadModule(SimpleProtocolTCPModule module) throws IOException {
        var briHandler = getPulseaudioBridgeHandler();
        if (briHandler == null) {
            throw new IOException("bridge is not ready");
        }
        briHandler.getClient().unloadModule(module);
    }

    private String safeGetDeviceNameOrDescription() {
        DeviceIdentifier deviceIdentifierFinal = deviceIdentifier;
        return deviceIdentifierFinal == null ? "UNKNOWN" : deviceIdentifierFinal.getNameOrDescription();
    }

    public int getBasicProtocolSOTimeout() {
        var soTimeout = (BigDecimal) getThing().getConfiguration().get(DEVICE_PARAMETER_AUDIO_SOCKET_SO_TIMEOUT);
        return soTimeout != null ? soTimeout.intValue() : 500;
    }
}
