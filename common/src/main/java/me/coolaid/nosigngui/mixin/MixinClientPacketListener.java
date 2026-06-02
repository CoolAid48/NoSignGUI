package me.coolaid.nosigngui.mixin;

import me.coolaid.nosigngui.NoSignGUI;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Inject(method = "handleOpenSignEditor", at = @At("HEAD"), cancellable = true)
    private void onHandleOpenSignEditor(ClientboundOpenSignEditorPacket packet, CallbackInfo info) {
        if (NoSignGUI.shouldSkipPlacedSignGui(packet.getPos())) {
            info.cancel();
        }
    }
}
