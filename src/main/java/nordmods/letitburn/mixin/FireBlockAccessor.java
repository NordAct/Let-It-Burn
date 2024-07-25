package nordmods.letitburn.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FireBlock.class)
public interface FireBlockAccessor {
    @Accessor
    Object2IntMap<Block> getBurnChances();

    @Accessor
    Object2IntMap<Block> getSpreadChances();
}
