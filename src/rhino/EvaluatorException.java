package rhino;

/**
 * The class of exceptions thrown by the JavaScript engine.
 */
public class EvaluatorException extends RhinoException{
    public EvaluatorException(String detail){
        super(detail);
    }

    /**
     * Create an exception with the specified detail message.
     * <p>
     * Errors internal to the JavaScript engine will simply throw a
     * RuntimeException.
     * @param detail the error message
     * @param sourceName the name of the source reponsible for the error
     * @param lineNumber the line number of the source
     */
    public EvaluatorException(String detail, String sourceName,
                              int lineNumber){
        this(detail, sourceName, lineNumber, null, 0);
    }

    /**
     * Create an exception with the specified detail message.
     * <p>
     * Errors internal to the JavaScript engine will simply throw a
     * RuntimeException.
     * @param detail the error message
     * @param sourceName the name of the source responsible for the error
     * @param lineNumber the line number of the source
     * @param columnNumber the columnNumber of the source (may be zero if
     * unknown)
     * @param lineSource the source of the line containing the error (may be
     * null if unknown)
     */
    public EvaluatorException(String detail, String sourceName, int lineNumber,
                              String lineSource, int columnNumber){
        super(detail);
        recordErrorOrigin(sourceName, lineNumber, lineSource, columnNumber);
    }


}
