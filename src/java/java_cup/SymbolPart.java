package java_cup;

/**
 * This class represents a part of a production which is a symbol (terminal or
 * non terminal). This simply maintains a reference to the symbol in question.
 *
 * @author Scott Hudson
 * @version last updated: 11/25/95
 * @see Production
 */
public class SymbolPart extends ProductionPart {

    /**
     * Full constructor.
     *
     * @param symbol the symbol that this part is made up of.
     * @param label  an optional label string for the part.
     */
    public SymbolPart(Cymbol symbol, String label) throws internal_error {
        super(label);

        if (symbol == null)
            throw new internal_error("Attempt to construct a symbol_part with a null symbol");
        this.symbol = symbol;
    }

    /**
     * Constructor with no label.
     *
     * @param symbol the symbol that this part is made up of.
     */
    public SymbolPart(Cymbol symbol) throws internal_error {
        this(symbol, null);
    }

    /**
     * The symbol that this part is made up of.
     */
    protected Cymbol symbol;

    /**
     * The symbol that this part is made up of.
     */
    public Cymbol symbol() {
        return symbol;
    }

    /**
     * Respond that we are not an action part.
     */
    @Override
    public boolean isAction() {
        return false;
    }

    /**
     * Equality comparison.
     */
    public boolean equals(SymbolPart other) {
        return other != null && super.equals(other) && symbol().equals(other.symbol());
    }

    /**
     * Generic equality comparison.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SymbolPart))
            return false;
        else
            return equals((SymbolPart) other);
    }

    /**
     * Produce a hash code.
     */
    @Override
    public int hashCode() {
        return super.hashCode() ^ (symbol() == null ? 0 : symbol().hashCode());
    }

    /**
     * Convert to a string.
     */
    @Override
    public String toString() {
        if (symbol() != null)
            return super.toString() + symbol();
        else
            return super.toString() + "$MISSING-SYMBOL$";
    }
}
