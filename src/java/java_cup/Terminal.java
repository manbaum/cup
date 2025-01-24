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

    /**
     * Full constructor.
     *
     * @param name     the name of the terminal.
     * @param javaType the type of the terminal.
     */
    public Terminal(String name, String javaType, int associativity, int precedence) {
        /* superclass does most of the work */
        super(name, javaType);

        /* add to set of all terminals and check for duplicates */
        Object conflict = byName.put(name, this);
        if (conflict != null) {
            // can't throw an execption here because this is used in static
            // initializers, so we do a crash instead
            // was:
            // throw new internal_error("Duplicate terminal (" + nm + ") created");
            (new internal_error("Duplicate terminal (" + name + ") created")).crash();
        }

        /* assign a unique index */
        index = nextIndex++;

        /* set the precedence */
        this.precedence = precedence;
        this.associativity = associativity;

        /* add to by_index set */
        byIndex.put(index, this);
    }

    /**
     * Constructor for non-precedented terminal
     */
    public Terminal(String name, String javaType) {
        this(name, javaType, assoc.no_prec, -1);
    }

    /**
     * Constructor with default type.
     *
     * @param name the name of the terminal.
     */
    public Terminal(String name) {
        this(name, null);
    }

    private int associativity;
    private int precedence;

    /**
     * Table of all terminals. Elements are stored using name strings as the key
     */
    protected static HashMap<String, Terminal> byName = new HashMap<>();

    // Hm Added clear to clear all static fields
    public static void clear() {
        byName.clear();
        byIndex.clear();
        nextIndex = 0;
        EOF = new Terminal("EOF");
        error = new Terminal("error");
    }

    /**
     * Access to all terminals.
     */
    public static Iterable<Terminal> all() {
        return byName.values();
    }

    /**
     * Lookup a terminal by name string.
     */
    public static Terminal findByName(String name) {
        return byName.get(name);
    }

    /**
     * Table of all terminals indexed by their index number.
     */
    protected static HashMap<Integer, Terminal> byIndex = new HashMap<>();

    /**
     * Lookup a terminal by index.
     */
    public static Terminal findByIndex(int index) {
        return byIndex.get(index);
    }

    /**
     * Total number of terminals.
     */
    public static int size() {
        return byName.size();
    }

    /**
     * Static counter to assign unique index.
     */
    protected static int nextIndex = 0;

    /**
     * Special terminal for end of input.
     */
    public static Terminal EOF = new Terminal("EOF");

    /**
     * special terminal used for error recovery
     */
    public static Terminal error = new Terminal("error");

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
        return super.toString() + "[" + index() + "]";
    }

    /**
     * get the precedence of a terminal
     */
    public int precedence() {
        return precedence;
    }

    public int associativity() {
        return associativity;
    }

    /**
     * set the precedence of a terminal
     */
    public void setAssociativityAndPrecedence(int associativity, int precedence) {
        this.associativity = associativity;
        this.precedence = precedence;
    }
}
