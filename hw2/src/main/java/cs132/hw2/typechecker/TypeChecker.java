package cs132.hw2.typechecker;

import cs132.hw2.syntaxtree.ClassDeclaration;
import cs132.hw2.syntaxtree.ClassExtendsDeclaration;
import cs132.hw2.syntaxtree.Identifier;
import cs132.hw2.syntaxtree.MainClass;
import cs132.hw2.syntaxtree.MethodDeclaration;
import cs132.hw2.syntaxtree.Type;
import cs132.hw2.syntaxtree.VarDeclaration;
//import cs132.hw2.typechecker.old.Table;
import cs132.hw2.visitor.DepthFirstVisitor;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO -- Questions
 * 1. How do you implement scoping? The way the book suggests is that you do 2 passes:
 *    First pass - construct symbol table
 *    Second pass - do type checking
 *    If this is the case, based on the beginScope/endScope implementation suggestion,
 *    you would wind up with an empty SymbolTable: "page 110 -- To implement endScope,
 *    symbols are popped off the stack down to and including the topmost marker. As
 *    each symbol is popped, the head binding in its bucket is removed"
 * 2. What is the structure of a program's symbol table?
 *    page 111 -- Previously, it was implied that a symbol table contained all of the vars
 *    along with their typing with some scope associated with them. However, p. 111
 *    implies that you just store one entry in a symbol table per class, then each
 *    class symbol maps to their own symbol tables down until you reach method params/locals.
 *    What is the structure here?
 *    "The two class names B and C are each mapped to two tables for fields and methods.
 * 3. What is this `currMethod`/`currClass` information in the visitor? p 112
 *    I assume this is used for scoping as you're using the constructed symboltable in phase two?
 *    Also, wouldn't there be a notion of a Block for MiniJava too? What about scoping for that?
 */


/**
 * I have a complete parser for MiniJava and a set of classes used for traversing the Abstract Syntax Tree.
 * I also have two different default Visitors: DepthFirstVisitor and GJDepthFirst.
 * I need to extend these two visitors in order to do the homework.
 *
 * should print either "Program type checked successfully" or "Type error".
 */
public class TypeChecker extends DepthFirstVisitor {
//    Table symbolTable = new Table();

    // ****************************
    // ***** Logging Methods ******
    // ****************************
    private static final String MSG_ERR = "Type error.";
    private static final String MSG_SUCC = "Program type checked successfully.";

    private static void printInfo(String msg) {
        System.out.println("[INFO] TypeChecker: " + msg);
    }

    private static void printError(String msg) {
        System.err.println("[ERR] TypeChecker: " + msg);
    }

    private static void printTypeCheckSuccess() {
        printInfo(MSG_SUCC);
    }

    private static void printTypeCheckError() {
        printInfo(MSG_ERR);
    }

    // ******************************************************************************************************
    // ****** Helper Methods -- https://web.cs.ucla.edu/~palsberg/course/cs132/miniJava-typesystem.pdf ******
    // ******************************************************************************************************

    /**
     * 6.1 The `classname` Helper Function.
     * The function classname returns the name of a class.
     * <p>
     * Definition (5).
     */
    private static Identifier classname(MainClass c) {
        return c.f1;
    }

    /**
     * 6.1 The Classname Helper Function.
     * The function classname returns the name of a class.
     * <p>
     * Definition (6).
     */
    private static Identifier classname(ClassDeclaration c) {
        return c.f1;
    }

    /**
     * 6.1 The Classname Helper Function.
     * The function classname returns the name of a class.
     * <p>
     * Definition (7).
     */
    private static Identifier classname(ClassExtendsDeclaration c) {
        return c.f1;
    }

    /**
     * 6.2 The `linkSet` Helper Function.
     * The function `linkSet` returns the connection between a class and its
     * superclass (represented as a singleton set with one pair of class names),
     * or the emptyset if a class has no superclass.
     * <p>
     * Definition (8).
     */
    private static Set<Pair<Identifier, Identifier>> linkSet(MainClass c) {
        return Collections.emptySet();
    }

    /**
     * 6.2 The `linkSet` Helper Function.
     * The function `linkSet` returns the connection between a class and its
     * superclass (represented as a singleton set with one pair of class names),
     * or the emptyset if a class has no superclass.
     * <p>
     * Definition (9).
     */
    private static Set<Pair<Identifier, Identifier>> linkSet(ClassExtendsDeclaration c) {
        return Collections.singleton(new Pair<>(c.f1, c.f3));
    }

    /**
     * 6.2 The `linkSet` Helper Function.
     * The function `linkSet` returns the connection between a class and its
     * superclass (represented as a singleton set with one pair of class names),
     * or the emptyset if a class has no superclass.
     * <p>
     * Definition (10).
     */
    private static Set<Pair<Identifier, Identifier>> linkSet(ClassDeclaration c) {
        return Collections.emptySet();
    }

    /**
     * 6.3 The `methodName` Helper Function.
     * The function `methodName` returns the name of a method definition.
     * <p>
     * Definition (11).
     */
    private static Identifier methodName(MethodDeclaration m) {
        return m.f2;
    }

    /**
     * 6.4 The `distinct` Helper Function.
     * The `distinct` function checks that the identifiers in a list are pairwise distinct.
     * <p>
     * Definition (12).
     */
    private static boolean distinct(List<Identifier> ids) {
        Set<Identifier> s = new HashSet<>(ids);

        // No duplicates if set size is equal to list size
        return s.size() == ids.size();
    }

    /**
     * 6.5 The `acyclic` Helper Function.
     * The `acyclic` function checks that a set of pairs contains no cycles.
     * <p>
     * Definition (13).
     */
    protected static boolean acyclic(Set<Pair<Identifier, Identifier>> pairSet) {
        Set<Pair<Identifier, Identifier>> pairs = new HashSet<>(pairSet);

        for (Pair<Identifier, Identifier> parent : pairs) {
            // Find any inheritance chains
            Set<Identifier> firsts = new HashSet<>();
            firsts.add(parent.getValue0());

            for (Pair<Identifier, Identifier> possibleChild : pairs) {
                if (!parent.equals(possibleChild) &&
                        !possibleChild.getValue0().equals(possibleChild.getValue1()) &&
                        parent.getValue1().equals(possibleChild.getValue0())) {
                    // This is a continuation of the chain
                    parent = possibleChild;
                    firsts.add(possibleChild.getValue0());
                    if (firsts.contains(possibleChild.getValue1())) {
                        // We've found a cycle
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 6.6 The `fields` Helper Function.
     * We use the notation `fields(C)` to denote a type environment constructed from the fields
     * of `C` and the fields of the superclasses of `C`. The fields in `C` take precedence over
     * the fields in the superclasses of `C`.
     * <p>
     * Definition (14).
     */
    private List<VarDeclaration> fieldsTODO(Identifier cId) {
        ArrayList<VarDeclaration> varDeclarations = new ArrayList<>();

        // TODO -- get the ClassDeclaration
//        for (Node v : c.f3.nodes) {
//            varDeclarations.add((VarDeclaration) v);
//        }

        return varDeclarations;
    }

    /**
     * 6.6 The `fields` Helper Function.
     * We use the notation `fields(C)` to denote a type environment constructed from the fields
     * of `C` and the fields of the superclasses of `C`. The fields in `C` take precedence over
     * the fields in the superclasses of `C`.
     * <p>
     * Definition (15).
     */
    private void fields(Identifier cId) {
        // TODO -- get the ClassExtendsDeclaration with the cId
        // TODO
//        List<VarDeclaration> superDeclarations = fields(c.f3);
//        Type
    }

    /**
     * 6.7  The `methodtype` Helper Function. -- TODO
     * We use the notation `methodtype(id, id-M)` to denote the list of argument types of the
     * method with name id-M in class id (or a superclass of id) together with the return type
     * (or BOTTOM if no such method exists).
     * <p>
     * Definition (16).
     */

    /**
     * 6.8  The `noOverloading` Helper Function. -- TODO
     * <p>
     * Definition (20).
     */


    // TODO -- this part
    // From page 112 in book:
    // Type-checking of a MiniJava program proceeds in two phases.
    // Phase 1: First, we build the symbol table.
    //   Can be implemented by a visitor that visits nodes in a MiniJava syntax tree and builds a symbol table.
    //   For instance, the visit method in Program 5.8 handles variable declarations.
    //   It will add the variable name and type to a data structure for the current class which later will be
    //   added to the symbol table. Notice that the visit method checks whether a variable is declared more than
    //   once and, if so, then it prints an appropriate error message.
    // Phase 2: Type-Check the statements and expressions.
    //   During the second phase, the symbol table is consulted for each identifier that is found.
    //   Can be implemented by a visitor that type-checks all statements and expressions. The result type of each
    //   visit method is `String`, for representing MiniJava types. The idea is that when the visitor visits an
    //   expression, then it returns the type of that expression. If the expression does not type-check, then the
    //   type check is terminated with an error message.

//    public void visit(VarDeclaration var) {
//        Type t = var.f0.accept(this);
//        String id = var.f1.toString();
//
//        if (currMethod == null) {
//            if (!currClass.add)
//        }
//    }
}