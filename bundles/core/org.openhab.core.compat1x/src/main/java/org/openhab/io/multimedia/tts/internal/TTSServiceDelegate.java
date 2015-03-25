/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.multimedia.tts.internal;

import org.eclipse.smarthome.io.multimedia.tts.TTSService;

/**
 * This class serves as a mapping from the "old" org.openhab namespace to the
 * new org.eclipse.smarthome namespace for the action service. It wraps an
 * instance with the old interface into a class with the new interface.
 * 
 * @author Tobias Bräutigam - Initial contribution and API
 */
public class TTSServiceDelegate implements TTSService {

	private org.openhab.io.multimedia.tts.TTSService service;

	public TTSServiceDelegate(org.openhab.io.multimedia.tts.TTSService service) {
		this.service = service;
	}

	@Override
	public void say(String text, String voice, String outputDevice) {
		service.say(text, voice, outputDevice);
	}

}
