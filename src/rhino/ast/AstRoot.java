package rhino.ast;

import rhino.*;

import java.util.*;

/**
 * Node for the root of a parse tree.  It contains the statements and functions
 * in the script, and a list of {@link Comment} nodes associated with the script
 * as a whole.  Node type is {@link Token#SCRIPT}.
 *
 * <p>Note that the tree itself does not store errors. To collect the parse errors
 * and warnings, pass an {@link ErrorReporter} to the
 * {@link Parser} via the
 * {@link CompilerEnvirons}.
 */
public class AstRoot extends ScriptNode{

    private SortedSet<Comment> comments;

    {
        type = Token.SCRIPT;
    }

    public AstRoot(){
    }

    public AstRoot(int pos){
        super(pos);
    }

    /**
     * Returns comment set
     * @return comment set, sorted by start position. Can be {@code null}.
     */
    public SortedSet<Comment> getComments(){
        return comments;
    }

    /**
     * Sets comment list, and updates the parent of each entry to point
     * to this node.  Replaces any existing comments.
     * @param comments comment list.  can be {@code null}.
     */
    public void setComments(SortedSet<Comment> comments){
        if(comments == null){
            this.comments = null;
        }else{
            if(this.comments != null)
                this.comments.clear();
            for(Comment c : comments)
                addComment(c);
        }
    }

    /**
     * Add a comment to the comment set.
     * @param comment the comment node.
     * @throws IllegalArgumentException if comment is {@code null}
     */
    public void addComment(Comment comment){
        assertNotNull(comment);
        if(comments == null){
            comments = new TreeSet<>((n1, n2) -> n1.position - n2.position);
        }
        comments.add(comment);
        comment.setParent(this);
    }

    /**
     * Visits the comment nodes in the order they appear in the source code.
     * The comments are not visited by the {@link #visit} function - you must
     * use this function to visit them.
     * @param visitor the callback object.  It is passed each comment node.
     * The return value is ignored.
     */
    public void visitComments(NodeVisitor visitor){
        if(comments != null){
            for(Comment c : comments){
                visitor.visit(c);
            }
        }
    }

    /**
     * Visits the AST nodes, then the comment nodes.
     * This method is equivalent to calling {@link #visit}, then
     * {@link #visitComments}.  The return value
     * is ignored while visiting comment nodes.
     * @param visitor the callback object.
     */
    public void visitAll(NodeVisitor visitor){
        visit(visitor);
        visitComments(visitor);
    }

    @Override
    public String toSource(int depth){
        StringBuilder sb = new StringBuilder();
        for(Node node : this){
            sb.append(((AstNode)node).toSource(depth));
            if(node.getType() == Token.COMMENT){
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * A debug-printer that includes comments (at the end).
     */
    @Override
    public String debugPrint(){
        DebugPrintVisitor dpv = new DebugPrintVisitor(new StringBuilder(1000));
        visitAll(dpv);
        return dpv.toString();
    }

    /**
     * Debugging function to check that the parser has set the parent
     * link for every node in the tree.
     * @throws IllegalStateException if a parent link is missing
     */
    public void checkParentLinks(){
        this.visit(node -> {
            int type = node.getType();
            if(type == Token.SCRIPT)
                return true;
            if(node.getParent() == null)
                throw new IllegalStateException
                ("No parent for node: " + node
                + "\n" + node.toSource(0));
            return true;
        });
    }
}
