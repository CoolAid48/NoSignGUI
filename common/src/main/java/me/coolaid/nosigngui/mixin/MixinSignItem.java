package me.coolaid.nosigngui.mixin;

import me.coolaid.nosigngui.NoSignGUI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignItem.class)
public abstract class MixinSignItem {

    @Inject(method = "updateCustomBlockEntityTag", at = @At("HEAD"))
    private void onUpdateCustomBlockEntityTag(
            BlockPos pos, Level level, Player player, ItemStack itemStack, BlockState placedState, CallbackInfoReturnable<Boolean> info
    ) {
        if (level.isClientSide() && player != null) {
            NoSignGUI.markPlacedSignForSkippedGui(pos);
        }
    }
}
