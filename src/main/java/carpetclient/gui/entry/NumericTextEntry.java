package carpetclient.gui.entry;

import carpetclient.coders.Pokechu22.GuiNumericTextField;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public class NumericTextEntry extends StandardRowEntry<NumericTextEntry> implements IKeyboardEntry {
    protected GuiNumericTextField field;

    public NumericTextEntry(String title, String text, boolean reset, boolean useInt) {
        super(title, false, reset, null);

        initEntrys(text, useInt);
    }

    public NumericTextEntry(String title, String text, boolean reset, @Nonnull String infoStr, boolean useInt) {
        super(title, true, reset, infoStr);

        initEntrys(text, useInt);
    }

    private void initEntrys(String text, boolean useInt) {
        field = new GuiNumericTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 100, 20, useInt);
        field.setText(text);
    }

    public GuiNumericTextField getTextField() { return field; }

    @Override
    public void draw(int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, float partialTicks) {
        field.x = x + listWidth / 2;
        field.y = y;
        field.drawTextBox();
    }

    @Override
    protected boolean mouseDown(int x, int y, int button) {
        field.mouseClicked(x, y, button);
        return false;
    }

    @Override
    protected void mouseUp(int x, int y, int button) {
    }

    @Override
    public void onUpdate() {
        this.field.updateCursorCounter();
    }

    @Override
    public void keyDown(char typedChar, int keyCode) {
//        if (field.isFocused()) System.out.println("type " + keyCode + " keytypechar " + typedChar);
        if (this.field.textboxKeyTyped(typedChar, keyCode)) {
//                    setRule(title, Float.toString(this.field.getValue()));
        } else if (keyCode == Keyboard.KEY_RETURN) {
            if (field.isFocused())
                this.performAction();

            field.setFocused(false);
        }
    }

    @Override
    protected void onFocusChanged() {
        field.setFocused(this.isFocused());
    }
}
