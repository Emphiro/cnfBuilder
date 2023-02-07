package cnfBuilder;

import java.util.*;

public class PostfixNotation {
    private Queue<String> postfixNotation = new LinkedList<>();
    private char[] delims = new char[0];
    private String[] operators = new String[0];
    private String[] leftAssociative = new String[0];
    private String[] functions = new String[0];
    private String[] argumentSeparators = new String[0];
    private String[][] precedences = new String[0][];
    private String leftParentheses = "(";
    private String rightParentheses = ")";
    public PostfixNotation(){
    }

    public PostfixNotation(char[] delims, String[] operators){
        setDelims(delims);
        setOperators(operators);
    }

    public void setDelims(char[] delims) {
        this.delims = delims;
    }

    private boolean isDelim(char token){
        for (char delim : delims){
            if(token == delim)
                return true;
        }
        return false;
    }

    public void setOperators(String[] ops) {
        this.operators = ops;
    }
    private boolean isOperator(String token){
        return contains(operators, token);
    }

    public void setFunction(String[] funcs){
        this.functions = funcs;
    }
    private boolean isFunction(String token){
        return contains(functions, token);
    }

    public void setLeftAssociative(String[] leftAssociative){
        this.leftAssociative = leftAssociative;
    }
    private boolean isLeftAssociative(String token){
        return contains(leftAssociative, token);
    }
    public void setArgumentSeparator(String[] separators){
        argumentSeparators = separators;
    }

    private boolean isArgumentSeparator(String token){
        return contains(argumentSeparators, token);
    }

    public void setPrecedence(String[]... precedences){
        this.precedences = precedences;
    }

    private boolean isLiteral(String token){
        return !(token.equals(leftParentheses) || token.equals(rightParentheses) || isFunction(token) || isOperator(token) || isArgumentSeparator(token));
    }

    private int getPrecedence(String token){
        int precedence = 0;
        for (int i = 0; i < precedences.length; i++) {
            if(contains(precedences[i], token)){
                precedence = i+1;
                return precedence;
            }
        }
        return precedence;
    }

    private static boolean contains(String[] array, String token){
        for (String el : array){
            if(token.equals(el))
                return true;
        }
        return false;
    }

    public void test(){
        System.out.printf("pre * :%s pre v :%s\n",getPrecedence("*"), getPrecedence("v"));
    }



    public LinkedList<String> tokenize(String t){
        if(t == null || t.length() == 0) {
            return new LinkedList<String>();
        }
        LinkedList<String> result = new LinkedList<>();
        LinkedList<Integer> posT = new LinkedList<>();
        t = t.replaceAll(" ", "");
        char[] chars = t.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if(isDelim(chars[i]))
                posT.add(i);
        }
        if (posT.size() == 0){
            result.add(t);
            return result;
        }
        int pos0 = (int) posT.get(0).doubleValue();
        if(posT.get(0) != 0)
            result.add(t.substring(0, pos0));
        result.add(String.valueOf(chars[posT.get(0)]));

        for (int i = 1; i < posT.size(); i++) {
            int pos1 = posT.get(i);
            if (pos0 + 1 != pos1)
                result.add(t.substring(pos0 + 1, pos1));
            result.add(String.valueOf(chars[pos1]));
            pos0 = pos1;
        }
        if(posT.getLast() != chars.length - 1 )
            result.add(t.substring((posT.getLast() + 1), chars.length));
        return result;


    }
    public String toString(){
        return Arrays.toString(postfixNotation.toArray(new String[0]));
    }

    public String toInfixNotation(){
        LinkedList<String> postfix = new LinkedList<>(postfixNotation);
        return toInfixNotation(postfix);
    }
    public String toInfixNotation(LinkedList<String> postfix){
        String result = "";
        if(postfix.size() == 1 || isLiteral(postfix.getLast())){
            return postfix.removeLast();
        }

        String op = postfix.removeLast();
        if(isLeftAssociative(op)){
            String rightSide = toInfixNotation(postfix) + result;
            String leftSide  = toInfixNotation(postfix)+ result;
            result = leftSide + op + rightSide;
            result =  leftParentheses + result + rightParentheses;
        } else {
            result += op + leftParentheses + toInfixNotation(postfix) + rightParentheses;
        }

        return result;
    }
    public Queue<String> toPostfixNotation(String t){
        Stack<String> stack = new Stack<>();
        LinkedList<String> output = new LinkedList<>();
        LinkedList<String> tokens = tokenize(t);
        while(tokens.size() != 0){
            String token = tokens.remove();

            int precedence = 0;
            int stackPrecedence = 0;

            boolean isLeftAssociative = isLeftAssociative(token);
            boolean isLeftParentheses = token.equals(leftParentheses);
            boolean isRightParentheses = token.equals(rightParentheses);
            boolean isFunction = isFunction(token);
            boolean isOperator = isOperator(token);
            boolean isArgumentSeparator = isArgumentSeparator(token);
            boolean isLiteral = isLiteral(token);
            boolean stackIsOperator = false;
            boolean stackIsLeftParentheses = false;

            if (!stack.isEmpty()) {
                String st = stack.peek();
                stackIsOperator = isOperator(st);
                stackIsLeftParentheses = st.equals(leftParentheses);
            }
            //token-precedence | stack-precedence
            {
                precedence = getPrecedence(token);

                if(!stack.isEmpty()) {
                    String st = stack.peek();
                    stackPrecedence = getPrecedence(st);
                }
            }

            if(isLiteral) {
                output.add(token);
            }
            if(isFunction) {
                stack.push(token);
            }

            if(isOperator){
                while((stack.size() != 0 && stackIsOperator && isLeftAssociative && precedence <= stackPrecedence)){
                    output.add(stack.pop());
                    stackPrecedence = 0;
                    stackIsOperator = false;
                    if(!stack.isEmpty()) {
                        String st = stack.peek();
                        stackIsOperator = isOperator(st);
                        stackPrecedence = getPrecedence(st);
                    }
                }
                stack.push(token);
            }
            if(isArgumentSeparator){
                while(!(stackIsLeftParentheses)){
                    if(stack.size() == 0)
                        throw new IllegalArgumentException();
                    output.add(stack.pop());
                    stackIsLeftParentheses = false;
                    if(!stack.isEmpty()) {
                        String st = stack.peek();
                        stackIsLeftParentheses = st.equals(leftParentheses);
                    }
                }
            }
            if(isLeftParentheses){
                stack.push(token);
            }
            if(isRightParentheses){
                while (!(stackIsLeftParentheses)){
                    if(stack.size() == 0)
                        throw new IllegalArgumentException();
                    output.add(stack.pop());
                    stackIsLeftParentheses = false;
                    if(!stack.isEmpty()) {
                        String st = stack.peek();
                        stackIsLeftParentheses = st.equals(leftParentheses);
                    }
                }
                stack.pop();
                boolean stackIsFunction = false;
                if(!stack.isEmpty()) {
                    String st = stack.peek();
                    stackIsFunction = isFunction(st);
                }
                if (stackIsFunction)
                    output.add(stack.pop());
            }

        }
        while(stack.size() != 0){
            boolean stackIsLeftParentheses = false;
            if(!stack.isEmpty()) {
                String st = stack.peek();
                stackIsLeftParentheses = st.equals(leftParentheses);
            }
            if(stackIsLeftParentheses)
                throw new IllegalArgumentException();
            output.add(stack.pop());
        }

        postfixNotation = output;
        return output;
    }
}
