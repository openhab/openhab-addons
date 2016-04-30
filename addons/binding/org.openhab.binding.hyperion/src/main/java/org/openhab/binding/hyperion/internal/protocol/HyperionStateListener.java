/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol;

/**
 * The {@link HyperionStateListener} interface provides a mechanism for
 * being notified of a change of state in the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public interface HyperionStateListener {

    public void stateChanged(String property, Object oldValue, Object newValue);

}
