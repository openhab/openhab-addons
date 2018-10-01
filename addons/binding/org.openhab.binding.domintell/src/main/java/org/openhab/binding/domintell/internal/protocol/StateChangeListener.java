/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol;

import org.openhab.binding.domintell.internal.protocol.model.Item;

/**
* The {@link StateChangeListener} is listener interface for item state changes
*
* @author Gabor Bicskei - Initial contribution
*/
public interface StateChangeListener {
    void itemStateChanged(Item item);
}
