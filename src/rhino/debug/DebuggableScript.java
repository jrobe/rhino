// API class

package rhino.debug;

/**
 * This interface exposes debugging information from executable
 * code (either functions or top-level scripts).
 */
public interface DebuggableScript{
    boolean isTopLevel();

    /**
     * Returns true if this is a function, false if it is a script.
     */
    boolean isFunction();

    /**
     * Get name of the function described by this script.
     * Return null or an empty string if this script is not a function.
     */
    String getFunctionName();

    /**
     * Get number of declared parameters in the function.
     * Return 0 if this script is not a function.
     * @see #getParamAndVarCount()
     * @see #getParamOrVarName(int index)
     */
    int getParamCount();

    /**
     * Get number of declared parameters and local variables.
     * Return number of declared global variables if this script is not a
     * function.
     * @see #getParamCount()
     * @see #getParamOrVarName(int index)
     */
    int getParamAndVarCount();

    /**
     * Get name of a declared parameter or local variable.
     * <tt>index</tt> should be less then {@link #getParamAndVarCount()}.
     * If <tt>index&nbsp;&lt;&nbsp;{@link #getParamCount()}</tt>, return
     * the name of the corresponding parameter, otherwise return the name
     * of variable.
     * If this script is not function, return the name of the declared
     * global variable.
     */
    String getParamOrVarName(int index);

    /**
     * Get the name of the source (usually filename or URL)
     * of the script.
     */
    String getSourceName();

    /**
     * Returns true if this script or function were runtime-generated
     * from JavaScript using <tt>eval</tt> function or <tt>Function</tt>
     * or <tt>Script</tt> constructors.
     */
    boolean isGeneratedScript();

    /**
     * Get array containing the line numbers that
     * that can be passed to <code>DebugFrame.onLineChange()</code>.
     * Note that line order in the resulting array is arbitrary
     */
    int[] getLineNumbers();

    int getFunctionCount();

    DebuggableScript getFunction(int index);

    DebuggableScript getParent();

}
