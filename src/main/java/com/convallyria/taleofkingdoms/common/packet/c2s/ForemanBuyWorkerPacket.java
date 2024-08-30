package com.convallyria.taleofkingdoms.common.packet.c2s;

import com.convallyria.taleofkingdoms.common.packet.Packets;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ForemanBuyWorkerPacket(int entityId) implements CustomPayload {

    public static final PacketCodec<RegistryByteBuf, ForemanBuyWorkerPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ForemanBuyWorkerPacket::entityId,
            ForemanBuyWorkerPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return Packets.FOREMAN_BUY_WORKER;
    }
}
