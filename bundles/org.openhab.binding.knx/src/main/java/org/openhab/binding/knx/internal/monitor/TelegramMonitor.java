/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.monitor;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.monitor.MonitorStatus.Connection;
import org.openhab.core.types.Type;
import org.slf4j.LoggerFactory;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * The group monitor of one bridge: the last telegrams in a ring, and the last telegram per group address. Lives as
 * long as the bridge handler, across reconnects.
 * <p>
 * Appends come from the link's notifier thread and from the threads that send, queries from the console. Appends
 * only store what was seen; data type, value and names are added to a snapshot when rows are handed out, outside
 * the lock.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class TelegramMonitor {
    public static final int DEFAULT_BUFFER_SIZE = 2000;
    public static final int MAX_BUFFER_SIZE = 100_000;
    /** Addresses the sender bookkeeping of not-decrypted telegrams holds; later addresses are counted only. */
    public static final int MAX_FAILED_ADDRESSES = 256;
    /** Senders it holds per address; later senders are counted only. */
    public static final int MAX_FAILED_SENDERS = 16;

    /**
     * Told of new rows and of the outcome of own sends, on the thread that received or sent, after the monitor's
     * lock is released. It only notes what happened and returns at once; what it throws is caught.
     */
    public interface Listener {

        /** The row with this number is in the buffer. */
        void appended(long number);

        /** The own send of the row with this number, which was handed out as pending, ended so. */
        void outcomeChanged(long number, SendOutcome outcome);
    }

    private final Clock clock;
    private final String id = UUID.randomUUID().toString();
    private final Object lock = new Object();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    // guarded by lock
    private final ArrayDeque<StoredTelegram> ring = new ArrayDeque<>();
    private final Map<GroupAddress, StoredTelegram> lastPerAddress = new HashMap<>();
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private long lastNumber;
    private long received;
    private long sent;
    private long plainOnSecuredAddress;
    // not-decrypted bookkeeping, reset at every connect; guarded by lock
    private final Map<NotDecryptedReason, Long> notDecryptedCounts = new EnumMap<>(NotDecryptedReason.class);
    private long notDecryptedOtherReason;
    private final Map<GroupAddress, Set<IndividualAddress>> failedSenders = new LinkedHashMap<>();
    private final Set<WarnKey> warned = new HashSet<>();

    private volatile DataTypeLookup channelTypes = address -> null;
    private volatile ProjectLookup project = ProjectLookup.NONE;
    private volatile @Nullable Connection connection;

    public TelegramMonitor() {
        this(Clock.systemUTC());
    }

    public TelegramMonitor(Clock clock) {
        this.clock = clock;
    }

    /** {@return the size clamped into 0 to MAX_BUFFER_SIZE} */
    public static int clampBufferSize(int size) {
        return Math.max(0, Math.min(MAX_BUFFER_SIZE, size));
    }

    /** Keeps the newest rows that fit; size 0 keeps no rows and no last values. */
    public void resize(int size) {
        int newSize = clampBufferSize(size);
        synchronized (lock) {
            bufferSize = newSize;
            while (ring.size() > newSize) {
                ring.pollFirst();
            }
            if (newSize == 0) {
                lastPerAddress.clear();
            }
        }
    }

    /** Empties the rows and the last values; the running numbers keep counting. */
    public void clear() {
        synchronized (lock) {
            ring.clear();
            lastPerAddress.clear();
        }
    }

    public void useChannelTypes(DataTypeLookup channelTypes) {
        this.channelTypes = channelTypes;
    }

    public void useProject(ProjectLookup project) {
        this.project = project;
    }

    public void connected(ConnectionKind kind, IndividualAddress ownAddress) {
        connection = new Connection(kind, ownAddress);
    }

    public void disconnected() {
        connection = null;
    }

    /** Starts the not-decrypted counters, the sender bookkeeping and the warned set afresh; the rows stay. */
    public void resetNotDecrypted() {
        synchronized (lock) {
            notDecryptedCounts.clear();
            notDecryptedOtherReason = 0;
            failedSenders.clear();
            warned.clear();
        }
    }

    /**
     * Records a secured telegram to a group address that could not be decrypted: a row without payload, a count
     * and the sender. Works with buffer size 0, where only the count and the sender remain.
     *
     * @param reason why not; null if the library gave an error type outside the known ones
     * @param usedByChannel tells whether a channel listens to the address; asked outside the lock, and only while
     *            no warning was given yet for this address and reason on this connection
     * @return the warning to log, once per address and reason since the last connect and only for an address a
     *         channel uses; null otherwise
     */
    public @Nullable NotDecryptedWarning notDecrypted(IndividualAddress source, GroupAddress destination,
            @Nullable NotDecryptedReason reason, Predicate<GroupAddress> usedByChannel) {
        WarnKey key = new WarnKey(destination, reason);
        boolean alreadyWarned;
        long number;
        boolean stored;
        synchronized (lock) {
            number = appendLocked(source, destination, TelegramType.UNKNOWN, Direction.IN, new byte[0],
                    SecurityState.NOT_DECRYPTED, reason, null, null, null, false).number;
            stored = bufferSize > 0;
            if (reason == null) {
                notDecryptedOtherReason++;
            } else {
                notDecryptedCounts.merge(reason, 1L, Long::sum);
            }
            Set<IndividualAddress> senders = failedSenders.get(destination);
            if (senders == null && failedSenders.size() < MAX_FAILED_ADDRESSES) {
                senders = new LinkedHashSet<>();
                failedSenders.put(destination, senders);
            }
            if (senders != null && senders.size() < MAX_FAILED_SENDERS) {
                senders.add(source);
            }
            alreadyWarned = warned.contains(key);
        }
        if (stored) {
            appended(number);
        }
        if (alreadyWarned || !usedByChannel.test(destination)) {
            return null;
        }
        synchronized (lock) {
            if (!warned.add(key)) {
                return null;
            }
            Set<IndividualAddress> senders = failedSenders.get(destination);
            List<IndividualAddress> known = senders == null || senders.isEmpty() ? List.of(source)
                    : List.copyOf(senders);
            return new NotDecryptedWarning(destination, reason, known);
        }
    }

    /** {@return the senders of not-decrypted telegrams per address since the last connect, as far as bounded} */
    public Map<GroupAddress, List<IndividualAddress>> notDecryptedSenders() {
        synchronized (lock) {
            Map<GroupAddress, List<IndividualAddress>> result = new LinkedHashMap<>();
            failedSenders.forEach((address, senders) -> result.put(address, List.copyOf(senders)));
            return result;
        }
    }

    /**
     * What a warning is about.
     *
     * @param senders the senders seen for the address since the last connect, at least the current one
     */
    public record NotDecryptedWarning(GroupAddress address, @Nullable NotDecryptedReason reason,
            List<IndividualAddress> senders) {
    }

    private record WarnKey(GroupAddress address, @Nullable NotDecryptedReason reason) {
    }

    /**
     * Records a received telegram.
     *
     * @param asdu the plain value bytes; empty for a read request
     */
    public void received(IndividualAddress source, GroupAddress destination, TelegramType type, byte[] asdu,
            SecurityState security) {
        received(source, destination, type, asdu, security, false);
    }

    /**
     * Records a received telegram.
     *
     * @param asdu the plain value bytes; empty for a read request
     * @param plainOnSecuredAddress a plain telegram to an address that has a group key
     */
    public void received(IndividualAddress source, GroupAddress destination, TelegramType type, byte[] asdu,
            SecurityState security, boolean plainOnSecuredAddress) {
        append(source, destination, type, Direction.IN, asdu, security, null, null, null, null, plainOnSecuredAddress);
    }

    /**
     * Records an own send whose library call has ended.
     *
     * @param payload the value bytes the library put into the frame
     */
    public void sent(IndividualAddress source, GroupAddress destination, TelegramType type, byte[] payload,
            @Nullable String dataType, @Nullable ValueText value, SecurityState security, SendOutcome outcome) {
        append(source, destination, type, Direction.OUT, payload.clone(), security, null, dataType, value, outcome,
                false);
    }

    /** Records an own send before its library call; the caller reports the outcome when the call has ended. */
    public OwnSend sending(IndividualAddress source, GroupAddress destination, TelegramType type,
            @Nullable String dataType, @Nullable ValueText value, SecurityState security) {
        return append(source, destination, type, Direction.OUT, new byte[0], security, null, dataType, value,
                SendOutcome.PENDING, false);
    }

    private StoredTelegram append(IndividualAddress source, GroupAddress destination, TelegramType type,
            Direction direction, byte[] asdu, SecurityState security, @Nullable NotDecryptedReason reason,
            @Nullable String dataType, @Nullable ValueText value, @Nullable SendOutcome outcome,
            boolean plainOnSecured) {
        StoredTelegram row;
        boolean stored;
        synchronized (lock) {
            row = appendLocked(source, destination, type, direction, asdu, security, reason, dataType, value, outcome,
                    plainOnSecured);
            stored = bufferSize > 0;
        }
        if (stored) {
            appended(row.number);
        }
        return row;
    }

    /** Called with {@code lock} held. */
    private StoredTelegram appendLocked(IndividualAddress source, GroupAddress destination, TelegramType type,
            Direction direction, byte[] asdu, SecurityState security, @Nullable NotDecryptedReason reason,
            @Nullable String dataType, @Nullable ValueText value, @Nullable SendOutcome outcome,
            boolean plainOnSecured) {
        StoredTelegram row = new StoredTelegram(++lastNumber, clock.instant(), source, destination, type, direction,
                asdu, security, reason, dataType, value, outcome, plainOnSecured,
                outcome == SendOutcome.PENDING ? this::outcomeChanged : null);
        if (plainOnSecured) {
            plainOnSecuredAddress++;
        }
        if (direction == Direction.IN) {
            received++;
        } else {
            sent++;
        }
        if (bufferSize > 0) {
            ring.addLast(row);
            if (ring.size() > bufferSize) {
                ring.pollFirst();
            }
            if (type != TelegramType.READ && security != SecurityState.NOT_DECRYPTED) {
                lastPerAddress.put(destination, row);
            }
        }
        return row;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void appended(long number) {
        for (Listener listener : listeners) {
            try {
                listener.appended(number);
            } catch (RuntimeException e) {
                listenerFailed(e);
            }
        }
    }

    private void outcomeChanged(long number, SendOutcome outcome) {
        for (Listener listener : listeners) {
            try {
                listener.outcomeChanged(number, outcome);
            } catch (RuntimeException e) {
                listenerFailed(e);
            }
        }
    }

    private static void listenerFailed(RuntimeException e) {
        LoggerFactory.getLogger(TelegramMonitor.class).debug("A group monitor listener failed: {}",
                e.getClass().getName());
    }

    /** {@return a text that is different for every monitor; the row numbers of a monitor start again at 1} */
    public String id() {
        return id;
    }

    /** {@return the number of the newest row appended, also one the buffer no longer holds; 0 before the first} */
    public long lastNumber() {
        synchronized (lock) {
            return lastNumber;
        }
    }

    /** {@return every row held, oldest first} */
    public List<TelegramRecord> records() {
        return query(TelegramFilter.ALL);
    }

    /** {@return the rows the filter chooses, oldest first} */
    public List<TelegramRecord> query(TelegramFilter filter) {
        List<StoredTelegram> rows;
        long after = filter.afterNumber();
        synchronized (lock) {
            if (after < 0) {
                rows = new ArrayList<>(ring);
            } else if (filter.choosesByNumberOnly() && filter.limit() < ring.size()) {
                rows = oldestAfterLocked(after, filter.limit());
            } else {
                // the ring is ordered by number, so the rows above it are its tail
                rows = new ArrayList<>();
                Iterator<StoredTelegram> newestFirst = ring.descendingIterator();
                while (newestFirst.hasNext()) {
                    StoredTelegram row = newestFirst.next();
                    if (row.number <= after) {
                        break;
                    }
                    rows.add(row);
                }
                Collections.reverse(rows);
            }
        }
        Enricher enricher = new Enricher();
        // the filter needs the data type of every row, values are decoded only for the rows handed out
        Map<Long, StoredTelegram> byNumber = new HashMap<>();
        List<TelegramRecord> typed = new ArrayList<>(rows.size());
        for (StoredTelegram row : rows) {
            byNumber.put(row.number, row);
            typed.add(enricher.record(row, false));
        }
        List<TelegramRecord> result = new ArrayList<>();
        for (TelegramRecord chosen : filter.apply(typed)) {
            StoredTelegram row = byNumber.get(chosen.number());
            if (row != null) {
                result.add(enricher.record(row, true));
            }
        }
        return result;
    }

    /**
     * The rows a window chose, and the numbers of the first and the last row it looked at; 0 when it looked at none.
     */
    public record Window(List<TelegramRecord> rows, long first, long last) {
    }

    /**
     * Looks at the oldest rows above the number, at most {@code lookAt}, and stops at the row that reaches the
     * limit. The filter's own limit and context span are not used.
     *
     * @return the rows looked at that the filter {@link TelegramFilter#matches}, oldest first, and how far it looked
     */
    public Window window(TelegramFilter filter, long after, int limit, int lookAt) {
        List<StoredTelegram> rows;
        synchronized (lock) {
            rows = oldestAfterLocked(Math.max(0, after), lookAt);
        }
        Enricher enricher = new Enricher();
        List<TelegramRecord> chosen = new ArrayList<>();
        long first = 0;
        long last = 0;
        for (StoredTelegram row : rows) {
            if (chosen.size() >= limit) {
                break;
            }
            if (first == 0) {
                first = row.number;
            }
            last = row.number;
            if (filter.matches(enricher.record(row, false))) {
                chosen.add(enricher.record(row, true));
            }
        }
        return new Window(List.copyOf(chosen), first, last);
    }

    /** Called with {@code lock} held; walks in from the end of the ring nearer to the number. */
    private List<StoredTelegram> oldestAfterLocked(long after, int limit) {
        if (ring.isEmpty() || ring.getLast().number <= after || limit == 0) {
            return new ArrayList<>();
        }
        if (after - ring.getFirst().number < ring.getLast().number - after) {
            List<StoredTelegram> rows = new ArrayList<>(limit);
            for (StoredTelegram row : ring) {
                if (row.number > after) {
                    rows.add(row);
                    if (rows.size() >= limit) {
                        break;
                    }
                }
            }
            return rows;
        }
        ArrayDeque<StoredTelegram> kept = new ArrayDeque<>();
        Iterator<StoredTelegram> newestFirst = ring.descendingIterator();
        while (newestFirst.hasNext()) {
            StoredTelegram row = newestFirst.next();
            if (row.number <= after) {
                break;
            }
            kept.addFirst(row);
            if (kept.size() > limit) {
                kept.pollLast();
            }
        }
        return new ArrayList<>(kept);
    }

    /** {@return the last telegram per group address, read requests left out} */
    public Map<GroupAddress, TelegramRecord> lastTelegrams() {
        List<StoredTelegram> rows;
        synchronized (lock) {
            rows = new ArrayList<>(lastPerAddress.values());
        }
        Enricher enricher = new Enricher();
        Map<GroupAddress, TelegramRecord> result = new LinkedHashMap<>();
        rows.stream().sorted((a, b) -> Long.compare(a.number, b.number))
                .forEach(row -> result.put(row.destination, enricher.record(row, true)));
        return result;
    }

    public MonitorStatus status() {
        synchronized (lock) {
            return new MonitorStatus(connection, bufferSize, ring.size(), received, sent, notDecryptedCounts,
                    notDecryptedOtherReason, plainOnSecuredAddress);
        }
    }

    /** Adds data type, value and names to rows; remembers the data type per address for one hand-out. */
    private class Enricher {
        private final DataTypeLookup channels = channelTypes;
        private final ProjectLookup names = project;
        private final Map<GroupAddress, Optional<DataType>> dataTypes = new HashMap<>();

        TelegramRecord record(StoredTelegram row, boolean withValue) {
            String dataTypeId = row.sentDataType;
            ValueText value = row.sentValue;
            if (row.direction == Direction.IN) {
                DataType dataType = dataTypeOf(row.destination);
                dataTypeId = dataType == null ? null : dataType.dpt();
                if (withValue && dataType != null && row.payload.length > 0
                        && row.security != SecurityState.NOT_DECRYPTED) {
                    value = decode(dataType, row.payload);
                }
            }
            return new TelegramRecord(row.number, row.time, row.source, row.destination, row.type, row.direction,
                    row.payload, row.security, row.reason, row.outcome(), dataTypeId,
                    value == null ? null : value.value(), value == null ? null : value.unit(), name(row.source),
                    name(row.destination), row.plainOnSecuredAddress);
        }

        private @Nullable DataType dataTypeOf(GroupAddress address) {
            Optional<DataType> known = dataTypes.get(address);
            if (known == null) {
                known = lookUp(address);
                dataTypes.put(address, known);
            }
            return known.orElse(null);
        }

        private Optional<DataType> lookUp(GroupAddress address) {
            try {
                DataType dataType = channels.dataTypeOf(address);
                if (dataType != null) {
                    return Optional.of(dataType);
                }
                String fromProject = names.dataType(address);
                return fromProject == null ? Optional.empty() : Optional.of(new DataType(fromProject, Type.class));
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        }

        private @Nullable ValueText decode(DataType dataType, byte[] payload) {
            try {
                Type value = MonitorDecoder.decode(dataType.dpt(), payload, dataType.preferredType());
                return value == null ? null : MonitorDecoder.text(value);
            } catch (RuntimeException e) {
                return null;
            }
        }

        private @Nullable String name(IndividualAddress address) {
            try {
                return names.deviceName(address);
            } catch (RuntimeException e) {
                return null;
            }
        }

        private @Nullable String name(GroupAddress address) {
            try {
                return names.groupAddressName(address);
            } catch (RuntimeException e) {
                return null;
            }
        }
    }
}
