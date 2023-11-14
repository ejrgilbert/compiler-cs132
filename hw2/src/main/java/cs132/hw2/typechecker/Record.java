package cs132.hw2.typechecker;

import java.util.HashMap;
import java.util.Map;

public class Record {
    protected String id;
    protected String type;

    public Record (String id, String type){
        this.id = id;
        this.type = type;
    }

    public String getId(){
        return this.id;
    }

    public String getType(){
        return this.type;
    }

    @Override
    public String toString() {
        return "Record: " + id +" : " + type;
    }
}

class ClassRecord extends Record {
    private HashMap<String, MethodRecord> methods = new HashMap<String, MethodRecord>();
    private HashMap<String, VarRecord> fields = new HashMap<String, VarRecord>();

    public ClassRecord(String id, String type) {
        super(id, type);
    }

    public void addMethod(String methodName, MethodRecord methodRecord) {
        this.methods.put(methodName, methodRecord);
    }

    public Record getMethod(String methodName) {
        return methods.get(methodName);
    }

    public void addField(String fieldName, VarRecord varRecord) {
        this.fields.put(fieldName, varRecord);
    }

    public Record getField(String fieldName) {
        return fields.get(fieldName);
    }

    public void printMethods() {
        for (Map.Entry<String, MethodRecord> entry : methods.entrySet()) {
            MethodRecord method = entry.getValue();
            System.out.print("\t-> " + method.getId() + ":" + method.getType() + " == ");
            method.printParameters();
        }
    }

    public void printFields() {
        for (Map.Entry<String, VarRecord> entry : fields.entrySet()) {
            VarRecord var = entry.getValue();
            System.out.print("\tFIELD: " + var.getId() + ":" + var.getType());
        }
    }
}

class MethodRecord extends Record {
    private HashMap<Integer, VarRecord> parameters = new HashMap<>();
    int nextParamNumber = 0;

    public MethodRecord(String id, String type) {
        super(id, type);
    }

    public void addParameter(VarRecord parameter) {
        this.parameters.put(nextParamNumber, parameter);
        this.nextParamNumber++;
    }

    public boolean containsParameter(int paramNumber, Record parameter) {
        Record rec = parameters.get(paramNumber);
        if (parameter == null || rec == null) {
            return false;
        }
        return rec.getType().equals(parameter.getType());
    }

    public boolean containsParameter(String id) {
        for (VarRecord param : parameters.values()) {
            if (param.getId().equals(id)) return true;
        }
        return false;
    }

    public int paramCount() {
        return parameters.size();
    }

    public void printParameters() {
        System.out.println("(");
        for (Map.Entry<Integer, VarRecord> entry : parameters.entrySet()) {
//            System.out.println("\t" + entry.getKey() + " -> " + entry.getValue().toString());
            System.out.println("\t" + entry.getValue().toString());
        }
        System.out.println(")");
    }
}

class VarRecord extends Record {
    public VarRecord(String id, String type) {
        super(id, type);
    }
}