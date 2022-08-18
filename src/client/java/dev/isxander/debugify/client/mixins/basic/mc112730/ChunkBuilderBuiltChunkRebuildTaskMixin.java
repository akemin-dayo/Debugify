package dev.isxander.debugify.client.mixins.basic.mc112730;

import dev.isxander.debugify.fixes.BugFix;
import dev.isxander.debugify.fixes.FixCategory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@BugFix(id = "MC-112730", category = FixCategory.BASIC, env = BugFix.Env.CLIENT, fabricConflicts = "optifabric")
@Mixin(ChunkBuilder.BuiltChunk.RebuildTask.class)
public class ChunkBuilderBuiltChunkRebuildTaskMixin {
    /**
     * Extremely annoying to make because:
     * A. You can't append else statements to ifs
     * B. You can't use WrapWithCondition because List.add returns boolean
     *
     * Bytecode is also super funky because &lt;E extends BlockEntity&gt; but
     * the param wants an Object instead of E making way for weird unnecessary casting
     */
    @SuppressWarnings("unchecked")
    @Redirect(method = "addBlockEntity", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    private <E extends BlockEntity> boolean elseAlternative(List<BlockEntity> list, Object blockEntity) {
        E be = (E) blockEntity;
        if (!MinecraftClient.getInstance().getBlockEntityRenderDispatcher().get(be).rendersOutsideBoundingBox(be)) {
            return list.add(be);
        }
        return false;
    }
}
