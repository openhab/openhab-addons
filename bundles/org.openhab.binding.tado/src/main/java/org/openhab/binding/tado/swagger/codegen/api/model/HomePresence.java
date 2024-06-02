package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class HomePresence {
    @SerializedName("homePresence")
    private PresenceState homePresence = null;

    public HomePresence homePresence(PresenceState homePresence) {
        this.homePresence = homePresence;
        return this;
    }

    public PresenceState getHomePresence() {
        return homePresence;
    }

    public void setHomePresence(PresenceState homePresence) {
        this.homePresence = homePresence;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HomePresence homePresence = (HomePresence) o;
        return Objects.equals(this.homePresence, homePresence.homePresence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(homePresence);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HomePresence {\n");

        sb.append("    homePresence: ").append(toIndentedString(homePresence)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
