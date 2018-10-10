package carpetclient.coders.Pokechu22;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class GuiNumericIntTextField extends GuiTextField implements GuiListExtended.IGuiListEntry {
    /**
     * Last text that was successfully entered.
     */
    private String lastSafeText = "0";

    public GuiNumericIntTextField(int id, FontRenderer fontRenderer,
                                  int x, int y, int width, int height) {
        super(id, fontRenderer, x, y, width,
                height);
        setText("0");
    }

    @Override
    public void drawTextBox() {
        super.drawTextBox();
    }

    /**
     * Sets the value.
     *
     * @param value
     * @return
     */
    public void setValue(int value) {
        String text = String.valueOf(value);
        lastSafeText = text;
        setText(text);
    }

    @Override
    public String getText() {
        String text = super.getText();

        try {
            if (text.length() == 0 || super.getText().equals("-")) return "0";
            return String.valueOf(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            setText(lastSafeText);
            return lastSafeText;
        }
    }

    @Override
    public void setText(String text) {
        String value = "";

        try {
            value = String.valueOf(Integer.parseInt(text));
        } catch (NumberFormatException e) {
            value = lastSafeText;
        }

        super.setText(value);
        lastSafeText = value;
    }

    protected boolean mouseDown(int x, int y, int button) {
        mouseClicked(x, y, button);
        return false;
    }

    public void keyDown(char typedChar, int keyCode) {
        if (this.textboxKeyTyped(typedChar, keyCode)) {
            try {
                Integer.parseInt(getText());
                lastSafeText = getText();
            } catch (NumberFormatException e) {
                setText(lastSafeText);
            }
        } else if (keyCode == Keyboard.KEY_RETURN && isFocused()) {
            if (super.getText().length() == 0 || super.getText().equals("-")) {
                setText("0");
            }
            performTextAction();
            setFocused(false);
        }
    }

    public void performTextAction() {
    }

    @Override
    public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {

    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        return mouseDown(mouseX, mouseY, mouseEvent);
    }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

    }
}
