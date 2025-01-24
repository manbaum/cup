package java_cup;

/* Defines integers that represent the associativity of terminals
 * @version last updated: 7/3/96
 * @author  Frank Flannery
 */
public class Assoc {

    /* various associativity, UNKNOWN being the default value */
    public final static int UNKNOWN = -1;
    public final static int LEFT = 0;
    public final static int RIGHT = 1;
    public final static int NON_ASSOC = 2;
}