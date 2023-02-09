package cnfBuilder;

import java.util.*;

public class PostfixNotation {
    private LinkedList<String> postfixNotation = new LinkedList<>();
    private char[] delims = new char[0];
    private String[] operators = new String[0];
    private String[] leftAssociative = new String[0];
    private String[] functions = new String[0];
    private String[] argumentSeparators = new String[0];
    private String[][] precedences = new String[0][];

    private String[] leftParentheses = new String[]{"("};
    private String[] rightParentheses = new String[]{")"};
    private HashMap<String, Integer> numOps = new HashMap<>();
    public PostfixNotation(){
    }

    public PostfixNotation(char[] delims, String[] operators){
        setDelims(delims);
        setOperators(operators);
    }

    public void setDelims(char[] delims) {
        if(delims == null)
            return;
        this.delims = delims;
    }
    public void addDelims(char... delims) {
        char[] result = new char[this.delims.length+delims.length];
        for (int i = 0; i < this.delims.length; i++) {
            result[i] = this.delims[i];
        }
        for (int j = 0; j < delims.length; j++) {
            result[this.delims.length+j] = delims[j];
        }
        this.delims = result;
    }

    private boolean isDelim(char token){
        for (char delim : delims){
            if(token == delim)
                return true;
        }
        return false;
    }

    public void setOperators(String[] ops) {
        if(ops == null)
            return;
        this.operators = ops;
    }
    private boolean isOperator(String token){
        return contains(operators, token);
    }
    public void addOperator(String... ops){
        this.operators = add(this.operators, ops);
    }

    public void setFunction(String[] funcs){
        if(funcs == null)
            return;
        this.functions = funcs;
    }
    public void addFunction(String... func){
        this.functions = add(this.functions, func);
    }
    private boolean isFunction(String token){
        return contains(functions, token);
    }
    public int getNumOp(String token){
        if(numOps.containsKey(token))
            return numOps.get(token);
        if(contains(leftAssociative, token))
            return 2;
        if(!isLiteral(token))
            return 1;
        return 0;
    }
    public void addNumOp(String op, int num){
        numOps.put(op, num);
    }
    public void setNumOp(HashMap<String, Integer> numOps){
        this.numOps = numOps;
    }


    public void setLeftAssociative(String[] leftAssociative){
        if(leftAssociative == null)
            return;
        this.leftAssociative = leftAssociative;
    }
    public void addLeftAssociative(String... leftAssociative){
        add(this.leftAssociative, leftAssociative);
    }
    private boolean isLeftAssociative(String token){
        return contains(leftAssociative, token);
    }


    public void setArgumentSeparator(String[] separators){
        if(separators == null)
            return;
        this.argumentSeparators = separators;
    }
    public void addArgumentSeparator(String ... separators){
        this.argumentSeparators = add(this.argumentSeparators, separators);
    }
    private boolean isArgumentSeparator(String token){
        return contains(argumentSeparators, token);
    }
    private String getArgumentSeparator(){
        if (argumentSeparators.length > 0)
            return argumentSeparators[0];
        return "";
    }
    public void setLeftParentheses(String[] leftParentheses){
        if(leftParentheses == null)
            return;
        this.leftParentheses = leftParentheses;
    }
    public void addLeftParentheses(String... leftParentheses){
        add(this.leftParentheses, leftParentheses);
    }
    private boolean isLeftParentheses(String token){
        return contains(leftParentheses, token);
    }
    private String getLeftParenthesis(){
        if (leftParentheses.length > 0)
            return leftParentheses[0];
        return "";
    }
    public void setRightParentheses(String[] rightParentheses){
        if(rightParentheses == null)
            return;
        this.rightParentheses = rightParentheses;
    }
    public void addRightParentheses(String... rightParentheses){
        add(this.rightParentheses, rightParentheses);
    }
    private boolean isRightParentheses(String token){
        return contains(rightParentheses, token);
    }
    private String getRightParenthesis(){
        if (rightParentheses.length > 0)
            return rightParentheses[0];
        return "";
    }

    public void setPrecedence(String[]... precedences){
        this.precedences = precedences;
    }

    private boolean isLiteral(String token){
        return !(isLeftParentheses(token) || isRightParentheses(token)|| isFunction(token) || isOperator(token) || isArgumentSeparator(token));
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

    private static String[] add(String[] arr1, String[] arr2){
        String[] result = new String[arr1.length+arr2.length];
        for (int i = 0; i < arr1.length; i++) {
            result[i] = arr1[i];
        }
        for (int j = 0; j < arr2.length; j++) {
            result[arr1.length+j] = arr2[j];
        }
        return result;
    }

    private static boolean contains(String[] array, String token){
        for (String el : array){
            if(token.equals(el))
                return true;
        }
        return false;
    }

    public void test(){
        System.out.println("test " + Arrays.toString(argumentSeparators));
        LinkedList<String>[] children = getChildren();
        for (LinkedList<String> child : children){
            System.out.println(Arrays.toString(child.toArray(new String[0])));
        }
        System.out.printf("pre * :%s pre v :%s\n",getPrecedence("*"), getPrecedence("v"));
    }



    private LinkedList<String> tokenize(String t){
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
        int pos0 = posT.get(0);
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
    public static LinkedList<String> combineLists(LinkedList<String>[] list){
        LinkedList<String> result = new LinkedList<>();
        for (int i = 0; i < list.length; i++) {
            result.addAll(list[i]);
        }
        return result;
    }

    public LinkedList<String>[] getChildren(){
        return getChildren(postfixNotation);
    }

    public LinkedList<String>[] getChildren(LinkedList<String> postfix){
        postfix = new LinkedList<>(postfix);
        String op = postfix.removeLast();
        LinkedList<String>[] result = new LinkedList[1+getNumOp(op)];
        result[getNumOp(op)] = new LinkedList<>();
        result[getNumOp(op)].add(op);
        for (int i = 1; i <= getNumOp(op); i++) {
            result[result.length-i-1] = getChild(postfix);
        }
        return result;
    }

    private LinkedList<String> getChild(LinkedList<String> postfix){
        LinkedList<String> result = new LinkedList<>();
        for(int counter = 1; counter > 0; counter --){
            String nextToken = postfix.removeLast();
            counter += getNumOp(nextToken);
            result.addFirst(nextToken);
        }
        return result;
    }

    public String toString(){
        return Arrays.toString(postfixNotation.toArray(new String[0]));
    }

    public String toInfixNotation(){
        return toInfixNotation(postfixNotation);
    }
    public String toInfixNotation(LinkedList<String> postfix){
        postfix = new LinkedList<>(postfix);
        return toInfixNotationR(postfix);
    }
    public void setPostfixNotation(LinkedList<String> postfix){
        postfixNotation = postfix;
    }
    public void setPostfixNotation(LinkedList<String>... postfix){
        LinkedList<String> result = postfix[0];
        for (int i = 1; i < postfix.length; i++) {
            result.addAll(postfix[i]);
        }
        setPostfixNotation(result);
    }
    private String toInfixNotationR(LinkedList<String> postfix){
        String result = "";
        if(postfix.size() == 1 || isLiteral(postfix.getLast())){
            return postfix.removeLast();
        }

        String op = postfix.removeLast();
        if(isLeftAssociative(op)){
            String opR = postfix.getLast();
            String rightSide = toInfixNotationR(postfix);
            String opL = postfix.getLast();
            String leftSide  = toInfixNotationR(postfix);
            if(!isLiteral(opR) && getPrecedence(op) > getPrecedence(opR))
                rightSide = getLeftParenthesis() + rightSide + getRightParenthesis();
            if(!isLiteral(opL) && getPrecedence(op) > getPrecedence(opL))
                leftSide = getLeftParenthesis() + leftSide + getRightParenthesis();
            result = leftSide + op + rightSide;
        } else if(isFunction(op)){
            String middle = "";
            for (int i = 0; i < getNumOp(op); i++) {
                middle = toInfixNotationR(postfix) + middle;
                if(i+1 < getNumOp(op))
                    middle = getArgumentSeparator() + middle;
            }
            result = op + getLeftParenthesis() + middle + getRightParenthesis();
        } else {
            String opR = postfix.getLast();
            result = toInfixNotationR(postfix);
            if(!isLiteral(opR) && getPrecedence(op) > getPrecedence(opR))
                result = getLeftParenthesis() + result + getRightParenthesis();
            result = op + result;
        }

        return result;
    }
    public LinkedList<String> toPostfixNotation(String t){
        Stack<String> stack = new Stack<>();
        LinkedList<String> output = new LinkedList<>();
        LinkedList<String> tokens = tokenize(t);
        while(tokens.size() != 0){
            String token = tokens.remove();

            int precedence = 0;
            int stackPrecedence = 0;

            boolean isLeftAssociative = isLeftAssociative(token);
            boolean isLeftParentheses = isLeftParentheses(token);
            boolean isRightParentheses = isRightParentheses(token);
            boolean isFunction = isFunction(token);
            boolean isOperator = isOperator(token);
            boolean isArgumentSeparator = isArgumentSeparator(token);
            boolean isLiteral = isLiteral(token);
            boolean stackIsOperator = false;
            boolean stackIsLeftParentheses = false;

            if (!stack.isEmpty()) {
                String st = stack.peek();
                stackIsOperator = isOperator(st);
                stackIsLeftParentheses = isLeftParentheses(st);
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
                        stackIsLeftParentheses = isLeftParentheses(st);
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
                        stackIsLeftParentheses = isLeftParentheses(st);
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
                stackIsLeftParentheses = isLeftParentheses(st);
            }
            if(stackIsLeftParentheses)
                throw new IllegalArgumentException();
            output.add(stack.pop());
        }

        postfixNotation = output;
        return output;
    }
}
