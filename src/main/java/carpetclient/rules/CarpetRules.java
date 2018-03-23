package carpetclient.rules;

import carpetclient.gui.ScrollGUI;
import carpetclient.pluginchannel.CarpetPluginChannel;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import com.google.common.base.Charsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/*
Carpet rules that is recieved from the server, stored here for use to update GUI and other client related options.
 */
public class CarpetRules {
    private static PacketBuffer data;
    private static final Map<String, CarpetSettingEntry> rules;

    private static final int CHANGE_RULE = 0;
    private static final int CHANGE_TEXT_RULE = 1;
    private static final int RESET_RULE = 2;
    private static final int REQUEST_RULE_TIP = 3;

    static {
        rules = new HashMap<String, CarpetSettingEntry>();
    }

    /**
     * Setter to set the packet recieved from the server.
     *
     * @param data The data that is recieved from the server.
     */
    public static void setAllRules(PacketBuffer data) {
        CarpetRules.data = data;
        decodeData();
    }

    /**
     * Data recieved from server updating a single rule.
     *
     * @param data the data related to a single rule.
     */
    public static void ruleData(PacketBuffer data) {
        String rule = data.readString(100);
        int infoType = data.readInt();
        String text = data.readString(100);

        if (CHANGE_RULE == infoType) {
            rules.get(rule).changeRule(text);
        } else if (REQUEST_RULE_TIP == infoType) {
            rules.get(rule).setRuleTip(text);
        }

        ScrollGUI.updateGUI();
    }

    /**
     * Returns the list of rules synced with the server.
     *
     * @return returns the list of all rules.
     */
    public static ArrayList<CarpetSettingEntry> getAllRules() {
        ArrayList<CarpetSettingEntry> res = new ArrayList<CarpetSettingEntry>();
        for (String rule : rules.keySet().stream().sorted().collect(Collectors.toList())) {
            res.add(rules.get(rule));
        }
        return res;
    }

    /**
     * Requests server to change a rule.
     *
     * @param rule the rule name.
     */
    public static void ruleChange(String rule) {
        PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
        sender.writeInt(CarpetPluginChannel.RULE_REQUEST);
        sender.writeInt(CHANGE_RULE);
        sender.writeString(rule);

        CarpetPluginChannel.packatSender(sender);
    }

    /**
     * Requests server to change a rule with given text.
     *
     * @param rule the rule name
     * @param text the text to change the rule with
     */
    public static void textRuleChange(String rule, String text) {
        PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
        sender.writeInt(CarpetPluginChannel.RULE_REQUEST);
        sender.writeInt(CHANGE_TEXT_RULE);
        sender.writeString(rule);
        sender.writeString(text);

        CarpetPluginChannel.packatSender(sender);
    }

    /**
     * Requests server to reset a rule.
     *
     * @param rule the rule name.
     */
    public static void resetRule(String rule) {
        PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
        sender.writeInt(CarpetPluginChannel.RULE_REQUEST);
        sender.writeInt(RESET_RULE);
        sender.writeString(rule);

        CarpetPluginChannel.packatSender(sender);
    }

    /**
     * Requests server to send info on a rule.
     *
     * @param rule the rule name.
     */
    public static void ruleTipRequest(String rule) {
        PacketBuffer sender = new PacketBuffer(Unpooled.buffer());
        sender.writeInt(CarpetPluginChannel.RULE_REQUEST);
        sender.writeInt(REQUEST_RULE_TIP);
        sender.writeString(rule);

        CarpetPluginChannel.packatSender(sender);
    }

    /**
     * Decoder for the packet into rules.
     */
    private static void decodeData() {
        int ruleListSize = data.readInt();

        for (int ruleNum = 0; ruleNum < ruleListSize; ruleNum++) {
            String rule = data.readString(100);
            String current = data.readString(100);
            String def = data.readString(100);
            boolean isFloat = data.readBoolean();
//            int optionsSize = data.readInt();
//
//            String[] options = new String[optionsSize];
//            for (int optionNum = 0; optionNum < optionsSize; optionNum++) {
//                options[optionNum] = data.readString(100);
//            }

            rules.put(rule, new CarpetSettingEntry(rule, current, null, def, isFloat));
        }
    }

    /*
     * Class that stores the detailed rules.
     */
    public static class CarpetSettingEntry {
        private String rule;
        private String currentOption;
        private boolean isNumber;
        private String[] options;
        private String defaultOption;
        private boolean isDefault;
        private String ruleTip;
        private boolean isFloat;

        private int integer;
        private float flt;
        private boolean bool;

        public CarpetSettingEntry(String rule, String currentOption, String[] options, String defaultOption, boolean isFlt) {
            this.rule = rule;
            this.currentOption = currentOption;
            this.options = options;
            this.defaultOption = defaultOption;
            ruleTip = "";
            isFloat = isFlt;
            checkValues();
            checkDefault();
        }

        private void checkValues() {
            bool = Boolean.parseBoolean(currentOption);

            try {
                integer = Integer.parseInt(currentOption);
            } catch (NumberFormatException e) {
                integer = 0;
            }

            try {
                flt = Float.parseFloat(currentOption);
                isNumber = true;
            } catch (NumberFormatException e) {
                isNumber = false;
                flt = 0.0F;
            }
        }

        /**
         * Checks if the default is the same as the set rule
         */
        private void checkDefault() {
            isDefault = !currentOption.equals(defaultOption);
        }

        /**
         * Getter for the rule name
         *
         * @return rule name
         */
        public String getRule() {
            return rule;
        }

        /**
         * Getter for the currently selected rule value.
         *
         * @return rule value
         */
        public String getCurrentOption() {
            return currentOption;
        }

        /**
         * Getter for the default being true or false.
         *
         * @return default value
         */
        public boolean isDefault() {
            return isDefault;
        }

        /**
         * Getter for the option to be a number or a toggleable option.
         *
         * @return number or button
         */
        public boolean isNumber() {
            return isNumber;
        }

        /**
         * A setter for the tooltip of each rule.
         *
         * @param ruletip the value that is going to be set as the tooltip.
         */
        public void setRuleTip(String ruletip) {
            ruleTip = ruletip;
        }

        /**
         * Getter for the tooltip.
         *
         * @return returns the tooltip text
         */
        public String getRuleTip() {
            return ruleTip;
        }

        public void changeRule(String change) {
            this.currentOption = change;
            checkValues();
            checkDefault();
        }

        /**
         * Checks if this rule uses a floating point or integer when doing the text field restrictions.
         * @return returns true for is integer field.
         */
        public boolean useInteger() {
            return !isFloat;
        }
    }
}
