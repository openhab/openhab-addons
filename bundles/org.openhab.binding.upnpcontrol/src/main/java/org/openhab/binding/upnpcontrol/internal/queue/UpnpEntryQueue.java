/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal.queue;

import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.PLAYLIST_FILE_EXTENSION;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * The class {@link UpnpEntryQueue} represents a queue of UPnP media entries to be played on a renderer. It keeps track
 * of a current index in the queue. It has convenience methods to play previous/next entries, whereby the queue can be
 * organized to play from first to last (with no repetition), to restart at the start when the end is reached (in a
 * continuous loop), or to random shuffle the entries. Repeat and shuffle are off by default, but can be set using the
 * {@link #setRepeat} and {@link #setShuffle} methods.
 *
 * @author Mark Herwege - Initial contribution
 *
 */
@NonNullByDefault
public class UpnpEntryQueue {

    private final Logger logger = LoggerFactory.getLogger(UpnpEntryQueue.class);

    private volatile boolean repeat = false;
    private volatile boolean shuffle = false;

    private volatile int currentIndex = -1;

    private class Playlist {
        @SuppressWarnings("unused")
        String name; // Used in serialization
        volatile Map<String, List<UpnpEntry>> masterList;

        Playlist(String name, Map<String, List<UpnpEntry>> masterList) {
            this.name = name;
            this.masterList = masterList;
        }
    }

    private volatile Playlist playlist;

    private volatile List<UpnpEntry> currentQueue;
    private volatile List<UpnpEntry> shuffledQueue = Collections.emptyList();

    private final Gson gson = new Gson();

    public UpnpEntryQueue() {
        this(Collections.emptyList());
    }

    /**
     * @param queue
     */
    public UpnpEntryQueue(List<UpnpEntry> queue) {
        this(queue, "");
    }

    /**
     * @param queue
     * @param udn Defines the UPnP media server source of the queue, could be used to re-query the server if URL
     *            resources are out of date.
     */
    public UpnpEntryQueue(List<UpnpEntry> queue, @Nullable String udn) {
        String serverUdn = (udn != null) ? udn : "";
        Map<String, List<UpnpEntry>> masterList = Collections.synchronizedMap(new HashMap<>());
        List<UpnpEntry> localQueue = new ArrayList<>(queue);
        masterList.put(serverUdn, localQueue);
        playlist = new Playlist("", masterList);
        currentQueue = localQueue.stream().filter(e -> !e.isContainer()).collect(Collectors.toList());
    }

    /**
     * Switch on/off repeat mode.
     *
     * @param repeat
     */
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    /**
     * Switch on/off shuffle mode.
     *
     * @param shuffle
     */
    public synchronized void setShuffle(boolean shuffle) {
        if (shuffle) {
            shuffle();
        } else {
            int index = currentIndex;
            if (index != -1) {
                currentIndex = currentQueue.indexOf(shuffledQueue.get(index));
            }
            this.shuffle = false;
        }
    }

    private synchronized void shuffle() {
        UpnpEntry current = null;
        int index = currentIndex;
        if (index != -1) {
            current = this.shuffle ? shuffledQueue.get(index) : currentQueue.get(index);
        }

        // Shuffle the queue again
        shuffledQueue = new ArrayList<>(currentQueue);
        Collections.shuffle(shuffledQueue);
        if (current != null) {
            // Put the current entry at the beginning of the shuffled queue
            shuffledQueue.remove(current);
            shuffledQueue.add(0, current);
            currentIndex = 0;
        }

        this.shuffle = true;
    }

    /**
     * @return will return the next element in the queue, or null when the end of the queue has been reached. With
     *         repeat set, will restart at the beginning of the queue when the end has been reached. The method will
     *         return null if the queue is empty.
     */
    public synchronized @Nullable UpnpEntry next() {
        currentIndex++;
        if (currentIndex >= size()) {
            if (shuffle && repeat) {
                currentIndex = -1;
                shuffle();
            }
            currentIndex = repeat ? 0 : -1;
        }
        return currentIndex >= 0 ? get(currentIndex) : null;
    }

    /**
     * @return will return the previous element in the queue, or null when the start of the queue has been reached. With
     *         repeat set, will restart at the end of the queue when the start has been reached. The method will return
     *         null if the queue is empty.
     */
    public synchronized @Nullable UpnpEntry previous() {
        currentIndex--;
        if (currentIndex < 0) {
            if (shuffle && repeat) {
                currentIndex = -1;
                shuffle();
            }
            currentIndex = repeat ? (size() - 1) : -1;
        }
        return currentIndex >= 0 ? get(currentIndex) : null;
    }

    /**
     * @return the index of the current element in the queue.
     */
    public int index() {
        return currentIndex;
    }

    /**
     * @return the index of the next element in the queue that will be served if {@link #next} is called, or -1 if
     *         nothing to serve for next.
     */
    public synchronized int nextIndex() {
        int index = currentIndex + 1;
        if (index >= size()) {
            index = repeat ? 0 : -1;
        }
        return index;
    }

    /**
     * @return the index of the previous element in the queue that will be served if {@link #previous} is called, or -1
     *         if nothing to serve for next.
     */
    public synchronized int previousIndex() {
        int index = currentIndex - 1;
        if (index < 0) {
            index = repeat ? (size() - 1) : -1;
        }
        return index;
    }

    /**
     * @return true if there is an element to server when calling {@link #next}.
     */
    public synchronized boolean hasNext() {
        int size = currentQueue.size();
        if (repeat && (size > 0)) {
            return true;
        }
        return (currentIndex < (size - 1));
    }

    /**
     * @return true if there is an element to server when calling {@link #previous}.
     */
    public synchronized boolean hasPrevious() {
        int size = currentQueue.size();
        if (repeat && (size > 0)) {
            return true;
        }
        return (currentIndex > 0);
    }

    /**
     * @param index
     * @return the UpnpEntry at the index position in the queue, or null when none can be retrieved.
     */
    public @Nullable synchronized UpnpEntry get(int index) {
        if ((index >= 0) && (index < size())) {
            if (shuffle) {
                return shuffledQueue.get(index);
            } else {
                return currentQueue.get(index);
            }
        } else {
            return null;
        }
    }

    /**
     * Reset the queue position to before the start of the queue (-1).
     */
    public synchronized void resetIndex() {
        currentIndex = -1;
        if (shuffle) {
            shuffle();
        }
    }

    /**
     * @return number of element in the queue.
     */
    public synchronized int size() {
        return currentQueue.size();
    }

    /**
     * @return true if the queue is empty.
     */
    public synchronized boolean isEmpty() {
        return currentQueue.isEmpty();
    }

    /**
     * Persist queue as a playlist with name "current"
     *
     * @param path of playlist directory
     */
    public void persistQueue(String path) {
        persistQueue("current", false, path);
    }

    /**
     * Persist the queue as a playlist.
     *
     * @param name of the playlist
     * @param append to the playlist if it already exists
     * @param path of playlist directory
     */
    public synchronized void persistQueue(String name, boolean append, String path) {
        String fileName = path + name + PLAYLIST_FILE_EXTENSION;
        File file = new File(fileName);

        String json;

        try {
            // ensure full path exists
            file.getParentFile().mkdirs();

            if (append && file.exists()) {
                try {
                    logger.debug("Reading contents of {} for appending", file.getAbsolutePath());
                    final byte[] contents = Files.readAllBytes(file.toPath());
                    json = new String(contents, StandardCharsets.UTF_8);
                    Playlist appendList = gson.fromJson(json, Playlist.class);
                    if (appendList == null) {
                        // empty playlist file, so just overwrite
                        playlist.name = name;
                        json = gson.toJson(playlist);
                    } else {
                        // Merging masterList with persistList, overwriting persistList UpnpEntry objects with same id
                        playlist.masterList.forEach((u, list) -> appendList.masterList.merge(u, list,
                                (oldlist,
                                        newlist) -> new ArrayList<>(Stream.of(oldlist, newlist).flatMap(List::stream)
                                                .collect(Collectors.toMap(UpnpEntry::getId, entry -> entry,
                                                        (UpnpEntry oldentry, UpnpEntry newentry) -> newentry))
                                                .values())));

                        json = gson.toJson(new Playlist(name, appendList.masterList));
                    }
                } catch (JsonParseException | UnsupportedOperationException e) {
                    logger.debug("Could not append, JsonParseException reading {}: {}", file.toPath(), e.getMessage(),
                            e);
                    return;
                } catch (IOException e) {
                    logger.debug("Could not append, IOException reading playlist {} from {}", name, file.toPath());
                    return;
                }
            } else {
                playlist.name = name;
                json = gson.toJson(playlist);
            }

            final byte[] contents = json.getBytes(StandardCharsets.UTF_8);
            Files.write(file.toPath(), contents);
        } catch (IOException e) {
            logger.debug("IOException writing playlist {} to {}", name, file.toPath());
        }
    }

    /**
     * Replace the current queue with the playlist name and reset the queue index.
     *
     * @param name
     * @param path directory containing playlist to restore
     */
    public void restoreQueue(String name, @Nullable String path) {
        restoreQueue(name, null, path);
    }

    /**
     * Replace the current queue with the playlist name and reset the queue index. Filter the content of the playlist on
     * the server udn.
     *
     * @param name
     * @param udn of the server the playlist entries were created on, all entries when null
     * @param path of playlist directory
     */
    public synchronized void restoreQueue(String name, @Nullable String udn, @Nullable String path) {
        if (path == null) {
            return;
        }

        String fileName = path + name + PLAYLIST_FILE_EXTENSION;
        File file = new File(fileName);

        if (file.exists()) {
            try {
                logger.debug("Reading contents of {}", file.getAbsolutePath());
                final byte[] contents = Files.readAllBytes(file.toPath());
                final String json = new String(contents, StandardCharsets.UTF_8);

                Playlist list = gson.fromJson(json, Playlist.class);
                if (list == null) {
                    logger.debug("Empty playlist file {}", file.getAbsolutePath());
                    return;
                }

                playlist = list;

                Stream<Entry<String, List<UpnpEntry>>> stream = playlist.masterList.entrySet().stream();
                if (udn != null) {
                    stream = stream.filter(u -> u.getKey().equals(udn));
                }
                currentQueue = stream.map(p -> p.getValue()).flatMap(List::stream).filter(e -> !e.isContainer())
                        .collect(Collectors.toList());
                resetIndex();
            } catch (JsonParseException | UnsupportedOperationException e) {
                logger.debug("JsonParseException reading {}: {}", file.toPath(), e.getMessage(), e);
            } catch (IOException e) {
                logger.debug("IOException reading playlist {} from {}", name, file.toPath());
            }
        }
    }

    /**
     * @return list of all UpnpEntries in the queue.
     */
    public List<UpnpEntry> getEntryList() {
        return currentQueue;
    }
}
