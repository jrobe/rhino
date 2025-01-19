package rhino;

import java.lang.reflect.*;

/**
 * Wrapper class for Method and Constructor instances to cache
 * getParameterTypes() results, recover from IllegalAccessException
 * in some cases and provide serialization support.
 * @author Igor Bukanov
 */

final class MemberBox{
    private transient Member memberObject;
    transient Class<?>[] argTypes;
    transient Object delegateTo;
    transient boolean vararg;


    MemberBox(Method method){
        init(method);
    }

    MemberBox(Constructor<?> constructor){
        init(constructor);
    }

    private void init(Method method){
        this.memberObject = method;
        this.argTypes = method.getParameterTypes();
        this.vararg = method.isVarArgs();
    }

    private void init(Constructor<?> constructor){
        this.memberObject = constructor;
        this.argTypes = constructor.getParameterTypes();
        this.vararg = constructor.isVarArgs();
    }

    Method method(){
        return (Method)memberObject;
    }

    Constructor<?> ctor(){
        return (Constructor<?>)memberObject;
    }

    Member member(){
        return memberObject;
    }

    boolean isMethod(){
        return memberObject instanceof Method;
    }

    boolean isCtor(){
        return memberObject instanceof Constructor;
    }

    boolean isStatic(){
        return Modifier.isStatic(memberObject.getModifiers());
    }

    boolean isPublic(){
        return Modifier.isPublic(memberObject.getModifiers());
    }

    String getName(){
        return memberObject.getName();
    }

    Class<?> getDeclaringClass(){
        return memberObject.getDeclaringClass();
    }

    String toJavaDeclaration(){
        StringBuilder sb = new StringBuilder();
        if(isMethod()){
            Method method = method();
            sb.append(method.getReturnType());
            sb.append(' ');
            sb.append(method.getName());
        }else{
            Constructor<?> ctor = ctor();
            String name = ctor.getDeclaringClass().getName();
            int lastDot = name.lastIndexOf('.');
            if(lastDot >= 0){
                name = name.substring(lastDot + 1);
            }
            sb.append(name);
        }
        sb.append(JavaMembers.liveConnectSignature(argTypes));
        return sb.toString();
    }

    @Override
    public String toString(){
        return memberObject.toString();
    }

    Object invoke(Object target, Object[] args){
        Method method = method();
        try{
            try{
                return method.invoke(target, args);
            }catch(IllegalAccessException ex){
                Method accessible = searchAccessibleMethod(method, argTypes);
                if(accessible != null){
                    memberObject = accessible;
                    method = accessible;
                }else{
                    if(!VMBridge.tryToMakeAccessible(method)){
                        throw Context.throwAsScriptRuntimeEx(ex);
                    }
                }
                // Retry after recovery
                return method.invoke(target, args);
            }
        }catch(InvocationTargetException ite){
            // Must allow ContinuationPending exceptions to propagate unhindered
            Throwable e = ite;
            do{
                e = ((InvocationTargetException)e).getTargetException();
            }while((e instanceof InvocationTargetException));
            if(e instanceof ContinuationPending)
                throw (ContinuationPending)e;
            throw Context.throwAsScriptRuntimeEx(e);
        }catch(Exception ex){
            throw Context.throwAsScriptRuntimeEx(ex);
        }
    }

    Object newInstance(Object[] args){
        Constructor<?> ctor = ctor();
        try{
            try{
                return ctor.newInstance(args);
            }catch(IllegalAccessException ex){
                if(!VMBridge.tryToMakeAccessible(ctor)){
                    throw Context.throwAsScriptRuntimeEx(ex);
                }
            }
            return ctor.newInstance(args);
        }catch(Exception ex){
            throw Context.throwAsScriptRuntimeEx(ex);
        }
    }

    private static Method searchAccessibleMethod(Method method, Class<?>[] params){
        int modifiers = method.getModifiers();
        if(Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)){
            Class<?> c = method.getDeclaringClass();
            if(!Modifier.isPublic(c.getModifiers())){
                String name = method.getName();
                Class<?>[] intfs = c.getInterfaces();
                for(int i = 0, N = intfs.length; i != N; ++i){
                    Class<?> intf = intfs[i];
                    if(Modifier.isPublic(intf.getModifiers())){
                        try{
                            return intf.getMethod(name, params);
                        }catch(Exception ex){
                        }
                    }
                }
                while(true){
                    c = c.getSuperclass();
                    if(c == null){
                        break;
                    }
                    if(Modifier.isPublic(c.getModifiers())){
                        try{
                            Method m = c.getMethod(name, params);
                            int mModifiers = m.getModifiers();
                            if(Modifier.isPublic(mModifiers)
                            && !Modifier.isStatic(mModifiers)){
                                return m;
                            }
                        }catch(Exception ex){
                        }
                    }
                }
            }
        }
        return null;
    }

}

