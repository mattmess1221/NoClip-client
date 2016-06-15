package com.ponyvillesquare.speed;

import java.text.DecimalFormat;

import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiSlider;

public class GuiSliderInterv extends GuiSlider {

    public GuiSliderInterv(GuiResponder guiResponder, int idIn, int x, int y, String name, float min, float max, float defaultValue, FormatHelper formatter) {
        super(guiResponder, idIn, x, y, name, min, max, defaultValue, formatter);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        super.mouseReleased(mouseX, mouseY);
        this.setSliderValue(roundValue(this.getSliderValue()), true);
    }

    private float roundValue(float f) {
        return Float.parseFloat(new DecimalFormat("#.0").format(f));
    }
}
