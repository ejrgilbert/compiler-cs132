package cs132.hw2.typechecker;

import cs132.hw2.syntaxtree.AndExpression;
import cs132.hw2.syntaxtree.ArrayLength;
import cs132.hw2.syntaxtree.ArrayLookup;
import cs132.hw2.syntaxtree.ArrayType;
import cs132.hw2.syntaxtree.AssignmentStatement;
import cs132.hw2.syntaxtree.ClassDeclaration;
import cs132.hw2.syntaxtree.ClassExtendsDeclaration;
import cs132.hw2.syntaxtree.CompareExpression;
import cs132.hw2.syntaxtree.FormalParameter;
import cs132.hw2.syntaxtree.FormalParameterList;
import cs132.hw2.syntaxtree.Goal;
import cs132.hw2.syntaxtree.Identifier;
import cs132.hw2.syntaxtree.MainClass;
import cs132.hw2.syntaxtree.MessageSend;
import cs132.hw2.syntaxtree.MethodDeclaration;
import cs132.hw2.syntaxtree.MinusExpression;
import cs132.hw2.syntaxtree.Node;
import cs132.hw2.syntaxtree.PlusExpression;
import cs132.hw2.syntaxtree.PrimaryExpression;
import cs132.hw2.syntaxtree.TimesExpression;
import cs132.hw2.syntaxtree.Type;
import cs132.hw2.syntaxtree.TypeDeclaration;
import cs132.hw2.syntaxtree.VarDeclaration;
import cs132.hw2.visitor.DepthFirstVisitor;
import jdk.jshell.spi.ExecutionControl;

import java.util.Enumeration;

public class SymbolTableBuilder extends DepthFirstVisitor {
    SymbolTable symbolTable;
    ClassRecord currentClass;
    MethodRecord currentMethod;

    public SymbolTableBuilder(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    // ****************************
    // ***** Logging Methods ******
    // ****************************

    private static void printInfo(String msg) {
        System.out.println("[INFO] SymbolTableBuilder" + msg);
    }

    private static void printError(String msg) {
        System.err.println("[ERROR] SymbolTableBuilder: " + msg);
    }

    /**
     * f0 -> MainClass()
     * f1 -> ( TypeDeclaration() )*
     * f2 -> <EOF>
     */
    public void visit(Goal n) {
        printInfo(".visit(Goal): Visited a Goal node.");
        symbolTable.setCurrentScopeNameAndType("prog", ScopeType.PROGRAM);

        // visit everything else
        super.visit(n);
    }

    private void addClass(String id) {
        if (symbolTable.lookup(id) != null) {
            printError(".visit(Class): Duplicated class name [ " + id + " ]");
        }

        // create record
        ScopeType type = ScopeType.CLASS;
        currentClass = new ClassRecord(id, type.toString());

        // add class to program scope
        symbolTable.put(id, currentClass);

        // enter class scope
        symbolTable.enterScope();

        // set scope name and type
        symbolTable.setCurrentScopeNameAndType(id, type);

        // update the class
        symbolTable.setCurrentScopeClass(currentClass);
    }

    private void addMethod(String id, String type) {
        if (currentClass.getMethod(id) != null) {
            printError(".visit(Method): Method [ " + id + " ] duplicated for class [ " + currentClass.getId() + " ]");
        }
        currentMethod = new MethodRecord(id, type);

        // add method to current class's scope
        symbolTable.put(id, currentMethod);

        // add method to currentClass's record
        currentClass.addMethod(id, currentMethod);

        // enter method scope
        symbolTable.enterScope();

        // set scope name and type
        symbolTable.setCurrentScopeNameAndType(id, ScopeType.METHOD);

        // inherit current class from parent scope
        symbolTable.setCurrentScopeClass(currentClass);
    }

    private void addParam(String type, String id) {
        if (currentMethod.containsParameter(id)) {
            printError(".visit(Parameter): Parameter [ " + id + " ] duplicated for method [ " + currentMethod.getId() + " ] in class [ " + currentClass.getId() + " ]");
        }
        // create record
        VarRecord param = new VarRecord(id, type);

        // add parameter to method
        currentMethod.addParameter(param);

        // insert record into scope (should be in the scope of the current method)
        symbolTable.put(id, param);
    }

    private void addField(String type, String id) {
        // insert record into scope (should be in the scope of the current method)
        if (symbolTable.lookup(id) != null) {
            printError(".visit(VarDeclaration): Duplicated identifier [ " + id + " ]");
        }
        // create record
        VarRecord field = new VarRecord(id, type);

        // add field to class
        currentClass.addField(id, field);

        symbolTable.setCurrentScopeClass(currentClass);
        symbolTable.put(id, field);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    public void visit(MainClass n) {
        printInfo(".visit(MainClass): Visited a MainClass node.");

        addClass(n.f1.f0.toString());

        // add main method
        String mainType = n.f5.toString();
        String mainId = n.f6.toString();
        addMethod(mainId, mainType);

        // add parameter of String[] with f11 -> Identifier()
        String paramId = n.f11.f0.toString();
        String paramType = new ArrayType().toString();
        addParam(paramType, paramId);

        // visit everything else
        super.visit(n);

        // now exit this scope!
        printInfo(".visit(MainClass): Exiting MainClass declaration for " + currentClass.getId() + ".");
        symbolTable.exitScope();
    }

    /**
     * f0 -> ClassDeclaration()
     *       | ClassExtendsDeclaration()
     */
    public void visit(TypeDeclaration n) {
        printInfo(".visit(TypeDeclaration): Visited a TypeDeclaration node.");

        // visit everything else
        super.visit(n);
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    public void visit(ClassDeclaration n) {
        printInfo(".visit(ClassDeclaration): Visited a ClassDeclaration node.");

        addClass(n.f1.f0.toString());

        // visit everything else
        super.visit(n);

        // now exit this scope!
        printInfo(".visit(ClassDeclaration): Exiting ClassDeclaration declaration for " + currentClass.getId() + ".");
        symbolTable.exitScope();
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    public void visit(ClassExtendsDeclaration n) {
        printError(".visit(ClassExtendsDeclaration): TODO -- not yet implemented!");
        System.exit(1);
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    public void visit(VarDeclaration n) {
        printInfo(".visit(VarDeclaration): Visited a VarDeclaration node.");

        // add field to current class
        String type = n.f0.f0.choice.toString();
        String id = n.f1.f0.toString();
        addField(type, id);
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    public void visit(MethodDeclaration n) {
        printInfo(".visit(MethodDeclaration): Visited a MethodDeclaration node.");

        // create record
        String type = n.f1.f0.choice.toString();
        String id = n.f2.f0.toString();
        addMethod(id, type);

        // visit everything else
        super.visit(n);

        // now exit this scope!
        printInfo(".visit(MethodDeclaration): Exiting MethodDeclaration declaration for " + currentMethod.getId() + ".");
        symbolTable.exitScope();
    }

//    /**
//     * f0 -> FormalParameter()
//     * f1 -> ( FormalParameterRest() )*
//     */
//    public void visit(FormalParameterList n) {
//        System.out.println("[INFO] SymbolTableBuilder.visit(FormalParameterList): Visited a FormalParameterList node.");
//
//        // visit everything else
//        super.visit(n);
//    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public void visit(FormalParameter n) {
        printInfo(".visit(FormalParameter): Visited a FormalParameter node.");

        // add parameter to current method
        String type = n.f0.f0.choice.toString();
        String id = n.f1.f0.toString();
        addParam(type, id);

        // visit everything else
//        super.visit(n);
    }

//    /**
//     * f0 -> Identifier()
//     * f1 -> "="
//     * f2 -> Expression()
//     * f3 -> ";"
//     */
//    public void visit(AssignmentStatement n) {
//        System.out.println("[INFO] SymbolTableBuilder.visit(AssignmentStatement): Visited an AssignmentStatement node.");
//
//        // add parameter to current method
//        String id = n.f0.f0.toString();
//
//        Node exprType = n.f2.f0.choice;
//        if (exprType instanceof AndExpression || exprType instanceof CompareExpression) {
//            // boolean
//        } else if (exprType instanceof PlusExpression ||
//                   exprType instanceof MinusExpression ||
//                   exprType instanceof TimesExpression ||
//                   exprType instanceof ArrayLength ||
//                   exprType instanceof ArrayLookup) {
//            // int
//        } else if (exprType instanceof MessageSend) {
//            // this is a method call...need to look up the return type of the called method
//        } else if (exprType instanceof PrimaryExpression) {
//            /**
//             * Grammar production:
//             * f0 -> IntegerLiteral()
//             *       | TrueLiteral()
//             *       | FalseLiteral()
//             *       | Identifier()
//             *       | ThisExpression()
//             *       | ArrayAllocationExpression()
//             *       | AllocationExpression()
//             *       | NotExpression()
//             *       | BracketExpression()
//             */
//        } else {
//            // TODO error
//        }
//
//        String type = super.visit(n.f2);
//        addParam(type, id);
//    }
}
