
package java_cup;

import java.util.BitSet;
import java.util.Objects;

/**
 * A set of terminals implemented as a bitset.
 *
 * @author Scott Hudson
 * @version last updated: 11/25/95
 */
public class TerminalSet {

    /**
     * Constructor for an empty set.
     */
    public TerminalSet() {
        /* allocate the bitset at what is probably the right size */
        elements = new BitSet(Terminal.size());
    }

    /**
     * Constructor for cloning from another set.
     *
     * @param other the set we are cloning from.
     */
    public TerminalSet(TerminalSet other) throws internal_error {
        Objects.requireNonNull(other);
        elements = (BitSet) other.elements.clone();
    }

    /**
     * Constant for the empty set.
     */
    public static final TerminalSet EMPTY = new TerminalSet();

    /**
     * Bitset to implement the actual set.
     */
    protected BitSet elements;

    /**
     * Determine if the set is empty.
     */
    public boolean empty() {
        return equals(EMPTY);
    }

    /**
     * Determine if the set contains a particular terminal.
     *
     * @param sym the terminal symbol we are looking for.
     */
    public boolean contains(Terminal sym) throws internal_error {
        Objects.requireNonNull(sym);
        return elements.get(sym.index());
    }

    /**
     * Given its index determine if the set contains a particular terminal.
     *
     * @param index the index of the terminal in question.
     */
    public boolean contains(int index) {
        return elements.get(index);
    }

    /**
     * Determine if this set is an (improper) subset of another.
     *
     * @param other the set we are testing against.
     */
    public boolean isSubsetOf(TerminalSet other) throws internal_error {
        Objects.requireNonNull(other);

        /* make a copy of the other set */
        BitSet copy_other = (BitSet) other.elements.clone();

        /* and or in */
        copy_other.or(elements);

        /* if it hasn't changed, we were a subset */
        return copy_other.equals(other.elements);
    }

    /**
     * Determine if this set is an (improper) superset of another.
     *
     * @param other the set we are testing against.
     */
    public boolean isSupersetOf(TerminalSet other) throws internal_error {
        Objects.requireNonNull(other);
        return other.isSubsetOf(this);
    }

    /**
     * Add a single terminal to the set.
     *
     * @param sym the terminal being added.
     * @return true if this changes the set.
     */
    public boolean add(Terminal sym) throws internal_error {
        boolean result;

        Objects.requireNonNull(sym);

        /* see if we already have this */
        result = elements.get(sym.index());

        /* if not we add it */
        if (!result)
            elements.set(sym.index());

        return result;
    }

    /**
     * Remove a terminal if it is in the set.
     *
     * @param sym the terminal being removed.
     */
    public void remove(Terminal sym) throws internal_error {
        Objects.requireNonNull(sym);
        elements.clear(sym.index());
    }

    /**
     * Add (union) in a complete set.
     *
     * @param other the set being added.
     * @return true if this changes the set.
     */
    public boolean addAll(TerminalSet other) throws internal_error {
        Objects.requireNonNull(other);

        /* make a copy */
        BitSet copy = (BitSet) elements.clone();

        /* or in the other set */
        elements.or(other.elements);

        /* changed if we are not the same as the copy */
        return !elements.equals(copy);
    }

    /**
     * Determine if this set intersects another.
     *
     * @param other the other set in question.
     */
    public boolean isIntersect(TerminalSet other) throws internal_error {
        Objects.requireNonNull(other);
        return elements.intersects(other.elements);
    }

    /**
     * Equality comparison.
     */
    public boolean equals(TerminalSet other) {
        if (other == null)
            return false;
        else
            return elements.equals(other.elements);
    }

    /**
     * Generic equality comparison.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TerminalSet))
            return false;
        else
            return equals((TerminalSet) other);
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    /**
     * Convert to string.
     */
    @Override
    public String toString() {
        String result;
        boolean comma_flag;

        result = "{";
        comma_flag = false;
        for (int t = 0; t < Terminal.size(); t++) {
            if (elements.get(t)) {
                if (comma_flag)
                    result += ", ";
                else
                    comma_flag = true;

                result += Terminal.findByIndex(t).name();
            }
        }
        result += "}";

        return result;
    }
}
