package cnfBuilder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

public class NNF {
    static final String not = "!";
    static final String or = " OR ";
    static final String and = " AND ";
    static final String nnf = "NNF";
    static final String cnf = "KNF";
    static final char lor = 'v';
    static final char land = '*';
    static final char lnnf = 'f';
    static final char lcnf = 'k';
    static final char lnot = '!';
    static boolean done = false;

    public static void main(String... args){
        PostfixNotation pol = new PostfixNotation();

        pol.setOperators(new String[]{String.valueOf(lor), String.valueOf(land),  String.valueOf(lnot)});
        pol.setLeftAssociative(new String[]{String.valueOf(lor), String.valueOf(land)});
        pol.setPrecedence(new String[][]{ {String.valueOf(lor)}, {String.valueOf(land)},{String.valueOf(lnot)}});
        pol.setDelims(new char[]{lnot, lor, land , ')', '('});
        pol.addFunction(String.valueOf(lcnf), String.valueOf(lnnf));

        //pol.toPostfixNotation("k(f(!a*!(bvc)))");
        pol.toPostfixNotation("f(!(((AvB)*(CvD))))");
        System.out.print(pol + " - ");
        System.out.println( pol.toInfixNotation());
        /*
        String str = "(!(!(B*(!A*!C))*(((!AvC)*B)v(C*A))))*!C";
        //str = "(((!(B*(!A*!C)))*(((!AvC)*B)v(C*A))))*!C";
        str = "k(f((((!(B*(!A*!C)))*(((!AvC)*B)v(C*A))))*!C))";
        //str = "f(!(((AvB)*(CvD))))";
        //str = "AvC";
        //str = "!!A";
        printToCNF(str);
        printToCNF("k(f((Av!B)*B*!A))");
        printToCNF("k(f((Av!B)*B*!B))");
        */


    }

    static void printToCNF(String str){
        LinkedList<Character> polish = toPolish(str);//
        done = false;
        while (!done) {
            //System.out.printf("Polish  : %s\n", Arrays.toString(polish.toArray()));
            System.out.printf("%s\n", toString(polish));
            polish = apply(polish, lnnf);
        }
        System.out.println();
        done = false;
        while (!done) {
            //System.out.printf("Polish  : %s\n", Arrays.toString(polish.toArray()));
            System.out.printf("%s\n", toString(polish));
            polish = apply(polish, lcnf);
        }
        System.out.println();
        System.out.printf("As set: %s\n",Arrays.deepToString(cnfToSet(polish).toArray()));
        System.out.printf("No doubs: %s\n",Arrays.deepToString(eliminateDoubles(cnfToSet(polish)).toArray()));
        System.out.println();
    }

    static LinkedList<Character> apply(LinkedList<Character> polish, char func){
        switch (func){
            case lnnf: return applyNNF(polish);
            case lcnf: return applyCNF(polish);
            default: throw new UnsupportedOperationException("Unsupported function applied");
        }
    }
    static LinkedList<Character> applyNNF(LinkedList<Character> polish){
        for (int i = 0; i < polish.size(); i++) {
            char cur = polish.get(i);
            if(cur == lnnf){
                polish.remove(i);
                switch(polish.get(i)){
                    case lor: return applyNNForAnd(polish, i, lor);
                    case land: return applyNNForAnd(polish, i, land);
                    case lnot: return applyNNFnot(polish, i);
                    default: return polish;
                }

            }
        }
        done = true;
        return polish;
    }
    static LinkedList<Character> applyNNForAnd(LinkedList<Character> polish, int position, char operation){
        LinkedList<Character> result = truncate(polish, 0 , polish.size());
        int pivot = position+pivot(truncate(polish, position, polish.size()));
        result.remove(position);
        result.add(position, operation);
        result.add(pivot,lnnf);
        result.add(position+1,lnnf);
        return result;
    }
    static LinkedList<Character> applyNNFnot(LinkedList<Character> polish, int position){
        LinkedList<Character> result = truncate(polish, 0 , polish.size());
        result.remove(position);
        switch(result.get(position)){
            case(lnot): result.remove(position); return result;
            case(lor): return applyNNFnotAndOr(result,position, land);
            case(land): return applyNNFnotAndOr(result,position, lor);
            default: result.add(position, lnot);return result;
        }

    }
    static LinkedList<Character> applyNNFnotAndOr(LinkedList<Character> polish, int position, char operation){
        LinkedList<Character> result = truncate(polish, 0 , polish.size());
        int pivot = position+pivot(truncate(polish, position, polish.size()));
        result.remove(position);
        result.add(position, operation);
        result.add(pivot,lnot);
        result.add(pivot,lnnf);
        result.add(position+1,lnot);
        result.add(position+1,lnnf);
        return result;
    }
    static class Literal{
        final boolean bool;
        final char value;
        Literal(char value, boolean bool){
            this.bool = bool;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%s%c", bool?"":not, value);
        }

        @Override
        public boolean equals(Object obj) {
            Literal lit = (Literal) obj;
            return (lit.bool == this.bool) && (lit.value == this.value);
        }
    }
    static LinkedList<LinkedList<Literal>> cnfToSet(LinkedList<Character> polish){
        LinkedList<LinkedList<Literal>> result= new LinkedList<>();
        cnfToSet(polish, result);
        return result;
    }
    static void cnfToSet(LinkedList<Character> polish, LinkedList<LinkedList<Literal>> result){
        if(polish.peek() == land){
            int pivot = pivot(polish);
            cnfToSet(truncate(polish, 1, pivot), result);
            cnfToSet(truncate(polish, pivot, polish.size()), result);
        }else{
            LinkedList<Literal> clause = new LinkedList<>();
            for (int i = 0; i < polish.size(); i++) {
                char c = polish.get(i);
                if(!isOperator(c)){
                    clause.add(new Literal(c, true));
                }
                if(c == lnot){
                    clause.add(new Literal(polish.get(++i), false));
                }
            }
            result.add(clause);
        }
    }
    static LinkedList<LinkedList<Literal>> eliminateDoubles(LinkedList<LinkedList<Literal>> sets){
        LinkedList<LinkedList<Literal>> result = new LinkedList<>();
        for(LinkedList<Literal> clause : sets){
            LinkedList<Literal> newClause = new LinkedList<>();
            for(Literal l : clause){
                if(!contains(l, newClause))
                    newClause.add(l);
            }
            result.add(newClause);
        }
        return result;
    }
    static boolean contains(Literal l, LinkedList<Literal> list){
        for (Literal lit : list){
            if(l.equals(lit))
                return true;
        }
        return false;
    }
    static LinkedList<Character> applyCNF(LinkedList<Character> polish){

        for (int i = 0; i < polish.size(); i++) {
            char cur = polish.get(i);
            if(cur == lcnf){
                int pivot = i+pivot(truncate(polish, i, polish.size()));

                if(!truncate(polish, i, pivot).contains(land)){
                    //System.out.printf("Debug 1:%s\n", Arrays.toString(truncate(polish, i, pivot).toArray()));
                    polish.remove(i);
                    return polish;
                }
                //System.out.printf("Debug polish: %s\n", Arrays.toString(polish.toArray()));
                polish.remove(i);
                switch(polish.get(i)){
                    case lor: return applyCNFor(polish, i);
                    case land: return applyCNFand(polish, i);
                }

            }
        }
        done = true;
        return polish;
    }

    static LinkedList<Character> applyCNFor(LinkedList<Character> polish, int position){
        //System.out.printf("Debug end: %s\n", Arrays.toString(truncate(polish, position, polish.size()).toArray()));
        int end = position+end(truncate(polish, position, polish.size()));
        //System.out.printf("Debug polish: %s\n", Arrays.toString(polish.toArray()));
        LinkedList<Character> left = truncate(polish, 0, position);
        LinkedList<Character> right = truncate(polish, end, polish.size());
        LinkedList<Character> middle = truncate(polish, position, end);
        //System.out.printf("Debug middle: %s\n", Arrays.toString(middle.toArray()));
        int posAnd = -1;
        for (int i = 0; i < middle.size(); i++) {
            char c = middle.get(i);
            if (c == land){
                posAnd = i;
            }
        }
        if(posAnd < 0)
            throw new IllegalArgumentException("applyCNFor() was used on a term that doesnt contain" + and);
        int posPivot = posAnd+pivot(truncate(middle, posAnd, middle.size()));
        int posEnd = posAnd+end(truncate(middle, posAnd, middle.size()));

        LinkedList<Character> k1 = concat(truncate(middle, 0, posAnd), truncate(middle, posAnd+1, posPivot), truncate(middle, posEnd, middle.size()));
        LinkedList<Character> k2 = concat(truncate(middle, 0, posAnd), truncate(middle, posPivot, posEnd), truncate(middle, posEnd, middle.size()));
        k1 = concat(lcnf,k1);
        k2 = concat(lcnf,k2);
        LinkedList<Character> result = concat(land, k1, k2);
        //System.out.printf("Debug result: %s\n", Arrays.toString(result.toArray()));
        return concat(left, result, right);
    }
    static LinkedList<Character> applyCNFand(LinkedList<Character> polish, int position){
        LinkedList<Character> result = truncate(polish, 0 , polish.size());
        int pivot = position+pivot(truncate(polish, position, polish.size()));
        result.add(pivot,lcnf);
        result.add(position+1,lcnf);
        return result;
    }
    static String toString(LinkedList<Character> polish){
        String str1 = new String();
        String str2 = new String();
        String str3 = new String();
        String str4 = new String();
        //System.out.printf("Debug: %s\n", Arrays.toString(polish.toArray()));
        if(polish.size() == 1){
            return str1+(char)polish.peek();
        }
        if(polish.peek() == lnot){
            if(polish.size() > 2){
                str1="(";
                str2=")";
            }
            return not+str1+toString(truncate(polish,1, polish.size()))+str2;
        }
        if (polish.peek() == lnnf || polish.peek() == lcnf){
            String func = polish.peek() == lnnf ? nnf : cnf;
            return func+"("+toString(truncate(polish,1,polish.size())) + ")";
        }
        if(polish.peek() == lor){
            LinkedList v1 = truncate(polish, 1, pivot(polish));
            LinkedList v2 = truncate(polish,pivot(polish), polish.size());
            return ""+toString(v1) + or + toString(v2)+"";
        }
        if(polish.peek() == land){
            LinkedList a1 = truncate(polish, 1, pivot(polish));
            LinkedList a2 = truncate(polish,pivot(polish), polish.size());
            if(a1.contains(lor)){
                str1="(";
                str2=")";
            }
            if(a2.contains(lor)){
                str3="(";
                str4=")";
            }
            return str1+toString(a1)+str2+ and +str3+toString(a2)+str4;
        }
        return str1;
    }
    static LinkedList<Character> concat(LinkedList<Character>... lists){
        LinkedList<Character> result = new LinkedList<>();
        for(LinkedList<Character> list : lists){
            for(Character cha : list){
                result.add(cha);
            }
        }
        return result;
    }

    static LinkedList<Character> concat(char c, LinkedList<Character>... lists){
        LinkedList<Character> result = new LinkedList<>();

        result.add(c);
        for(LinkedList<Character> list : lists){
            for(Character cha : list){
                result.add(cha);
            }
        }
        return result;
    }

    static String fromPolish(LinkedList<Character> polish){
        String str1 = new String();
        String str2 = new String();
        String str3 = new String();
        String str4 = new String();
        if(polish.size() == 1){
            return str1+(char)polish.peek();
        }
        if(polish.peek() == lnot){
            if(polish.size() > 2){
                str1="(";
                str2=")";
            }
            return "!"+str1+fromPolish(truncate(polish,1, polish.size()))+str2;
        }
        if (polish.peek() == lnnf || polish.peek() == lcnf){
            return polish.peek()+"("+fromPolish(truncate(polish,1,polish.size())) + ")";
        }
        if(polish.peek() == lor){
            LinkedList v1 = truncate(polish, 1, pivot(polish));
            LinkedList v2 = truncate(polish,pivot(polish), polish.size());
            return "("+fromPolish(v1) + ")"+lor+"("+ fromPolish(v2)+")";
        }
        if(polish.peek() == land){
            LinkedList a1 = truncate(polish, 1, pivot(polish));
            LinkedList a2 = truncate(polish,pivot(polish), polish.size());
            return "("+fromPolish(a1)+ ")"+land+"("+fromPolish(a2)+")";
        }
        return str1;
    }
    public static boolean isOperator(char token){
        return ((token == land) || (token == lor) || (token == lnot) || (token == lnnf) || (token == lcnf) );
    }
    public static int pivot(LinkedList<Character> polish){
        if(polish.size() == 1){
            return 1;
        }
        LinkedList<Character> list = (LinkedList<Character>) polish.clone();
        list.pop();
        int counter = 1;
        int i = 1;
        for (;; i++) {
            if(counter == 0)
                break;
            char c = list.pop();
            if(c == lnot || c == lnnf || c == lcnf){
                //counter ++;
                continue;
            }
            if(c == lor || c == land){
                counter +=1;
                continue;
            }
            counter --;
            //if(counter <= 0)
            //    break;

        }
        return i;
    }
    public static int end(LinkedList<Character> polish){
        if(polish.size() == 1){
            return 1;
        }
        LinkedList<Character> list = (LinkedList<Character>) polish.clone();
        char first = list.pop();
        int counter = 1;
        if(first == lor || first == land)
            counter = 2;
        int i = 1;
        for (;; i++) {
            if(counter == 0)
                break;
            char c = list.pop();
            if(c == lnot || c == lnnf || c == lcnf){
                //counter ++;
                continue;
            }
            if(c == lor || c == land){
                counter +=1;
                continue;
            }
            counter --;
            //if(counter <= 0)
            //    break;

        }
        return i;
    }

    public static LinkedList<Character> truncate(LinkedList<Character> list, int from, int to){
        LinkedList<Character> result = new LinkedList<>();
        for (int i = from; i < to; i++) {
            result.add(list.get(i));
        }
        return result;
    }
    public static LinkedList<Character> toPolish(String t){
        return  toPolishR(fromPolish(toPolishR(t)));
    }
    public static LinkedList<Character> toPolishR(String t){
        Stack<Character> stack = new Stack<>();
        LinkedList<Character> output = new LinkedList<>();
        char[] arr = t.toCharArray();
        LinkedList<Character> tokens = new LinkedList<Character>();

        for (int i = 0; i < arr.length; i++) {
            tokens.add(arr[i]);
        }
        while(tokens.size() != 0){
            char token = tokens.remove();

            int precedence = 0;
            int stackPrecedence = 0;

            boolean isLeftAssociative = !(token == lnot || (token == lnnf || token == lcnf));
            boolean isLeftParenthese = (token == '(');
            boolean isRightParenthese = (token == ')');
            boolean isFunction = false;
            boolean isOperator = isOperator(token);
            boolean isArgumentSeperator = false;
            boolean isNumber = !(isLeftParenthese || isRightParenthese || isFunction || isOperator || isArgumentSeperator);
            boolean stackIsOperator = false;
            boolean stackIsLeftParenthese = false;
            if (!stack.isEmpty()) {
                char st = stack.peek();
                stackIsOperator = isOperator(st);
                stackIsLeftParenthese = (st == '(');
            }
            //token-precedence | stack-precedence
            {
                if (token == lnot ||(token == lnnf || token == lcnf))
                    stackPrecedence = 4;
                if (token == lor)
                    precedence = 2;
                if (token == land)
                    precedence = 3;

                if(!stack.isEmpty()) {
                    char st = stack.peek();
                    if (st == lnot|| st == lnnf || st == lcnf)
                        stackPrecedence = 4;
                    if (st == lor)
                        precedence = 2;
                    if (st == land)
                        precedence = 3;
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
                        char st = stack.peek();
                        stackIsOperator = isOperator(st);
                        if (st == lnot || (st == lnnf || st == lcnf))
                            stackPrecedence = 4;
                        if (st == lor)
                            precedence = 2;
                        if (st == land)
                            precedence = 3;
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
                        char st = stack.peek();
                        stackIsLeftParenthese = (st == '(');
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
                        char st = stack.peek();
                        stackIsLeftParenthese = (st == '(');
                    }
                }
                stack.pop();
                boolean stackIsFunction = false;
                if(!stack.isEmpty()) {
                    char st = stack.peek();
                    stackIsFunction = false;
                }
                if (stackIsFunction)
                    output.add(stack.pop());
            }

        }
        while(stack.size() != 0){
            boolean stackIsLeftParenthese = false;
            if(!stack.isEmpty()) {
                char st = stack.peek();
                stackIsLeftParenthese = (st == '(');
            }
            if(stackIsLeftParenthese)
                throw new IllegalArgumentException();
            output.add(stack.pop());
        }
        //LinkedList<Character> test = new LinkedList<>();
        //for (int i = 0; i < output.size(); i++) {
        //    test.add(output.get(i));
        //}
        //evaluate(test);
        output = reverse(output);
        return output;
    }

    static LinkedList<Character> reverse(LinkedList<Character> list){
        LinkedList<Character> result = new LinkedList<>();
        for (int i = list.size()-1; i >= 0; i--) {
            result.add(list.get(i));
        }
        return result;
    }

    /*
    static LinkedList<Character> NNF(LinkedList<Character> polish){
        System.out.printf("NNF(%s)\n", toString(polish));
        return NNF(polish,"","");
    }
    static LinkedList<Character> NNF(LinkedList<Character> polish, String s1, String s2){
        char t = polish.peek();
        switch(t){
            case '!':return NNFnot(polish, s1, s2);
            case '*':return NNFand(polish, s1, s2);
            case 'v':return NNFor(polish, s1, s2);
            default:return NNFLiteral(polish, s1, s2);
        }
    }

    private static LinkedList<Character> NNFnot(LinkedList<Character> polish, String s1, String s2) {
        return null;
    }
    private static LinkedList<Character> NNFand(LinkedList<Character> polish, String s1, String s2) {
        int pivot = pivot(polish);
        LinkedList<Character> p1 = truncate(polish, 1, pivot);
        LinkedList<Character> p2 = truncate(polish, pivot, polish.size());
        System.out.printf("%s(NNF(%s)"+and+"NNF(%s))%s\n",s1 , toString(p1), toString(p2),s2);
        String leftOver = ""+and+"NNF("+toString(p2)+"))"+s2+"";
        LinkedList<Character> a1 = NNF(p1, s1+"(", leftOver);
        String carry = s1+toString(a1) + ""+and +"";
        LinkedList<Character> a2 = NNF(p2, carry, ""+s2);
        return(concat('*', a1, a2));
    }
    private static LinkedList<Character> NNFor(LinkedList<Character> polish, String s1, String s2) {
        int pivot = pivot(polish);
        LinkedList<Character> p1 = truncate(polish, 1, pivot);
        LinkedList<Character> p2 = truncate(polish, pivot, polish.size());
        System.out.printf("%s(NNF(%s)"+or+"NNF(%s))%s\n",s1 , toString(p1), toString(p2),s2);
        String leftOver = ""+or+"NNF("+toString(p2)+"))"+s2+"";
        LinkedList<Character> a1 = NNF(p1, s1+"(", leftOver);
        String carry = s1+toString(a1) + ""+or +"";
        LinkedList<Character> a2 = NNF(p2, carry, ""+s2);
        //System.out.printf("%sNNF(%s)"+or+"NNF(%s%s)\n",s1 , toString(p1), toString(p2),s2);
        return(concat('v', a1, a2));
    }
    private static LinkedList<Character> NNFLiteral(LinkedList<Character> polish, String s1, String s2) {
        System.out.printf("%s%s%s\n\n",s1,toString(polish),s2);
        return polish;
    }

     */

    /*

    static void checkValid(String str){
        char[] chr = str.toCharArray();

        int p = 0;
        for (int i = 0; i < chr.length; i++) {
            switch(chr[i]){
                case '(':p++; break;
                case ')':p--; break;
                default: break;
            }
        }
        if(p != 0)
            throw new IllegalArgumentException("U messed up the parentheses! >:|");
    }

    static String NNF(char[] str){
        if(str.length == 1 || str.length == 2)
            return new String(str);
        //if(str[0] == '!' && str[1] != '('){
        //    //System.out.printf("test %s\n", new String(str));
        //    return new String(str);
        //}
        str = trim(str);
        //System.out.printf("test trim: %s%n", new String(str));




        int p = 0;
        String result;
        for (int i = 0; i < str.length; i++) {

            switch(str[i]){
                case '(':p++; break;
                case ')':p--; break;
                case 'v': if (p==0)return NNFor(str, p, i);
                case '*': if (p==0)return NNFand(str, p, i);
                default: break;
            }
        }
        if(str[0] == '!'){
            char[] strn = new char[str.length-1];
            for (int i = 0; i < strn.length; i++) {
                strn[i] = str[i+1];
            }
            char[] strN = trim(Arrays.copyOfRange(str, 1, str.length));
            return NNFnot(strN);
        }
        return new String(str);
    }

    static String NNFnot(char[] str){
        //System.out.printf("Test NNFnot %s\n", new String(str));
        int p = 0;
        String result;
        for (int i = 0; i < str.length; i++) {

            switch(str[i]){
                case '(':p++; break;
                case ')':p--; break;
                case 'v': if(p==0)return NNFNand(str, p, i);
                case '*': if(p==0)return NNFNor(str, p, i);
                default: break;
            }
        }

        if(str[0] == '!'){
            return NNF(Arrays.copyOfRange(str,1,str.length));
        }
        return "!"+new String(str);
    }
    static String NNFor(char[] str, int p, int i){
        char[] str1 = Arrays.copyOfRange(str, 0, i);
        char[] str2 = Arrays.copyOfRange(str, i+1, str.length);
        System.out.printf("OR: NNF(%s) v NNF(%s)\n", new String(str1), new String(str2));

        return "("+NNF(str1) + ")v(" + NNF(str2)+")";
    }
    static String NNFand(char[] str, int p, int i){
        char[] str1 = Arrays.copyOfRange(str, 0, i);
        char[] str2 = Arrays.copyOfRange(str, i+1, str.length);
        //System.out.println("And:");
        System.out.printf("AND: NNF(%s) * NNF(%s)\n", new String(str1), new String(str2));

        return "("+NNF(str1) + ")*(" + NNF(str2)+")";
    }

    static String NNFNor(char[] str, int p, int i){
        char[] str1 = Arrays.copyOfRange(str, 0, i);
        char[] str2 = Arrays.copyOfRange(str, i+1, str.length);
        char[] str1N = new char[str1.length+1];
        str1N[0] = '!';
        //str1N[1] = '(';
        //str1N[str1N.length-1] = ')';
        for (int j = 0; j < str1.length; j++) {
            str1N[j+1] = str1[j];
        }
        char[] str2N = new char[str2.length+1];
        str2N[0] = '!';
        //str2N[1] = '(';
        //str2N[str2N.length-1] = ')';
        for (int j = 0; j < str2.length; j++) {
            str2N[j+1] = str2[j];
        }
        System.out.printf("NOT AND: NNF(%s) v NNF(%s)\n", new String(str1N), new String(str2N));

        return "("+NNF(str1N)+")v(" + NNF(str2N)+")";
    }
    static String NNFNand(char[] str, int p, int i){
        char[] str1 = Arrays.copyOfRange(str, 0, i);
        //System.out.printf("Test str: %s str1: %s\n", new String(str), new String(str1));

        char[] str2 = Arrays.copyOfRange(str, i+1, str.length);
        //System.out.println("And:");
        char[] str1N = new char[str1.length+1];
        str1N[0] = '!';
        //str1N[1] = '(';
        //str1N[str1N.length-1] = ')';
        for (int j = 0; j < str1.length; j++) {
            str1N[j+1] = str1[j];
        }
        char[] str2N = new char[str2.length+1];
        str2N[0] = '!';
        //str2N[1] = '(';
        //str2N[str2N.length-1] = ')';
        for (int j = 0; j < str2.length; j++) {
            str2N[j+1] = str2[j];
        }
        System.out.printf("NOT OR: NNF(%s) * NNF(%s)\n", new String(str1N), new String(str2N));

        return "("+NNF(str1N) + ")*(" + NNF(str2N)+")";
    }

    static char[] trim(char[] str){
        if(str[0] != '(' || str[str.length-1] != ')')
            return str;
        boolean trim = true;
        int p = 1;
        for (int i = 1; i < str.length-1; i++) {
            switch(str[i]){
                case '(':p++; break;
                case ')':p--; break;
                default: break;
            }
            if(p <= 0){
                trim =false;
                break;
            }
        }
        return trim ? Arrays.copyOfRange(str, 1, str.length-1) : str;
    }

     */

}
