/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.helper;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.bosesoundtouch.handler.BoseSoundTouchHandler.BSTKeys;

/**
 * The {@link WSHelperInterface} class defines methods to be implemented to communicate with the REST API of the
 * speaker.
 *
 * @author syracom - Initial contribution
 */
public interface WSHelperInterface {

    public String pressAndReleaseButtonOnSpeaker(BSTKeys keyIdentifier);

    public String selectAUX();

    public String selectBluetooth();

    public String setVolume(PercentType num);

    public String setBass(DecimalType num);

    public String get(String service);

}
