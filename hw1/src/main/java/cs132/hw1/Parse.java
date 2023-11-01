package cs132.hw1;

import java.util.Scanner;

import static cs132.hw1.Parse.Expression.*;
import static cs132.hw1.Parse.Terminal.*;

public class Parse {

    enum Terminal {
        NUM,       // {0, 1, ..., 9}
        BINOP,     // {+ -}
        CONCAT,    // {<empty>}
        L_PARENS,  // {(}
        R_PARENS,  // {)}
        FIELD_REF, // {$}
        INCROP,    // {++ --}
        EOF,       // {EOF}
        ERR        // NOT IN GRAMMAR
    }

    static void S() {
        C(); eat(EOF);
        System.out.println("_");
    }

    static void C() {
        E(); CPrime();
    }

    static void CPrime() {
        if (currToken == INCROP || currToken == FIELD_REF ||
            currToken == L_PARENS || currToken == NUM) { // C' -> concat E C'
            eat(CONCAT); E(); CPrime();
        }
        // default: C' -> ε
    }

    static void E() {
        T(); EPrime();
    }

    static void EPrime() {
        if (currToken == BINOP) { // E' -> binop T E'
            String binop = currLiteral;
            eat(BINOP); T();
            System.out.print(binop + " ");

            EPrime();
        }
        // default: E' -> ε
    }

    // Pre-increment operation
    static void T() {
        if (currToken == INCROP) { // T -> incrop T
            String incrop = currLiteral;
            eat(INCROP);
            T();
            System.out.print(incrop + "_ ");
        } else {
            U(); // default: T -> U
        }
    }

    // Post-increment operation
    static void U() {
        L(); UPrime(); // U -> L U'
    }

    static void UPrime() {
        if (currToken == INCROP) { // U' -> incrop U'
            System.out.print("_" + currLiteral + " ");
            eat(INCROP); UPrime();
        }
        // default: U' -> ε
    }

    static void L() {
        if (currToken == FIELD_REF) { // L -> $L
            eat(FIELD_REF); L();
            System.out.print("$" + " ");
        } else {
            P(); // default: L -> P
        }
    }

    static void P() {
        if (currToken == L_PARENS) { // P -> ( C )
            eat(L_PARENS); C(); eat(R_PARENS);
        } else if (currToken == NUM) { // P -> num
            System.out.print(currLiteral + " ");
            eat(NUM);
        }
    }

    public static void parse(String awkExpr) {
        Expression.init(awkExpr);
        S();

        System.out.println("Expression parsed successfully");
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        StringBuilder awkExpr = new StringBuilder();
        while (scan.hasNextLine()) {
            awkExpr.append(scan.nextLine());
            if (scan.hasNextLine()) {
                // if we've reached EOF, don't append \n
                awkExpr.append("\n");
            }
        }

        parse(awkExpr.toString());
    }

    static class Expression {
        static String awkExpr;     // The AWK expression we want to parse
        static Terminal currToken; // The current token we're parsing in the AWK expression
        static String currLiteral; // The literal value of the current token we're parsing in the AWK expression

        private static int currIndex = -1;  // The index we are at in the AWK expression
        private static int currLineNum = 1;        // The current line number we're on

        static void error() {
            System.out.println("Parse error in line " + currLineNum);
            System.exit(1);
        }

        static void advance() {
            currToken = getToken();

            if (currToken == ERR) {
                error();
            }
        }

        static void eat(Terminal type) {
            if (type == CONCAT) {
                // Doesn't eat anything, continue
                return;
            }

            if (currToken == type) {
                advance();
            } else {
                error();
            }
        }

        static boolean hasMoreTokens() {
            return currIndex + 1 < awkExpr.length();
        }

        private static Terminal getToken() {
            if (!hasMoreTokens()) {
                return EOF;
            }
            int newIdx = currIndex + 1;

            // Discard tabs/spaces/newlines/comments
            char newTok;
            while (true) {
                newTok = awkExpr.charAt(newIdx);
                if (newTok == ' ' || newTok == '\t') {
                    newIdx += 1;
                } else if (newTok == '\n') {
                    currLineNum += 1;
                    newIdx += 1;
                } else if (newTok == '#') {
                    // Ignore everything in the comment, skip to next newline
                    int idxNextNewline = awkExpr.indexOf("\n", currIndex);

                    currLineNum += 1;
                    newIdx = idxNextNewline + 1;
                } else {
                    break;
                }
            }

            currIndex = newIdx;
            currLiteral = String.valueOf(newTok);
            if (newTok == '+' || newTok == '-') {
                char followingTok = awkExpr.charAt(newIdx + 1);
                if (followingTok == newTok) {
                    // This is an INCR or DECR operation
                    currIndex += 1;
                    currLiteral += followingTok;
                    return INCROP;
                }
                // This is a binary PLUS or MINUS operation
                return BINOP;
            } else if (Character.isDigit(newTok)) {
                return NUM;
            } else if (newTok == '(') {
                return L_PARENS;
            } else if (newTok == ')') {
                return R_PARENS;
            } else if (newTok == '$') {
                return FIELD_REF;
            }

            return ERR;
        }

        static void init(String that) {
            awkExpr = that;
            advance();
        }
    }

}