package rhino.typedarrays;

import rhino.*;

/**
 * An array view that stores 32-bit quantities and implements the JavaScript "Uint32Array" interface.
 * It also implements List&lt;Long&gt; for direct manipulation in Java.
 */

public class NativeUint32Array
extends NativeTypedArrayView<Long>{
    private static final String CLASS_NAME = "Uint32Array";
    private static final int BYTES_PER_ELEMENT = 4;

    public NativeUint32Array(){
    }

    public NativeUint32Array(NativeArrayBuffer ab, int off, int len){
        super(ab, off, len, len * BYTES_PER_ELEMENT);
    }

    public NativeUint32Array(int len){
        this(new NativeArrayBuffer(len * BYTES_PER_ELEMENT), 0, len);
    }

    @Override
    public String getClassName(){
        return CLASS_NAME;
    }

    public static void init(Context cx, Scriptable scope, boolean sealed){
        NativeUint32Array a = new NativeUint32Array();
        a.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
    }

    @Override
    protected NativeUint32Array construct(NativeArrayBuffer ab, int off, int len){
        return new NativeUint32Array(ab, off, len);
    }

    @Override
    public int getBytesPerElement(){
        return BYTES_PER_ELEMENT;
    }

    @Override
    protected NativeUint32Array realThis(Scriptable thisObj, IdFunctionObject f){
        if(!(thisObj instanceof NativeUint32Array)){
            throw incompatibleCallError(f);
        }
        return (NativeUint32Array)thisObj;
    }

    @Override
    protected Object js_get(int index){
        if(checkIndex(index)){
            return Undefined.instance;
        }
        return ByteIo.readUint32(arrayBuffer.buffer, (index * BYTES_PER_ELEMENT) + offset, useLittleEndian());
    }

    @Override
    protected Object js_set(int index, Object c){
        if(checkIndex(index)){
            return Undefined.instance;
        }
        long val = Conversions.toUint32(c);
        ByteIo.writeUint32(arrayBuffer.buffer, (index * BYTES_PER_ELEMENT) + offset, val, useLittleEndian());
        return null;
    }

    @Override
    public Long get(int i){
        if(checkIndex(i)){
            throw new IndexOutOfBoundsException();
        }
        return (Long)js_get(i);
    }

    @Override
    public Long set(int i, Long aByte){
        if(checkIndex(i)){
            throw new IndexOutOfBoundsException();
        }
        return (Long)js_set(i, aByte);
    }
}
