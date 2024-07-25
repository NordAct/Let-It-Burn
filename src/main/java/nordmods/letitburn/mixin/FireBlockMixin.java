package nordmods.letitburn.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.block.FireBlock;
import nordmods.letitburn.LetItBurn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    @WrapOperation(method = "getSpreadChance", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2IntMap;getInt(Ljava/lang/Object;)I"))
    private int replaceSpreadChance(Object2IntMap instance, Object o, Operation<Integer> original) {
        return LetItBurn.getConfig().enabled ? LetItBurn.getSpreadChance((Block) o) : original.call(instance, o);
    }

    @WrapOperation(method = "getBurnChance(Lnet/minecraft/block/BlockState;)I", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2IntMap;getInt(Ljava/lang/Object;)I"))
    private int replaceBurnChance(Object2IntMap instance, Object o, Operation<Integer> original) {
        return LetItBurn.getConfig().enabled ? LetItBurn.getBurnChance((Block) o) : original.call(instance, o);
    }
}
