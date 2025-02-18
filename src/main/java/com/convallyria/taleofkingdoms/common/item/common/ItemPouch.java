package com.convallyria.taleofkingdoms.common.item.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.UUID;

public class ItemPouch extends Item {

    private final int maxCoins = 1000;

    public ItemPouch(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        final UUID uuid = user.getUuid();
        if (true || world.isClient()) return TypedActionResult.fail(user.getStackInHand(hand));
//        ConquestInstance instance = TaleOfKingdoms.getAPI().getConquestInstanceStorage().mostRecentInstance().get();
//        final GuildPlayer guildPlayer = instance.getPlayer(user);
        ItemStack itemStack = user.getStackInHand(hand);
//        if (itemStack.hasNbt()) {
//            NbtCompound compoundTag = itemStack.getNbt();
//            if (compoundTag.contains("coins")) {
//                int coins = compoundTag.getInt("coins");
//                if (coins == maxCoins) {
//                    guildPlayer.setCoins(guildPlayer.getCoins() + coins);
//                    compoundTag.remove("coins");
//                    user.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.MASTER, 1f, 1f);
//                    itemStack.setNbt(compoundTag);
//                    return TypedActionResult.pass(itemStack);
//                }
//            }
//        }
//
//        if (guildPlayer.getCoins() >= 100) {
//            if (!itemStack.hasNbt()) {
//                NbtCompound compoundTag = new NbtCompound();
//                compoundTag.putInt("coins", 100);
//                guildPlayer.setCoins(guildPlayer.getCoins() - 100);
//                itemStack.setNbt(compoundTag);
//                user.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.MASTER, 1f, 1f);
//            } else {
//                NbtCompound compoundTag = itemStack.getNbt();
//                if (!compoundTag.contains("coins")) {
//                    compoundTag.putInt("coins", 100);
//                    guildPlayer.setCoins(guildPlayer.getCoins() - 100);
//                    user.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.MASTER, 1f, 1f);
//                } else {
//                    int coins = compoundTag.getInt("coins");
//                    if (coins <= (maxCoins - 100)) {
//                        compoundTag.putInt("coins", coins + 100);
//                        guildPlayer.setCoins(guildPlayer.getCoins() - 100);
//                        user.playSound(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.MASTER, 1f, 1f);
//                    }
//                }
//                itemStack.setNbt(compoundTag);
//            }
//        }
        return TypedActionResult.pass(itemStack);
    }

//    @Override
//    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
//        if (stack.hasNbt()) {
//            NbtCompound compoundTag = stack.getNbt();
//            if (compoundTag.contains("coins")) {
//                int coins = compoundTag.getInt("coins");
//                tooltip.add(Text.literal("Coins: " + coins).formatted(Formatting.GOLD, Formatting.ITALIC));
//            }
//        }
//    }
}
