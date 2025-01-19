package rhino;

import java.lang.reflect.*;

/**
 * This class reflects Java arrays into the JavaScript environment.
 * @author Mike Shaver
 * @see NativeJavaClass
 * @see NativeJavaObject
 * @see NativeJavaPackage
 */

public class NativeJavaArray
extends NativeJavaObject
implements SymbolScriptable{
    @Override
    public String getClassName(){
        return "JavaArray";
    }

    public static NativeJavaArray wrap(Scriptable scope, Object array){
        return new NativeJavaArray(scope, array);
    }

    @Override
    public Object unwrap(){
        return array;
    }

    public NativeJavaArray(Scriptable scope, Object array){
        super(scope, null, ScriptRuntime.ObjectClass);
        Class<?> cl = array.getClass();
        if(!cl.isArray()){
            throw new RuntimeException("Array expected");
        }
        this.array = array;
        this.length = Array.getLength(array);
        this.cls = cl.getComponentType();
    }

    @Override
    public boolean has(String id, Scriptable start){
        return id.equals("length") || super.has(id, start);
    }

    @Override
    public boolean has(int index, Scriptable start){
        return 0 <= index && index < length;
    }

    @Override
    public boolean has(Symbol key, Scriptable start){
        return SymbolKey.IS_CONCAT_SPREADABLE.equals(key);
    }

    @Override
    public Object get(String id, Scriptable start){
        if(id.equals("length"))
            return length;
        Object result = super.get(id, start);
        if(result == NOT_FOUND &&
        !ScriptableObject.hasProperty(getPrototype(), id)){
            throw Context.reportRuntimeError2(
            "msg.java.member.not.found", array.getClass().getName(), id);
        }
        return result;
    }

    @Override
    public Object get(int index, Scriptable start){
        if(0 <= index && index < length){
            Context cx = Context.getContext();
            Object obj = Array.get(array, index);
            return cx.getWrapFactory().wrap(cx, this, obj, cls);
        }
        return Undefined.instance;
    }

    @Override
    public Object get(Symbol key, Scriptable start){
        if(SymbolKey.IS_CONCAT_SPREADABLE.equals(key)){
            return true;
        }
        return Scriptable.NOT_FOUND;
    }

    @Override
    public void put(String id, Scriptable start, Object value){
        // Ignore assignments to "length"--it's readonly.
        if(!id.equals("length"))
            throw Context.reportRuntimeError1(
            "msg.java.array.member.not.found", id);
    }

    @Override
    public void put(int index, Scriptable start, Object value){
        if(0 <= index && index < length){
            Array.set(array, index, Context.jsToJava(value, cls));
        }else{
            throw Context.reportRuntimeError2(
            "msg.java.array.index.out.of.bounds", String.valueOf(index),
            String.valueOf(length - 1));
        }
    }

    @Override
    public void delete(Symbol key){
        // All symbols are read-only
    }

    @Override
    public Object getDefaultValue(Class<?> hint){
        if(hint == null || hint == ScriptRuntime.StringClass)
            return array.toString();
        if(hint == ScriptRuntime.BooleanClass)
            return Boolean.TRUE;
        if(hint == ScriptRuntime.NumberClass)
            return ScriptRuntime.NaNobj;
        return this;
    }

    @Override
    public Object[] getIds(){
        Object[] result = new Object[length];
        int i = length;
        while(--i >= 0)
            result[i] = i;
        return result;
    }

    @Override
    public boolean hasInstance(Scriptable value){
        if(!(value instanceof Wrapper))
            return false;
        Object instance = ((Wrapper)value).unwrap();
        return cls.isInstance(instance);
    }

    @Override
    public Scriptable getPrototype(){
        if(prototype == null){
            prototype =
            ScriptableObject.getArrayPrototype(this.getParentScope());
        }
        return prototype;
    }

    Object array;
    int length;
    Class<?> cls;
}
