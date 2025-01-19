package rhino;

import java.util.*;

/**
 * This class reflects Java packages into the JavaScript environment.  We
 * lazily reflect classes and subpackages, and use a caching/sharing
 * system to ensure that members reflected into one JavaPackage appear
 * in all other references to the same package (as with Packages.java.lang
 * and java.lang).
 * @author Mike Shaver
 * @see NativeJavaArray
 * @see NativeJavaObject
 * @see NativeJavaClass
 */

public class NativeJavaPackage extends ScriptableObject{
    private final String packageName;
    private transient ClassLoader classLoader;
    private Set<String> negativeCache = null;

    public NativeJavaPackage(String packageName, ClassLoader classLoader){
        this.packageName = packageName;
        this.classLoader = classLoader;
    }

    @Deprecated
    public NativeJavaPackage(boolean completelyPointless, String packageName, ClassLoader classLoader){
        this(packageName, classLoader);
    }

    @Override
    public String getClassName(){
        return "JavaPackage";
    }

    @Override
    public boolean has(String id, Scriptable start){
        return true;
    }

    @Override
    public boolean has(int index, Scriptable start){
        return false;
    }

    @Override
    public void put(String id, Scriptable start, Object value){
        // Can't add properties to Java packages.  Sorry.
    }

    @Override
    public void put(int index, Scriptable start, Object value){
        throw Context.reportRuntimeError0("msg.pkg.int");
    }

    @Override
    public Object get(String id, Scriptable start){
        return getPkgProperty(id, start, true);
    }

    @Override
    public Object get(int index, Scriptable start){
        return NOT_FOUND;
    }

    // set up a name which is known to be a package so we don't
    // need to look for a class by that name
    NativeJavaPackage forcePackage(String name, Scriptable scope){
        Object cached = super.get(name, this);
        if(cached != null && cached instanceof NativeJavaPackage){
            return (NativeJavaPackage)cached;
        }
        String newPackage = packageName.length() == 0
        ? name
        : packageName + "." + name;
        NativeJavaPackage pkg = new NativeJavaPackage(newPackage, classLoader);
        ScriptRuntime.setObjectProtoAndParent(pkg, scope);
        super.put(name, this, pkg);
        return pkg;
    }

    synchronized Object getPkgProperty(String name, Scriptable start,
                                       boolean createPkg){
        Object cached = super.get(name, start);
        if(cached != NOT_FOUND)
            return cached;
        if(negativeCache != null && negativeCache.contains(name)){
            // Performance optimization: see bug 421071
            return null;
        }

        String className = (packageName.length() == 0)
        ? name : packageName + '.' + name;
        Context cx = Context.getContext();
        ClassShutter shutter = cx.getClassShutter();
        Scriptable newValue = null;
        if(shutter == null || shutter.visibleToScripts(className)){
            Class<?> cl;
            if(classLoader != null){
                cl = Kit.classOrNull(classLoader, className);
            }else{
                cl = Kit.classOrNull(className);
            }
            if(cl != null){
                WrapFactory wrapFactory = cx.getWrapFactory();
                newValue = wrapFactory.wrapJavaClass(cx, getTopLevelScope(this), cl);
                newValue.setPrototype(getPrototype());
            }
        }
        if(newValue == null){
            if(createPkg){
                NativeJavaPackage pkg;
                pkg = new NativeJavaPackage(className, classLoader);
                ScriptRuntime.setObjectProtoAndParent(pkg, getParentScope());
                newValue = pkg;
            }else{
                // add to negative cache
                if(negativeCache == null)
                    negativeCache = new HashSet<>();
                negativeCache.add(name);
            }
        }
        if(newValue != null){
            // Make it available for fast lookup and sharing of
            // lazily-reflected constructors and static members.
            super.put(name, start, newValue);
        }
        return newValue;
    }

    @Override
    public Object getDefaultValue(Class<?> ignored){
        return toString();
    }

    @Override
    public String toString(){
        return "[JavaPackage " + packageName + "]";
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof NativeJavaPackage){
            NativeJavaPackage njp = (NativeJavaPackage)obj;
            return packageName.equals(njp.packageName) &&
            classLoader == njp.classLoader;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return packageName.hashCode() ^
        (classLoader == null ? 0 : classLoader.hashCode());
    }
}
