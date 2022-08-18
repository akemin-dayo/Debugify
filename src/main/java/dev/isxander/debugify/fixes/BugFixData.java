package dev.isxander.debugify.fixes;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record BugFixData(String bugId, FixCategory category, BugFix.Env env, boolean enabledByDefault, List<String> conflicts) implements Comparable<BugFixData> {
    public List<String> getActiveConflicts() {
        List<String> activeConflicts = new ArrayList<>();
        for (String conflict : conflicts)
            if (FabricLoader.getInstance().isModLoaded(conflict))
                activeConflicts.add(conflict);
        return activeConflicts;
    }

    @Override
    public int compareTo(@NotNull BugFixData o) {
        return bugId.compareTo(o.bugId);
    }
}
