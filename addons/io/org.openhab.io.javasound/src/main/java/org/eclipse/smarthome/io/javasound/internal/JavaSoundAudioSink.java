/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.eclipse.smarthome.io.javasound.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;

import org.apache.commons.collections.Closure;
import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an audio sink that is registered as a service, which can play wave files to the hosts outputs (e.g. speaker,
 * line-out).
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Christoph Weitkamp - Added getSupportedStreams() and UnsupportedAudioStreamException
 *
 */
@Component(service = AudioSink.class, immediate = true)
public class JavaSoundAudioSink implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(JavaSoundAudioSink.class);

    private boolean isMac = false;
    private PercentType macVolumeValue = null;

    // we accept anything that is WAVE with signed PCM codec
    private static final Set<AudioFormat> SUPPORTED_AUDIO_FORMATS = Collections.singleton(AudioFormat.WAV);
    // we accept any stream
    private static final Set<Class<? extends AudioStream>> SUPPORTED_AUDIO_STREAMS = Collections
            .singleton(AudioStream.class);

    @Activate
    protected void activate(BundleContext context) {
        String os = context.getProperty(Constants.FRAMEWORK_OS_NAME);
        if (os != null && os.toLowerCase().startsWith("macos")) {
            isMac = true;
        }
    }

    @Override
    public void process(AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        AudioPlayer audioPlayer = new AudioPlayer(audioStream);
        audioPlayer.start();
        try {
            audioPlayer.join();
        } catch (InterruptedException e) {
            logger.error("Playing audio has been interrupted.");
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_AUDIO_FORMATS;
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_AUDIO_STREAMS;
    }

    @Override
    public String getId() {
        return "javasound";
    }

    @Override
    public String getLabel(Locale locale) {
        return "System Speaker";
    }

    @Override
    public PercentType getVolume() throws IOException {
        if (!isMac) {
            final Float[] volumes = new Float[1];
            runVolumeCommand(new Closure() {
                @Override
                public void execute(Object input) {
                    FloatControl volumeControl = (FloatControl) input;
                    volumes[0] = volumeControl.getValue();
                }
            });
            if (volumes[0] != null) {
                return new PercentType(new BigDecimal(volumes[0] * 100f));
            } else {
                throw new IOException("Cannot determine master volume level");
            }
        } else {
            // we use a cache of the value as the script execution is pretty slow
            if (macVolumeValue == null) {
                Process p = Runtime.getRuntime()
                        .exec(new String[] { "osascript", "-e", "output volume of (get volume settings)" });
                String value = IOUtils.toString(p.getInputStream()).trim();
                macVolumeValue = new PercentType(value);
            }
            return macVolumeValue;
        }
    }

    @Override
    public void setVolume(final PercentType volume) throws IOException {
        if (volume.intValue() < 0 || volume.intValue() > 100) {
            throw new IllegalArgumentException("Volume value must be in the range [0,100]!");
        }
        if (!isMac) {
            runVolumeCommand(new Closure() {
                @Override
                public void execute(Object input) {
                    FloatControl volumeControl = (FloatControl) input;
                    volumeControl.setValue(volume.floatValue() / 100f);
                }
            });
        } else {
            Runtime.getRuntime()
                    .exec(new String[] { "osascript", "-e", "set volume output volume " + volume.intValue() });
            macVolumeValue = volume;
        }
    }

    private void runVolumeCommand(Closure closure) {
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : infos) {
            Mixer mixer = AudioSystem.getMixer(info);
            if (mixer.isLineSupported(Port.Info.SPEAKER)) {
                Port port;
                try {
                    port = (Port) mixer.getLine(Port.Info.SPEAKER);
                    port.open();
                    if (port.isControlSupported(FloatControl.Type.VOLUME)) {
                        FloatControl volume = (FloatControl) port.getControl(FloatControl.Type.VOLUME);
                        closure.execute(volume);
                    }
                    port.close();
                } catch (LineUnavailableException e) {
                    logger.error("Cannot access master volume control", e);
                }
            }
        }
    }
}
