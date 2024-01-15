package ch.sbb.polarion.extension.interceptor;

import com.polarion.core.util.logging.Logger;
import com.polarion.platform.persistence.IDataService;
import org.apache.hivemind.InterceptorStack;
import org.apache.hivemind.ServiceInterceptorFactory;
import org.apache.hivemind.internal.Module;

import java.lang.reflect.Proxy;
import java.util.List;

public class DataServiceInterceptorFactory implements ServiceInterceptorFactory {

    private static final Logger logger = Logger.getLogger(DataServiceInterceptorFactory.class);

    @Override
    public void createInterceptor(InterceptorStack stack, Module module, List parameters) {
        final Object delegate = stack.peek();
        stack.push(Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{IDataService.class}, new ActionInvocationHandler(delegate)));
        logger.info("Interceptor proxy created...");
    }
}
