package ch.sbb.polarion.extension.interceptor;

import ch.sbb.polarion.extension.interceptor.model.ActionHook;
import ch.sbb.polarion.extension.interceptor.model.HooksRegistry;
import com.polarion.platform.persistence.model.IPObject;
import org.apache.hivemind.InterceptorStack;
import org.apache.hivemind.ServiceInterceptorFactory;
import org.apache.hivemind.internal.Module;
import org.apache.hivemind.service.BodyBuilder;
import org.apache.hivemind.service.ClassFab;
import org.apache.hivemind.service.ClassFabUtils;
import org.apache.hivemind.service.ClassFactory;
import org.apache.hivemind.service.MethodIterator;
import org.apache.hivemind.service.MethodSignature;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"unused", "rawtypes"})
public class DataServiceInterceptorFactory implements ServiceInterceptorFactory {

    private final DataServiceInterceptor interceptor;
    private ClassFactory classFactory;

    public DataServiceInterceptorFactory() {
        HooksRegistry.HOOKS.refresh();
        interceptor = new DataServiceInterceptor();
    }

    private static boolean checkMethodSignature(MethodSignature sig, String name, List<Class> params) {
        Set<Class> parameterTypes = Arrays.stream(sig.getParameterTypes()).collect(Collectors.toSet());
        return sig.getName().equals(name) && parameterTypes.size() == params.size() && parameterTypes.containsAll(params);
    }

    public void setFactory(ClassFactory factory) {
        classFactory = factory;
    }

    @Override
    public void createInterceptor(InterceptorStack stack, Module invokingModule, List parameters) {
        try {
            Class<?> interceptorClass = constructInterceptorClass(stack);
            Object stackTop = stack.peek();
            Object proxy = interceptorClass.getConstructors()[0].newInstance(stackTop, interceptor);
            stack.push(proxy);
        } catch (
                InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | SecurityException e) {
            throw new IllegalStateException("The DataService Interceptor could not be instantiated.", e);
        }
    }

    private Class<?> constructInterceptorClass(InterceptorStack stack) {
        Class<?> serviceInterfaceClass = stack.getServiceInterface();
        String name = ClassFabUtils.generateClassName(serviceInterfaceClass);
        ClassFab classFab = classFactory.newClass(name, Object.class);
        classFab.addInterface(serviceInterfaceClass);
        createInfrastructure(stack, classFab);
        addServiceMethods(stack, classFab);
        return classFab.createClass();
    }

    private void createInfrastructure(InterceptorStack stack, ClassFab classFab) {
        Class<?> topClass = ClassFabUtils.getInstanceClass(stack.peek(), stack.getServiceInterface());
        classFab.addField("_delegate", topClass);
        classFab.addField("_interceptor", DataServiceInterceptor.class);
        classFab.addConstructor(
                new Class[]{topClass, DataServiceInterceptor.class}, null, "{_delegate = $1; _interceptor = $2;}"
        );
    }

    private void addServiceMethods(InterceptorStack stack, ClassFab fab) {
        MethodIterator mi = new MethodIterator(stack.getServiceInterface());
        while (mi.hasNext()) {
            MethodSignature sig = mi.next();
            if (checkMethodSignature(sig, ActionHook.ActionType.SAVE.getMethodName(), List.of(IPObject.class))) {
                addSaveMethodImplementation(fab, sig);
            } else if (checkMethodSignature(sig, ActionHook.ActionType.DELETE.getMethodName(), List.of(IPObject.class))) {
                addDeleteMethodImplementation(fab, sig);
            } else {
                addPassThroughMethodImplementation(fab, sig);
            }
        }
    }

    private void addSaveMethodImplementation(ClassFab classFab, MethodSignature sig) {
        BodyBuilder builder = new BodyBuilder();
        builder.begin();
        builder.add("Object _hooksExecutors = _interceptor.getHookExecutors(true, $1); ");
        builder.add("_interceptor.callExecutors(_hooksExecutors, true, $1); ");
        builder.add("_delegate.{0}($$); ", sig.getName());
        builder.add("_interceptor.callExecutors(_hooksExecutors, false, $1); ");
        builder.end();
        classFab.addMethod(1, sig, builder.toString());
    }

    private void addDeleteMethodImplementation(ClassFab classFab, MethodSignature sig) {
        BodyBuilder builder = new BodyBuilder();
        builder.begin();
        builder.add("Object _hooksExecutors = _interceptor.getHookExecutors(false, $1); ");
        builder.add("_interceptor.callExecutors(_hooksExecutors, true, $1); ");
        builder.add("_delegate.{0}($$); ", sig.getName());
        builder.add("_interceptor.callExecutors(_hooksExecutors, false, $1); ");
        builder.end();
        classFab.addMethod(1, sig, builder.toString());
    }

    private void addPassThroughMethodImplementation(ClassFab classFab, MethodSignature sig) {
        BodyBuilder builder = new BodyBuilder();
        builder.begin();
        builder.add("return ($r) _delegate." + sig.getName() + "($$);");
        builder.end();
        classFab.addMethod(1, sig, builder.toString());
    }

}