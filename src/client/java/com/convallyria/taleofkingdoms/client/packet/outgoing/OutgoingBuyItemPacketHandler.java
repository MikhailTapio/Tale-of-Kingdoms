package com.convallyria.taleofkingdoms.client.packet.outgoing;

import com.convallyria.taleofkingdoms.client.packet.ClientPacketHandler;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.context.PacketContext;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OutgoingBuyItemPacketHandler extends ClientPacketHandler {

    public OutgoingBuyItemPacketHandler() {
        super(Packets.BUY_ITEM);
    }

    @Override
    public void handleIncomingPacket(PacketContext context, PacketByteBuf attachedData) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public void handleOutgoingPacket(@NotNull PlayerEntity player, @Nullable Object... data) {
        PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
        passedData.writeString((String) data[0]);
        passedData.writeInt((Integer) data[1]);
        passedData.writeEnumConstant((Enum<?>) data[2]);
        sendPacket(player, passedData);
    }
}
