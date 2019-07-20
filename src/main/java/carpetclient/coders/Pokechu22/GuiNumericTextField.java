package carpetclient.coders.Pokechu22;


import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

public class GuiNumericTextField extends GuiTextField {
    /**
     * Last text that was successfully entered.
     */
    private String lastSafeText = "0";
    private boolean useInteger;

    public GuiNumericTextField(int id, FontRenderer fontRenderer, int x, int y, int width, int height) {
        this(id, fontRenderer, x, y, width, height, true);
    }

    public GuiNumericTextField(int id, FontRenderer fontRenderer, int x, int y, int width, int height, boolean useInt) {
        super(id, fontRenderer, x, y, width, height);
        setText("0");
        this.useInteger = useInt;
    }

    /**
     * Sets the value.
     *
     * @param value
     * @return
     */
    public void setValue(int value) {
        String text = String.valueOf(value);
        this.lastSafeText = text;
        setText(text);
    }

    @Override
    public String getText() {
        String text = super.getText();

        try {
            if (text.contains("d") || text.contains("f"))
                return this.lastSafeText;

            if (this.useInteger) {
                return String.valueOf(Integer.parseInt(text));
            } else {
                return String.valueOf(Float.parseFloat(text));
            }
        } catch (NumberFormatException e) {
            setText(this.lastSafeText);
            return this.lastSafeText;
        }
    }

    @Override
    public void setText(String text) {
        String value;

        try {
            if (this.useInteger) {
                value = String.valueOf(Integer.parseInt(text));
            } else {
                value = String.valueOf(Float.parseFloat(text));
            }
        } catch (NumberFormatException e) {
            value = this.lastSafeText;
        }

        super.setText(value);
        this.lastSafeText = value;
    }

    // ===== Events ===== //
    public void keyDown(char typedChar, int keyCode) {
        if (this.textboxKeyTyped(typedChar, keyCode)) {
            this.lastSafeText = getText();
        } else if (keyCode == Keyboard.KEY_RETURN && isFocused()) {
            if (super.getText().length() == 0 || super.getText().equals("-")) {
                setText("0");
            }
            this.performTextAction();
            this.setFocused(false);
        }
    }

    public void performTextAction() {
    }
}