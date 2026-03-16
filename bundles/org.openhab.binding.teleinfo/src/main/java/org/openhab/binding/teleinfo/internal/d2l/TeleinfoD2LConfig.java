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
package org.openhab.binding.teleinfo.internal.d2l;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoElectricityMeterConfiguration;

/**
 * The {@link TeleinfoElectricityMeterConfiguration} class stores electricity meter thing configuration
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class TeleinfoD2LConfig {
    private Map<Long, D2LConfig> d2lConfig = new HashMap<Long, D2LConfig>();
    private int listenningPort = 7845;
    private String encryptionKeys = "";

    public TeleinfoD2LConfig() {
    }

    public void initKey() {
        String[] encryptionKeyTab = encryptionKeys.split(";");
        for (String encryptionKey : encryptionKeyTab) {
            String[] encriptionParamTab = encryptionKey.split(":");
            String idd2lSt = encriptionParamTab[0];
            String appKey = encriptionParamTab[1];
            String ivKey = encriptionParamTab[2];

            long idd2l = -1;
            if (!idd2lSt.isBlank()) {
                idd2l = Long.parseLong(idd2lSt);
            }

            D2LConfig config = new D2LConfig();
            config.setIdd2l(idd2l);
            config.setAppKey(appKey);
            config.setIvKey(ivKey);

            d2lConfig.put(idd2l, config);
        }
    }

    public int getListenningPort() {
        return listenningPort;
    }

    public @Nullable D2LConfig getConfig(long idd2l) {
        return d2lConfig.get(idd2l);
    }

    class D2LConfig {
        private long idd2l = -1;
        private String appKey = "";
        private String ivKey = "";

        public long getIdd2l() {
            return idd2l;
        }

        public String getAppKey() {
            return appKey;
        }

        public String getIvKey() {
            return ivKey;
        }

        public void setIdd2l(long idd2l) {
            this.idd2l = idd2l;
        }

        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }

        public void setIvKey(String ivKey) {
            this.ivKey = ivKey;
        }
    }

}
