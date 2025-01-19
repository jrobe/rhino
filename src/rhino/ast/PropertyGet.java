package rhino.ast;

import rhino.*;

/**
 * AST node for the '.' operator.  Node type is {@link Token#GETPROP}.
 */
public class PropertyGet extends InfixExpression{

    {
        type = Token.GETPROP;
    }

    public PropertyGet(){
    }

    public PropertyGet(int pos){
        super(pos);
    }

    public PropertyGet(int pos, int len){
        super(pos, len);
    }

    public PropertyGet(int pos, int len, AstNode target, Name property){
        super(pos, len, target, property);
    }

    /**
     * Constructor.  Updates bounds to include left ({@code target}) and
     * right ({@code property}) nodes.
     */
    public PropertyGet(AstNode target, Name property){
        super(target, property);
    }

    public PropertyGet(AstNode target, Name property, int dotPosition){
        super(Token.GETPROP, target, property, dotPosition);
    }

    /**
     * Returns the object on which the property is being fetched.
     * Should never be {@code null}.
     */
    public AstNode getTarget(){
        return getLeft();
    }

    /**
     * Sets target object, and sets its parent to this node.
     * @param target expression evaluating to the object upon which
     * to do the property lookup
     * @throws IllegalArgumentException} if {@code target} is {@code null}
     */
    public void setTarget(AstNode target){
        setLeft(target);
    }

    /**
     * Returns the property being accessed.
     */
    public Name getProperty(){
        return (Name)getRight();
    }

    /**
     * Sets the property being accessed, and sets its parent to this node.
     * @throws IllegalArgumentException} if {@code property} is {@code null}
     */
    public void setProperty(Name property){
        setRight(property);
    }

    @Override
    public String toSource(int depth){
        String sb = makeIndent(depth) +
        getLeft().toSource(0) +
        "." +
        getRight().toSource(0);
        return sb;
    }

    /**
     * Visits this node, the target expression, and the property name.
     */
    @Override
    public void visit(NodeVisitor v){
        if(v.visit(this)){
            getTarget().visit(v);
            getProperty().visit(v);
        }
    }
}
