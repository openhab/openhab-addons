package org.openhab.binding.gmailparadoxparser.internal;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.gmailparadoxparser.mail.adapter.MailAdapter;
import org.openhab.binding.gmailparadoxparser.model.ParadoxPartition;

public class StatesCache implements Cache<String, ParadoxPartition> {

    private static Map<String, ParadoxPartition> partitionsStates = new HashMap<String, ParadoxPartition>();
    private static StatesCache instance;

    public static StatesCache getInstance() {
        if (instance == null) {
            synchronized (StatesCache.class) {
                instance = new StatesCache();
            }
        }
        return instance;
    }

    @Override
    public void put(String key, ParadoxPartition value) {
        synchronized (StatesCache.class) {
            partitionsStates.put(key, value);
        }

    }

    @Override
    public ParadoxPartition get(String key) {
        return partitionsStates.get(key);
    }

    @Override
    public void refresh() {
        this.refresh(MailAdapter.QUERY_UNREAD);
    }

    @Override
    public void refresh(String query) {

    }

}
