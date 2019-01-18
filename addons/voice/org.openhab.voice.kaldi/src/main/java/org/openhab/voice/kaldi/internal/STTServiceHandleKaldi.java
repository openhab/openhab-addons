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
package org.openhab.voice.kaldi.internal;

import org.eclipse.smarthome.core.voice.STTServiceHandle;

/**
 * Kaldi implementation of a STTServiceHandle
 *
 * @author Kelly Davis - Initial contribution and API
 *
 */
public class STTServiceHandleKaldi implements STTServiceHandle {

    /**
     * STTServiceKaldiRunnable managed by this instance
     */
    private final STTServiceKaldiRunnable sttServiceKaldiRunnable;

    /**
     * Creates an instance to manage the passed STTServiceKaldiRunnable
     *
     * @param sttServiceKaldiRunnable The managed STTServiceKaldiRunnable
     */
    public STTServiceHandleKaldi(STTServiceKaldiRunnable sttServiceKaldiRunnable) {
        this.sttServiceKaldiRunnable = sttServiceKaldiRunnable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abort() {
        this.sttServiceKaldiRunnable.abort();
    }
}
