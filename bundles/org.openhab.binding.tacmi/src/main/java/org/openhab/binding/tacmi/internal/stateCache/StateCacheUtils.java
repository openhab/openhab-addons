/**
 * Copyright (c) 2015-2020 Contributors to the openHAB project
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
import java.util.Collection;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tacmi.internal.podData.PodData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public StateCacheUtils(File file, Collection<@Nullable PodData> podDatas) {
        this.stateCacheFile = file;
        if (this.stateCacheFile.exists()) {
            FileReader fr = null;
            try {
                fr = new FileReader(stateCacheFile);
                @Nullable
                StateCache sc = gson.fromJson(fr, StateCache.class);
                if (sc.pods != null) {
                    for (PodStates storedPod : sc.pods) {
                        if (storedPod.entries != null) {
                            for (PodData pod : podDatas) {
                                // pod.message is only initialzied for outgoing pod's
                                if (pod != null && pod.message != null && pod.podId == storedPod.podId) {
                                    Iterator<PodState> spi = storedPod.entries.iterator();
                                    int id = 0;
                                    while (spi.hasNext() && id < 4) {
                                        @Nullable
                                        PodState ps = spi.next();
                                        pod.message.setValue(id, (short) (ps.value & 0xffff), ps.measureType);
                                        id++;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                logger.warn("Restore of state file {} failed: {}", this.stateCacheFile, t.getMessage(), t);
            } finally {
                if (fr != null)
                    try {
                        fr.close();
                    } catch (Throwable t) {
                        // ignore...
                    }
            }

        }
    }

    public void persistStates(Collection<@Nullable PodData> data) {
        try {
            boolean dirty = false;
            for (PodData pd : data) {
                if (pd != null && pd.message != null && pd.dirty)
                    dirty = true;
            }
            if (!dirty)
                return;

            // we have to persist - transfer state to json structure...
            StateCache sc = new StateCache();
            for (PodData pd : data) {
                if (pd != null && pd.message != null) {
                    PodStates ps = new PodStates();
                    ps.podId = pd.podId;
                    for (int i = 0; i < 4; i++) {
                        PodState p = new PodState();
                        p.value = Short.toUnsignedInt(pd.message.getValue(i));
                        p.measureType = pd.message.getMeasureType(i);
                        ps.entries.add(p);
                    }
                    sc.pods.add(ps);
                    pd.dirty = false;
                }
            }

            String json = gson.toJson(sc);

            if (!this.stateCacheFile.getParentFile().exists())
                this.stateCacheFile.getParentFile().mkdirs();

            FileWriter fw = new FileWriter(this.stateCacheFile);
            fw.write(json);
            fw.close();
        } catch (Throwable t) {
            logger.warn("Persistance of state file {} failed: {}", this.stateCacheFile, t.getMessage(), t);
        }
    }

}
