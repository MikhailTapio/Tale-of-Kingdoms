package com.convallyria.taleofkingdoms.common.world;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.entity.EntityTypes;
import com.convallyria.taleofkingdoms.common.entity.generic.LoneVillagerEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.GuildMasterEntity;
import com.convallyria.taleofkingdoms.common.generator.processor.GatewayStructureProcessor;
import com.convallyria.taleofkingdoms.common.kingdom.PlayerKingdom;
import com.convallyria.taleofkingdoms.common.schematic.Schematic;
import com.convallyria.taleofkingdoms.common.schematic.SchematicOptions;
import com.convallyria.taleofkingdoms.common.translation.Translations;
import com.convallyria.taleofkingdoms.common.utils.EntityUtils;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.google.gson.Gson;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.structure.processor.JigsawReplacementStructureProcessor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ConquestInstance {

    public static final Codec<ConquestInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.STRING.fieldOf("name").forGetter(ConquestInstance::getName),
                    Codec.BOOL.fieldOf("has_loaded").forGetter(ConquestInstance::isLoaded),
                    BlockPos.CODEC.fieldOf("start").forGetter(ConquestInstance::getStart),
                    BlockPos.CODEC.fieldOf("end").forGetter(ConquestInstance::getEnd),
                    BlockPos.CODEC.fieldOf("origin").forGetter(ConquestInstance::getOrigin),
                    Codec.BOOL.fieldOf("under_attack").forGetter(ConquestInstance::isUnderAttack),
                    BlockPos.CODEC.listOf().fieldOf("reficule_attack_locations").forGetter(ConquestInstance::getReficuleAttackLocations),
                    Uuids.CODEC.listOf().fieldOf("reficule_attackers").forGetter(ConquestInstance::getReficuleAttackers),
                    Uuids.CODEC.listOf().optionalFieldOf("lone_villagers_with_rooms").forGetter(ci -> Optional.of(ci.getLoneVillagersWithRooms())),
                    Codec.unboundedMap(Uuids.CODEC, GuildPlayer.CODEC).fieldOf("guild_players").forGetter(ConquestInstance::getGuildPlayers)
            ).apply(instance, (name, hasLoaded, start, end, origin, underAttack, attackLocations, attackers, lVWR, guildPlayers) -> {
                ConquestInstance conquestInstance = new ConquestInstance(name, start, end, origin);
                conquestInstance.uploadData(start, end, hasLoaded, underAttack, attackLocations, attackers, lVWR.orElse(new ArrayList<>()), guildPlayers);
                return conquestInstance;
            }
    ));

    public void uploadData(ConquestInstance newData) {
        this.uploadData(newData.start, newData.end, newData.hasLoaded, newData.underAttack, newData.reficuleAttackLocations, newData.reficuleAttackers, newData.loneVillagersWithRooms, newData.guildPlayers);
    }

    public void uploadData(BlockPos start, BlockPos end, boolean hasLoaded, boolean underAttack, List<BlockPos> attackLocations, List<UUID> attackers, List<UUID> loneVillagersWithRooms, Map<UUID, GuildPlayer> guildPlayers) {
        this.setStart(start);
        this.setEnd(end);
        this.setLoaded(hasLoaded);
        this.setUnderAttack(underAttack);
        this.getReficuleAttackLocations().addAll(attackLocations);
        this.getReficuleAttackers().addAll(attackers);
        this.getLoneVillagersWithRooms().addAll(loneVillagersWithRooms);
        this.getGuildPlayers().putAll(guildPlayers);
    }

    private final String name;
    @Deprecated(forRemoval = true)
    private boolean hasLoaded;
    private BlockPos start;
    private BlockPos end;
    private final BlockPos origin;
    private boolean underAttack;
    private final List<BlockPos> reficuleAttackLocations;
    private final List<UUID> reficuleAttackers;
    private List<UUID> loneVillagersWithRooms;

    private final Map<UUID, GuildPlayer> guildPlayers;

    private transient boolean didUpgrade;

    public ConquestInstance(String name, BlockPos start, BlockPos end, BlockPos origin) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.origin = origin;
        this.loneVillagersWithRooms = new ArrayList<>();
        this.reficuleAttackLocations = new ArrayList<>();
        this.reficuleAttackers = new ArrayList<>();
        this.guildPlayers = new ConcurrentHashMap<>();
    }

    public boolean didUpgrade() {
        return didUpgrade;
    }

    public void setDidUpgrade(boolean didUpgrade) {
        this.didUpgrade = didUpgrade;
    }

    public String getName() {
        return name;
    }

    public boolean isLoaded() {
        return hasLoaded;
    }

    public void setLoaded(boolean loaded) {
        this.hasLoaded = loaded;
    }

    public BlockPos getStart() {
        return start;
    }

    public void setStart(BlockPos start) {
        this.start = start;
    }

    public BlockPos getEnd() {
        return end;
    }

    public void setEnd(BlockPos end) {
        this.end = end;
    }

    public BlockPos getOrigin() {
        return origin;
    }

    public Vec3d getCentre() {
        return Box.enclosing(start, end).getCenter();
    }

    public boolean canAttack(PlayerEntity player) {
        if (player.getWorld().getRegistryKey() != World.OVERWORLD) return false;
        final GuildPlayer guildPlayer = guildPlayers.get(player.getUuid());
        if (guildPlayer == null) return false;
        return guildPlayer.getWorthiness() >= (1500.0F / 2) && !isUnderAttack() && !guildPlayer.hasRebuiltGuild();
    }
    
    /**
     * Returns true if and only if the guild is not currently under attack and the worthiness of the player is greater than 750
     * @param uuid player uuid to check, nullable
     * @return If the guild has been attacked
     */
    public boolean hasAttacked(UUID uuid) {
        final GuildPlayer guildPlayer = guildPlayers.get(uuid);
        if (guildPlayer == null) return false;
        return !isUnderAttack() && guildPlayer.getWorthiness() > 750 && guildPlayer.hasRebuiltGuild();
    }

    public void attack(PlayerEntity player, ServerWorldAccess world) {
        if (canAttack(player)) {
            TaleOfKingdoms.LOGGER.info("Initiating guild attack for player {}", player.getName());
            EntityUtils.spawnEntity(EntityTypes.GUILDMASTER_DEFENDER, world, player.getBlockPos());
            this.underAttack = true;
            Translations.GUILDMASTER_HELP.send(player);

            Identifier gateway = Identifier.of(TaleOfKingdoms.MODID, "gateway/gateway");
            world.toServerWorld().getStructureTemplateManager().getTemplate(gateway).ifPresent(structure -> {
                for (BlockPos reficuleAttackLocation : reficuleAttackLocations) {
                    StructurePlacementData structurePlacementData = new StructurePlacementData();
                    structurePlacementData.addProcessor(GatewayStructureProcessor.INSTANCE);
                    structurePlacementData.addProcessor(JigsawReplacementStructureProcessor.INSTANCE);
                    structurePlacementData.addProcessor(BlockIgnoreStructureProcessor.IGNORE_AIR);
                    BlockPos newPos = reficuleAttackLocation.subtract(new Vec3i(6, 1, 6));
                    structure.place(world, newPos, newPos, structurePlacementData, Random.create(), Block.NOTIFY_ALL);
                }
            });
        }
    }

    /**
     * @return If the guild is currently under attack
     */
    public boolean isUnderAttack() {
        return underAttack;
    }

    public void setUnderAttack(boolean underAttack) {
        this.underAttack = underAttack;
    }

    public List<UUID> getLoneVillagersWithRooms() {
        if (loneVillagersWithRooms == null) this.loneVillagersWithRooms = new ArrayList<>();
        return loneVillagersWithRooms;
    }

    public void addLoneVillagerWithRoom(LoneVillagerEntity entity) {
        if (loneVillagersWithRooms == null) this.loneVillagersWithRooms = new ArrayList<>();
        loneVillagersWithRooms.add(entity.getUuid());
    }

    public List<BlockPos> getReficuleAttackLocations() {
        return reficuleAttackLocations;
    }

    public List<UUID> getReficuleAttackers() {
        return reficuleAttackers;
    }

    public Map<UUID, GuildPlayer> getGuildPlayers() {
        return guildPlayers;
    }

    public GuildPlayer getPlayer(PlayerEntity player) {
        return guildPlayers.get(player.getUuid());
    }

    public GuildPlayer getPlayer(UUID uuid) {
        return guildPlayers.get(uuid);
    }

    public boolean hasPlayer(UUID playerUuid) {
        return guildPlayers.containsKey(playerUuid);
    }

    public void reset(@NotNull PlayerEntity player) {
        guildPlayers.put(player.getUuid(), new GuildPlayer());
    }

    public void addCoins(UUID uuid, int coins) {
        final GuildPlayer player = getPlayer(uuid);
        if (player == null) return;
        player.setCoins(player.getCoins() + coins);
    }

    public Optional<GuildMasterEntity> getGuildMaster(World world) {
        if (start == null || end == null) return Optional.empty();
        Box box = Box.enclosing(getStart(), getEnd());
        return world.getEntitiesByType(EntityTypes.GUILDMASTER, box, guildMaster -> !guildMaster.isFireImmune()).stream().findFirst();
    }

    public <T extends Entity> Optional<T> getGuildEntity(World world, EntityType<T> type) {
        if (start == null || end == null) return Optional.empty();
        Box box = Box.enclosing(getStart(), getEnd());
        return world.getEntitiesByType(type, box, entity -> true).stream().findFirst();
    }

    private List<BlockPos> validRest;

    /**
     * Gets valid sleep area locations. This gets the sign, not the bed head.
     * @param player the player
     * @return list of signs where sleeping is allowed
     */
    @NotNull
    public List<BlockPos> getSleepLocations(PlayerEntity player) {
        if (validRest == null) validRest = new ArrayList<>();
        if (validRest.isEmpty()) { // Find a valid resting place. This will only run if validRest is empty, which is also saved to file.
            int topBlockX = (Math.max(start.getX(), end.getX()));
            int bottomBlockX = (Math.min(start.getX(), end.getX()));

            int topBlockY = (Math.max(start.getY(), end.getY()));
            int bottomBlockY = (Math.min(start.getY(), end.getY()));

            int topBlockZ = (Math.max(start.getZ(), end.getZ()));
            int bottomBlockZ = (Math.min(start.getZ(), end.getZ()));

            for (int x = bottomBlockX; x <= topBlockX; x++) {
                for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                    for (int y = bottomBlockY; y <= topBlockY; y++) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        BlockEntity tileEntity = player.getEntityWorld().getChunk(blockPos).getBlockEntity(blockPos);
                        if (tileEntity instanceof BedBlockEntity) {
                            validRest.add(blockPos);
                        }
                    }
                }
            }
        }

        return validRest;
    }

    public List<BlockPos> getValidRest() {
        return validRest;
    }

    /**
     * Checks if an entity is in the guild.
     * @param entity the entity
     * @return true if player is in guild, false if not
     */
    public boolean isInGuild(Entity entity) {
        return isInGuild(entity.getBlockPos());
    }

    /**
     * Checks if a location is in the guild.
     * @param pos the {@link BlockPos}
     * @return true if position is in guild, false if not
     */
    public boolean isInGuild(BlockPos pos) {
        if (start == null || end == null) return false; // Probably still pasting.
        BlockBox blockBox = new BlockBox(end.getX(), end.getY(), end.getZ(), start.getX(), start.getY(), start.getZ());
        return blockBox.contains(pos);
    }

    public boolean isInKingdom(Entity player) {
        if (!hasPlayer(player.getUuid())) return false;
        final PlayerKingdom kingdom = guildPlayers.get(player.getUuid()).getKingdom();
        if (kingdom == null) return false;
        BlockPos start = kingdom.getStart();
        BlockPos end = kingdom.getEnd();
        BlockBox blockBox = new BlockBox(end.getX(), end.getY(), end.getZ(), start.getX(), start.getY(), start.getZ());
        return blockBox.contains(player.getBlockPos());
    }

    /**
     * Searches the player's current location (guild or kingdom) for an entity
     * @param world
     * @param type
     * @return
     * @param <T>
     */
    public <T extends Entity> Optional<T> search(PlayerEntity player, World world, EntityType<T> type) {
        if (start == null || end == null) return Optional.empty();
        if (isInKingdom(player)) {
            if (!hasPlayer(player.getUuid())) return Optional.empty();
            final PlayerKingdom kingdom = guildPlayers.get(player.getUuid()).getKingdom();
            if (kingdom == null) return Optional.empty();
            return kingdom.getKingdomEntity(world, type);
        } else if (isInGuild(player)) {
            return getGuildEntity(world, type);
        }
        return Optional.empty();
    }

    public CompletableFuture<BlockBox> rebuild(ServerPlayerEntity serverPlayerEntity, TaleOfKingdomsAPI api, SchematicOptions... options) {
        return api.getSchematicHandler().pasteSchematic(Schematic.GUILD_CASTLE, serverPlayerEntity, getOrigin().subtract(new Vec3i(0, 21, 0)), options);
    }

    public static final String FILE_TYPE = ".cqworld";

    public void save(String worldName) {
        final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        File file = new File(api.getDataFolder() + "worlds" + File.separator + worldName + FILE_TYPE);
        try (Writer writer = new FileWriter(file)) {
            Gson gson = api.getMod().getGson();
            gson.toJson(this, writer);
            TaleOfKingdoms.LOGGER.info("Saved data");
        } catch (IOException e) {
            TaleOfKingdoms.LOGGER.error("Error saving data: ", e);
            e.printStackTrace();
        }
    }
}