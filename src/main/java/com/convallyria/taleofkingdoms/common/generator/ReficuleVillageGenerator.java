package com.convallyria.taleofkingdoms.common.generator;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.entity.EntityTypes;
import com.convallyria.taleofkingdoms.common.generator.structure.TOKStructures;
import com.convallyria.taleofkingdoms.common.utils.EntityUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiecesHolder;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;

public class ReficuleVillageGenerator {

    private static final Identifier ONE = Identifier.of(TaleOfKingdoms.MODID, "reficule_village/reficule_village_one");
    private static final Identifier THREE = Identifier.of(TaleOfKingdoms.MODID, "reficule_village/reficule_village_three");
    private static final Identifier FOUR = Identifier.of(TaleOfKingdoms.MODID, "reficule_village/reficule_village_four");
    private static final Identifier MIDDLE = Identifier.of(TaleOfKingdoms.MODID, "reficule_village/reficule_village_middle");
    private static final Identifier MIDDLE_TWO = Identifier.of(TaleOfKingdoms.MODID, "reficule_village/reficule_village_middle_two");
    private static final Identifier TOWER = Identifier.of(TaleOfKingdoms.MODID, "reficule_village/reficule_village_tower");

    public static void addPieces(StructureTemplateManager manager, BlockPos pos, BlockRotation blockRotation, StructurePiecesHolder structurePiecesHolder, Random random) {
        ReficuleVillagePiece onePiece = new ReficuleVillagePiece(manager, ONE, pos.subtract(new Vec3i(0, 6, 0)), BlockRotation.NONE, 0);
        structurePiecesHolder.addPiece(onePiece);

        BlockPos middlePos = pos.add(new Vec3i(48, 0, 0));
        ReficuleVillagePiece middlePiece = new ReficuleVillagePiece(manager, MIDDLE, middlePos, BlockRotation.NONE, 0);
        structurePiecesHolder.addPiece(middlePiece);

        BlockPos threePos = middlePos.add(new Vec3i(32, 0, 0));
        ReficuleVillagePiece threePiece = new ReficuleVillagePiece(manager, THREE, threePos, BlockRotation.NONE, 0);
        structurePiecesHolder.addPiece(threePiece);

        BlockPos middleTwoPos = middlePos.subtract(new Vec3i(0, 0, 36));
        ReficuleVillagePiece middleTwoPiece = new ReficuleVillagePiece(manager, MIDDLE_TWO, middleTwoPos, BlockRotation.NONE, 0);
        structurePiecesHolder.addPiece(middleTwoPiece);

        BlockPos fourPos = middleTwoPos.add(new Vec3i(0, 0, 13)).add(new Vec3i(32, 0, 0));
        ReficuleVillagePiece fourPiece = new ReficuleVillagePiece(manager, FOUR, fourPos, BlockRotation.NONE, 0);
        structurePiecesHolder.addPiece(fourPiece);

        BlockPos towerPos = pos.subtract(new Vec3i(0, 0, 32));
        ReficuleVillagePiece towerPiece = new ReficuleVillagePiece(manager, TOWER, towerPos, BlockRotation.NONE, 0);
        structurePiecesHolder.addPiece(towerPiece);
    }

    public static class ReficuleVillagePiece extends SimpleStructurePiece {

        public ReficuleVillagePiece(StructureTemplateManager structureManager, Identifier identifier, BlockPos blockPos, BlockRotation blockRotation, int i) {
            super(TOKStructures.REFICULE_VILLAGE, 0, structureManager, identifier, identifier.toString(), createPlacementData(blockRotation), blockPos);
        }

        public ReficuleVillagePiece(StructureTemplateManager structureManager, NbtCompound nbtCompound) {
            super(TOKStructures.REFICULE_VILLAGE, nbtCompound, structureManager, (identifier) -> createPlacementData(null));
        }

        public ReficuleVillagePiece(StructureContext structureContext, NbtCompound nbtCompound) {
            super(TOKStructures.REFICULE_VILLAGE, nbtCompound, structureContext.structureTemplateManager(), (identifier) -> createPlacementData(null));
        }

        private static StructurePlacementData createPlacementData(@Nullable BlockRotation rotation) {
            return (new StructurePlacementData())
                    .setRotation(rotation != null ? rotation : BlockRotation.random(Random.create()))
                    .setMirror(BlockMirror.NONE)
                    .addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS);
        }

        @Override
        protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, net.minecraft.util.math.random.Random random, BlockBox boundingBox) {
            double percent = Math.random() * 100;
            if (metadata.equals("Survivor")) {
                if (percent > 20) {
                    EntityUtils.spawnEntity(EntityTypes.LONEVILLAGER, world, pos);
                }
                return;
            }

            if (percent > 60) {
                switch (metadata) {
                    case "ReficuleSoldier" -> EntityUtils.spawnEntity(EntityTypes.REFICULE_SOLDIER, world, pos);
                    case "ReficuleArcher" -> EntityUtils.spawnEntity(EntityTypes.REFICULE_GUARDIAN, world, pos);
                    case "ReficuleMage" -> EntityUtils.spawnEntity(EntityTypes.REFICULE_MAGE, world, pos);
                }
            }
        }
    }
}
