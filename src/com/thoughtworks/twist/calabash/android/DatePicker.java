package com.thoughtworks.twist.calabash.android;

import org.joda.time.DateTime;
import org.jruby.RubyHash;

public class DatePicker extends UIElement {
    public DatePicker(RubyHash data, String query, AndroidCalabashWrapper calabashWrapper) {
        super(data, query, calabashWrapper);
    }

    /**
     * set the date on a date picker element
     *
     * @param date date to be set
     * @throws CalabashException
     */
    public void setDate(DateTime date) throws CalabashException {
        getCalabashWrapper().setDate(getQuery(), date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
    }

    public DateTime getDate() throws CalabashException {
        return getCalabashWrapper().getDate(getQuery());
    }

}
