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
package org.openhab.binding.verisure.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.verisure.internal.dto.VerisureBaseThingDTO.Installation;

import com.google.gson.annotations.SerializedName;

/**
 * The installation(s) of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureInstallationsDTO {

    private Data data = new Data();

    public Data getData() {
        return data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + data.hashCode();
        return result;
    }

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        VerisureInstallationsDTO other = (VerisureInstallationsDTO) obj;
        if (!data.equals(other.data)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "VerisureInstallationsDTO [data=" + data + "]";
    }

    public static class Data {
        private Installation installation = new Installation();
        private Account account = new Account();

        public Account getAccount() {
            return account;
        }

        public Installation getInstallation() {
            return installation;
        }

        public void setInstallation(Installation installation) {
            this.installation = installation;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + account.hashCode();
            result = prime * result + installation.hashCode();
            return result;
        }

        @SuppressWarnings("PMD.SimplifyBooleanReturns")
        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Data other = (Data) obj;
            if (!account.equals(other.account)) {
                return false;
            }
            if (!installation.equals(other.installation)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Data [installation=" + installation + ", account=" + account + "]";
        }
    }

    public static class Account {

        @SerializedName("__typename")
        private @Nullable String typename;
        private List<Owainstallation> owainstallations = new ArrayList<>();

        public @Nullable String getTypename() {
            return typename;
        }

        public List<Owainstallation> getOwainstallations() {
            return owainstallations;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + owainstallations.hashCode();
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Account other = (Account) obj;
            if (!owainstallations.equals(other.owainstallations)) {
                return false;
            }
            String localTypeName = typename;
            if (localTypeName == null) {
                if (other.typename != null) {
                    return false;
                }
            } else if (!localTypeName.equals(other.typename)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Account [typename=" + typename + ", owainstallations=" + owainstallations + "]";
        }
    }

    public static class Owainstallation {

        @SerializedName("__typename")
        private @Nullable String typename;
        private @Nullable String alias;
        private @Nullable String dealerId;
        private @Nullable String giid;
        private @Nullable String subsidiary;
        private @Nullable String type;

        public @Nullable String getTypename() {
            return typename;
        }

        public @Nullable String getAlias() {
            return alias;
        }

        public @Nullable String getDealerId() {
            return dealerId;
        }

        public @Nullable String getGiid() {
            return giid;
        }

        public @Nullable String getSubsidiary() {
            return subsidiary;
        }

        public @Nullable String getType() {
            return type;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            String localAlias = alias;
            result = prime * result + ((localAlias == null) ? 0 : localAlias.hashCode());
            String localDealerId = dealerId;
            result = prime * result + ((localDealerId == null) ? 0 : localDealerId.hashCode());
            String localGiid = giid;
            result = prime * result + ((localGiid == null) ? 0 : localGiid.hashCode());
            String localSubsidiary = subsidiary;
            result = prime * result + ((localSubsidiary == null) ? 0 : localSubsidiary.hashCode());
            String localType = type;
            result = prime * result + ((localType == null) ? 0 : localType.hashCode());
            String localTypeName = typename;
            result = prime * result + ((localTypeName == null) ? 0 : localTypeName.hashCode());
            return result;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Owainstallation other = (Owainstallation) obj;
            String localAlias = alias;
            if (localAlias == null) {
                if (other.alias != null) {
                    return false;
                }
            } else if (!localAlias.equals(other.alias)) {
                return false;
            }
            String localDealerId = dealerId;
            if (localDealerId == null) {
                if (other.dealerId != null) {
                    return false;
                }
            } else if (!localDealerId.equals(other.dealerId)) {
                return false;
            }
            String localGiid = giid;
            if (localGiid == null) {
                if (other.giid != null) {
                    return false;
                }
            } else if (!localGiid.equals(other.giid)) {
                return false;
            }
            String localSubsidiary = subsidiary;
            if (localSubsidiary == null) {
                if (other.subsidiary != null) {
                    return false;
                }
            } else if (!localSubsidiary.equals(other.subsidiary)) {
                return false;
            }
            String localType = type;
            if (localType == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!localType.equals(other.type)) {
                return false;
            }
            String localTypeName = typename;
            if (localTypeName == null) {
                if (other.typename != null) {
                    return false;
                }
            } else if (!localTypeName.equals(other.typename)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Owainstallation [typename=" + typename + ", alias=" + alias + ", dealerId=" + dealerId + ", giid="
                    + giid + ", subsidiary=" + subsidiary + ", type=" + type + "]";
        }
    }
}
