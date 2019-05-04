package carpetclient.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ConfigBase<T> implements IObservable<ConfigBase<T>, BiConsumer<ConfigBase<T>, T>> {
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

    private List<BiConsumer<ConfigBase<T>, T>> subscribers;

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

        if (subscribers != null)
            subscribers.forEach((r) -> r.accept(this, value));
    }

    @Override
    public ConfigBase<T> subscribe(BiConsumer<ConfigBase<T>, T> subscriber) {
        if ( subscribers == null)
            subscribers = new ArrayList<>();

        subscribers.add(subscriber);
        return this;
    }

    @Override
    public ConfigBase<T> unsubscribe(BiConsumer<ConfigBase<T>, T> subscriber) {
        if (subscribers.contains(subscriber))
            subscribers.remove(subscriber);

        return this;
    }
}
