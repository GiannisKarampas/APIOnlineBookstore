package domain.interfaces;

/**
 * A path the API exposes. Implemented by the endpoint enums so the transport can take
 * any of them without knowing which resource it is talking to.
 */
public interface IEndpoint {
    String getPath();

    @Override
    String toString();
}
