/*
 * CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *
 * Copyright 2008, 2009, 2012, 2017 Dave Oxley <dave@daveoxley.co.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.cbus.internal.cgate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public abstract class CGateObject {
    private final CGateSession cgate_session;

    protected CGateObject(CGateSession cgate_session) {
        this.cgate_session = cgate_session;
    }

    protected CGateSession getCGateSession() {
        return cgate_session;
    }

    static HashMap<String, String> responseToMap(String cgate_response) {
        return responseToMap(cgate_response, false);
    }

    static HashMap<String, String> responseToMap(String cgate_response, boolean db_response) {
        cgate_response = cgate_response.substring(4);

        HashMap<String, String> map = new HashMap<String, String>();

        if (db_response)
            addResponseToMap(cgate_response, map);
        else {
            String resp_array[] = cgate_response.split(" ");
            for (String resp : resp_array)
                addResponseToMap(resp, map);
        }

        return map;
    }

    private static void addResponseToMap(String resp, HashMap<String, String> map) {
        int index = resp.indexOf("=");
        if (index > -1) {
            String value = resp.substring(index + 1);
            if (value.equals("null"))
                value = "";
            map.put(resp.substring(0, index), value);
        }
    }

    protected abstract String getKey();

    private final HashMap<String, Map<String, CGateObject>> subtree_cache = new HashMap<String, Map<String, CGateObject>>();

    public abstract CGateObject getCGateObject(String address) throws CGateException;

    abstract String getProjectAddress();

    abstract String getResponseAddress(boolean id);

    public final String getAddress() {
        String respAddress = getResponseAddress(true);
        return getProjectAddress() + ((respAddress != null && !respAddress.equals("")) ? "/" + respAddress : "");
    }

    protected void setupSubtreeCache(String cache_key) {
        subtree_cache.put(cache_key, Collections.synchronizedMap(new HashMap<String, CGateObject>()));
    }

    protected CGateObject cacheObject(String cache_key, CGateObject cgate_obj) {
        return subtree_cache.get(cache_key).put(cgate_obj.getKey(), cgate_obj);
    }

    protected CGateObject uncacheObject(String cache_key, CGateObject cgate_obj) {
        return subtree_cache.get(cache_key).remove(cgate_obj);
    }

    protected CGateObject getCachedObject(String cache_key, String key) {
        return subtree_cache.get(cache_key).get(key);
    }

    protected Collection<CGateObject> getAllCachedObjects(String cache_key) {
        return subtree_cache.get(cache_key).values();
    }

    protected void clearCache(String cache_key) {
        subtree_cache.get(cache_key).clear();
    }

    protected void clearCache() {
        for (Map<String, CGateObject> map : subtree_cache.values())
            map.clear();
    }
}
