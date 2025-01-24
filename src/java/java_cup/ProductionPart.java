package java_cup;

/**
 * This class represents one part (either a symbol or an action) of a
 * production. In this base class it contains only an optional label string that
 * the user can use to refer to the part within actions.
 * <p>
 * <p>
 * This is an abstract class.
 *
 * @author Scott Hudson
 * @version last updated: 11/25/95
 * @see java_cup.production
 */
public abstract class ProductionPart {

    /**
     * Simple constructor.
     */
    public ProductionPart(String label) {
        this.label = label;
    }

    /**
     * Optional label for referring to the part within an action (null for no
     * label).
     */
    protected String label;

    /**
     * Optional label for referring to the part within an action (null for no
     * label).
     */
    public String label() {
        return label;
    }

    /**
     * Indicate if this is an action (rather than a symbol). Here in the base class,
     * we don't this know yet, so its an abstract method.
     */
    public abstract boolean isAction();

    /**
     * Equality comparison.
     */
    public boolean equals(ProductionPart other) {
        if (other == null)
            return false;

        /* compare the labels */
        if (label() != null)
            return label().equals(other.label());
        else
            return other.label() == null;
    }

    /**
     * Generic equality comparison.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ProductionPart))
            return false;
        else
            return equals((ProductionPart) other);
    }

    /**
     * Produce a hash code.
     */
    @Override
    public int hashCode() {
        return label() == null ? 0 : label().hashCode();
    }

    /**
     * Convert to a string.
     */
    @Override
    public String toString() {
        if (label() != null)
            return label() + ":";
        else
            return " ";
    }
}
