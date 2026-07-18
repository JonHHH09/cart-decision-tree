package cart.decisiontree.application.model;

public final class CartApplicationException extends RuntimeException {

    private final String code;

    public CartApplicationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
