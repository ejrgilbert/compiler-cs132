package cs132.hw2.typechecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

enum ScopeType {
    PROGRAM("program"),
    CLASS("class"),
    METHOD("method");

    private final String text;

    private ScopeType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}

public class SymbolTable {
    private Scope root;
    private Scope current;

    public SymbolTable() {
        this.root = new Scope(null);
        this.current = root;
    }

    public String getCurrentClassName() {
        return this.current.getContainer().getId();
    }

    public String getCurrentScopeName() {
        return this.current.getName();
    }

    public String getCurrentScopeType() {
        return this.current.getType();
    }

    public void setCurrentScopeNameAndType(String name, ScopeType type) {
        this.current.setNameAndType(name, type);
    }

    public void setCurrentScopeClass(ClassRecord container) {
        this.current.setContainer(container);
    }

    // create a new scope if necessary
    public void enterScope() {
        current = current.nextChild();
    }

    public void exitScope() {
        current = current.getParent();
    }

    public void put(String key, Record rec) {
        current.put(key, rec);
    }

    public Record lookup(String key) {
        return current.lookup(key);
    }

    // called before each traversal
    public void resetTable() {
        root.resetScope();
    }

    public void printTable() {
        System.out.println("\nPrinting the Symbol Table:\n");
        System.out.printf("%s %n", "+-------------------------------------------------------------------------------------------+");
        System.out.printf("%" + 18 + "s %" + 32 + "s %" + 32 + "s %n", "ID", "RECORD", "SCOPE");
        System.out.printf("%s %n", "+-------------------------------------------------------------------------------------------+");
        root.printScope();
        System.out.printf("%s %n", "+-------------------------------------------------------------------------------------------+");
    }

    private void printLine(String id, String record, String scope) {
        System.out.printf("%" + 20 + "s %" + 32 + "s %" + 35 + "s %n", id, record, scope);
    }

    private class Scope {
        private int next;
        private Scope parent;
        private ArrayList<Scope> children = new ArrayList<>();
        private Map<String, Record> records = new HashMap<>();
        // This could be a scope inside a method, this ClassRecord container lets you quickly look up
        // items in the containing class.
        ClassRecord container = new ClassRecord("prog", "program");

        String name = "";
        ScopeType type;

        public Scope(Scope parent) {
            // if this is the root scope, parent will be null
            this.parent = parent;
        }

        public String getName(){
            return this.name;
        }

        public String getType(){
            return type.toString();
        }

        public void setNameAndType(String name, ScopeType type) {
            this.name = name;
            this.type = type;
        }

        public ClassRecord getContainer() {
            return container;
        }

        public void setContainer(ClassRecord container) {
            this.container = container;
        }

        public Scope getParent() {
            return parent;
        }

        public Record getMethod(String methodName){
            return this.container.getMethod(methodName);
        }

        public void put(String key, Record rec) {
            this.records.put(key, rec);
        }

        public Scope nextChild() {
            Scope nextChild;
            if (next >= children.size()) {
                nextChild = new Scope(this); // create a new Scope passing the parent scope
                children.add(nextChild);
            } else {
                // child exists
                nextChild = children.get(next); // visited the next
            }

            next++;
            return nextChild;
        }

        public Record lookup(String key) {
            if (key.equals("this")) { // 'this' case
                return container;
            }

            if (records.containsKey(key)) { // is the key in the current scope?
                return records.get(key);
            }

            // move the scope to the parent scope
            if (parent == null) {
                return null; // identifier is not contained
            }

            return parent.lookup(key); // send the request to the parent
        }

        public void resetScope() {
            next = 0; // first child to visit next
            for (Scope child : children) {
                child.resetScope();
            }
        }

        public void printScope() {
            // print all the containing records
            for (Map.Entry<String, Record> entry : records.entrySet()) {
                String id = entry.getKey();
                Record rec = entry.getValue();
                printLine(id, rec.getId() + " - " + rec.getType(), name+" [ "+type+" ]");
            }

            // print children
            if (children == null) {
                System.out.println("<<<SYMBOL TABLE HAS NO CHILDREN>>>");
                return;
            }

            for (Scope scopeIt : children) {
                scopeIt.printScope();
            }
        }
    }
}
