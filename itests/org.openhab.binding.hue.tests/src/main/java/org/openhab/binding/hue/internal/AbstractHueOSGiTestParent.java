/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.hue.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class AbstractHueOSGiTestParent extends JavaOSGiTest {

    /**
     * Gets the handler of a thing if it fits to a specific type.
     *
     * @param thing the thing
     * @param clazz type of thing handler
     * @return the thing handler
     */
    protected <T extends ThingHandler> T getThingHandler(Thing thing, Class<T> clazz) {
        return waitForAssert(() -> {
            final ThingHandler tmp = thing.getHandler();
            if (clazz.isInstance(tmp)) {
                return clazz.cast(tmp);
            } else {
                assertNotNull(tmp);
                assertEquals(clazz, tmp.getClass());
                throw new RuntimeException();
            }
        });
    }
}
