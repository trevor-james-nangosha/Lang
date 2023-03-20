package com.nangosha.lang;

import java.util.List;

interface LangCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
