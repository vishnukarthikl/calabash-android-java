package com.thoughtworks.twist.calabash.android;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ConditionalWaiterTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldThrowExceptionWhenWaitFails() throws CalabashException {
        expectedException.expect(CalabashException.class);
        expectedException.expectMessage("Wait condition timed out after 1000 ms");

        ConditionalWaiter conditionalWaiter = new ConditionalWaiter(new ICondition() {
            @Override
            public boolean test() throws CalabashException {
                return false;
            }
        });

        conditionalWaiter.run(1000);
    }
}
