package calabash.java.android;

public abstract class ICondition {
    private final String errorMessage;

    public ICondition(String errorMessage){
        this.errorMessage = errorMessage;
    }

    protected ICondition() {
        errorMessage = null;
    }

    public abstract boolean test() throws CalabashException;

    public String getErrorMessage() {
        return errorMessage;
    }

}
