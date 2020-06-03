package net.islandearth.taleofkingdoms.common.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.item.Item;

import java.util.Random;

public class ItemHelper {
	
	private static final Random random = new Random();

	/**
	 * Checks if an entity is hostile.
	 * @param entityLiving entity to check
	 * @return true if entity is hostile
	 */
	public static boolean isHostileEntity(LivingEntity entityLiving) { 
		return entityLiving instanceof MonsterEntity;
	}

	/**
	 * Drops coins for an entity if it is hostile.
	 * This uses the same randomness used in the old TOK mod.
	 * @see ItemCoin
	 * @param entityLiving entity to drop coins for
	 */
	public static void dropCoins(LivingEntity entityLiving) {
		if (isHostileEntity(entityLiving) && !entityLiving.world.isRemote) {
			int bound = random.nextInt(25);
			for (int i = 0; i < bound; i++) {
				dropItem(ItemRegistry.items.get("coin"), 1, entityLiving);
			} 
		} 
	}
	
	private static void dropItem(Item item, int meta, LivingEntity livingBase) { 
		livingBase.entityDropItem(item, meta); 
	}
}
