/**
 *
 */
package calabash.java.android;

import org.jruby.RubyArray;
import org.jruby.RubyHash;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 */
public final class UIElements extends ArrayList<UIElement> {

    private static final long serialVersionUID = 3506802535880079938L;

    public UIElements() {
    }

    public UIElements(RubyArray elements, String query, AndroidCalabashWrapper wrapper)
            throws CalabashException {
        query = query.trim();
        Pattern pattern = Pattern.compile("^.+index:[0-9]+$");
        Matcher matcher = pattern.matcher(query);
        boolean indexedQuery = matcher.matches();

        for (int i = 0; i < elements.size(); i++) {
            try {
                RubyHash object = (RubyHash) elements.get(i);
                String q = query;
                if (!indexedQuery)
                    q += " index:" + i;
                this.add(new UIElement(object, q, wrapper));
            } catch (Exception e) {
                throw new CalabashException("Unsupported result format.\n"
                        + elements.toString(), e);
            }
        }
    }

    /**
     * Gets the first element
     *
     * @return
     * @throws CalabashException
     */
    public UIElement first() throws CalabashException {
        if (this.size() == 0) {
            throw new CalabashException("Empty elements collection");
        }

        return this.get(0);
    }

    private void ensureCollectionIsNotEmpty() throws CalabashException {
        if (this.size() == 0) {
            throw new CalabashException(
                    "Cannot perform action on an empty list");
        }
    }

}
