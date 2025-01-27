
package java_cup;

import java.util.BitSet;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A set of terminals implemented as a bitset.
 *
 * @author Scott Hudson
 * @version last updated: 11/25/95
 */
public class TerminalSet {

    /**
     * Bitset to implement the actual set.
     */
    protected BitSet elements;

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
     * Determine if the set is empty.
     */
    public boolean empty() {
        return elements.isEmpty();
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
    public boolean containsIndex(int index) {
        return elements.get(index);
    }

    /**
     * Determine if this set is an (improper) subset of another.
     *
     * @param other the set we are testing against.
     */
    public boolean isSubsetOf(TerminalSet other) throws internal_error {
        Objects.requireNonNull(other);
        /* make a copy of this set */
        BitSet copy = (BitSet) elements.clone();
        /* remove all elements in the other set */
        copy.andNot(other.elements);
        /* if it is empty, we were a subset */
        return copy.isEmpty();
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
        Objects.requireNonNull(sym);
        /* see if we already have this */
        if (elements.get(sym.index())) return false;
        /* if not we add it */
        elements.set(sym.index());
        return true;
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
     * Generic equality comparison.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerminalSet that = (TerminalSet) o;
        return Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }

    /**
     * Convert to string.
     */
    @Override
    public String toString() {
        return elements.stream()
                       .mapToObj(Terminal::findByIndex)
                       .map(Terminal::name)
                       .collect(Collectors.joining(", ", "{", "}"));
    }
}
