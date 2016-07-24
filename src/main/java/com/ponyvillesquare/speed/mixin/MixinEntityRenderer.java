package com.ponyvillesquare.speed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.ponyvillesquare.speed.LiteModSpeedRunner;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManagerReloadListener;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer implements IResourceManagerReloadListener {

    private static final String EntityPlayerSP = "Lnet/minecraft/client/entity/EntityPlayerSP;";

    @Redirect(
            method = "renderWorldPass(IFJ)V",
            at = @At(
                    value = "INVOKE",
                    target = EntityPlayerSP + "isSpectator()Z"))
    private boolean fixSpectator(EntityPlayerSP player) {
        // fixes the world being culled while noclipping underground
        return player.isSpectator() || LiteModSpeedRunner.instance().isNoclipping();
    }
}
