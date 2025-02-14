
package java_cup;

import java.util.Hashtable;

/**
 * This class represents a production in the grammar. It contains a LHS non
 * terminal, and an array of RHS symbols. As various transformations are done on
 * the RHS of the production, it may shrink. As a result a separate length is
 * always maintained to indicate how much of the RHS array is still valid.
 * <p>
 * 
 * I addition to construction and manipulation operations, productions provide
 * methods for factoring out actions (see remove_embedded_actions()), for
 * computing the nullability of the production (i.e., can it derive the empty
 * string, see check_nullable()), and operations for computing its first set
 * (i.e., the set of terminals that could appear at the beginning of some string
 * derived from the production, see check_first_set()).
 * 
 * @see ProductionPart
 * @see SymbolPart
 * @see ActionPart
 * @version last updated: 7/3/96
 * @author Frank Flannery
 */

public class Production {

  /*-----------------------------------------------------------*/
  /*--- Constructor(s) ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /**
   * Full constructor. This constructor accepts a LHS non terminal, an array of
   * RHS parts (including terminals, non terminals, and actions), and a string for
   * a final reduce action. It does several manipulations in the process of
   * creating a production object. After some validity checking it translates
   * labels that appear in actions into code for accessing objects on the runtime
   * parse stack. It them merges adjacent actions if they appear and moves any
   * trailing action into the final reduce actions string. Next it removes any
   * embedded actions by factoring them out with new action productions. Finally
   * it assigns a unique index to the production.
   * <p>
   *
   * Factoring out of actions is accomplished by creating new "hidden" non
   * terminals. For example if the production was originally:
   * 
   * <pre>
   *    A ::= B {action} C D
   * </pre>
   * 
   * then it is factored into two productions:
   * 
   * <pre>
   *    A ::= B X C D
   *    X ::= {action}
   * </pre>
   * 
   * (where X is a unique new non terminal). This has the effect of placing all
   * actions at the end where they can be handled as part of a reduce by the
   * parser.
   */
  public Production(NonTerminal lhs_sym, ProductionPart rhs_parts[], int rhs_l, String action_str)
      throws internal_error {
    int i;
    ActionPart tail_action;
    String declare_str;
    int rightlen = rhs_l;

    /* remember the length */
    if (rhs_l >= 0)
      _rhs_length = rhs_l;
    else if (rhs_parts != null)
      _rhs_length = rhs_parts.length;
    else
      _rhs_length = 0;

    /* make sure we have a valid left-hand-side */
    if (lhs_sym == null)
      throw new internal_error("Attempt to construct a production with a null LHS");

    /*
     * I'm not translating labels anymore, I'm adding code to declare labels as
     * valid variables. This way, the users code string is untouched 6/96 frankf
     */

    /*
     * check if the last part of the right hand side is an action. If it is, it
     * won't be on the stack, so we don't want to count it in the rightlen. Then
     * when we search down the stack for a Symbol, we don't try to search past
     * action
     */

    if (rhs_l > 0) {
      if (rhs_parts[rhs_l - 1].isAction()) {
        rightlen = rhs_l - 1;
      } else {
        rightlen = rhs_l;
      }
    }

    /* get the generated declaration code for the necessary labels. */
    declare_str = Emit.declareLabel(rhs_parts, rightlen, action_str);

    if (action_str == null)
      action_str = declare_str;
    else
      action_str = declare_str + action_str;

    /* count use of lhs */
    lhs_sym.noteUse();

    /* create the part for left-hand-side */
    _lhs = new SymbolPart(lhs_sym);

    /* merge adjacent actions (if any) */
    _rhs_length = merge_adjacent_actions(rhs_parts, _rhs_length);

    /* strip off any trailing action */
    tail_action = strip_trailing_action(rhs_parts, _rhs_length);
    if (tail_action != null)
      _rhs_length--;

    /*
     * Why does this run through the right hand side happen over and over? here a
     * quick combination of two prior runs plus one I wanted of my own frankf
     * 6/25/96
     */
    /* allocate and copy over the right-hand-side */
    /* count use of each rhs symbol */
    _rhs = new ProductionPart[_rhs_length];
    for (i = 0; i < _rhs_length; i++) {
      _rhs[i] = rhs_parts[i];
      if (!_rhs[i].isAction()) {
        ((SymbolPart) _rhs[i]).symbol().noteUse();
        if (((SymbolPart) _rhs[i]).symbol() instanceof Terminal) {
          _rhs_prec = ((Terminal) ((SymbolPart) _rhs[i]).symbol()).precedence();
          _rhs_assoc = ((Terminal) ((SymbolPart) _rhs[i]).symbol()).associativity();
        }
      }
    }

    /*
     * now action string is really declaration string, so put it in front! 6/14/96
     * frankf
     */
    if (action_str == null)
      action_str = "";
    if (tail_action != null && tail_action.code() != null)
      action_str = action_str + "\t\t" + tail_action.code();

    /* stash the action */
    _action = new ActionPart(action_str);

    /* rewrite production to remove any embedded actions */
    remove_embedded_actions();

    /* assign an index */
    _index = next_index++;

    /* put us in the global collection of productions */
    _all.put(Integer.valueOf(_index), this);

    /* put us in the production list of the lhs non terminal */
    lhs_sym.addProduction(this);
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Constructor with no action string. */
  public Production(NonTerminal lhs_sym, ProductionPart rhs_parts[], int rhs_l) throws internal_error {
    this(lhs_sym, rhs_parts, rhs_l, null);
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /*
   * Constructor with precedence and associativity of production contextually
   * define
   */
  public Production(NonTerminal lhs_sym, ProductionPart rhs_parts[], int rhs_l, String action_str, int prec_num,
                    int prec_side) throws internal_error {
    this(lhs_sym, rhs_parts, rhs_l, action_str);

    /* set the precedence */
    set_precedence_num(prec_num);
    set_precedence_side(prec_side);
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /*
   * Constructor w/ no action string and contextual precedence defined
   */
  public Production(NonTerminal lhs_sym, ProductionPart rhs_parts[], int rhs_l, int prec_num, int prec_side)
      throws internal_error {
    this(lhs_sym, rhs_parts, rhs_l, null);
    /* set the precedence */
    set_precedence_num(prec_num);
    set_precedence_side(prec_side);
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /*-----------------------------------------------------------*/
  /*--- (Access to) Static (Class) Variables ------------------*/
  /*-----------------------------------------------------------*/

  /**
   * Table of all productions. Elements are stored using their index as the key.
   */
  protected static Hashtable<Integer, Production> _all = new Hashtable<>();

  /** Access to all productions. */
  public static Iterable<Production> all() {
    return _all.values();
  }

  /** Lookup a production by index. */
  public static Production find(int indx) {
    return _all.get(Integer.valueOf(indx));
  }

  // Hm Added clear to clear all static fields
  public static void clear() {
    _all.clear();
    next_index = 0;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Total number of productions. */
  public static int number() {
    return _all.size();
  }

  /** Static counter for assigning unique index numbers. */
  protected static int next_index;

  /*-----------------------------------------------------------*/
  /*--- (Access to) Instance Variables ------------------------*/
  /*-----------------------------------------------------------*/

  /** The left hand side non-terminal. */
  protected SymbolPart _lhs;

  /** The left hand side non-terminal. */
  public SymbolPart lhs() {
    return _lhs;
  }

  /** The precedence of the rule */
  protected int _rhs_prec = -1;
  protected int _rhs_assoc = -1;

  /** Access to the precedence of the rule */
  public int precedence_num() {
    return _rhs_prec;
  }

  public int precedence_side() {
    return _rhs_assoc;
  }

  /** Setting the precedence of a rule */
  public void set_precedence_num(int prec_num) {
    _rhs_prec = prec_num;
  }

  public void set_precedence_side(int prec_side) {
    _rhs_assoc = prec_side;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** A collection of parts for the right hand side. */
  protected ProductionPart _rhs[];

  /** Access to the collection of parts for the right hand side. */
  public ProductionPart rhs(int indx) throws internal_error {
    if (indx >= 0 && indx < _rhs_length)
      return _rhs[indx];
    else
      throw new internal_error("Index out of range for right hand side of production");
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** How much of the right hand side array we are presently using. */
  protected int _rhs_length;

  /** How much of the right hand side array we are presently using. */
  public int rhs_length() {
    return _rhs_length;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /**
   * An action_part containing code for the action to be performed when we reduce
   * with this production.
   */
  protected ActionPart _action;

  /**
   * An action_part containing code for the action to be performed when we reduce
   * with this production.
   */
  public ActionPart action() {
    return _action;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Index number of the production. */
  protected int _index;

  /** Index number of the production. */
  public int index() {
    return _index;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Count of number of reductions using this production. */
  protected int _num_reductions = 0;

  /** Count of number of reductions using this production. */
  public int num_reductions() {
    return _num_reductions;
  }

  /** Increment the count of reductions with this non-terminal */
  public void note_reduction_use() {
    _num_reductions++;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Is the nullability of the production known or unknown? */
  protected boolean _nullable_known = false;

  /** Is the nullability of the production known or unknown? */
  public boolean nullable_known() {
    return _nullable_known;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Nullability of the production (can it derive the empty string). */
  protected boolean _nullable = false;

  /** Nullability of the production (can it derive the empty string). */
  public boolean nullable() {
    return _nullable;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /**
   * First set of the production. This is the set of terminals that could appear
   * at the front of some string derived from this production.
   */
  protected TerminalSet _first_set = new TerminalSet();

  /**
   * First set of the production. This is the set of terminals that could appear
   * at the front of some string derived from this production.
   */
  public TerminalSet first_set() {
    return _first_set;
  }

  /*-----------------------------------------------------------*/
  /*--- Static Methods ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /**
   * Determine if a given character can be a label id starter.
   * 
   * @param c the character in question.
   */
  protected static boolean is_id_start(char c) {
    return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';

    // later need to handle non-8-bit chars here
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /**
   * Determine if a character can be in a label id.
   * 
   * @param c the character in question.
   */
  protected static boolean is_id_char(char c) {
    return is_id_start(c) || c >= '0' && c <= '9';
  }

  /*-----------------------------------------------------------*/
  /*--- General Methods ---------------------------------------*/
  /*-----------------------------------------------------------*/

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /**
   * Helper routine to merge adjacent actions in a set of RHS parts
   * 
   * @param rhs_parts array of RHS parts.
   * @param len       amount of that array that is valid.
   * @return remaining valid length.
   */
  protected int merge_adjacent_actions(ProductionPart rhs_parts[], int len) {
    int from_loc, to_loc, merge_cnt;

    /* bail out early if we have no work to do */
    if (rhs_parts == null || len == 0)
      return 0;

    merge_cnt = 0;
    to_loc = -1;
    for (from_loc = 0; from_loc < len; from_loc++) {
      /* do we go in the current position or one further */
      if (to_loc < 0 || !rhs_parts[to_loc].isAction() || !rhs_parts[from_loc].isAction()) {
        /* next one */
        to_loc++;

        /* clear the way for it */
        if (to_loc != from_loc)
          rhs_parts[to_loc] = null;
      }

      /* if this is not trivial? */
      if (to_loc != from_loc) {
        /* do we merge or copy? */
        if (rhs_parts[to_loc] != null && rhs_parts[to_loc].isAction() && rhs_parts[from_loc].isAction()) {
          /* merge */
          rhs_parts[to_loc] = new ActionPart(
                  ((ActionPart) rhs_parts[to_loc]).code() + ((ActionPart) rhs_parts[from_loc]).code());
          merge_cnt++;
        } else {
          /* copy */
          rhs_parts[to_loc] = rhs_parts[from_loc];
        }
      }
    }

    /* return the used length */
    return len - merge_cnt;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /**
   * Helper routine to strip a trailing action off rhs and return it
   * 
   * @param rhs_parts array of RHS parts.
   * @param len       how many of those are valid.
   * @return the removed action part.
   */
  protected ActionPart strip_trailing_action(ProductionPart rhs_parts[], int len) {
    ActionPart result;

    /* bail out early if we have nothing to do */
    if (rhs_parts == null || len == 0)
      return null;

    /* see if we have a trailing action */
    if (rhs_parts[len - 1].isAction()) {
      /* snip it out and return it */
      result = (ActionPart) rhs_parts[len - 1];
      rhs_parts[len - 1] = null;
      return result;
    } else
      return null;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /**
   * Remove all embedded actions from a production by factoring them out into
   * individual action production using new non terminals. if the original
   * production was:
   * 
   * <pre>
   *    A ::= B {action1} C {action2} D
   * </pre>
   * 
   * then it will be factored into:
   * 
   * <pre>
   *    A ::= B NT$1 C NT$2 D
   *    NT$1 ::= {action1}
   *    NT$2 ::= {action2}
   * </pre>
   * 
   * where NT$1 and NT$2 are new system created non terminals.
   */

  /*
   * the declarations added to the parent production are also passed along, as
   * they should be perfectly valid in this code string, since it was originally a
   * code string in the parent, not on its own. frank 6/20/96
   */
  protected void remove_embedded_actions(

  ) throws internal_error {
    NonTerminal new_nt;
    String declare_str;
    int lastLocation = -1;
    /* walk over the production and process each action */
    for (int act_loc = 0; act_loc < rhs_length(); act_loc++)
      if (rhs(act_loc).isAction()) {

        declare_str = Emit.declareLabel(_rhs, act_loc, "");
        /* create a new non terminal for the action production */
        new_nt = NonTerminal.createNT(null, lhs().symbol().javaType()); // TUM 20060608 embedded actions patch
        new_nt.isEmbeddedAction = true; /* 24-Mar-1998, CSA */

        /* create a new production with just the action */
        new action_production(this, new_nt, null, 0,
            declare_str + ((ActionPart) rhs(act_loc)).code(),
            lastLocation == -1 ? -1 : act_loc - lastLocation);

        /* replace the action with the generated non terminal */
        _rhs[act_loc] = new SymbolPart(new_nt);
        lastLocation = act_loc;
      }
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /**
   * Check to see if the production (now) appears to be nullable. A production is
   * nullable if its RHS could derive the empty string. This results when the RHS
   * is empty or contains only non terminals which themselves are nullable.
   */
  public boolean check_nullable() throws internal_error {
    ProductionPart part;
    Cymbol sym;
    int pos;

    /* if we already know bail out early */
    if (nullable_known())
      return nullable();

    /* if we have a zero size RHS we are directly nullable */
    if (rhs_length() == 0) {
      /* stash and return the result */
      return set_nullable(true);
    }

    /* otherwise we need to test all of our parts */
    for (pos = 0; pos < rhs_length(); pos++) {
      part = rhs(pos);

      /* only look at non-actions */
      if (!part.isAction()) {
        sym = ((SymbolPart) part).symbol();

        /* if its a terminal we are definitely not nullable */
        if (!sym.isNonTerm())
          return set_nullable(false);
        /* its a non-term, is it marked nullable */
        else if (!((NonTerminal) sym).nullable())
          /* this one not (yet) nullable, so we aren't */
          return false;
      }
    }

    /* if we make it here all parts are nullable */
    return set_nullable(true);
  }

  /** set (and return) nullability */
  boolean set_nullable(boolean v) {
    _nullable_known = true;
    _nullable = v;
    return v;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /**
   * Update (and return) the first set based on current NT firsts. This assumes
   * that nullability has already been computed for all non terminals and
   * productions.
   */
  public TerminalSet check_first_set() throws internal_error {
    int part;
    Cymbol sym;

    /* walk down the right hand side till we get past all nullables */
    for (part = 0; part < rhs_length(); part++) {
      /* only look at non-actions */
      if (!rhs(part).isAction()) {
        sym = ((SymbolPart) rhs(part)).symbol();

        /* is it a non-terminal? */
        if (sym.isNonTerm()) {
          /* add in current firsts from that NT */
          _first_set.addAll(((NonTerminal) sym).firstSet());

          /* if its not nullable, we are done */
          if (!((NonTerminal) sym).nullable())
            break;
        } else {
          /* its a terminal -- add that to the set */
          _first_set.add((Terminal) sym);

          /* we are done */
          break;
        }
      }
    }

    /* return our updated first set */
    return first_set();
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Equality comparison. */
  public boolean equals(Production other) {
    if (other == null)
      return false;
    return other._index == _index;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Generic equality comparison. */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Production))
      return false;
    else
      return equals((Production) other);
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Produce a hash code. */
  @Override
  public int hashCode() {
    /* just use a simple function of the index */
    return _index * 13;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Convert to a string. */
  @Override
  public String toString() {
    String result;

    /* catch any internal errors */
    try {
      result = "production [" + index() + "]: ";
      result += lhs() != null ? lhs().toString() : "$$NULL-LHS$$";
      result += " :: = ";
      for (int i = 0; i < rhs_length(); i++)
        result += rhs(i) + " ";
      result += ";";
      if (action() != null && action().code() != null)
        result += " {" + action().code() + "}";

      if (nullable_known())
        if (nullable())
          result += "[NULLABLE]";
        else
          result += "[NOT NULLABLE]";
    } catch (internal_error e) {
      /*
       * crash on internal error since we can't throw it from here (because superclass
       * does not throw anything.
       */
      e.crash();
      result = null;
    }

    return result;
  }

  /* . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */

  /** Convert to a simpler string. */
  public String to_simple_string() throws internal_error {
    String result;

    result = lhs() != null ? lhs().symbol().name() : "NULL_LHS";
    result += " ::= ";
    for (int i = 0; i < rhs_length(); i++)
      if (!rhs(i).isAction())
        result += ((SymbolPart) rhs(i)).symbol().name() + " ";

    return result;
  }

  /*-----------------------------------------------------------*/

}
