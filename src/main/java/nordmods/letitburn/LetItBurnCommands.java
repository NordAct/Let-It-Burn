package nordmods.letitburn;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nordmods.letitburn.mixin.FireBlockAccessor;

import java.util.ArrayList;
import java.util.List;

public class LetItBurnCommands {
    private static final LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("letitburn").requires(source -> source.hasPermissionLevel(2));

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    builder.then(CommandManager.literal("enable")
                            .then(CommandManager.argument("state", BoolArgumentType.bool())
                                    .executes(context -> toggleMod(BoolArgumentType.getBool(context,"state"), context.getSource())))

                            .executes(context -> getStatus(context.getSource())))
            );

            dispatcher.register(
                    builder.then(CommandManager.literal("reload")
                                    .executes(context -> reloadConfig(context.getSource())))
            );

            dispatcher.register(
                    builder.then(CommandManager.literal("genconfig")
                                    .executes(context -> generateConfig(context.getSource())))
            );

            dispatcher.register(
                    builder.then(CommandManager.literal("add")
                            .then(CommandManager.argument("block", BlockStateArgumentType.blockState(registryAccess))
                                    .then(CommandManager.argument("burn chance", IntegerArgumentType.integer(1, 100))
                                            .then(CommandManager.argument("spread chance", IntegerArgumentType.integer(1, 100))
                                                    .executes(context -> addBlock(
                                                            BlockStateArgumentType.getBlockState(context, "block").getBlockState().getBlock(),
                                                            IntegerArgumentType.getInteger(context, "burn chance"),
                                                            IntegerArgumentType.getInteger(context, "spread chance"),
                                                            context.getSource()))))))
            );

            dispatcher.register(
                    builder.then(CommandManager.literal("remove")
                            .then(CommandManager.argument("block", BlockStateArgumentType.blockState(registryAccess))
                                    .executes(context -> removeBlock(BlockStateArgumentType.getBlockState(context, "block").getBlockState().getBlock(), context.getSource()))))
            );
        });
    }


    private static int toggleMod(boolean bool, ServerCommandSource source) {
        int toReturn = bool != LetItBurn.getConfig().enabled ? 1 : 0;
        LetItBurn.getConfig().enabled = bool;
        source.sendFeedback(() -> Text.literal("\"Let It Burn\" enable status was set to " + bool).formatted(Formatting.GRAY), true);
        LetItBurn.saveConfig();
        return toReturn;
    }

    private static int getStatus(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("\"Let It Burn\" enable status: " + LetItBurn.getConfig().enabled).formatted(Formatting.GRAY), true);
        return 1;
    }

    private static int generateConfig(ServerCommandSource source) {
        FireBlock fireBlock = (FireBlock) Blocks.FIRE;
        List<LetItBurnConfig.FlammabilityInfo> list = new ArrayList<>();
        ((FireBlockAccessor)fireBlock).getBurnChances().forEach((block, burnChance) ->
                list.add(new LetItBurnConfig.FlammabilityInfo(
                        Registries.BLOCK.getId(block).toString(),
                        burnChance,
                        ((FireBlockAccessor)fireBlock).getSpreadChances().getInt(block)
                )));
        LetItBurn.getConfig().flammableBlocks = list;
        source.sendFeedback(() -> Text.literal("Config for \"Let It Burn\" was generated").formatted(Formatting.GRAY), true);
        LetItBurn.saveConfig();
        return 1;
    }

    private static int reloadConfig(ServerCommandSource source) {
        LetItBurn.loadConfig();
        source.sendFeedback(() -> Text.literal("Config for \"Let It Burn\" was reloaded").formatted(Formatting.GRAY), true);
        return 1;
    }

    private static int addBlock(Block block, int burnChance, int spreadChance, ServerCommandSource source) {
        LetItBurnConfig.FlammabilityInfo info = new LetItBurnConfig.FlammabilityInfo(Registries.BLOCK.getId(block).toString(), burnChance, spreadChance);
        if (LetItBurn.getConfig().flammableBlocks.contains(info)) {
            source.sendFeedback(() -> Text.literal("Duplicate entry").formatted(Formatting.RED), false);
            return 0;
        }

        LetItBurnConfig.FlammabilityInfo sameBlock = null;
        for (LetItBurnConfig.FlammabilityInfo blockInfo : LetItBurn.getConfig().flammableBlocks) {
            if (blockInfo.id().equals(info.id())) {
                sameBlock = blockInfo;
                break;
            }
        }

        if (sameBlock != null) {
            LetItBurn.getConfig().flammableBlocks.remove(sameBlock);
            source.sendFeedback(() -> Text.literal(String.format("Entry for %s was overridden successfully", info.id())).formatted(Formatting.GRAY), true);
        } else source.sendFeedback(() -> Text.literal(String.format("Entry for %s was added successfully", info.id())).formatted(Formatting.GRAY), true);

        LetItBurn.getConfig().flammableBlocks.add(info);
        LetItBurn.saveConfig();
        return 1;
    }

    private static int removeBlock(Block block, ServerCommandSource source) {
        String id = Registries.BLOCK.getId(block).toString();
        LetItBurnConfig.FlammabilityInfo sameBlock = null;
        for (LetItBurnConfig.FlammabilityInfo blockInfo : LetItBurn.getConfig().flammableBlocks) {
            if (blockInfo.id().equals(id)) {
                sameBlock = blockInfo;
                break;
            }
        }

        if (sameBlock == null) {
            source.sendFeedback(() -> Text.literal("No entry for this block was found").formatted(Formatting.RED), false);
            return 0;
        }
        LetItBurn.getConfig().flammableBlocks.remove(sameBlock);
        source.sendFeedback(() -> Text.literal(String.format("Entry for %s was removed successfully", id)).formatted(Formatting.GRAY), true);
        LetItBurn.saveConfig();
        return 1;
    }
}
