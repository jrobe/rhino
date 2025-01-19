package rhino;

import java.lang.reflect.*;

/**
 * Avoid loading classes unless they are used.
 *
 * <p> This improves startup time and average memory usage.
 */
public final class LazilyLoadedCtor{
    private static final int STATE_BEFORE_INIT = 0;
    private static final int STATE_INITIALIZING = 1;
    private static final int STATE_WITH_VALUE = 2;

    private final ScriptableObject scope;
    private final String propertyName;
    private final String className;
    private final boolean sealed;
    private Object initializedValue;
    private int state;

    LazilyLoadedCtor(ScriptableObject scope, String propertyName, String className, boolean sealed){
        this.scope = scope;
        this.propertyName = propertyName;
        this.className = className;
        this.sealed = sealed;
        this.state = STATE_BEFORE_INIT;

        scope.addLazilyInitializedValue(propertyName, 0, this, ScriptableObject.DONTENUM);
    }

    void init(){
        synchronized(this){
            if(state == STATE_INITIALIZING)
                throw new IllegalStateException(
                "Recursive initialization for " + propertyName);
            if(state == STATE_BEFORE_INIT){
                state = STATE_INITIALIZING;
                // Set value now to have something to set in finally block if
                // buildValue throws.
                Object value = Scriptable.NOT_FOUND;
                try{
                    value = buildValue();
                }finally{
                    initializedValue = value;
                    state = STATE_WITH_VALUE;
                }
            }
        }
    }

    Object getValue(){
        if(state != STATE_WITH_VALUE)
            throw new IllegalStateException(propertyName);
        return initializedValue;
    }

    private Object buildValue(){
        return buildValue0();
    }

    private Object buildValue0(){
        Class<? extends Scriptable> cl = cast(Kit.classOrNull(className));
        if(cl != null){
            try{
                Object value = ScriptableObject.buildClassCtor(scope, cl,
                sealed, false);
                if(value != null){
                    return value;
                }
                // cl has own static initializer which is expected
                // to set the property on its own.
                value = scope.get(propertyName, scope);
                if(value != Scriptable.NOT_FOUND)
                    return value;
            }catch(InvocationTargetException ex){
                Throwable target = ex.getTargetException();
                if(target instanceof RuntimeException){
                    throw (RuntimeException)target;
                }
            }catch(Exception ex){
            }
        }
        return Scriptable.NOT_FOUND;
    }

    @SuppressWarnings({"unchecked"})
    private Class<? extends Scriptable> cast(Class<?> cl){
        return (Class<? extends Scriptable>)cl;
    }

}
