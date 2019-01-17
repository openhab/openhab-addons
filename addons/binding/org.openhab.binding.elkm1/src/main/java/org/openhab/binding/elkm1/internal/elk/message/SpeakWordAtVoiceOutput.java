/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;
import org.openhab.binding.elkm1.internal.elk.ElkVoiceWords;

/**
 * Speaks the word at the voice output.
 *
 * @author David Bennett - Initial COntribution
 */
public class SpeakWordAtVoiceOutput extends ElkMessage {
    private ElkVoiceWords word;

    public SpeakWordAtVoiceOutput(ElkVoiceWords word) {
        super(ElkCommand.SpeakWordAtVoiceOutput);
        this.word = word;
    }

    @Override
    protected String getData() {
        return String.format("%03d", word.getValue());
    }
}
