package carpetclient.coders.Pokechu22;

import carpetclient.gui.ClientGUI;
import carpetclient.rules.CarpetRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
This is a scrolling class for implementing scrollable GUI to minecraft.
"Based on code originally in WDL (by pokechu22), used with permission." - Pokechu22
For use of this code, ask permission from Pokechu22.
 */
public class ScrollGUI extends GuiScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final GuiScreen parent;
    private static PacketBuffer data;
    private final ClientGUI clientGUI;
    private String title;
    private String title2 = "Carpet server version";
    private static String serverVersion = "";
    private static final int SET_TEXT_FIELD = 0xE0E0E0, DEFAULT_TEXT_FIELD = 0x808080;
    @Nullable
    private static String hoveredToolTip;

    public static void initGUI(GuiIngameMenu guiIngameMenu) {
        Minecraft.getMinecraft().displayGuiScreen(new ScrollGUI(guiIngameMenu));
    }
    
    public static void setServerVersion(String s){
        serverVersion = s;
    }

    public ScrollGUI(GuiScreen parent) {
        this.parent = parent;
        this.clientGUI = new ClientGUI(this);
    }

    public void initGui() {
        if (ClientGUI.list == null) {
            ClientGUI.list = new GuiGameRuleList();
        }
        this.title = "Carpet Client";

        this.buttonList.add(new GuiButton(100, this.width / 2 - 100,
                this.height - 29, "Back"));
//        this.buttonList.add(new GuiButton(666, this.width / 2 - 100,
//                15, I18n.format("Force Update")));
        clientGUI.getList().setDimensions(this.width, this.height, 39, this.height - 32);
        ClientGUI.display();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 100) {
            if (clientGUI.isRootElseDropTo()) {
                this.mc.displayGuiScreen(this.parent);
            }
        }
//        if (button.id == 666) {
//            CarpetRules.requestUpdate();
//        }
    }

    public void ruleButtonClicked(String ruleName) {
//        System.out.println("button clicked " + ruleName);
        CarpetRules.ruleChange(ruleName);
    }

    public void textButtonClicked(String ruleName, String text) {
//        System.out.println("text clicked " + ruleName + " " + text);
        CarpetRules.textRuleChange(ruleName, text);
    }

    public void resetButtonClicked(String ruleName) {
//        System.out.println("reset clicked " + ruleName);
        CarpetRules.resetRule(ruleName);
    }

    public void infoButtonClicked(String ruleName) {
//        System.out.println("info clicked " + ruleName);
        CarpetRules.ruleTipRequest(ruleName);
    }

    public void buttonClicked(int buttonID) {
        clientGUI.buttonGUIAction(buttonID);
    }

    public class GuiGameRuleList extends GuiListExtended {
        @Nullable
        private String lastClickedRule = null;
        private final List<IGuiListEntry> entries = new ArrayList<>();

        public GuiGameRuleList() {
            super(ScrollGUI.this.mc, ScrollGUI.this.width, ScrollGUI.this.height, 39, ScrollGUI.this.height - 32, 24);
        }

        public void clear() {
            entries.clear();
        }

        public void addNewButton(String btnText, int id) {
            this.entries.add(new ButtonEntry(btnText, id));
        }

        public void addNewRuleButton(String str, String btnText, boolean reset, String info) {
            this.entries.add(new ButtonRuleEntry(str, btnText, reset, info));
        }

        public void addNewRuleButton(String str, String btnText, boolean reset, String info, int buttonAction) {
            this.entries.add(new ButtonRuleEntry(str, btnText, reset, info, buttonAction));
        }

        public void addNewText(String str, String txtText, boolean reset, String info, boolean useInt) {
            this.entries.add(new TextRuleEntry(str, txtText, reset, info, useInt));
        }

        @Override
        public int getListWidth() {
            return 180 * 2;
        }

        @Override
        protected int getScrollBarX() {
            return this.width / 2 + getListWidth() / 2 + 4;
        }

        public GuiGameRuleList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
            super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        }

        @Override
        public IGuiListEntry getListEntry(int index) {
            return entries.get(index);
        }

        @Override
        protected int getSize() {
            return entries.size();
        }

        public void update() {
            // Use a manual for loop to avoid concurrent modification exceptions
            for (int i = 0; i < getSize(); i++) {
                IGuiListEntry entry = getListEntry(i);
                if (entry instanceof KeyboardEntry) {
                    ((KeyboardEntry) entry).onUpdate();
                }
            }
        }

        public void keyDown(char typedChar, int keyCode) {
            // Use a manual for loop to avoid concurrent modification exceptions
            for (int i = 0; i < getSize(); i++) {
                IGuiListEntry entry = getListEntry(i);
                if (entry instanceof KeyboardEntry) {
                    ((KeyboardEntry) entry).keyDown(typedChar, keyCode);
                }
            }
        }

        private class ButtonEntry extends RuleEntry {
            private GuiButton button;
            private int buttonID;

            public ButtonEntry(String btnText, int buttonID) {
                super();
                this.buttonID = buttonID;
                button = new GuiButton(0, 0, 0, 150, 20, btnText);
            }

            @Override
            protected void draw(int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, float partialTicks) {
                this.button.x = x + listWidth / 2 - button.getButtonWidth() / 2;
                this.button.y = y;
//                this.button.displayString = getRule(ruleName);
                button.drawButton(mc, mouseX, mouseY, partialTicks);
            }

            @Override
            protected boolean mouseDown(int x, int y, int button) {
                if (this.button.mousePressed(mc, x, y)) {
                    this.button.playPressSound(mc.getSoundHandler());
                    performRuleAction();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            protected void mouseUp(int x, int y, int button) {
                this.button.mouseReleased(x, y);
            }

            @Override
            public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
            }

            protected void performRuleAction() {
                buttonClicked(buttonID);
            }
        }

        private class ButtonRuleEntry extends RuleEntry {
            private GuiButton button;
            private int buttonAction;

            public ButtonRuleEntry(String ruleName, boolean reset, String info) {
                super(ruleName, reset, info);
                button = new GuiButton(0, 0, 0, 100, 20, "asdf");
            }

            public ButtonRuleEntry(String str, String btnText, boolean reset, String info) {
                super(str, reset, info);
                button = new GuiButton(0, 0, 0, 100, 20, btnText);
                buttonAction = -1;
            }

            public ButtonRuleEntry(String str, String btnText, boolean reset, String info, int buttonAction) {
                super(str, reset, info);
                button = new GuiButton(0, 0, 0, 100, 20, btnText);
                this.buttonAction = buttonAction;
            }

            @Override
            protected void draw(int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, float partialTicks) {
                this.button.x = x + listWidth / 2;
                this.button.y = y;
//                this.button.displayString = getRule(ruleName);
                button.drawButton(mc, mouseX, mouseY, partialTicks);
            }

            @Override
            protected boolean mouseDown(int x, int y, int button) {
                if (this.button.mousePressed(mc, x, y)) {
                    this.button.playPressSound(mc.getSoundHandler());
                    performRuleAction();
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            protected void mouseUp(int x, int y, int button) {
                this.button.mouseReleased(x, y);
            }

//            @Override
//            protected boolean isMouseOverControl(int mouseX, int mouseY) {
//                return button.isMouseOver();
//            }

            @Override
            public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

            }

            protected void performRuleAction() {
                if (buttonAction < 0) {
                    ruleButtonClicked(ruleName);
                } else {
                    buttonClicked(buttonAction);
                }
            }
        }

        private class TextRuleEntry extends RuleEntry implements KeyboardEntry {
            private GuiNumericTextField field;

            public TextRuleEntry(String ruleName, String text, boolean reset, String info, boolean useInt) {
                super(ruleName, reset, info);
                field = new GuiNumericTextField(0, fontRenderer, 0, 0, 100, 20, useInt);
                field.setText(text);
            }

            @Override
            public void draw(int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, float partialTicks) {
                if (!this.isFocused()) {
                    field.setFocused(false);
                }
//                if (isRuleSet(this.ruleName)) {
//                    field.setTextColor(SET_TEXT_FIELD);
//                } else {
//                    field.setTextColor(DEFAULT_TEXT_FIELD);
//                }
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
                if (field.isFocused()) System.out.println("type " + keyCode + " keytypechar " + typedChar);
                if (this.field.textboxKeyTyped(typedChar, keyCode)) {
//                    setRule(ruleName, Float.toString(this.field.getValue()));
                } else if (keyCode == Keyboard.KEY_RETURN) {
                    if (field.isFocused()) performTextAction();
                    field.setFocused(false);
                }
            }

//            @Override
//            protected boolean isMouseOverControl(int mouseX, int mouseY) {
////                return isMouseOverTextBox(mouseX, mouseY, field);
//                return false;
//            }

            protected void performTextAction() {
                textButtonClicked(ruleName, field.getText());
            }

            @Override
            public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

            }
        }

        private abstract class RuleEntry implements IGuiListEntry {
            @Nonnull
            protected String ruleName;
            private boolean justButton;
            private GuiButton resetButton;
            private GuiButton infoButton;
            private String ruleInfo;

            public RuleEntry() {
                justButton = true;
            }

            public RuleEntry(@Nonnull String ruleName, boolean reset, String info) {
                this.ruleName = ruleName;
                this.resetButton = new GuiButton(0, 0, 0, 50, 20, "reset");
                this.infoButton = new GuiButton(0, 0, 0, 14, 15, "i");
                resetButton.enabled = reset;
                ruleInfo = info;
            }

            @Override
            public final void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
                drawString(fontRenderer, this.ruleName, x, y + 6, 0xFFFFFFFF);
                if (!justButton) {
                    this.resetButton.x = x + listWidth / 2 + 110;
                    this.resetButton.y = y;
//                this.resetButton.enabled = isRuleSet(this.ruleName);
                    resetButton.drawButton(mc, mouseX, mouseY, partialTicks);

                    this.infoButton.x = x + listWidth / 2 - 17;
                    this.infoButton.y = y + 2;
                    this.infoButton.enabled = (ruleInfo.length() == 0);
                    infoButton.drawButton(mc, mouseX, mouseY, partialTicks);
                }
                this.draw(x, y, listWidth, slotHeight, mouseX, mouseY, partialTicks);

//                if (this.isMouseOverControl(mouseX, mouseY)) {
//                    String key = ruleName;
//                    hoveredToolTip = ruleName;
////                    if (I18n.hasKey(key)) { // may return false for mods
////                        hoveredToolTip = I18n.format(key);
////                    }
//                }

                if (this.isMouseOverInfo(mouseX, mouseY)) {
                    if (ruleInfo.length() > 0) hoveredToolTip = ruleInfo;
                }
            }

            @Override
            public final boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
                lastClickedRule = this.ruleName;
                if (!justButton) {
                    if (resetButton.mousePressed(mc, mouseX, mouseY)) {
                        resetButton.playPressSound(mc.getSoundHandler());
                        this.performResetAction();
                        return true;
                    }
                    if (infoButton.mousePressed(mc, mouseX, mouseY)) {
                        infoButton.playPressSound(mc.getSoundHandler());
                        this.performInfoAction();
                        return true;
                    }
                }
                return mouseDown(mouseX, mouseY, mouseEvent);
            }

            @Override
            public final void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
                if (!justButton) resetButton.mouseReleased(mouseX, mouseY);
                mouseUp(mouseX, mouseY, mouseEvent);
            }

            protected abstract void draw(int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, float partialTicks);

            protected abstract boolean mouseDown(int x, int y, int button);

            protected abstract void mouseUp(int x, int y, int button);

//            protected abstract boolean isMouseOverControl(int mouseX, int mouseY);

            protected boolean isMouseOverInfo(int mouseX, int mouseY) {
                if (justButton) return false;
                return infoButton.isMouseOver();
            }

            protected boolean isFocused() {
                return lastClickedRule == this.ruleName;  // Ref equals
            }

            /**
             * Called when the reset button is clicked.
             */
            protected void performResetAction() {
                resetButtonClicked(ruleName);
            }

            protected void performInfoAction() {
                infoButtonClicked(ruleName);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        hoveredToolTip = null;
        clientGUI.getList().drawScreen(mouseX, mouseY, partialTicks);

        super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawCenteredString(fontRenderer, title, width / 2, 4, 0xFFFFFF);
        this.drawCenteredString(fontRenderer, String.format("%s: %s", title2, serverVersion), width / 2, 20, 0xFFFFFF);

        if (hoveredToolTip != null) {
            drawGuiInfoBox(hoveredToolTip, 360, 168, width, height, 48);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
            throws IOException {
        clientGUI.getList().mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        clientGUI.getList().mouseReleased(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        clientGUI.getList().keyDown(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        clientGUI.getList().handleMouseInput();
    }

    private static interface KeyboardEntry extends IGuiListEntry {
        public abstract void keyDown(char typedChar, int keyCode);

        public abstract void onUpdate();
    }

    public static void drawGuiInfoBox(String text, int infoBoxWidth,
                                      int infoBoxHeight, int guiWidth, int guiHeight, int bottomPadding) {
        if (text == null) {
            return;
        }

        int infoX = guiWidth / 2 - infoBoxWidth / 2;
        int infoY = 40;
        int y = infoY + 5;

        Gui.drawRect(infoX, infoY, infoX + infoBoxWidth, infoY
                + infoBoxHeight, 0xCF000000);

        List<String> lines = wordWrap(text, infoBoxWidth - 10);

        for (String s : lines) {
            Minecraft.getMinecraft().fontRenderer.drawString(s, infoX + 5, y, 0xFFFFFF);
            y += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        }
    }

    public static List<String> wordWrap(String s, int width) {
        s = s.replace("\r", ""); // If we got a \r\n in the text somehow, remove it.

        List<String> lines = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(s, width);

        return lines;
    }

    public static boolean isMouseOverTextBox(int mouseX, int mouseY,
                                             GuiTextField textBox) {
        int scaledX = mouseX - textBox.x;
        int scaledY = mouseY - textBox.y;

        // Standard text box height -- there is no actual getter for the real
        // one.
        final int height = 20;

        return scaledX >= 0 && scaledX < textBox.getWidth() && scaledY >= 0
                && scaledY < height;
    }

    class GuiNumericTextField extends GuiTextField {
        /**
         * Last text that was successfully entered.
         */
        private String lastSafeText = "0";
        private boolean useInteger = true;

        public GuiNumericTextField(int id, FontRenderer fontRenderer,
                                   int x, int y, int width, int height, boolean useInt) {
            super(id, fontRenderer, x, y, width,
                    height);
            setText("0");
            useInteger = useInt;
        }

        @Override
        public void drawTextBox() {
            // Save last safe text.

            try {
                if (useInteger) {
                    Integer.parseInt(getText());
                } else {
                    Float.parseFloat(getText());
                }
                lastSafeText = getText();
            } catch (NumberFormatException e) {
                setText(lastSafeText);
            }

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
                if (text.contains("d") || text.contains("f")) return lastSafeText;
                if (useInteger) {
                    return String.valueOf(Integer.parseInt(text));
                } else {
                    return String.valueOf(Float.parseFloat(text));
                }
            } catch (NumberFormatException e) {
                setText(lastSafeText);
                return lastSafeText;
            }
        }

        @Override
        public void setText(String text) {
            String value;

            try {
                if (useInteger) {
                    value = String.valueOf(Integer.parseInt(text));
                } else {
                    value = String.valueOf(Float.parseFloat(text));
                }
            } catch (NumberFormatException e) {
                value = lastSafeText;
            }

            super.setText(value);
            lastSafeText = value;
        }
    }
}
