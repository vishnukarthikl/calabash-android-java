package calabash.java.android;

public interface AndroidElementAction {

    /**
     * performs touch operation on the particular element
     *
     * @throws CalabashException
     */
    void touch() throws CalabashException;

    /**
     * long press on the element
     *
     * @throws CalabashException
     */
    void longPress() throws CalabashException;
}
