package rhino;

/**
 * The class for results of the Function.bind operation
 * EcmaScript 5 spec, 15.3.4.5
 * @author Raphael Speyer
 */
public class BoundFunction extends BaseFunction{

    private final Callable targetFunction;
    private final Scriptable boundThis;
    private final Object[] boundArgs;
    private final int length;

    public BoundFunction(Context cx, Scriptable scope, Callable targetFunction, Scriptable boundThis,
                         Object[] boundArgs){
        this.targetFunction = targetFunction;
        this.boundThis = boundThis;
        this.boundArgs = boundArgs;
        if(targetFunction instanceof BaseFunction){
            length = Math.max(0, ((BaseFunction)targetFunction).getLength() - boundArgs.length);
        }else{
            length = 0;
        }

        ScriptRuntime.setFunctionProtoAndParent(this, scope);

        Function thrower = ScriptRuntime.typeErrorThrower(cx);
        NativeObject throwing = new NativeObject();
        throwing.put("get", throwing, thrower);
        throwing.put("set", throwing, thrower);
        throwing.put("enumerable", throwing, false);
        throwing.put("configurable", throwing, false);
        throwing.preventExtensions();

        this.defineOwnProperty(cx, "caller", throwing, false);
        this.defineOwnProperty(cx, "arguments", throwing, false);
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] extraArgs){
        Scriptable callThis = boundThis != null ? boundThis : ScriptRuntime.getTopCallScope(cx);
        return targetFunction.call(cx, scope, callThis, concat(boundArgs, extraArgs));
    }

    @Override
    public Scriptable construct(Context cx, Scriptable scope, Object[] extraArgs){
        if(targetFunction instanceof Function){
            return ((Function)targetFunction).construct(cx, scope, concat(boundArgs, extraArgs));
        }
        throw ScriptRuntime.typeError0("msg.not.ctor");
    }

    @Override
    public boolean hasInstance(Scriptable instance){
        if(targetFunction instanceof Function){
            return ((Function)targetFunction).hasInstance(instance);
        }
        throw ScriptRuntime.typeError0("msg.not.ctor");
    }

    @Override
    public int getLength(){
        return length;
    }

    private Object[] concat(Object[] first, Object[] second){
        Object[] args = new Object[first.length + second.length];
        System.arraycopy(first, 0, args, 0, first.length);
        System.arraycopy(second, 0, args, first.length, second.length);
        return args;
    }

    static boolean equalObjectGraphs(BoundFunction f1, BoundFunction f2, EqualObjectGraphs eq){
        return eq.equalGraphs(f1.boundThis, f2.boundThis) && eq.equalGraphs(f1.targetFunction, f2.targetFunction) && eq.equalGraphs(f1.boundArgs, f2.boundArgs);
    }
}
