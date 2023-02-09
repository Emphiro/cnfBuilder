package cnfBuilder;

import java.util.LinkedList;

public class CNF {
    static final char orC = 'v';
    static final char andC = '*';
    static final char notC = '!';
    static final String or = "v";
    static final String and = "*";
    static final String not = "!";
    static final String nnf = "NNF";
    static final String cnf = "CNF";
    static final String forall = "forall";
    static final String exists = "exists";
    boolean isNNF = false;
    boolean isCNF = false;
    boolean verbose = false;

    private LinkedList<String> postfix = new LinkedList<>();
    private PostfixNotation post;
    public CNF(String exp){
        initPost();
        postfix = post.toPostfixNotation(exp);
        postfix.add(cnf);
        postfix.add(nnf);
    }
    
    private void applyNNF(){
        postfix = applyNNF(postfix);
    }
    private LinkedList<String> applyNNF(LinkedList<String> postfix){
        LinkedList<String>[] children = post.getChildren(postfix);
        if(children[children.length-1].getFirst().equals(nnf))
            return nnf(children[0]);

        LinkedList<String> result = new LinkedList<>();
        for (int i = 0; i < children.length; i++) {
            result.addAll(applyNNF(children[i]));
        }
        return result;
    }

    private LinkedList<String> nnf(LinkedList<String> postfix){
        LinkedList<String>[] children = post.getChildren(postfix);
        String op = children[children.length-1].getFirst();
        LinkedList<String> result = new LinkedList<>();
        if(op.equals(not)){
            for (int i = 0; i < children.length - 1; i++){
                result.addAll(applyNot(children[i]));
            }
        }
        return result;
    }

    private LinkedList<String> applyNot(LinkedList<String> postfix){
        return null;
    }

    private void applyCNF(){
        postfix = applyCNF(postfix);
    }
    private LinkedList<String> applyCNF(LinkedList<String> postfix){
        return null;
    }
    
    private void initPost(){
        post = new PostfixNotation();
        post.setOperators(new String[]{or, and,  not});
        post.setLeftAssociative(new String[]{or, and});
        post.setPrecedence(new String[][]{ {or}, {and},{not}});
        post.setDelims(new char[]{notC, orC, andC, ')', '('});
        post.addFunction(cnf, nnf, forall, exists);
        post.addArgumentSeparator(",");
        post.addNumOp(forall, 2);
        post.addNumOp(exists, 2);
    }
}
