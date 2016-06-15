package com.ponyvillesquare.speed;

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.mumfrey.liteloader.client.gui.GuiCheckbox;
import com.mumfrey.liteloader.core.LiteLoader;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiSlider.FormatHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

public class GuiSpeedRunner extends GuiScreen implements GuiResponder, FormatHelper {

    private enum Settings {
        FLY,
        WALK,
        JUMP,
        STEP,
        FLY_RESET,
        WALK_RESET,
        JUMP_RESET,
        TOGGLE
    }

    private final LiteModSpeedRunner speed = LiteModSpeedRunner.instance();

    private GuiCheckbox toggle;
    private GuiSlider fly;
    private GuiSlider walk;
    private GuiSlider jump;
    private GuiCheckbox step;

    @Override
    public void initGui() {
        this.toggle = new GuiCheckbox(Settings.TOGGLE.ordinal(), 50, 20, "Toggle Speed");
        this.toggle.checked = this.speed.toggle;
        this.buttonList.add(this.toggle);

        this.step = new GuiCheckbox(Settings.STEP.ordinal(), 50, 34, "Step Assist");
        this.step.checked = this.speed.step;
        this.buttonList.add(this.step);

        this.fly = new GuiSliderInterv(this, Settings.FLY.ordinal(), 52, 50, "fly modifier", 0, 15, speed.getFlyModifier(), this);
        this.buttonList.add(this.fly);

        this.walk = new GuiSliderInterv(this, Settings.WALK.ordinal(), 52, 72, "walk modifier", 0, 15, speed.getWalkModifier(), this);
        this.buttonList.add(this.walk);

        this.jump = new GuiSliderInterv(this, Settings.JUMP.ordinal(), 52, 94, "jump modifier", 0, 15, speed.getJumpModifier(), this);
        this.buttonList.add(this.jump);

        this.buttonList.add(new GuiButton(Settings.FLY_RESET.ordinal(), 30, 50, 20, 20, "*"));
        this.buttonList.add(new GuiButton(Settings.WALK_RESET.ordinal(), 30, 72, 20, 20, "*"));
        this.buttonList.add(new GuiButton(Settings.JUMP_RESET.ordinal(), 30, 94, 20, 20, "*"));
    }

    @Override
    public void onGuiClosed() {
        LiteLoader.getInstance().writeConfig(speed);
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        Preconditions.checkElementIndex(button.id, Settings.values().length);
        switch (Settings.values()[button.id]) {
        case FLY_RESET:
            fly.setSliderValue(LiteModSpeedRunner.DEFAULT_SPEED, true);
            break;
        case WALK_RESET:
            walk.setSliderValue(LiteModSpeedRunner.DEFAULT_SPEED, true);
            break;
        case JUMP_RESET:
            jump.setSliderValue(LiteModSpeedRunner.DEFAULT_SPEED, true);
            break;
        case STEP:
            speed.step = step.checked ^= true;
            break;
        case TOGGLE:
            speed.toggle = toggle.checked ^= true;
            break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Speed settings", width / 2, 6, -1);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public String getText(int id, String name, float value) {
        TextFormatting color = TextFormatting.WHITE;
        if (value > 10F)
            color = TextFormatting.RED;
        if (value < 0.5)
            color = TextFormatting.YELLOW;
        return color + I18n.format(name) + ": " + String.format("%.1f", value);
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void setEntryValue(int id, float value) {
        Preconditions.checkElementIndex(id, Settings.values().length);
        switch (Settings.values()[id]) {
        case FLY:
            speed.setFlyModifier(value);
            break;
        case JUMP:
            speed.setJumpModifier(value);
            break;
        case WALK:
            speed.setWalkModifier(value);
            break;
        }

    }

    @Override
    public void setEntryValue(int id, boolean value) {}

    @Override
    public void setEntryValue(int id, String value) {}
}
