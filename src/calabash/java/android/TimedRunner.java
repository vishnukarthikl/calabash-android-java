package calabash.java.android;

public class TimedRunner {

    public TimedRunner(Condition condition, int times, int seconds) {
        if (!condition.isSatisfied()) {
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {

            }
        }
    }
}
