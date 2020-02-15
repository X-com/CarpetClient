package carpetclient.config;

import carpetclient.util.BiObservable;

public class ConfigBase<T> extends BiObservable<ConfigBase<T>, T>
{
    public enum ConfigType {
        BOOLEAN,
        INTEGER,
        FLOAT
    }

    private ConfigType type;

    private String name;
    private String description;

    private T defaultValue;
    private T value;

    public ConfigBase(ConfigType type, String name, T value, String description) {
        this.type = type;

        this.name = name;
        this.description = description;

        this.defaultValue = value;
        this.value = value;
    }

    public ConfigType getType() { return type; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public T getDefaultValue() { return defaultValue; }

    public T getValue() { return value; }

    public void setValue(T value) {
        this.value = value;
        this.notifySubscribers(value);
    }
}
