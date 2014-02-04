package com.thoughtworks.calabash.android.unit;

import com.thoughtworks.calabash.android.CalabashWrapper;
import com.thoughtworks.calabash.android.UIElement;
import org.jruby.RubyArray;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UIElementTest {

    @Test
    public void shouldQueryCalabashWrapperIfPropertyIsNull() throws Exception{
        final String descriptionField = "description";
        final String descriptionValue = "description of element";
        final String contentDescriptionField = "contentDescription";
        final String contentDescriptionValue = "content description of element";
        final HashMap data = mock(HashMap.class);
        final CalabashWrapper wrapper = mock(CalabashWrapper.class);
        final RubyArray rubyHash = mock(RubyArray.class);
        when(rubyHash.get(0)).thenReturn(descriptionValue).thenReturn(contentDescriptionValue);
        when(data.get(descriptionField)).thenReturn(null);
        when(wrapper.query("query", descriptionField)).thenReturn(rubyHash);
        when(wrapper.query("query", contentDescriptionField)).thenReturn(rubyHash);
        final UIElement element = new UIElement(data, "query", wrapper);

        final String description = element.getDescription();
        verify(wrapper).query("query", descriptionField);
        assertEquals(descriptionValue, description);

        final String contentDescription = element.getContentDescription();
        verify(wrapper).query("query", contentDescriptionField);
        assertEquals(contentDescriptionValue, contentDescription);
    }
}
