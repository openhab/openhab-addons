/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal.link;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Not used in production code, simple class to quickly test / play around
 * with the protocol.
 *
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public class Test {

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        LightifyLink link = new LightifyLink("172.25.100.141", scheduler);
        link.performSearch(l -> {
            link.disconnect();
            if (l instanceof LightifyLight) {
                link.performSwitch(l, !l.isPowered(), System.out::println);
            }
        });
    }

}
