package calabash.java.android;

public abstract class ICondition {
    private final String errorMessage;

    public ICondition(String errorMessage){
        this.errorMessage = errorMessage;
    }
    public abstract boolean test() throws CalabashException;

    public String getErrorMessage() {
        return errorMessage;
    }

}
