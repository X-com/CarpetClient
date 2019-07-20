package carpetclient.util;

public interface IObservable<T, U> {
    T subscribe(U subscriber);

    T unsubscribe(U subscriber);
}
