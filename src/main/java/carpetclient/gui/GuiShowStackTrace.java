package carpetclient.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GuiShowStackTrace extends GuiSubWindow {

    private static final Pattern STACK_TRACE_ELEMENT_REGEX = Pattern.compile("(?:.+\\.)?(.+?\\..+?)\\(.+?(?::(\\d+))?\\)");

    private GuiButton doneButton;
    private GuiButton copyToClipboardButton;

    private List<String> stackTrace;

    public GuiShowStackTrace(GuiScreen parentScreen, GuiScreen backgroundScreen, List<String> stackTrace) {
        super("Stack Trace", parentScreen, backgroundScreen);
        this.stackTrace = stackTrace;
    }

    @Override
    public void initGui() {
        super.initGui();

        addButton(doneButton = new GuiButton(0, 0, 0, I18n.format("gui.done")));
        addButton(copyToClipboardButton = new GuiButton(1, 0, 0, "Copy to Clipboard"));
        layoutButtons(doneButton, copyToClipboardButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(parentScreen);
                break;
            case 1:
                setClipboardString(stackTrace.stream().collect(Collectors.joining("\n")));
                break;
            default:
                super.actionPerformed(button);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        fontRenderer.setUnicodeFlag(true);

        int lineHeight = fontRenderer.FONT_HEIGHT + 1;
        int y = getSubWindowTop() + 17;
        int maxY = getSubWindowBottom() - getFooterHeight() - 2;
        int maxLineCount = (maxY - y) / lineHeight;

        for (int lineInd = 0; lineInd < stackTrace.size() && lineInd < maxLineCount; lineInd++) {
            String line;
            if (lineInd == maxLineCount - 1 && lineInd != stackTrace.size() - 1) {
                line = "...";
            } else {
                line = stackTrace.get(lineInd);
                Matcher matcher = STACK_TRACE_ELEMENT_REGEX.matcher(line);
                if (matcher.matches()) {
                    line = matcher.group(1);
                    String lineNo = matcher.group(2);
                    if (lineNo != null)
                        line += " : L" + lineNo;
                }
            }
            if (fontRenderer.getStringWidth(TextFormatting.BOLD + line) > getSubWindowRight() - getSubWindowLeft() - 10) {
                while (fontRenderer.getStringWidth(TextFormatting.BOLD + "..." + line) > getSubWindowRight() - getSubWindowLeft() - 10)
                    line = line.substring(1);
                line = "..." + line;
            }
            line = TextFormatting.BOLD + line;
            fontRenderer.drawString(line, getSubWindowLeft() + 5, y, 0);
            y += lineHeight;
        }

        fontRenderer.setUnicodeFlag(mc.getLanguageManager().isCurrentLocaleUnicode() || mc.gameSettings.forceUnicodeFont);
    }
}
