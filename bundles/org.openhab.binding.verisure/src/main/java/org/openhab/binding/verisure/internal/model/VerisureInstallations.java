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
package org.openhab.binding.verisure.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The installations of the Verisure System.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureInstallations extends VerisureBaseThing {

    private Data data = new Data();

    public Data getData() {
        return data;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("data", data).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(data).toHashCode();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Data)) {
            return false;
        }
        VerisureInstallations rhs = ((VerisureInstallations) other);
        return new EqualsBuilder().append(data, rhs.data).isEquals();
    }

    @NonNullByDefault
    public static class Data {

        private Account account = new Account();

        public Account getAccount() {
            return account;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("account", account).toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(account).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Data)) {
                return false;
            }
            Data rhs = ((Data) other);
            return new EqualsBuilder().append(account, rhs.account).isEquals();
        }

    }

    @NonNullByDefault
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
        public String toString() {
            return new ToStringBuilder(this).append("owainstallations", owainstallations).append("typename", typename)
                    .toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(typename).append(owainstallations).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Account)) {
                return false;
            }
            Account rhs = ((Account) other);
            return new EqualsBuilder().append(typename, rhs.typename).append(owainstallations, rhs.owainstallations)
                    .isEquals();
        }
    }

    @NonNullByDefault
    public static class Owainstallation {

        @SerializedName("__typename")
        private @Nullable String typename;
        private @Nullable String alias;
        private @Nullable String dealerId;
        private @Nullable String giid;
        private @Nullable Object subsidiary;
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

        public @Nullable Object getSubsidiary() {
            return subsidiary;
        }

        public @Nullable String getType() {
            return type;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("giid", giid).append("alias", alias).append("type", type)
                    .append("subsidiary", subsidiary).append("dealerId", dealerId).append("typename", typename)
                    .toString();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(dealerId).append(alias).append(typename).append(giid).append(subsidiary)
                    .append(type).toHashCode();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof Owainstallation)) {
                return false;
            }
            Owainstallation rhs = ((Owainstallation) other);
            return new EqualsBuilder().append(dealerId, rhs.dealerId).append(alias, rhs.alias)
                    .append(typename, rhs.typename).append(giid, rhs.giid).append(subsidiary, rhs.subsidiary)
                    .append(type, rhs.type).isEquals();
        }
    }
}
