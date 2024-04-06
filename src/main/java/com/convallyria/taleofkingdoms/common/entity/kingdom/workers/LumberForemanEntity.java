package com.convallyria.taleofkingdoms.common.entity.kingdom.workers;

import com.convallyria.taleofkingdoms.common.entity.kingdom.ForemanEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;

public class LumberForemanEntity extends ForemanEntity {

    public LumberForemanEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }
}
