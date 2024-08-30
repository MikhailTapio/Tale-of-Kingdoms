package com.convallyria.taleofkingdoms.client.gui.entity.citybuilder;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.client.TaleOfKingdomsClient;
import com.convallyria.taleofkingdoms.client.gui.entity.citybuilder.confirm.ConfirmUpgradeKingdomGui;
import com.convallyria.taleofkingdoms.client.gui.generic.bar.BarWidget;
import com.convallyria.taleofkingdoms.common.entity.guild.CityBuilderEntity;
import com.convallyria.taleofkingdoms.common.kingdom.KingdomTier;
import com.convallyria.taleofkingdoms.common.kingdom.PlayerKingdom;
import com.convallyria.taleofkingdoms.common.kingdom.builds.BuildCosts;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.action.CityBuilderAction;
import com.convallyria.taleofkingdoms.common.packet.c2s.CityBuilderActionPacket;
import com.convallyria.taleofkingdoms.common.translation.Translations;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class CityBuilderTierGui extends BaseCityBuilderScreen {

    private static final Identifier BACKGROUND = Identifier.of(TaleOfKingdoms.MODID, "textures/gui/menu.png");

    private final PlayerEntity player;
    private final CityBuilderEntity entity;
    private final ConquestInstance instance;
    private ButtonComponent fixWholeKingdomButton;
    private ButtonComponent tierUpgradeButton;
    private BarWidget woodBar, stoneBar;
//    private LabelComponent oakWoodLabel, cobblestoneLabel;

    private final Map<BuildCosts, ButtonComponent> buildButtons = new HashMap<>(BuildCosts.values().length);

    public CityBuilderTierGui(PlayerEntity player, CityBuilderEntity entity, ConquestInstance instance) {
        super(DataSource.asset(Identifier.of(TaleOfKingdoms.MODID, "citybuilder_tier_model")));
        this.player = player;
        this.entity = entity;
        this.instance = instance;
        Translations.CITYBUILDER_GUI_OPEN.send(player);
    }

    private void update() {
        final GuildPlayer guildPlayer = instance.getPlayer(player);
        final PlayerKingdom kingdom = guildPlayer.getKingdom();
        AtomicInteger cobblestoneCount = new AtomicInteger(entity.getStone());
        AtomicInteger oakWoodCount = new AtomicInteger(entity.getWood());
        fixWholeKingdomButton.active(cobblestoneCount.get() == 320 && oakWoodCount.get() == 320);

        final float oakWoodPercent = oakWoodCount.get() * (100f / 320f);
        final float cobblestonePercent = cobblestoneCount.get() * (100f / 320f);
        woodBar.setBarProgress(oakWoodPercent / 100);
        woodBar.tooltip(Text.literal(oakWoodCount.get() + " / 320"));
        stoneBar.setBarProgress(cobblestonePercent / 100);
        stoneBar.tooltip(Text.literal(cobblestoneCount.get() + " / 320"));

        buildButtons.forEach((build, button) -> button.active(entity.canAffordBuild(kingdom, build) && kingdom.getTier() == build.getTier()));

        final boolean isMaxed = kingdom.getTier().ordinal() + 1 == KingdomTier.values().length;
        final boolean hasBuiltRequired = Arrays.stream(BuildCosts.values()).noneMatch(cost -> kingdom.getTier() == cost.getTier() && !kingdom.hasBuilt(cost));
        final boolean hasResources = entity.getWood() == 320 && entity.getStone() == 320;
        this.tierUpgradeButton.active(!isMaxed && hasBuiltRequired && hasResources);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        final FlowLayout inner = rootComponent.childById(FlowLayout.class, "inner");

        final GuildPlayer guildPlayer = instance.getPlayer(player);
        final PlayerKingdom kingdom = guildPlayer.getKingdom();

        inner.child(
            Components.label(Text.translatable("menu.taleofkingdoms.citybuilder.total_money", kingdom.getTier().getName(), guildPlayer.getCoins())).color(Color.ofDye(DyeColor.GRAY))
            .positioning(Positioning.relative(50, 5))
        );

        AtomicInteger cobblestoneCount = new AtomicInteger(entity.getStone());
        AtomicInteger oakWoodCount = new AtomicInteger(entity.getWood());

        //root.add(new WLabel(Text.literal("0      160      320")).setHorizontalAlignment(HorizontalAlignment.LEFT), 100, 30, 16, 2);

        inner.child(
            Components.button(Text.translatable("menu.taleofkingdoms.citybuilder.give_wood"), c -> {
                if (MinecraftClient.getInstance().getServer() == null) {
                    TaleOfKingdomsClient.getAPI().getClientPacket(Packets.CITYBUILDER_ACTION)
                            .sendPacket(player, new CityBuilderActionPacket(entity.getId(), CityBuilderAction.GIVE_64_WOOD, Optional.empty()));
                    return;
                }

                entity.give64wood(player);
            }).positioning(Positioning.relative(80, 20)).sizing(Sizing.fixed(100), Sizing.fixed(20))
        );

        inner.child(
            Components.button(Text.translatable("menu.taleofkingdoms.citybuilder.give_cobblestone"), c -> {
                if (MinecraftClient.getInstance().getServer() == null) {
                    TaleOfKingdomsClient.getAPI().getClientPacket(Packets.CITYBUILDER_ACTION)
                            .sendPacket(player, new CityBuilderActionPacket(entity.getId(), CityBuilderAction.GIVE_64_STONE, Optional.empty()));
                    return;
                }

                entity.give64stone(player);
            }).positioning(Positioning.relative(80, 30)).sizing(Sizing.fixed(100), Sizing.fixed(20))
        );

        inner.child(
            Components.label(Text.translatable("menu.taleofkingdoms.citybuilder.wood_amount"))
                    .positioning(Positioning.relative(76, 40))
        );

        final float oakWoodPercent = oakWoodCount.get() * (100f / 320f);
        inner.child(this.woodBar = (BarWidget) new BarWidget(0, 0, 100, 12, oakWoodPercent / 100)
                .positioning(Positioning.relative(80, 45))
                .tooltip(Text.literal(oakWoodCount.get() + " / 320")));

        inner.child(
                Components.label(Text.translatable("menu.taleofkingdoms.citybuilder.stone_amount"))
                        .positioning(Positioning.relative(76, 53))
        );

        final float cobblestonePercent = cobblestoneCount.get() * (100f / 320f);
        inner.child(this.stoneBar = (BarWidget) new BarWidget(0, 0, 100, 12, cobblestonePercent / 100)
                .positioning(Positioning.relative(80, 57))
                .tooltip(Text.literal(cobblestoneCount.get() + " / 320")));

        inner.child(
            this.fixWholeKingdomButton = (ButtonComponent) Components.button(Text.translatable("menu.taleofkingdoms.citybuilder.fix_kingdom"), c -> {
                if (MinecraftClient.getInstance().getServer() == null) {
                    TaleOfKingdomsClient.getAPI().getClientPacket(Packets.CITYBUILDER_ACTION)
                            .sendPacket(player, new CityBuilderActionPacket(entity.getId(), CityBuilderAction.FIX_KINGDOM, Optional.empty()));
                    return;
                }

                entity.fixKingdom(player, kingdom);
            }).tooltip(List.of(
                  Text.literal("Repairing the kingdom costs:"),
                  Text.literal(" - 320 oak wood"),
                  Text.literal(" - 320 cobblestone")
            )).positioning(Positioning.relative(80, 70)).sizing(Sizing.fixed(100), Sizing.fixed(20))
        );

        // Price list
        inner.child(
            Components.button(Text.translatable("menu.taleofkingdoms.citybuilder.price_list"), c -> {
                MinecraftClient.getInstance().setScreen(new CityBuilderPriceListGui(player, entity, instance, kingdom));
            }).positioning(Positioning.relative(80, 80)).sizing(Sizing.fixed(100), Sizing.fixed(20))
        );

        final boolean isMaxed = kingdom.getTier().ordinal() + 1 == KingdomTier.values().length;
        final boolean hasBuiltRequired = Arrays.stream(BuildCosts.values()).noneMatch(cost -> kingdom.getTier() == cost.getTier() && !kingdom.hasBuilt(cost));
        final boolean hasResources = entity.getWood() == 320 && entity.getStone() == 320;
        final KingdomTier nextTier = isMaxed ? kingdom.getTier() : KingdomTier.values()[kingdom.getTier().ordinal() + 1];
        inner.child(
            this.tierUpgradeButton = (ButtonComponent) Components.button(Text
                            .translatable("menu.taleofkingdoms.generic.build")
                            .append(" ").append(nextTier.getName()), c -> {
                    MinecraftClient.getInstance().setScreen(new ConfirmUpgradeKingdomGui(player, entity, instance));
                })
                .active(!isMaxed && hasBuiltRequired && hasResources)
                .positioning(Positioning.relative(80, 90)).sizing(Sizing.fixed(100), Sizing.fixed(20))
        );

        // Actually starts at 70, first has addition of +20
        int currentY = 15;
        int currentX = 10;
        final int maxPerRow = 7;
        int currentRow = 0;
        for (BuildCosts build : BuildCosts.values()) {
            if (kingdom.getTier() != build.getTier()) continue;

            if (currentRow >= maxPerRow) {
                currentRow = 0;
                currentY = 15;
                currentX += 35;
            }

            //todo: small houses / large houses require special changes
            final boolean hasBuilt = kingdom.hasBuilt(build);
            final boolean canAffordBuild = entity.canAffordBuild(kingdom, build);
            final MutableText text = (hasBuilt ? Text.translatable("menu.taleofkingdoms.generic.fix") : Text.translatable("menu.taleofkingdoms.generic.build")).append(" ");
            final MutableText pluralText = (hasBuilt ? Text.translatable("menu.taleofkingdoms.generic.repairing") : Text.translatable("menu.taleofkingdoms.generic.building")).append(" ");

            final Component button = Components.button(text.append(build.getDisplayName()), c -> {
                if (MinecraftClient.getInstance().getServer() == null) {
                    TaleOfKingdomsClient.getAPI().getClientPacket(Packets.CITYBUILDER_ACTION)
                            .sendPacket(player, new CityBuilderActionPacket(entity.getId(), CityBuilderAction.BUILD, Optional.of(build)));
                    return;
                }

                entity.build(player, build, kingdom);
                MinecraftClient.getInstance().currentScreen.close();
            }).active(canAffordBuild && kingdom.getTier() == build.getTier())
                    .tooltip(List.of(
                        pluralText.append(build.getDisplayName()).append(Text.literal(" costs:")),
                        Text.literal(" - " + build.getWood() + " oak wood"),
                        Text.literal(" - " + build.getStone() + " cobblestone")
                    ))
                    .positioning(Positioning.relative(currentX, currentY += 8))
                    .sizing(Sizing.fixed(100), Sizing.fixed(20));

            inner.child(button);

            buildButtons.put(build, (ButtonComponent) button);
            currentRow++;
        }

        inner.child(
            Components.button(
                Text.translatable("menu.taleofkingdoms.generic.exit"),
                (ButtonComponent button) -> {
                    this.close();
                    Translations.CITYBUILDER_GUI_CLOSE.send(player);
                }
            ).positioning(Positioning.relative(50, 85))
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.update();
    }
}