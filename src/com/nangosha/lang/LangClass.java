package com.nangosha.lang;

import java.util.List;
import java.util.Map;

public class LangClass implements  LangCallable{
    final String name;
    final LangClass superclass;
    private final Map<String, LangFunction> methods;

    LangClass(String name, LangClass superclass, Map<String, LangFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    LangFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (superclass != null) {
            return superclass.findMethod(name);
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments) {
        LangInstance instance = new LangInstance(this);
        LangFunction initializer = findMethod("init");

        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }
    @Override
    public int arity() {
        LangFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

}
