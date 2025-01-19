package rhino.ast;

/**
 * Encapsulates information for a JavaScript parse error or warning.
 */
public class ParseProblem{

    public enum Type{Error, Warning}

    private Type type;
    private String message;
    private String sourceName;
    private int offset;
    private int length;

    /**
     * Constructs a new ParseProblem.
     */
    public ParseProblem(ParseProblem.Type type, String message,
                        String sourceName, int offset, int length){
        setType(type);
        setMessage(message);
        setSourceName(sourceName);
        setFileOffset(offset);
        setLength(length);
    }

    public ParseProblem.Type getType(){
        return type;
    }

    public void setType(ParseProblem.Type type){
        this.type = type;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String msg){
        this.message = msg;
    }

    public String getSourceName(){
        return sourceName;
    }

    public void setSourceName(String name){
        this.sourceName = name;
    }

    public int getFileOffset(){
        return offset;
    }

    public void setFileOffset(int offset){
        this.offset = offset;
    }

    public int getLength(){
        return length;
    }

    public void setLength(int length){
        this.length = length;
    }

    @Override
    public String toString(){
        String sb = sourceName + ":" +
        "offset=" + offset + "," +
        "length=" + length + "," +
        (type == Type.Error ? "error: " : "warning: ") +
        message;
        return sb;
    }
}
