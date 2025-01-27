package java_cup;

import java.util.Objects;

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
     * The symbol that this part is made up of.
     */
    protected final Cymbol symbol;

    /**
     * The symbol that this part is made up of.
     */
    public Cymbol symbol() {
        return symbol;
    }

    /**
     * Full constructor.
     *
     * @param symbol the symbol that this part is made up of.
     * @param label  an optional label string for the part.
     */
    public SymbolPart(Cymbol symbol, String label) throws internal_error {
        super(label);
        this.symbol = Objects.requireNonNull(symbol);
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
     * Respond that we are not an action part.
     */
    @Override
    public boolean isAction() {
        return false;
    }

    /**
     * Generic equality comparison.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SymbolPart that = (SymbolPart) o;
        return Objects.equals(symbol, that.symbol);
    }

    /**
     * Produce a hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), symbol);
    }

    /**
     * Convert to a string.
     */
    @Override
    public String toString() {
        return super.toString() + symbol;
    }
}
