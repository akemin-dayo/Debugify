package dev.isxander.debugify.utils;

import dev.isxander.debugify.Debugify;
import dev.isxander.debugify.fixes.BugFix;
import dev.isxander.debugify.fixes.FixCategory;
import dev.isxander.debugify.fixes.BugFixData;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        MixinExtrasBootstrap.init();
        Debugify.onPreInitialize();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }


    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        Optional<BugFixData> bugFixOptional = getBugFixForMixin(mixinClassName);

        if (bugFixOptional.isEmpty())
            return true;

        BugFixData bugFix = bugFixOptional.get();
        Debugify.config.registerBugFix(bugFix);

        List<String> conflicts = bugFix.getActiveConflicts();
        if (!conflicts.isEmpty()) {
            Debugify.config.getBugFixes().replace(bugFix, false);
            Debugify.logger.warn("Force disabled " + bugFix.bugId() + " because it's conflicting with: " + String.join(", ", conflicts));
        }

        return Debugify.config.isBugFixEnabled(bugFix) && bugFix.getActiveConflicts().isEmpty();
    }

    private Optional<BugFixData> getBugFixForMixin(String mixinClassName) {
        AnnotationNode annotationNode;

        try {
            ClassNode classNode = MixinService.getService().getBytecodeProvider().getClassNode(mixinClassName);
            annotationNode = Annotations.getVisible(classNode, BugFix.class);
        } catch (ClassNotFoundException | IOException e) {
            annotationNode = null;
        }

        if (annotationNode == null)
            return Optional.empty();

        String id = Annotations.getValue(annotationNode, "id");
        FixCategory category = getAnnotationEnumValue(annotationNode, "category", FixCategory.class);
        BugFix.Env env = getAnnotationEnumValue(annotationNode, "env", BugFix.Env.class);
        boolean enabledByDefault = Annotations.getValue(annotationNode, "enabled", Boolean.valueOf(true));
        List<String> conflicts = Annotations.getValue(annotationNode, "fabricConflicts", true);

        return Optional.of(new BugFixData(id, category, env, enabledByDefault, conflicts));
    }

    private static <T extends Enum<T>> T getAnnotationEnumValue(AnnotationNode annotation, String key, Class<T> enumClass) {
        String[] value = Annotations.getValue(annotation, key);
        return Enum.valueOf(enumClass, value[1]);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
