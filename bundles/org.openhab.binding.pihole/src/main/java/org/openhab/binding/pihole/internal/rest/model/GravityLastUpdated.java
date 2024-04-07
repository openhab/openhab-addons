package org.openhab.binding.pihole.internal.rest.model;

import com.google.gson.annotations.SerializedName;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;

public class GravityLastUpdated {
    @SerializedName("file_exists")
    @Nullable
    private Boolean fileExists;
    @Nullable   private Long absolute;
    @Nullable    private Relative relative;

    public GravityLastUpdated() {
    }

    public GravityLastUpdated(Boolean fileExists, Long absolute, Relative relative) {
        this.fileExists = fileExists;
        this.absolute = absolute;
        this.relative = relative;
    }

    public Boolean getFileExists() {
        return fileExists;
    }

    public void setFileExists(Boolean fileExists) {
        this.fileExists = fileExists;
    }

    public Long getAbsolute() {
        return absolute;
    }

    public void setAbsolute(Long absolute) {
        this.absolute = absolute;
    }

    public Relative getRelative() {
        return relative;
    }

    public void setRelative(Relative relative) {
        this.relative = relative;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GravityLastUpdated that = (GravityLastUpdated) o;

        if (!Objects.equals(fileExists, that.fileExists)) return false;
        if (!Objects.equals(absolute, that.absolute)) return false;
        return Objects.equals(relative, that.relative);
    }

    @Override
    public int hashCode() {
        int result = fileExists != null ? fileExists.hashCode() : 0;
        result = 31 * result + (absolute != null ? absolute.hashCode() : 0);
        result = 31 * result + (relative != null ? relative.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GravityLastUpdated{" + //
                "fileExists=" + fileExists + //
                ", absolute=" + absolute + //
                ", relative=" + relative + //
                '}';
    }
}
