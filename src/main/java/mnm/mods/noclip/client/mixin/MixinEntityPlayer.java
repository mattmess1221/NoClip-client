package mnm.mods.noclip.client.mixin;

import mnm.mods.noclip.client.LiteModNoClip;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {

    @Redirect(
            method = "onUpdate()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    private boolean updateNoClipping(EntityPlayer player) {
        return player.isSpectator() || LiteModNoClip.instance().isNoclipping();
    }

}
