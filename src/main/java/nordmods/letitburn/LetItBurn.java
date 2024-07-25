package nordmods.letitburn;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class LetItBurn implements ModInitializer {
    private static LetItBurnConfig config;
    private static final Object2IntMap<Block> burnChances = new Object2IntOpenHashMap<>();
    private static final Object2IntMap<Block> spreadChances = new Object2IntOpenHashMap<>();

    public static LetItBurnConfig getConfig() {
        if (config == null) config = AutoConfig.getConfigHolder(LetItBurnConfig.class).getConfig();
        return config;
    }

    public static void saveConfig() {
        removeDuplicates();
        AutoConfig.getConfigHolder(LetItBurnConfig.class).save();
        config = null;
    }
    public static void loadConfig() {
        AutoConfig.getConfigHolder(LetItBurnConfig.class).load();
        removeDuplicates();
        config = null;
        populateMaps();
    }

    private static void populateMaps() {
        burnChances.clear();
        spreadChances.clear();

        for (LetItBurnConfig.FlammabilityInfo info : getConfig().flammableBlocks) {
            Block block = Registries.BLOCK.get(Identifier.of(info.id()));
            burnChances.put(block, info.burnChance());
            spreadChances.put(block, info.spreadChance());
        }
    }

    private static void removeDuplicates() {
        Map<String, int[]> map = new HashMap<>();
        for (LetItBurnConfig.FlammabilityInfo info : getConfig().flammableBlocks) {
            map.put(info.id(), new int[] {info.burnChance(), info.spreadChance()});
        }
        getConfig().flammableBlocks.clear();
        map.forEach((id, chances) -> getConfig().flammableBlocks.add(new LetItBurnConfig.FlammabilityInfo(id, chances[0], chances[1])));
    }

    public static int getBurnChance(Block block) {
        return burnChances.getInt(block);
    }

    public static int getSpreadChance(Block block) {
        return spreadChances.getInt(block);
    }

    @Override
    public void onInitialize() {
        LetItBurnCommands.init();
        AutoConfig.register(LetItBurnConfig.class, GsonConfigSerializer::new);
        populateMaps();
    }
}
