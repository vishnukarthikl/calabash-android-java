package com.thoughtworks.twist.calabash.android;

import static com.thoughtworks.twist.calabash.android.CalabashLogger.info;
import static java.lang.String.format;

public class ConditionalWaiter {
    private final ICondition condition;

    public ConditionalWaiter(ICondition condition) {
        this.condition = condition;
    }

    public void run(int times, int sleepTimeInSec) throws CalabashException {
        int timesTested = 0;
        int sleepTimeInMilli = sleepTimeInSec * 1000;
        while (!condition.test() && timesTested <= times) {
            try {
                info("Retrying wait condition: " + condition.getDescription());
                Thread.sleep(sleepTimeInMilli);
                timesTested++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (timesTested > times)
            throw new CalabashException("Wait condition failed : " + condition.getDescription());

    }

    public void run(int timeoutInMillis) throws CalabashException {
        long startTime = System.currentTimeMillis();
        do {
            if (condition.test()) {
                return;
            }
        } while ((System.currentTimeMillis() - startTime) < timeoutInMillis);
        throw new CalabashException(format("Wait condition (%s) timed out after %s ms", condition.getDescription(), timeoutInMillis));
    }
}
