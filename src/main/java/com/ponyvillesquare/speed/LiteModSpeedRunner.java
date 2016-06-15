package com.ponyvillesquare.speed;

import java.io.File;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.Expose;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mumfrey.liteloader.HUDRenderListener;
import com.mumfrey.liteloader.JoinGameListener;
import com.mumfrey.liteloader.Permissible;
import com.mumfrey.liteloader.PluginChannelListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;
import com.mumfrey.liteloader.permissions.PermissionsManager;
import com.mumfrey.liteloader.permissions.PermissionsManagerClient;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

@ExposableOptions(strategy = ConfigStrategy.Unversioned)
public class LiteModSpeedRunner implements Tickable, HUDRenderListener, JoinGameListener, PluginChannelListener, Permissible {

    public static final String NAME = "SpeedRunner";
    public static final String VERSION = "@VERSION@";
    public static final String PERM_NAME = "speed";
    public static final float PERM_VERSION = 1F;

    public static final float DEFAULT_SPEED = 1.5F;

    private static LiteModSpeedRunner INSTANCE;

    private boolean active;
    private boolean noclip;
    private boolean noclipAllowed;

    @Expose
    public boolean toggle;
    @Expose
    public boolean step;
    @Expose
    private float walkModifier = DEFAULT_SPEED;
    @Expose
    private float jumpModifier = DEFAULT_SPEED;
    @Expose
    private float flyModifier = DEFAULT_SPEED;

    private KeyBinding keySpeed = new KeyBinding("speed.speed", Keyboard.KEY_R, NAME);
    private KeyBinding keyNoclip = new KeyBinding("speed.noclip", Keyboard.KEY_N, NAME);
    private KeyBinding keyMenu = new KeyBinding("speed.menu", Keyboard.KEY_F7, NAME);

    private IPermissions permissions;

    public static LiteModSpeedRunner instance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String getPermissibleModName() {
        return PERM_NAME;
    }

    @Override
    public float getPermissibleModVersion() {
        return PERM_VERSION;
    }

    public float getWalkModifier() {
        return getModifier(walkModifier, Perms.WALK);
    }

    public float getJumpModifier() {
        return getModifier(jumpModifier, Perms.JUMP);
    }

    public float getFlyModifier() {
        return getModifier(flyModifier, Perms.FLY);
    }

    public float getStepModifier() {
        return getModifier(step ? 2 : 1, Perms.STEP);
    }

    private float getModifier(float modif, Perms perm) {
        return permissions.can(perm) ? modif : 1F;
    }

    public boolean isActive() {
        return (permissions != null ? permissions.hasRights() : true) && active;
    }

    public boolean isNoclipping() {
        if (!noclipAllowed) {
            noclip = false;
        }
        return noclip && Minecraft.getMinecraft().thePlayer.capabilities.isFlying;
    }

    public void setWalkModifier(float walkModifier) {
        this.walkModifier = walkModifier;
    }

    public void setJumpModifier(float jumpModifier) {
        this.jumpModifier = jumpModifier;
    }

    public void setFlyModifier(float flyModifier) {
        this.flyModifier = flyModifier;
    }

    @Override
    public void init(File configPath) {
        INSTANCE = this;
        LiteLoader.getInstance().registerExposable(this, null);
        LiteLoader.getInput().registerKeyBinding(this.keySpeed);
        LiteLoader.getInput().registerKeyBinding(this.keyNoclip);
        LiteLoader.getInput().registerKeyBinding(this.keyMenu);
    }

    @Override
    public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        if (inGame) {
            if (this.toggle) {
                if (this.keySpeed.isPressed()) {
                    this.active ^= true;
                }
            } else {
                this.active = this.keySpeed.isKeyDown();
            }
            if (this.keyMenu.isPressed()) {
                minecraft.displayGuiScreen(new GuiSpeedRunner());
            }
            if (this.keyNoclip.isPressed()) {
                this.noclip ^= true;

                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeByte(0);
                buffer.writeByte(this.noclip ? 1 : 0);
                Minecraft.getMinecraft().thePlayer.connection.sendPacket(new CPacketCustomPayload("DaFlight", buffer));
            }
        }
    }

    @Override
    public void onJoinGame(INetHandler netHandler, SPacketJoinGame joinGamePacket, ServerData serverData, RealmsServer realmsServer) {
        this.noclipAllowed = this.noclip = false;

        if (((NetHandlerPlayClient) netHandler).getNetworkManager().isLocalChannel()) {
            this.noclipAllowed = true;
        }
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeByte(1);
        Minecraft.getMinecraft().thePlayer.connection.sendPacket(new CPacketCustomPayload("DaFlight", buffer));

    }

    @Override
    public void onPostRenderHUD(int screenWidth, int screenHeight) {
        final FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        int pos = 2;
        if (this.isActive()) {
            font.drawString("Speed", 2, pos, -1);
            pos += font.FONT_HEIGHT;
        }
        if (this.isNoclipping()) {
            font.drawString("Noclip", 2, pos, -1);
        }
    }

    @Override
    public void onPreRenderHUD(int screenWidth, int screenHeight) {}

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {}

    @Override
    public void registerPermissions(PermissionsManagerClient perms) {
        PermissionManager.registerPermissions(perms, this);
        perms.tamperCheck();
    }

    @Override
    public void onPermissionsCleared(PermissionsManager manager) {
        manager.tamperCheck();
        this.permissions = PermissionManager.offline();
    }

    @Override
    public void onPermissionsChanged(PermissionsManager manager) {
        this.permissions = new PermissionManager(manager.getPermissions(this));
        manager.tamperCheck();

        ((PermissionManager) this.permissions).onChanged();

    }

    @Override
    public List<String> getChannels() {
        return ImmutableList.of("DaFlight");
    }

    @Override
    public void onCustomPayload(String channel, PacketBuffer data) {
        System.out.println(channel);
        if (channel.equals("DaFlight")) {
            byte type = data.readByte();
            byte value = data.readByte();

            if (type == 0) {
                // noclip
                boolean allowed = value == 1;

                String text = "DaFlightManager detected. " + (allowed ? "NoClip allowed!" : "NoClip not allowed!");

                ITextComponent chat = new TextComponentString("[Speed] ").setStyle(new Style().setColor(TextFormatting.DARK_PURPLE));
                chat.appendText(text);

                Minecraft.getMinecraft().thePlayer.addChatMessage(chat);
                this.noclipAllowed = value == 1;
            }
        }

    }

}
