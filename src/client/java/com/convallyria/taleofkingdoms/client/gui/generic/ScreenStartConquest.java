package com.convallyria.taleofkingdoms.client.gui.generic;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.client.TaleOfKingdomsClient;
import com.convallyria.taleofkingdoms.client.gui.ScreenTOK;
import com.convallyria.taleofkingdoms.common.translation.Translations;
import com.convallyria.taleofkingdoms.common.event.tok.KingdomStartCallback;
import com.convallyria.taleofkingdoms.common.schematic.Schematic;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;

public class ScreenStartConquest extends ScreenTOK {

    // Buttons
    private ButtonWidget mButtonClose;

    // Text fields
    private TextFieldWidget text;

    // Other
    private final String worldName;
    private final File toSave;
    private final PlayerEntity player;
    private boolean loading;

    public ScreenStartConquest(@NotNull String worldName, File toSave, PlayerEntity player) {
        super("menu.taleofkingdoms.startconquest.name");
        this.worldName = worldName;
        this.toSave = toSave;
        this.player = player;
    }

    @Override
    public void init() {
        super.init();
        this.children().clear();
        this.text = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, this.height / 2 - 40, 300, 20, Text.literal("Sir Punchwood"));
        this.addDrawableChild(mButtonClose = ButtonWidget.builder(Translations.START_CONQUEST.getTranslation(), button -> {
            if (loading) return;

            button.setMessage(Translations.BUILDING_CASTLE.getTranslation());
            final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
            if (api == null) {
                button.setMessage(Text.literal("No API present"));
                return;
            }

            MinecraftServer server = MinecraftClient.getInstance().getServer();
            if (server == null) {
                button.setMessage(Text.literal("No server present"));
                return;
            }
            ServerPlayerEntity serverPlayer = server.getPlayerManager().getPlayer(player.getUuid());
            if (serverPlayer == null) return;

            // Load guild castle schematic
            ConquestInstance instance = new ConquestInstance(text.getText(), null, null, serverPlayer.getBlockPos().add(0, 1, 0));
            instance.reset(player);
            api.getConquestInstanceStorage().addConquest(worldName, instance, true);

            BlockPos pastePos = serverPlayer.getBlockPos().subtract(new Vec3i(0, 20, 0));
            api.getSchematicHandler().pasteSchematic(Schematic.GUILD_CASTLE, serverPlayer, pastePos).thenAccept(oi -> api.executeOnServerEnvironment((s) -> {
                BlockPos start = new BlockPos(oi.getMaxX(), oi.getMaxY(), oi.getMaxZ());
                BlockPos end = new BlockPos(oi.getMinX(), oi.getMinY(), oi.getMinZ());
                instance.setStart(start);
                instance.setEnd(end);

                button.setMessage(Translations.SUMMONING_CITIZENS.getTranslation());

                api.executeOnMain(() -> {
                    button.setMessage(Text.literal("Reloading chunks..."));
                    MinecraftClient.getInstance().worldRenderer.reload();
                    close();
                    loading = false;
                    instance.setLoaded(true);
                    final GuildPlayer guildPlayer = instance.getPlayer(player.getUuid());
                    guildPlayer.setFarmerLastBread(-1); // Set to -1 in order to claim on first day
                    instance.save(worldName);
                });

                KingdomStartCallback.EVENT.invoker().kingdomStart(serverPlayer, instance); // Call kingdom start event
            }));
        }).dimensions(this.width / 2 - 100, this.height / 2 + 15, 200, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.taleofkingdoms.startconquest.delay").formatted(Formatting.RED), (b) -> {
            this.close();
        }).dimensions(this.width / 2 - 50, this.height / 2 + 50, 100, 20).build());

        this.text.setMaxLength(32);
        this.text.setText("Sir Punchwood");
        this.text.setFocusUnlocked(true);
        this.text.setFocused(true);
        this.text.setVisible(true);
        this.addSelectableChild(this.text);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        String text = Translations.DARKNESS.getFormatted();
        int currentHeight = this.height / 2 - 110;
        for (String toRender : text.split("\n")) {
            //todo change colour? 11111111 is nice
            context.drawCenteredTextWithShadow(this.textRenderer, toRender, this.width / 2, currentHeight, 0xFFFFFF);
            currentHeight = currentHeight + 10;
        }
        context.drawCenteredTextWithShadow(this.textRenderer, Translations.HERO.getFormatted(), this.width / 2, currentHeight + 10, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("menu.taleofkingdoms.startconquest.exit"), this.width / 2, currentHeight + 65, 0xFFFFFF);
        this.text.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    @Override
    public void close() {
        super.close();
        final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        Optional<ConquestInstance> instance = api.getConquestInstanceStorage().mostRecentInstance();
        if (instance.isEmpty()) {
            Text keyName = TaleOfKingdomsClient.START_CONQUEST_KEYBIND.getBoundKeyLocalizedText();
            player.sendMessage(Text.translatable("menu.taleofkingdoms.startconquest.closed", keyName.getString()), false);
        }
    }
}
