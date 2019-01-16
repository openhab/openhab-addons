/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.googletts.internal.protocol;

import java.util.List;

/**
 * The message returned to the client by the voices.list method.
 *
 * @author Wouter Born - Initial contribution
 */
public class ListVoicesResponse {

    /**
     * The list of voices.
     */
    private List<Voice> voices;

    public List<Voice> getVoices() {
        return voices;
    }

    public void setVoices(List<Voice> voices) {
        this.voices = voices;
    }

}
