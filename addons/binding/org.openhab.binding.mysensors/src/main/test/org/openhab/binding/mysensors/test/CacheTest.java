package org.openhab.binding.mysensors.test;

import java.util.ArrayList;

import org.junit.Test;
import org.openhab.binding.mysensors.internal.factory.MySensorsCacheFactory;

import com.google.gson.reflect.TypeToken;

/**
 * Test cases for the MySensorsCacheFactory (ID cache).
 *
 * @author Andrea Cioni
 *
 */
public class CacheTest {

    @Test
    public void writeGivenIdsCache() {
        MySensorsCacheFactory c = MySensorsCacheFactory.getCacheFactory();
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ids.add(2);
        ids.add(3);
        ids.add(5);
        c.writeCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, ids, new TypeToken<ArrayList<Integer>>() {
        }.getType());
    }

    @Test
    public void readGivenIdsCache() {
        MySensorsCacheFactory c = MySensorsCacheFactory.getCacheFactory();
        System.out.println(c.readCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, new ArrayList<Integer>(),
                new TypeToken<ArrayList<Integer>>() {
                }.getType()));
    }

}
