package com.ponyvillesquare.speed.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.authlib.GameProfile;
import com.ponyvillesquare.speed.LiteModSpeedRunner;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP extends AbstractClientPlayer {

    private final float ORIGINAL_STEP = 0.6F;

    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Override
    protected float getJumpUpwardsMotion() {
        LiteModSpeedRunner speed = LiteModSpeedRunner.instance();
        float modifier = speed.isActive() ? speed.getJumpModifier() : 1;
        return super.getJumpUpwardsMotion() * modifier;
    }

    @Override
    public void moveEntity(double x, double y, double z) {
        this.stepHeight = ORIGINAL_STEP * LiteModSpeedRunner.instance().getStepModifier();
        super.moveEntity(x, y, z);
    }

    @Override
    public void moveRelative(float strafe, float forward, float friction) {
        LiteModSpeedRunner speed = LiteModSpeedRunner.instance();
        if (!speed.isActive()) {
            super.moveRelative(strafe, forward, friction);
            return;
        }
        float f = strafe * strafe + forward * forward;

        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);

            if (f < 1.0F) {
                f = 1.0F;
            }

            // replace friction with grass
            friction = 0.099999F;

            f = friction / f;

            // apply the modifier after "friction" is applied
            f *= this.capabilities.isFlying ? speed.getFlyModifier() : speed.getWalkModifier();

            strafe = strafe * f;
            forward = forward * f;
            float f1 = MathHelper.sin(this.rotationYaw * 0.017453292F);
            float f2 = MathHelper.cos(this.rotationYaw * 0.017453292F);
            this.motionX += strafe * f2 - forward * f1;
            this.motionZ += forward * f2 + strafe * f1;
        }
    }

    @Redirect(
            method = "onLivingUpdate()V",
            expect = 2,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerCapabilities;getFlySpeed()F"))
    private float capabilities$flySpeed(PlayerCapabilities capabilities) {

        LiteModSpeedRunner speed = LiteModSpeedRunner.instance();
        float modifier = speed.isActive() ? speed.getFlyModifier() : 1F;
        return capabilities.getFlySpeed() * modifier;
    }

}
