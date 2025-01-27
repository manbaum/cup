package java_cup;

import java.util.HashMap;

/**
 * This class represents a non-terminal symbol in the grammar. Each non terminal
 * has a textual name, an index, and a string which indicates the type of object
 * it will be implemented with at runtime (i.e. the class of object that will be
 * pushed on the parse stack to represent it).
 *
 * @author Scott Hudson
 * @version last updated: 11/25/95
 */
public class NonTerminal extends Cymbol {

    /**
     * Full constructor.
     *
     * @param name     the name of the non terminal.
     * @param javaType the type string for the non terminal.
     */
    public NonTerminal(String name, String javaType) {
        /* super class does most of the work */
        super(name, javaType);

        /* add to set of all non terminals and check for duplicates */
        Object conflict = byName.put(name, this);
        if (conflict != null) {
            // can't throw an exception here because these are used in static
            // initializers, so we crash instead
            // was:
            // throw new internal_error("Duplicate non-terminal ("+nm+") created");
            (new internal_error("Duplicate non-terminal (" + name + ") created")).crash();
        }

        /* assign a unique index */
        index = nextIndex++;

        /* add to by_index set */
        byIndex.put(index, this);
    }

    /**
     * Constructor with default type.
     *
     * @param name the name of the non terminal.
     */
    public NonTerminal(String name) {
        this(name, null);
    }

    /**
     * Table of all non-terminals -- elements are stored using name strings as the
     * key
     */
    protected static HashMap<String, NonTerminal> byName = new HashMap<>();

    // Hm Added clear to clear all static fields
    public static void clear() {
        byName.clear();
        byIndex.clear();
        nextIndex = 1;
        nextNT = 0;
        byName.put(START_NT.name, START_NT);
        byIndex.put(Integer.valueOf(START_NT.index), START_NT);
    }

    /**
     * Access to all non-terminals.
     */
    public static Iterable<NonTerminal> all() {
        return byName.values();
    }

    /**
     * lookup a non terminal by name string
     */
    public static NonTerminal findByName(String name) {
        return byName.get(name);
    }

    /**
     * Table of all non terminals indexed by their index number.
     */
    protected static HashMap<Integer, NonTerminal> byIndex = new HashMap<>();

    /**
     * Lookup a non terminal by index.
     */
    public static NonTerminal find(int index) {
        return byIndex.get(index);
    }

    /**
     * Total number of non-terminals.
     */
    public static int size() {
        return byName.size();
    }

    /**
     * Static counter to assign unique indexes.
     */
    protected static int nextIndex = 0;

    /**
     * Static counter for creating unique non-terminal names
     */
    static protected int nextNT = 0;

    /**
     * special non-terminal for start symbol
     */
    public static final NonTerminal START_NT = new NonTerminal("$START");

    /**
     * flag non-terminals created to embed action productions
     */
    public boolean isEmbeddedAction = false; /* added 24-Mar-1998, CSA */

    /**
     * Method for creating a new uniquely named hidden non-terminal using the given
     * string as a base for the name (or "NT$" if null is passed).
     *
     * @param prefix base name to construct unique name from.
     */
    static NonTerminal createNT(String prefix) throws internal_error {
        return createNT(prefix, null); // TUM 20060608 embedded actions patch
    }

    /**
     * static routine for creating a new uniquely named hidden non-terminal
     */
    static NonTerminal createNT() throws internal_error {
        return createNT(null);
    }

    /**
     * TUM 20060608 bugfix for embedded action codes
     */
    static NonTerminal createNT(String prefix, String type) throws internal_error {
        if (prefix == null)
            prefix = "NT$";
        return new NonTerminal(prefix + nextNT++, type);
    }

    /**
     * Compute nullability of all non-terminals.
     */
    public static void computeNullability() throws internal_error {
        boolean change = true;
        /* repeat this process until there is no change */
        while (change) {
            /* look for a new change */
            change = false;

            /* consider each non-terminal */
            for (NonTerminal nt : all())
                /* only look at things that aren't already marked nullable */
                if (!nt.nullable())
                    if (nt.looksNullable()) {
                        nt.nullable = true;
                        change = true;
                    }

        }

        /* do one last pass over the productions to finalize all of them */
        for (Production prod : Production.all())
            prod.set_nullable(prod.check_nullable());
    }

    /**
     * Compute first sets for all non-terminals. This assumes nullability has
     * already computed.
     */
    public static void computeFirstSet() throws internal_error {
        boolean change = true;
        /* repeat this process until we have no change */
        while (change) {
            /* look for a new change */
            change = false;

            /* consider each non-terminal */
            for (NonTerminal nt : all()) {
                /* consider every production of that non terminal */
                for (Production prod : nt.productions()) {
                    /* get the updated first of that production */
                    TerminalSet prod_first = prod.check_first_set();

                    /* if this going to add anything, add it */
                    if (!prod_first.isSubsetOf(nt.firstSet)) {
                        change = true;
                        nt.firstSet.addAll(prod_first);
                    }
                }
            }
        }
    }

    /**
     * Table of all productions with this non terminal on the LHS.
     */
    protected HashMap<Production, Production> productions = new HashMap<>(11);

    /**
     * Access to productions with this non terminal on the LHS.
     */
    public Iterable<Production> productions() {
        return productions.values();
    }

    /**
     * Total number of productions with this non terminal on the LHS.
     */
    public int productionSize() {
        return productions.size();
    }

    /**
     * Add a production to our set of productions.
     */
    public void addProduction(Production prod) throws internal_error {
        /* catch improper productions */
        if (prod == null || prod.lhs() == null || prod.lhs().symbol() != this)
            throw new internal_error("Attempt to add invalid production to non terminal production table");

        /* add it to the table, keyed with itself */
        productions.put(prod, prod);
    }

    /**
     * Nullability of this non terminal.
     */
    protected boolean nullable;

    /**
     * Nullability of this non terminal.
     */
    public boolean nullable() {
        return nullable;
    }

    /**
     * First set for this non-terminal.
     */
    protected TerminalSet firstSet = new TerminalSet();

    /**
     * First set for this non-terminal.
     */
    public TerminalSet firstSet() {
        return firstSet;
    }

    /**
     * Indicate that this symbol is a non-terminal.
     */
    @Override
    public boolean isNonTerm() {
        return true;
    }

    /**
     * Test to see if this non terminal currently looks nullable.
     */
    protected boolean looksNullable() throws internal_error {
        /* look and see if any of the productions now look nullable */
        for (Production prod : productions())
            /* if the production can go to empty, we are nullable */
            if (prod.check_nullable())
                return true;

        /* none of the productions can go to empty, so we are not nullable */
        return false;
    }

    /**
     * convert to string
     */
    @Override
    public String toString() {
        return super.toString() + "[" + index() + "]" + (nullable() ? "*" : "");
    }
}
