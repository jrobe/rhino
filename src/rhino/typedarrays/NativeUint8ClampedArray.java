package rhino.typedarrays;

import rhino.*;

/**
 * An array view that stores 8-bit quantities and implements the JavaScript "Uint8ClampedArray" interface.
 * It also implements List&lt;Integer&gt; for direct manipulation in Java. Bytes inserted that fall out of the range
 * (0 &lt;= X &lt; 256) will be adjusted so that they match before insertion.
 */

public class NativeUint8ClampedArray
extends NativeTypedArrayView<Integer>{
    private static final String CLASS_NAME = "Uint8ClampedArray";

    public NativeUint8ClampedArray(){
    }

    public NativeUint8ClampedArray(NativeArrayBuffer ab, int off, int len){
        super(ab, off, len, len);
    }

    public NativeUint8ClampedArray(int len){
        this(new NativeArrayBuffer(len), 0, len);
    }

    @Override
    public String getClassName(){
        return CLASS_NAME;
    }

    public static void init(Context cx, Scriptable scope, boolean sealed){
        NativeUint8ClampedArray a = new NativeUint8ClampedArray();
        a.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
    }

    @Override
    protected NativeUint8ClampedArray construct(NativeArrayBuffer ab, int off, int len){
        return new NativeUint8ClampedArray(ab, off, len);
    }

    @Override
    public int getBytesPerElement(){
        return 1;
    }

    @Override
    protected NativeUint8ClampedArray realThis(Scriptable thisObj, IdFunctionObject f){
        if(!(thisObj instanceof NativeUint8ClampedArray)){
            throw incompatibleCallError(f);
        }
        return (NativeUint8ClampedArray)thisObj;
    }

    @Override
    protected Object js_get(int index){
        if(checkIndex(index)){
            return Undefined.instance;
        }
        return ByteIo.readUint8(arrayBuffer.buffer, index + offset);
    }

    @Override
    protected Object js_set(int index, Object c){
        if(checkIndex(index)){
            return Undefined.instance;
        }
        int val = Conversions.toUint8Clamp(c);
        ByteIo.writeUint8(arrayBuffer.buffer, index + offset, val);
        return null;
    }

    @Override
    public Integer get(int i){
        if(checkIndex(i)){
            throw new IndexOutOfBoundsException();
        }
        return (Integer)js_get(i);
    }

    @Override
    public Integer set(int i, Integer aByte){
        if(checkIndex(i)){
            throw new IndexOutOfBoundsException();
        }
        return (Integer)js_set(i, aByte);
    }
}
