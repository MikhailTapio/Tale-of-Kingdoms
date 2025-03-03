package com.convallyria.taleofkingdoms.client.packet.incoming;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.context.PacketContext;
import com.convallyria.taleofkingdoms.common.packet.s2c.InstanceSyncPacket;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class IncomingInstanceSyncPacketHandler extends InClientPacketHandler<InstanceSyncPacket> {

    public IncomingInstanceSyncPacketHandler() {
        super(Packets.INSTANCE_SYNC, InstanceSyncPacket.CODEC);
    }

    @Override
    public void handleIncomingPacket(PacketContext context, InstanceSyncPacket sync) {
        final ConquestInstance instance = sync.instance();
        context.taskQueue().execute(() -> {
            MinecraftClient client = (MinecraftClient) context.taskQueue();
            if (TaleOfKingdoms.CONFIG.mainConfig.developerMode) {
                final String text = "Received sync, " + instance;
                TaleOfKingdoms.LOGGER.info(text);
                if (client.player != null) client.player.sendMessage(Text.literal(text));
            }
            final PlayerEntity player = context.player();
            final String id = player.getUuidAsString();
            final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
            final ConquestInstance existing = api.getConquestInstanceStorage().getConquestInstance(id).orElse(null);
            if (existing != null) {
                existing.uploadData(instance);
            } else {
                api.getConquestInstanceStorage().addConquest(id, instance, true);
            }
        });
    }
}
