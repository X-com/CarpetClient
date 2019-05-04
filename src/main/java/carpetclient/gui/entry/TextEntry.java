package carpetclient.gui.entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;

public class TextEntry extends StandardRowEntry<TextEntry> implements IKeyboardEntry {
    private GuiTextField field;

    public TextEntry(String title, String text, boolean reset) {
        super(title, false, reset, null);

        initEntrys(text);
    }

    public TextEntry(String title, String text, boolean reset, @Nonnull String infoStr) {
        super(title, true, reset, infoStr);

        initEntrys(text);
    }

    private void initEntrys(String text) {
        field = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 100, 20);
        field.setText(text);
    }

    public GuiTextField getTextField() { return field; }

    @Override
    public void draw(int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, float partialTicks) {
        if (!this.isFocused()) {
            field.setFocused(false);
        }

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
//                if (field.isFocused()) System.out.println("type " + keyCode + " keytypechar " + typedChar);
        if (this.field.textboxKeyTyped(typedChar, keyCode)) {
//                    setRule(title, Float.toString(this.field.getValue()));
        } else if (keyCode == Keyboard.KEY_RETURN) {
            if (field.isFocused())
                this.performAction();

            field.setFocused(false);
        }
    }

        /*protected void performTextAction() {
//        System.out.println("text clicked " + title + " " + text);
            CarpetRules.textRuleChange(title, field.getText());
        }*/

        /*@Override
        protected void performResetAction() {
//        System.out.println("reset clicked " + title);
            CarpetRules.resetRule(title);
        }*/

        /*@Override
        protected void performInfoAction() {
//        System.out.println("info clicked " + title);
            CarpetRules.ruleTipRequest(title);
        }*/
}

