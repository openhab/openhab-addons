/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.voicerss.internal;
/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link AudioStream} interface for the
 * {@link VoiceRSSTTSService}. It is always a {@link FixedLengthAudioStream} to
 * allow streaming on device like Sonos.
 *
 * @author Jochen Hiller - Initial contribution and API
 */
class VoiceRSSAudioStream extends FixedLengthAudioStream {

	private final AudioFormat audioFormat;
	private final File audioFile;
	private final InputStream inputStream;

	/**
	 * Constructs an audio stream for a given file and audio format. Note: we
	 * need a file here to be able to return a cloned stream for a
	 * {@link FixedLengthAudioStream} on same file.
	 * 
	 * @param audioFile
	 *            the file with the audio file. It must be available as long as
	 *            the audio stream will be used, to support a cloned stream
	 * @param audioFormat
	 *            the supported audio format
	 * @throws FileNotFoundException
	 *             will be thrown if audioFile does not exist
	 */
	public VoiceRSSAudioStream(File audioFile, AudioFormat audioFormat) throws FileNotFoundException {
		this.audioFile = audioFile;
		this.inputStream = new FileInputStream(audioFile);
		this.audioFormat = audioFormat;
	}

	@Override
	public AudioFormat getFormat() {
		return this.audioFormat;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return this.inputStream.read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return this.inputStream.read(b, off, len);
	}

	@Override
	public int read() throws IOException {
		return this.inputStream.read();
	}

	// implementation of FixedLengthAudioStream

	/**
	 * A {@link FixedLengthAudioStream} needs the length. For a MP3 file we
	 * simply can use the file length.
	 */
	@Override
	public long length() {
		return this.audioFile.length();
	}

	@Override
	public InputStream getClonedStream() throws AudioException {
		try {
			return new FileInputStream(this.audioFile);
		} catch (FileNotFoundException ex) {
			Logger logger = LoggerFactory.getLogger(VoiceRSSAudioStream.class);
			String errorMsg = "Could not create cloned stream as file " + this.audioFile.toString() + " is missing";
			logger.error(errorMsg);
			throw new AudioException(errorMsg, ex);
		}
	}
}
