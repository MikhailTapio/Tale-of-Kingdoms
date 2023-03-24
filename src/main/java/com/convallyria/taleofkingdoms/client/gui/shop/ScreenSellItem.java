package com.convallyria.taleofkingdoms.client.gui.shop;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.entity.EntityTypes;
import com.convallyria.taleofkingdoms.common.entity.ShopEntity;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class ScreenSellItem extends HandledScreen<ScreenHandler> {
    //A path to the gui texture. In this example we use the texture from the dispenser
    private static final Identifier TEXTURE = new Identifier(TaleOfKingdoms.MODID, "textures/gui/guisell.png");

    private final PlayerInventory playerInventory;

    public ScreenSellItem(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.playerInventory = inventory;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        this.textRenderer.draw(matrices, Text.literal("Total Money:"), (float)this.playerInventoryTitleX + 20, (float)this.playerInventoryTitleY - 50, 4210752);
        final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        Optional<ConquestInstance> instance = api.getConquestInstanceStorage().mostRecentInstance();
        int x = this.playerInventoryTitleX + 25;
        int y = this.playerInventoryTitleY - 40;
        if (instance.isPresent()) {
            final GuildPlayer guildPlayer = instance.get().getPlayer(playerInventory.player);
            int coins = guildPlayer.getCoins();
            this.textRenderer.draw(matrices, Text.literal(coins + " Gold Coins"), x, y, 4210752);
        }
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    @Override
    public void close() {
        final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        api.getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
            World world = playerInventory.player.world;
            for (EntityType<? extends ShopEntity> shopEntity : EntityTypes.SHOP_ENTITIES) {
                instance.search(playerInventory.player, world, shopEntity).ifPresent(entity -> deleteBlock(api, entity));
            }
        });
        super.close();
    }

    protected void deleteBlock(TaleOfKingdomsAPI api, ShopEntity entity) {
        if (MinecraftClient.getInstance().getServer() == null) {
            api.getClientPacketHandler(Packets.TOGGLE_SELL_GUI)
                    .handleOutgoingPacket(playerInventory.player, true, entity.getGUIType());
            return;
        }

        api.getScheduler().queue(server -> {
            BlockPos pos = entity.getBlockPos().add(0, 2, 0);
            server.getOverworld().setBlockState(pos, Blocks.AIR.getDefaultState());
        }, 1);
    }
}

