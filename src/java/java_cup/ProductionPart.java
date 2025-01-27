package java_cup;

import java.util.Objects;

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
 * @see Production
 */
public abstract class ProductionPart {

    /**
     * Optional label for referring to the part within an action (null for no
     * label).
     */
    protected final String label;

    /**
     * Optional label for referring to the part within an action (null for no
     * label).
     */
    public String label() {
        return label;
    }

    /**
     * Simple constructor.
     */
    public ProductionPart(String label) {
        // label may be null, for example in action part.
        this.label = label;
    }

    /**
     * Indicate if this is an action (rather than a symbol). Here in the base class,
     * we don't this know yet, so its an abstract method.
     */
    public abstract boolean isAction();

    /**
     * Generic equality comparison.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductionPart that = (ProductionPart) o;
        return Objects.equals(label, that.label);
    }

    /**
     * Produce a hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(label);
    }

    /**
     * Convert to a string.
     */
    @Override
    public String toString() {
        return label != null ? label + ':' : "";
    }
}
