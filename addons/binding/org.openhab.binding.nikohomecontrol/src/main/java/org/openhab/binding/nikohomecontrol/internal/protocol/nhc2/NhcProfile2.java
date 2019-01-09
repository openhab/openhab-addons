/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

/**
 * {@link NhcProfile2} represents a Niko Home Control II profile. It is used when parsing the profile response json.
 *
 * @author Mark Herwege - Initial Contribution
 *
 */
class NhcProfile2 {

    String name;
    String type;
    String uuid;
}
