package java_cup;

import java.util.HashMap;

/**
 * This class represents a terminal symbol in the grammar. Each terminal has a
 * textual name, an index, and a string which indicates the type of object it
 * will be implemented with at runtime (i.e. the class of object that will be
 * returned by the scanner and pushed on the parse stack to represent it).
 *
 * @author Frank Flannery
 * @version last updated: 7/3/96
 */
public class Terminal extends Cymbol {

    private int associativity;
    private int precedence;

    /**
     * get the associativity of a terminal
     */
    public int associativity() {
        return associativity;
    }

    /**
     * get the precedence of a terminal
     */
    public int precedence() {
        return precedence;
    }

    /**
     * set the precedence of a terminal
     */
    public void setAssociativityAndPrecedence(int associativity, int precedence) {
        this.associativity = associativity;
        this.precedence = precedence;
    }

    /**
     * Full constructor.
     *
     * @param name     the name of the terminal.
     * @param javaType the type of the terminal.
     */
    private Terminal(int index, String name, String javaType, int associativity, int precedence) {
        /* superclass does most of the work */
        /* assign a unique index */
        super(index, name, javaType);

        /* set the precedence */
        this.precedence = precedence;
        this.associativity = associativity;

        /* add to set of all terminals and check for duplicates */
        register(this);
    }

    /**
     * Constructor with default type.
     *
     * @param name the name of the terminal.
     */
    private Terminal(int index, String name) {
        this(index, name, null, Assoc.UNKNOWN, -1);
    }

    /**
     * Static counter to assign unique index.
     */
    protected static int nextIndex = 0;

    public Terminal(String name, String javaType, int associativity, int precedence) {
        this(nextIndex++, name, javaType, associativity, precedence);
    }

    /**
     * Constructor for non-precedented terminal
     */
    public Terminal(String name, String javaType) {
        this(nextIndex++, name, javaType, Assoc.UNKNOWN, -1);
    }

    /**
     * Table of all terminals. Elements are stored using name strings as the key
     */
    protected static HashMap<String, Terminal> byName = new HashMap<>();
    /**
     * Table of all terminals indexed by their index number.
     */
    protected static HashMap<Integer, Terminal> byIndex = new HashMap<>();

    /**
     * Access to all terminals.
     */
    public static Iterable<Terminal> all() {
        return byName.values();
    }

    /**
     * Total number of terminals.
     */
    public static int size() {
        return byName.size();
    }

    /**
     * Lookup a terminal by name string.
     */
    public static Terminal findByName(String name) {
        return byName.get(name);
    }

    /**
     * Lookup a terminal by index.
     */
    public static Terminal findByIndex(int index) {
        return byIndex.get(index);
    }

    /* add to set of all terminals and check for duplicates */
    protected static void register(Terminal t) {
        Object conflict = byName.put(t.name, t);
        if (conflict != null) {
            // can't throw an execption here because this is used in static
            // initializers, so we do a crash instead
            // was:
            // throw new internal_error("Duplicate terminal (" + nm + ") created");
            (new internal_error("Duplicate terminal (" + t.name + ") created")).crash();
        }
        /* add to by_index set */
        byIndex.put(t.index, t);
    }

    // Hm Added clear to clear all static fields
    public static void clear() {
        byName.clear();
        byIndex.clear();
        nextIndex = 2;
        register(EOF);
        register(error);
    }

    /**
     * Special terminal for end of input.
     */
    public static Terminal EOF = new Terminal(0, "EOF");

    /**
     * special terminal used for error recovery
     */
    public static Terminal error = new Terminal(1, "error");

    /**
     * Report this symbol as not being a non-terminal.
     */
    @Override
    public boolean isNonTerm() {
        return false;
    }

    /**
     * Convert to a string.
     */
    @Override
    public String toString() {
        return name + '[' + index + ']';
    }
}
