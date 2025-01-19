package rhino.typedarrays;

import rhino.*;

/**
 * An array view that stores 64-bit quantities and implements the JavaScript "Float64Array" interface.
 * It also implements List&lt;Double&gt; for direct manipulation in Java.
 */

public class NativeFloat64Array
extends NativeTypedArrayView<Double>{
    private static final String CLASS_NAME = "Float64Array";
    private static final int BYTES_PER_ELEMENT = 8;

    public NativeFloat64Array(){
    }

    public NativeFloat64Array(NativeArrayBuffer ab, int off, int len){
        super(ab, off, len, len * BYTES_PER_ELEMENT);
    }

    public NativeFloat64Array(int len){
        this(new NativeArrayBuffer(len * BYTES_PER_ELEMENT), 0, len);
    }

    @Override
    public String getClassName(){
        return CLASS_NAME;
    }

    public static void init(Context cx, Scriptable scope, boolean sealed){
        NativeFloat64Array a = new NativeFloat64Array();
        a.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
    }

    @Override
    protected NativeFloat64Array construct(NativeArrayBuffer ab, int off, int len){
        return new NativeFloat64Array(ab, off, len);
    }

    @Override
    public int getBytesPerElement(){
        return BYTES_PER_ELEMENT;
    }

    @Override
    protected NativeFloat64Array realThis(Scriptable thisObj, IdFunctionObject f){
        if(!(thisObj instanceof NativeFloat64Array)){
            throw incompatibleCallError(f);
        }
        return (NativeFloat64Array)thisObj;
    }

    @Override
    protected Object js_get(int index){
        if(checkIndex(index)){
            return Undefined.instance;
        }
        long base = ByteIo.readUint64Primitive(arrayBuffer.buffer, (index * BYTES_PER_ELEMENT) + offset, useLittleEndian());
        return Double.longBitsToDouble(base);
    }

    @Override
    protected Object js_set(int index, Object c){
        if(checkIndex(index)){
            return Undefined.instance;
        }
        double val = ScriptRuntime.toNumber(c);
        long base = Double.doubleToLongBits(val);
        ByteIo.writeUint64(arrayBuffer.buffer, (index * BYTES_PER_ELEMENT) + offset, base, useLittleEndian());
        return null;
    }

    @Override
    public Double get(int i){
        if(checkIndex(i)){
            throw new IndexOutOfBoundsException();
        }
        return (Double)js_get(i);
    }

    @Override
    public Double set(int i, Double aByte){
        if(checkIndex(i)){
            throw new IndexOutOfBoundsException();
        }
        return (Double)js_set(i, aByte);
    }
}
