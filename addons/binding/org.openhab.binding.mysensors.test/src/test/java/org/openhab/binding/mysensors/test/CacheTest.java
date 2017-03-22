/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.test;

import java.util.ArrayList;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.junit.AfterClass;
import org.junit.Test;
import org.openhab.binding.mysensors.factory.MySensorsCacheFactory;

import com.google.gson.reflect.TypeToken;

/**
 * Test cases for the MySensorsCacheFactory (ID cache).
 *
 * @author Andrea Cioni
 *
 */
public class CacheTest {

    private static MySensorsCacheFactory c = new MySensorsCacheFactory(ConfigConstants.getUserDataFolder());;

    @Test
    public void writeGivenIdsCache() {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ids.add(2);
        ids.add(3);
        ids.add(5);
        c.writeCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, ids, new TypeToken<ArrayList<Integer>>() {
        }.getType());
    }

    @Test
    public void readGivenIdsCache() {
        System.out.println(c.readCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, new ArrayList<Integer>(),
                new TypeToken<ArrayList<Integer>>() {
                }.getType()));
    }

    @AfterClass
    public static void deleteCache() {
        c.deleteCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE);
    }
}
