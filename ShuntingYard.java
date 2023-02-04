import java.util.*;

public class ShuntingYard {
    public static LinkedList<String> tokenize(String t){
        if(t == null || t.length() == 0) {
            return new LinkedList<String>();
        }
        LinkedList<String> result = new LinkedList<>();
        LinkedList<Double> posT = new LinkedList<>();
        t = t.replaceAll(" ", "");
        char[] chars = t.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if(chars[i]  == '(' || chars[i]  == ')' || chars[i]  == '+' || chars[i]  == '-' || chars[i]  == '*' || chars[i]  == '/' || chars[i]  == '\u221A' || chars[i] == ',')
                posT.add(i+0d);
        }
        if (posT.size() == 0){
            result.add(t);
            return result;
        }
        int pos0 = (int) posT.get(0).doubleValue();
        if(posT.get(0) != 0)
            result.add(t.substring(0, pos0));
        result.add(String.valueOf(chars[(int)posT.get(0).doubleValue()]));

        for (int i = 1; i < posT.size(); i++) {
            int pos1 = (int) posT.get(i).doubleValue();
            if (pos0 + 1 != pos1)
                result.add(t.substring(pos0 + 1, pos1));
            result.add(String.valueOf(chars[pos1]));
            pos0 = pos1;
        }
        if((int)(posT.getLast() + 0) != chars.length - 1 )
            result.add(t.substring((int)(posT.getLast() + 1), chars.length));
        return result;


    }

    public static Queue<String> toReversePolishNotation(String t){
        Stack<String> stack = new Stack<>();
        LinkedList<String> output = new LinkedList<>();
        LinkedList<String> tokens = tokenize(t);
        while(tokens.size() != 0){
            String token = tokens.remove();

            int precedence = 0;
            int stackPrecedence = 0;

            boolean isLeftAssociative = !token.equals("\u221A");
            boolean isLeftParenthese = token.equals("(");
            boolean isRightParenthese = token.equals(")");
            boolean isFunction = token.equals("pow");
            boolean isOperator = (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("\u221A"));
            boolean isArgumentSeperator = token.equals(",");
            boolean isNumber = !(isLeftParenthese || isRightParenthese || isFunction || isOperator || isArgumentSeperator);
            boolean stackIsOperator = false;
            boolean stackIsLeftParenthese = false;

            if (!stack.isEmpty()) {
                String st = stack.peek();
                stackIsOperator = (st.equals("+") || st.equals("-") || st.equals("*") || st.equals("/") || st.equals("\u221A"));
                stackIsLeftParenthese = st.equals("(");
            }
            //token-precedence | stack-precedence
            {
                if (token.equals("\u221A"))
                    precedence = 4;
                if (token.equals("+") || token.equals("-"))
                    precedence = 2;
                if (token.equals("*") || token.equals("/"))
                    precedence = 3;

                if(!stack.isEmpty()) {
                    String st = stack.peek();
                    if (st.equals("\u221A"))
                        stackPrecedence = 4;
                    if (st.equals("+") || st.equals("-"))
                        stackPrecedence = 2;
                    if (st.equals("*") || st.equals("/"))
                        stackPrecedence = 3;
                }
            }

            if(isNumber){
                try {
                    Double.valueOf(token);
                } catch(NumberFormatException e){
                    throw new IllegalArgumentException();
                }
            }

            if(isNumber) {
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
                        stackIsOperator = (st.equals("+") || st.equals("-") || st.equals("*") || st.equals("/") || st.equals("\u221A"));
                        if (st.equals("\u221A"))
                            stackPrecedence = 4;
                        if (st.equals("+") || st.equals("-"))
                            stackPrecedence = 2;
                        if (st.equals("*") || st.equals("/"))
                            stackPrecedence = 3;
                    }
                }
                stack.push(token);
            }
            if(isArgumentSeperator){
                while(!(stackIsLeftParenthese)){
                    if(stack.size() == 0)
                        throw new IllegalArgumentException();
                    output.add(stack.pop());
                    stackIsLeftParenthese = false;
                    if(!stack.isEmpty()) {
                        String st = stack.peek();
                        stackIsLeftParenthese = st.equals("(");
                    }
                }
            }
            if(isLeftParenthese){
                stack.push(token);
            }
            if(isRightParenthese){
                while (!(stackIsLeftParenthese)){
                    if(stack.size() == 0)
                        throw new IllegalArgumentException();
                    output.add(stack.pop());
                    stackIsLeftParenthese = false;
                    if(!stack.isEmpty()) {
                        String st = stack.peek();
                        stackIsLeftParenthese = st.equals("(");
                    }
                }
                stack.pop();
                boolean stackIsFunction = false;
                if(!stack.isEmpty()) {
                    String st = stack.peek();
                    stackIsFunction = st.equals("pow");
                }
                if (stackIsFunction)
                    output.add(stack.pop());
            }

        }
        while(stack.size() != 0){
            boolean stackIsLeftParenthese = false;
            if(!stack.isEmpty()) {
                String st = stack.peek();
                stackIsLeftParenthese = st.equals("(");
            }
            if(stackIsLeftParenthese)
                throw new IllegalArgumentException();
            output.add(stack.pop());
        }
        LinkedList<String> test = new LinkedList<>();
        for (int i = 0; i < output.size(); i++) {
            test.add(output.get(i));
        }
        evaluate(test);
        return output;
    }

    public static double evaluate(Queue<String> polish){
        if(polish == null || polish.size() == 0)
            return 0;
        if(polish.size() == 1) {
            try {
                return Double.valueOf(polish.peek());
            } catch(NumberFormatException e){
                throw new IllegalArgumentException();
            }
        }
        Stack<Double> stack = new Stack<>();
        int length = polish.size();
        for (int i = 0; i < length; i++) {
            String token = polish.poll();
            boolean isFunction = token.equals("pow");
            boolean isOperator = (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("\u221A"));
            boolean isNumber = !(isFunction || isOperator);
            if(isNumber){
                double value;
                try {
                    value =  Double.valueOf(token);
                } catch(NumberFormatException e){
                    throw new IllegalArgumentException();
                }
                stack.push(value);
            } else {
                if (token.equals("\u221A")) {
                    if (stack.size() < 1)
                        throw new IllegalArgumentException();
                    stack.push(Math.sqrt(stack.pop()));
                } else {
                    if (stack.size() < 2)
                        throw new IllegalArgumentException();
                    double stack1 = stack.pop();
                    double stack2 = stack.pop();
                    switch (token) {
                        case "+":
                            stack.push(stack2 + stack1);
                            break;
                        case "-":
                            stack.push(stack2 - stack1);
                            break;
                        case "*":
                            stack.push(stack2 * stack1);
                            break;
                        case "/":
                            stack.push(stack2 / stack1);
                            break;
                        case "pow":
                            stack.push(Math.pow(stack2, stack1));
                            break;
                    }
                }
            }
        }
        if (stack.size() != 1)
            throw new IllegalArgumentException();

        return stack.pop();
    }


}
