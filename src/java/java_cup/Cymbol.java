package java_cup;

import java.util.Objects;

/**
 * This abstract class serves as the base class for grammar symbols (i.e., both
 * terminals and non-terminals). Each symbol has a name string, and a string
 * giving the type of object that the symbol will be represented by on the
 * runtime parse stack. In addition, each symbol maintains a use count in order
 * to detect symbols that are declared but never used, and an index number that
 * indicates where it appears in parse tables (index numbers are unique within
 * terminals or non terminals, but not across both).
 *
 * @author Frank Flannery
 * @version last updated: 7/3/96
 * @see java_cup.terminal
 * @see java_cup.non_terminal
 */
public abstract class Cymbol {

    /**
     * Full constructor.
     *
     * @param name     the name of the symbol.
     * @param javaType a string with the type name.
     */
    public Cymbol(String name, String javaType) {
        /* sanity check */
        this.name = Objects.requireNonNull(name);
        /* apply default if no type given */
        this.javaType = javaType != null ? javaType : "Object";
    }

    /**
     * Constructor with default type.
     *
     * @param name the name of the symbol.
     */
    public Cymbol(String name) {
        this(name, null);
    }

    /**
     * String for the human readable name of the symbol.
     */
    protected String name;

    /**
     * String for the human readable name of the symbol.
     */
    public String name() {
        return name;
    }

    /**
     * String for the type of object used for the symbol on the parse stack.
     */
    protected String javaType;

    /**
     * String for the type of object used for the symbol on the parse stack.
     */
    public String javaType() {
        return javaType;
    }

    /**
     * Count of how many times the symbol appears in productions.
     */
    protected int useCount = 0;

    /**
     * Count of how many times the symbol appears in productions.
     */
    public int useCount() {
        return useCount;
    }

    /**
     * Increment the use count.
     */
    public void noteUse() {
        useCount++;
    }

    /**
     * Index of this symbol (terminal or non terminal) in the parse tables. Note:
     * indexes are unique among terminals and unique among non terminals, however, a
     * terminal may have the same index as a non-terminal, etc.
     */
    protected int index;

    /**
     * Index of this symbol (terminal or non terminal) in the parse tables. Note:
     * indexes are unique among terminals and unique among non terminals, however, a
     * terminal may have the same index as a non-terminal, etc.
     */
    public int index() {
        return index;
    }

    /**
     * Indicate if this is a non-terminal. Here in the base class we don't know, so
     * this is abstract.
     */
    public abstract boolean isNonTerm();

    /**
     * Convert to a string.
     */
    @Override
    public String toString() {
        return name;
    }
}
