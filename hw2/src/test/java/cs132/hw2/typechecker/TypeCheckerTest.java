package cs132.hw2.typechecker;


import cs132.hw2.syntaxtree.Identifier;
import cs132.hw2.syntaxtree.NodeToken;
import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TypeCheckerTest {

    private void addAcyclic(Set<Pair<Identifier, Identifier>> pairs, Identifier A, Identifier B, Identifier C) {
        pairs.add(new Pair<>(A, B));
        pairs.add(new Pair<>(B, C));
        pairs.add(new Pair<>(C, C));
    }

    private void addCyclic(Set<Pair<Identifier, Identifier>> pairs, Identifier A, Identifier B, Identifier C) {
        pairs.add(new Pair<>(A, B));
        pairs.add(new Pair<>(B, C));
        pairs.add(new Pair<>(C, A));
    }

    @Test
    public void testAcyclic1() {
        Set<Pair<Identifier, Identifier>> pairs = new HashSet<>();
        Identifier A = new Identifier(new NodeToken("A"));
        Identifier B = new Identifier(new NodeToken("B"));
        Identifier C = new Identifier(new NodeToken("C"));
        addCyclic(pairs, A, B, C);

        assertFalse(TypeChecker.acyclic(pairs));
    }

    @Test
    public void testAcyclic2() {
        Set<Pair<Identifier, Identifier>> pairs = new HashSet<>();
        Identifier A = new Identifier(new NodeToken("A"));
        Identifier B = new Identifier(new NodeToken("B"));
        Identifier C = new Identifier(new NodeToken("C"));
        addAcyclic(pairs, A, B, C);

        assertTrue(TypeChecker.acyclic(pairs));
    }

    @Test
    public void testAcyclic3() {
        Set<Pair<Identifier, Identifier>> pairs = new HashSet<>();
        Identifier A = new Identifier(new NodeToken("A"));
        Identifier B = new Identifier(new NodeToken("B"));
        Identifier C = new Identifier(new NodeToken("C"));

        Identifier D = new Identifier(new NodeToken("D"));
        Identifier E = new Identifier(new NodeToken("E"));
        Identifier F = new Identifier(new NodeToken("F"));

        addCyclic(pairs, A, B, C);
        addAcyclic(pairs, D, E, F);

        // Contains disconnected graphs, one acyclic, one cyclic
        assertFalse(TypeChecker.acyclic(pairs));
    }

    @Test
    public void testAcyclic4() {
        Set<Pair<Identifier, Identifier>> pairs = new HashSet<>();
        Identifier A = new Identifier(new NodeToken("A"));
        Identifier B = new Identifier(new NodeToken("B"));
        Identifier C = new Identifier(new NodeToken("C"));

        Identifier D = new Identifier(new NodeToken("D"));
        Identifier E = new Identifier(new NodeToken("E"));
        Identifier F = new Identifier(new NodeToken("F"));

        addCyclic(pairs, A, B, C);
        addCyclic(pairs, D, E, F);

        // Contains disconnected graphs, both cyclic
        assertFalse(TypeChecker.acyclic(pairs));
    }

    @Test
    public void testAcyclic5() {
        Set<Pair<Identifier, Identifier>> pairs = new HashSet<>();
        Identifier A = new Identifier(new NodeToken("A"));
        Identifier B = new Identifier(new NodeToken("B"));
        Identifier C = new Identifier(new NodeToken("C"));

        Identifier D = new Identifier(new NodeToken("D"));
        Identifier E = new Identifier(new NodeToken("E"));
        Identifier F = new Identifier(new NodeToken("F"));

        addAcyclic(pairs, A, B, C);
        addAcyclic(pairs, D, E, F);

        // Contains disconnected graphs, both acyclic
        assertTrue(TypeChecker.acyclic(pairs));
    }
}