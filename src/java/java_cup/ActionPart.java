
package java_cup;

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
     * Simple constructor.
     *
     * @param code string containing the actual user code.
     */
    public ActionPart(String code) {
        super(/* never have a label on code */null);
        this.code = code;
    }

    /**
     * String containing code for the action in question.
     */
    protected String code;

    /**
     * String containing code for the action in question.
     */
    public String code() {
        return code;
    }

    /**
     * Set the code string.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Override to report this object as an action.
     */
    @Override
    public boolean isAction() {
        return true;
    }

    /**
     * Equality comparison for properly typed object.
     */
    public boolean equals(ActionPart other) {
        /* compare the strings */
        return other != null && super.equals(other) &&
               other.code().equals(code());
    }

    /**
     * Generic equality comparison.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ActionPart))
            return false;
        else
            return equals((ActionPart) other);
    }

    /**
     * Produce a hash code.
     */
    @Override
    public int hashCode() {
        return super.hashCode() ^
               (code() == null ? 0 : code().hashCode());
    }

    /**
     * Convert to a string.
     */
    @Override
    public String toString() {
        return super.toString() + "{" + code() + "}";
    }
}
