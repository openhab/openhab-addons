/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal.data;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SurePetcareDeviceControl} class is used to serialize a JSON object to control certain parameters of a
 * device (e.g. locking mode, curfew etc.).
 *
 * @author Rene Scherer - Initial contribution
 * @author Holger Eisold - Added pet feeder status
 */
public class SurePetcareDeviceControl {

    public class Curfew {
        public Curfew(boolean enabled, String lockTime, String unlockTime) {
            this.enabled = enabled;
            this.lockTime = lockTime;
            this.unlockTime = unlockTime;
        }

        public Boolean enabled;
        public String lockTime;
        public String unlockTime;
    }

    public class Bowls {
        public class BowlSettings {
            @SerializedName("food_type")
            private Integer foodId;
            @SerializedName("target")
            private Integer targetId;
        
            public BowlSettings(Integer foodId, Integer targetId) {
                this.foodId = foodId;
                this.targetId = targetId;
            }
        
            public Integer getFoodId() {
                return foodId;
            }
        
            public void setFoodId(Integer foodId) {
                this.foodId = foodId;
            }
        
            public Integer getTargetId() {
                return targetId;
            }
        
            public void setTargetId(Integer targetId) {
                this.targetId = targetId;
            }
        }

        @SerializedName("settings")
        private List<BowlSettings> bowlSettings;
        @SerializedName("type")
        private Integer bowlId;
    
        public Bowls(List<BowlSettings> bowlSettings, Integer bowlId) {
            this.bowlSettings = bowlSettings;
            this.bowlId = bowlId;
        }
    
        public List<BowlSettings> getBowlSettings() {
            return bowlSettings;
        }
    
        public void setBowlSettings(List<BowlSettings> bowlSettings) {
            this.bowlSettings = bowlSettings;
        }
    
        public Integer getBowlId() {
            return bowlId;
        }
    
        public void setBowlId(Integer bowlId) {
            this.bowlId = bowlId;
        }
    }

    public class Lid {
        @SerializedName("close_delay")
        private Integer closeDelayId;
    
        public Lid(Integer closeDelayId) {
            this.closeDelayId = closeDelayId;
        }
    
        public Integer getCloseDelayId() {
            return closeDelayId;
        }
    
        public void setCloseDelayId(Integer closeDelayId) {
            this.closeDelayId = closeDelayId;
        }
    
    }

    @SerializedName("locking")
    private Integer lockingModeId;
    private Boolean fastPolling;
    @SerializedName("led_mode")
    private Integer ledModeId;
    @SerializedName("pairing_mode")
    private Integer pairingModeId;
    private List<Curfew> curfew;
    @SerializedName("bowls")
    private Bowls bowls;
    @SerializedName("lid")
    private Lid lid;
    @SerializedName("training_mode")
    private Integer trainingModeId;

    public Integer getLockingModeId() {
        return lockingModeId;
    }

    public void setLockingModeId(Integer lockingModeId) {
        this.lockingModeId = lockingModeId;
    }

    public Boolean isFastPolling() {
        return fastPolling;
    }

    public void setFastPolling(Boolean fastPolling) {
        this.fastPolling = fastPolling;
    }

    public Integer getLedModeId() {
        return ledModeId;
    }

    public void setLedModeId(Integer ledModeId) {
        this.ledModeId = ledModeId;
    }

    public Integer getPairingModeId() {
        return pairingModeId;
    }

    public void setPairingModeId(Integer pairingModeId) {
        this.pairingModeId = pairingModeId;
    }

    public List<Curfew> getCurfew() {
        return curfew;
    }

    public void setCurfew(List<Curfew> curfew) {
        this.curfew = curfew;
    }

    public Bowls getBowls() {
        return bowls;
    }

    public void setBowls(Bowls bowls) {
        this.bowls = bowls;
    }

    public Lid getLid() {
        return lid;
    }

    public void setLid(Lid lid) {
        this.lid = lid;
    }

    public Integer getTrainingModeId() {
        return trainingModeId;
    }

    public void setTrainingModeId(Integer trainingModeId) {
        this.trainingModeId = trainingModeId;
    }
}