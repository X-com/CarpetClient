package carpetclient.gui.config;

import carpetclient.coders.Pokechu22.GuiConfigList;
import carpetclient.gui.entry.ButtonEntry;
import carpetclient.gui.entry.NumericTextEntry;
import carpetclient.gui.entry.SimpleButtonEntry;
import carpetclient.rules.CarpetRules;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;

public class CarpetServerList extends GuiConfigList {
    public CarpetServerList(Minecraft mcIn, int slotHeightIn) {
        super(mcIn, slotHeightIn);
    }

    @Override
    public void initGui() {
        ArrayList<CarpetRules.CarpetSettingEntry> rules = CarpetRules.getAllRules();

        addEntry(new SimpleButtonEntry("Force Update").onAction((source) -> CarpetRules.requestUpdate()));

        for (CarpetRules.CarpetSettingEntry r : rules) {
            if (r.isNumber()) {
                NumericTextEntry entry = addEntry(new NumericTextEntry(r.getRule(), r.getCurrentOption(), true, r.getRuleTip(), r.useInteger()) {
                    @Override
                    protected String getTooltip() {
                        return r.getRuleTip();
                    }

                    @Override
                    protected boolean isResetEnabled() { return r.isDefault(); }
                })
                .onAction((sender) -> {
                    //        System.out.println("text clicked " + title + " " + text);
                    CarpetRules.textRuleChange(sender.getTitle(), sender.getTextField().getText());
                })
                .onReset((sender) -> CarpetRules.resetRule(sender.getTitle()))
                .onInfo((sender) -> CarpetRules.ruleTipRequest(sender.getTitle()));

                r.subscribe((rule, value) -> entry.getTextField().setText(value));
            } else {
                ButtonEntry entry = addEntry(new ButtonEntry(r.getRule(), r.getCurrentOption(), true, "") {
                    @Override
                    protected String getTooltip() {
                        return r.getRuleTip();
                    }

                    @Override
                    protected boolean isResetEnabled() { return r.isDefault(); }
                })
                .onAction((sender) -> {
                    //        System.out.println("button clicked " + ruleName);
                    CarpetRules.ruleChange(sender.getTitle());
                })
                .onReset((sender) -> CarpetRules.resetRule(sender.getTitle()))
                .onInfo((sender) -> CarpetRules.ruleTipRequest(sender.getTitle()));

                r.subscribe((rule, value) -> entry.setDisplayString(value));
            }
        }
    }
}
