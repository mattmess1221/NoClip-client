package com.ponyvillesquare.speed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.ponyvillesquare.speed.LiteModSpeedRunner;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {

    private static final String EntityLivingBase = "Lnet/minecraft/entity/EntityLivingBase;";

    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "onUpdate()V",
            at = @At(
                    value = "INVOKE",
                    target = EntityLivingBase + "onUpdate()V",
                    shift = Shift.BEFORE))
    private void updateNoClipping(CallbackInfo ci) {
        this.noClip = LiteModSpeedRunner.instance().isNoclipping();
        if (this.noClip)
            this.onGround = false;
    }

}
