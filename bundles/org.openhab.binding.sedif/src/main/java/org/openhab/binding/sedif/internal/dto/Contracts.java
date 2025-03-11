package org.openhab.binding.sedif.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

public class Contracts extends Value {
    public @Nullable List<Contract> contrats = new ArrayList<Contract>();
}
