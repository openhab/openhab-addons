package org.openhab.binding.gmailparadoxparser.internal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.gmailparadoxparser.mail.adapter.GmailAdapter;
import org.openhab.binding.gmailparadoxparser.mail.adapter.MailAdapter;
import org.openhab.binding.gmailparadoxparser.mail.adapter.MailParser;
import org.openhab.binding.gmailparadoxparser.model.ParadoxPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatesCache implements Cache<String, ParadoxPartition> {

    private static StatesCache instance;
    private Map<String, ParadoxPartition> partitionsStates = new HashMap<String, ParadoxPartition>();
    private MailAdapter mailAdapter;
    private final Logger logger = LoggerFactory.getLogger(StatesCache.class);

    private StatesCache() {
        try {
            mailAdapter = new GmailAdapter();
        } catch (IOException | GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
        }
    }

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
    public void initialize() {
        refresh(MailAdapter.INITIAL_QUERY);
    }

    @Override
    public void refresh() {
        this.refresh(MailAdapter.QUERY_UNREAD);
    }

    @Override
    public void refresh(String query) {
        try {
            synchronized (getClass()) {
                List<String> retrievedMessages = mailAdapter.retrieveAllMessagesContentsAndMarkAllRead(query);
                Set<ParadoxPartition> partitionsUpdatedStates = MailParser.getInstance()
                        .parseToParadoxPartitionStates(retrievedMessages);

                if (partitionsUpdatedStates.isEmpty()) {
                    logger.debug("Received empty set. Nothing to update.");
                    return;
                }

                for (ParadoxPartition paradoxPartition : partitionsUpdatedStates) {
                    put(paradoxPartition.getPartition(), paradoxPartition);
                }
            }
        } catch (IOException e) {
            logger.trace(e.getMessage(), e);
        }
    }
}
