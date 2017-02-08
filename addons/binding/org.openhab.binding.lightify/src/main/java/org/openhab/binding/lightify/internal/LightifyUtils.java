/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.ServiceInfo;
import java.util.concurrent.Callable;

/**
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public final class LightifyUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(LightifyUtils.class);

    private LightifyUtils() {
    }

    public static boolean isLightifyGateway(ServiceInfo serviceInfo) {
        return serviceInfo.getName().contains("Lightify-");
    }

    public static String extractLightifyUID(String name) {
        return name.replace("Lightify-", "");
    }

    public static <R> R exceptional(Callable<R> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            LOGGER.error("Error on execution", e);
            throw new RuntimeException(e);
        }
    }

    public static void exceptional(Exceptional exceptional) {
        try {
            exceptional.call();
        } catch (Exception e) {
            LOGGER.error("Error on execution", e);
            throw new RuntimeException(e);
        }
    }

    public interface Exceptional {
        void call() throws Exception;
    }
}
