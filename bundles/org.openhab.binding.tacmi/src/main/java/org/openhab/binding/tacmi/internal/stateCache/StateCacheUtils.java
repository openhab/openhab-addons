/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.tacmi.internal.stateCache;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tacmi.internal.message.Message;
import org.openhab.binding.tacmi.internal.podData.PodData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link StateCache} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class StateCacheUtils {

    private final Logger logger = LoggerFactory.getLogger(StateCacheUtils.class);

    // pretty print
    final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    final File stateCacheFile;

    public StateCacheUtils(final File file, final Collection<@Nullable PodData> podDatas) {
        this.stateCacheFile = file;
        if (this.stateCacheFile.exists()) {
            FileReader fr = null;
            try {
                fr = new FileReader(stateCacheFile);
                @Nullable
                final StateCache sc = gson.fromJson(fr, StateCache.class);
                @Nullable
                final Collection<PodStates> pods = sc.pods;
                if (pods != null) {
                    for (PodStates storedPod : pods) {
                        @Nullable
                        Collection<PodState> spe = storedPod.entries;
                        if (spe != null) {
                            for (PodData pod : podDatas) {
                                if (pod == null) {
                                    continue;
                                }
                                @Nullable
                                Message message = pod.message;
                                // pod.message is only initialzied for outgoing pod's
                                if (message != null && pod.podId == storedPod.podId) {
                                    final Iterator<PodState> spi = spe.iterator();
                                    int id = 0;
                                    while (spi.hasNext() && id < 4) {
                                        @Nullable // seems quit idiotic here as null checks also generate warnings... ?
                                        final PodState ps = spi.next();
                                        // if (ps == null) {
                                        // message.setValue(id, (short) 0, 0);
                                        // } else {
                                        message.setValue(id, (short) (ps.value & 0xffff), ps.measureType);
                                        // }
                                        id++;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (final Exception t) {
                logger.warn("Restore of state file {} failed: {}", this.stateCacheFile, t.getMessage(), t);
            } finally {
                if (fr != null)
                    try {
                        fr.close();
                    } catch (final Exception t) {
                        // ignore...
                    }
            }

        }
    }

    public void persistStates(final Collection<@Nullable PodData> data) {
        try {
            boolean dirty = false;
            for (final PodData pd : data) {
                if (pd != null && pd.message != null && pd.dirty)
                    dirty = true;
            }
            if (!dirty)
                return;

            // we have to persist - transfer state to json structure...
            final StateCache sc = new StateCache();
            Collection<PodStates> pods = new ArrayList<>();
            sc.pods = pods;
            for (final PodData pd : data) {
                if (pd == null) {
                    continue;
                }
                Message pdm = pd.message;
                if (pdm != null) {
                    final PodStates ps = new PodStates();
                    final Collection<PodState> pse = new ArrayList<>();
                    ps.entries = pse;
                    ps.podId = pd.podId;
                    for (int i = 0; i < 4; i++) {
                        final PodState p = new PodState();
                        p.value = Short.toUnsignedInt(pdm.getValue(i));
                        p.measureType = pdm.getMeasureType(i);
                        pse.add(p);
                    }
                    pods.add(ps);
                    pd.dirty = false;
                }
            }

            final String json = gson.toJson(sc);

            if (!this.stateCacheFile.getParentFile().exists())
                this.stateCacheFile.getParentFile().mkdirs();

            final FileWriter fw = new FileWriter(this.stateCacheFile);
            fw.write(json);
            fw.close();
        } catch (final Exception t) {
            logger.warn("Persistance of state file {} failed: {}", this.stateCacheFile, t.getMessage(), t);
        }
    }
}
