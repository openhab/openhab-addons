/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.ets.profiles;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.UID;

/**
 * Identifier of a profile type.
 *
 * @author Karel Goderis - Initial contribution
 *
 */
@NonNullByDefault
public class KNXProfileTypeUID extends UID {

    public static final String KNX_SCOPE = "knx";

    public KNXProfileTypeUID(String profileType) {
        super(profileType);
    }

    public KNXProfileTypeUID(String scope, String id) {
        super(scope, id);
    }

    @Override
    protected int getMinimalNumberOfSegments() {
        return 2;
    }

    public String getScope() {
        return getSegment(0);
    }

    public String getId() {
        return getSegment(1);
    }

}
