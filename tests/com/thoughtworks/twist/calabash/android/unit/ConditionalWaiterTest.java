package com.thoughtworks.twist.calabash.android.unit;

import com.thoughtworks.twist.calabash.android.CalabashException;
import com.thoughtworks.twist.calabash.android.ConditionalWaiter;
import com.thoughtworks.twist.calabash.android.ICondition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ConditionalWaiterTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionWhenWaitFails() throws CalabashException {
        expectedException.expect(CalabashException.class);
        expectedException.expectMessage("Wait condition (wait description) timed out after 1000 ms");

        ConditionalWaiter conditionalWaiter = new ConditionalWaiter(new ICondition("wait description") {
            @Override
            public boolean test() throws CalabashException {
                return false;
            }
        });

        conditionalWaiter.run(1000);
    }
}
