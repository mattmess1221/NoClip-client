package mnm.mods.noclip.client;

import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mumfrey.liteloader.HUDRenderListener;
import com.mumfrey.liteloader.JoinGameListener;
import com.mumfrey.liteloader.PluginChannelListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.ClientPluginChannels;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.core.PluginChannels;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.List;

public class LiteModNoClip implements Tickable, HUDRenderListener, JoinGameListener, PluginChannelListener {

    public static final String NAME = "NoClip";
    public static final String VERSION = "@VERSION@";
    public static final String CHANNEL = "NOCLIP";

    private static LiteModNoClip INSTANCE;

    private boolean noclip;
    private boolean noclipAllowed;

    private KeyBinding keyNoclip = new KeyBinding("noclip.noclip", Keyboard.KEY_N, NAME);

    public static LiteModNoClip instance() {
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

    public boolean isNoclipping() {
        if (!noclipAllowed) {
            noclip = false;
        }
        return noclip && Minecraft.getMinecraft().player.capabilities.isFlying;
    }

    @Override
    public void init(File configPath) {
        INSTANCE = this;
        LiteLoader.getInput().registerKeyBinding(this.keyNoclip);
    }

    @Override
    public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock) {
        if (inGame && this.keyNoclip.isPressed()) {
            this.noclip ^= true;

            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeBoolean(this.noclip);
            ClientPluginChannels.sendMessage(CHANNEL, buffer, PluginChannels.ChannelPolicy.DISPATCH_ALWAYS);
        }
    }

    @Override
    public void onJoinGame(INetHandler netHandler, SPacketJoinGame joinGamePacket, ServerData serverData, RealmsServer realmsServer) {
        this.noclipAllowed = this.noclip = false;
        NetHandlerPlayClient client = (NetHandlerPlayClient) netHandler;
        if (client.getNetworkManager().isLocalChannel()) {
            // this is a local server.
            this.noclipAllowed = true;
        }

    }

    @Override
    public void onPostRenderHUD(int screenWidth, int screenHeight) {
        final FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        if (this.isNoclipping()) {
            font.drawString("NoClip", 2, 2, -1);
        }
    }

    @Override
    public void onPreRenderHUD(int screenWidth, int screenHeight) {
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }

    @Override
    public List<String> getChannels() {
        return ImmutableList.of(CHANNEL);
    }

    @Override
    public void onCustomPayload(String channel, PacketBuffer data) {
        if (CHANNEL.equals(channel)) {

            // noclip
            boolean allowed = data.readBoolean();

            String text = "Plugin detected. " + (allowed ? "NoClip allowed!" : "NoClip not allowed!");

            ITextComponent chat = new TextComponentString("[" + NAME + "] ")
                    .setStyle(new Style().setColor(TextFormatting.DARK_PURPLE))
                    .appendText(text);

            Minecraft.getMinecraft().player.sendMessage(chat);
            this.noclipAllowed = allowed;

        }

    }

}
