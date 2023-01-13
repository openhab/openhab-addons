package org.openhab.binding.freeboxos.internal.api.system;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ModelInfo;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SystemModelInfo {
    private ModelInfo name = ModelInfo.UNKNOWN;
    private @Nullable String prettyName;
    private boolean hasExpansions;
    private boolean hasLanSfp;
    private boolean hasDect;
    private boolean hasHomeAutomation;
    private boolean hasFemtocellExp;
    private boolean hasFixedFemtocell;
    private boolean hasVm;

    public ModelInfo getName() {
        return name;
    }

    public String getPrettyName() {
        return Objects.requireNonNull(prettyName);
    }

    public boolean hasExpansions() {
        return hasExpansions;
    }

    public boolean hasLanSfp() {
        return hasLanSfp;
    }

    public boolean hasDect() {
        return hasDect;
    }

    public boolean hasHomeAutomation() {
        return hasHomeAutomation;
    }

    public boolean hasFemtocellExp() {
        return hasFemtocellExp;
    }

    public boolean hasFixedFemtocell() {
        return hasFixedFemtocell;
    }

    public boolean hasVm() {
        return hasVm;
    }
}
