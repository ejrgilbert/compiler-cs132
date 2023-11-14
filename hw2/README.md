# hw2 - Type Checking of MiniJava#

This is a repository that contains my solution to [HW2](https://web.cs.ucla.edu/classes/spring11/cs132/kannan/index.html).
- Here is the [HW2 Handout](https://web.cs.ucla.edu/~palsberg/course/cs132/hw2Handout.pdf).
- Here is information about the [MiniJava Type System](https://web.cs.ucla.edu/~palsberg/course/cs132/miniJava-typesystem.pdf).

How to split up the HW into logical chunks:
- The basic challenge: implement the homework for only the `MainClass`; don't implement the homework for the `TypeDeclarations`.
- A bigger challenge: implement the homework for the `MainClass` and the `ClassDeclarations`; don't implement the homework for the `ClassExtendsDeclarations`.

NOTE: Everything in the `cs132.hw2` package has been generated _except_ for the `Typecheck.java` class, that contains my HW solution.

## Assignment ##

Use JTB and JavaCC and write in Java one or more visitors that type check a MiniJava program.

Your main file should be called Typecheck.java, and if P.java contains a program to be type checked, then:

`java Typecheck < P.java`

should print either "Program type checked successfully" or "Type error".

## MiniJava Specification ##

MiniJava is a subset of Java.  The meaning of a MiniJava program is given by its meaning as a Java program.  Overloading is not allowed in MiniJava. The MiniJava statement System.out.println( ... ); can only print integers. The MiniJava expression e.length only applies to expressions of type int[].