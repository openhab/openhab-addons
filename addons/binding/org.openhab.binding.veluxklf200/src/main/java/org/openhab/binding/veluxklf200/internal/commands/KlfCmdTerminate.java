/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.commands;

import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandStructure;
import org.openhab.binding.veluxklf200.internal.engine.KLFCommandProcessor;

/**
 * Dummy command that is never actually sent to the KLF200 unit. This is
 * effectively a 'poison pill' that is used to signal to the
 * {@link KLFCommandProcessor} to shutdown its processing queues.
 *
 * @author MFK - Initial Contribution
 */
public class KlfCmdTerminate extends BaseKLFCommand {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#getKLFCommandStructure
     * ()
     */
    @Override
    public KLFCommandStructure getKLFCommandStructure() {
        return KLFCommandStructure.NOTHING;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#handleResponse(byte[])
     */
    @Override
    public void handleResponse(byte[] data) {
        // Never used or needed for this command.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.velux.klf200.internal.commands.BaseKLFCommand#pack()
     */
    @Override
    protected byte[] pack() {
        // Although should never return null, its ok in this case as this
        // command should never progress into processing.
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.velux.klf200.internal.commands.BaseKLFCommand#extractSession(byte[])
     */
    @Override
    protected int extractSession(short responseCode, byte[] data) {
        return 0;
    }
}
