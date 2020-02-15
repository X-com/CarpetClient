package carpetclient.gui.entry;

import carpetclient.coders.Pokechu22.GuiNumericTextField;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public class NumericTextEntry extends StandardRowEntry<NumericTextEntry> implements IKeyboardEntry {
    private static final int TEXTFIELD_HEIGHT = 20;
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
        field = new GuiNumericTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 100, TEXTFIELD_HEIGHT, useInt);
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
    protected boolean onFocusChanged(int mouseX, int mouseY)
    {
        if (mouseX >= this.field.x && mouseX <= this.field.x + this.field.getWidth())
        {
            if (mouseY >= this.field.y && mouseY <= this.field.y + TEXTFIELD_HEIGHT)
            {
                this.getTextField().setFocused(true);
                return true;
            }
        }

        this.getTextField().setFocused(false);
        return super.onFocusChanged(mouseX, mouseY);
    }
}
