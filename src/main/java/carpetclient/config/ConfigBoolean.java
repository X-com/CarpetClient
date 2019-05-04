package carpetclient.config;

public class ConfigBoolean extends ConfigBase<Boolean> {
    public ConfigBoolean(String name, Boolean value, String description) {
        super(ConfigType.BOOLEAN, name, value, description);
    }
}
