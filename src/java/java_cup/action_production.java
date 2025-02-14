
package java_cup;

/** A specialized version of a production used when we split an existing
 *  production in order to remove an embedded action.  Here we keep a bit 
 *  of extra bookkeeping so that we know where we came from.
 * @version last updated: 11/25/95
 * @author  Scott Hudson
 */

public class action_production extends Production {

  /** Constructor.
   * @param base       the production we are being factored out of.
   * @param lhs_sym    the LHS symbol for this production.
   * @param rhs_parts  array of production parts for the RHS.
   * @param rhs_len    how much of the rhs_parts array is valid.
   * @param action_str the trailing reduce action for this production.
   * @param indexOfIntermediateResult the index of the result of the previous intermediate action on the stack relative to top, -1 if no previous action
   */ 
  public action_production(
    Production base,
    NonTerminal lhs_sym,
    ProductionPart rhs_parts[],
    int             rhs_len,
    String          action_str,
    int             indexOfIntermediateResult)
    throws internal_error
    {
      super(lhs_sym, rhs_parts, rhs_len, action_str);
      _base_production = base;
      this.indexOfIntermediateResult = indexOfIntermediateResult;
    }
  private int indexOfIntermediateResult;
  /**
   * @return the index of the result of the previous intermediate action on the stack relative to top, -1 if no previous action
   */
  public int getIndexOfIntermediateResult(){
      return indexOfIntermediateResult;
  }
  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The production we were taken out of. */
  protected Production _base_production;

  /** The production we were taken out of. */
  public Production base_production() {return _base_production;}
}
