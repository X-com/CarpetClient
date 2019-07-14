package carpetclient.gui.config;

import carpetclient.coders.Pokechu22.GuiConfigList;
import carpetclient.gui.ConfigGUI;
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
                addEntry(new NumericTextEntry(r.getRule(), r.getCurrentOption(), true, r.getRuleTip(), r.useInteger()) {
                    @Override
                    protected String getTooltip() {
                        return r.getRuleTip();
                    }
                })
                .onAction((sender) -> {
                    //        System.out.println("text clicked " + title + " " + text);
                    CarpetRules.textRuleChange(sender.getTitle(), sender.getTextField().getText());
                })
                .onReset((sender) -> CarpetRules.resetRule(sender.getTitle()))
                .onInfo((sender) -> CarpetRules.ruleTipRequest(sender.getTitle()));
            } else {
                addEntry(new ButtonEntry(r.getRule(), "", true, "") {
                    @Override
                    protected String getDisplayString() {
                        return r.getCurrentOption();
                    }

                    @Override
                    protected String getTooltip() {
                        return r.getRuleTip();
                    }
                })
                .onAction((sender) -> {
                    //        System.out.println("button clicked " + ruleName);
                    CarpetRules.ruleChange(sender.getTitle());
                })
                .onReset((sender) -> CarpetRules.resetRule(sender.getTitle()))
                .onInfo((sender) -> CarpetRules.ruleTipRequest(sender.getTitle()));
            }
        }
    }
}
