package carpetclient.config;

public class ConfigInteger extends ConfigBase<Integer> {
    public ConfigInteger(String name, int value, String description) {
        super(ConfigType.INTEGER, name, value, description);
    }
}
