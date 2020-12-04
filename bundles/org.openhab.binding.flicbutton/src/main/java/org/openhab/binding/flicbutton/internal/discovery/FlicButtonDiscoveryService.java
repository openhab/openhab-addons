/**
 * Copyright (c) 2016 - 2020 Patrick Fink
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 3
 * with the GNU Classpath Exception 2.0 which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-3.0 WITH Classpath-exception-2.0
 */
package org.openhab.binding.flicbutton.internal.discovery;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;

import io.flic.fliclib.javaclient.Bdaddr;
import io.flic.fliclib.javaclient.FlicClient;

/**
 *
 * @author Patrick Fink
 *
 */
public interface FlicButtonDiscoveryService extends DiscoveryService {

    /**
     *
     * @param bdaddr Bluetooth address of the discovered Flic button
     * @return UID that was created by the discovery service
     */
    public ThingUID flicButtonDiscovered(Bdaddr bdaddr);

    public void activate(@NonNull FlicClient client);

    public void deactivate();
}
