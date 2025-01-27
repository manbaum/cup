
package java_cup;

import java.util.Objects;

/**
 * This class represents a part of a production which contains an
 * action.  These are eventually eliminated from productions and converted
 * to trailing actions by factoring out with a production that derives the
 * empty string (and ends with this action).
 *
 * @author Scott Hudson
 * @version last update: 11/25/95
 * @see Production
 */
public class ActionPart extends ProductionPart {

    /**
     * String containing code for the action in question.
     */
    protected final String code;

    /**
     * String containing code for the action in question.
     */
    public String code() {
        return code;
    }

    /**
     * Simple constructor.
     *
     * @param code string containing the actual user code.
     */
    public ActionPart(String code) {
        super(/* never have a label on code */ null);
        this.code = Objects.requireNonNull(code);
    }

    /**
     * Override to report this object as an action.
     */
    @Override
    public boolean isAction() {
        return true;
    }

    /**
     * Generic equality comparison.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ActionPart that = (ActionPart) o;
        return Objects.equals(code, that.code);
    }

    /**
     * Produce a hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), code);
    }

    /**
     * Convert to a string.
     */
    @Override
    public String toString() {
        return super.toString() + "{: " + code + " :}";
    }
}
