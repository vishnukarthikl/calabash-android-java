package calabash.java.android;

public class ConditionalWaiter {
    public static final int MAX_RETRIES = 5;
    public static final int SLEEP_TIME = 1;
    private final ICondition condition;

    public ConditionalWaiter(ICondition condition) {
        this.condition = condition;
    }

    public void run() throws CalabashException {
        run(MAX_RETRIES, SLEEP_TIME);
    }

    public void run(int times, int sleepTimeInSec) throws CalabashException {
        int timesTested = 0;
        int sleepTimeInMilli = sleepTimeInSec * 1000;
        while (!condition.test() && timesTested <= times) {
            try {
                Thread.sleep(sleepTimeInMilli);
                timesTested++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (timesTested > times)
            throw new CalabashException(condition.getErrorMessage());

    }

    public void run(int timeoutInMillis) throws CalabashException {
        long startTime = System.currentTimeMillis();
        do {
            if (condition.test()) {
                return;
            }
        } while ((System.currentTimeMillis() - startTime) < timeoutInMillis);
        throw new CalabashException(String.format("Wait condition timed out after %s ms", timeoutInMillis));
    }
}
