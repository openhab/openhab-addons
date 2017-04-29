/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.cmusphinx.internal;

import org.eclipse.smarthome.core.voice.STTServiceHandle;

/**
 * CMU Sphinx implementation of a STTServiceHandle
 *
 * @author Yannick Schaus - Initial contribution and API
 *
 */
public class STTServiceHandleCMUSphinx implements STTServiceHandle {

    STTServiceCMUSphinxRunnable runnable;

    /**
     * Creates an instance to manage the passed STTServiceCMUSphinxRunnable
     *
     * @param runnable The managed STTServiceKaldiRunnable
     */
    public STTServiceHandleCMUSphinx(STTServiceCMUSphinxRunnable runnable) {
        this.runnable = runnable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abort() {

    }
}
