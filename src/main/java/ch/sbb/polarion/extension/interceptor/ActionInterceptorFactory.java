package ch.sbb.polarion.extension.interceptor;

import com.polarion.core.util.logging.Logger;
import com.polarion.platform.persistence.IDataService;
import com.polarion.platform.persistence.model.IPObject;
import com.polarion.platform.persistence.model.IPObjectFactory;
import com.polarion.platform.persistence.model.IPrototype;
import com.polarion.subterra.base.SubterraURI;
import com.polarion.subterra.base.data.object.IDataObject;
import org.apache.hivemind.InterceptorStack;
import org.apache.hivemind.ServiceInterceptorFactory;
import org.apache.hivemind.internal.Module;

import java.util.List;

/*
 * Intercept creation of polarion objects to make sure that the proxy IDataService instance is used.
 * Default implementation - Nothing to change
 */
public class ActionInterceptorFactory implements ServiceInterceptorFactory {

    private static final Logger logger = Logger.getLogger(ActionInterceptorFactory.class);

    private final IDataService dataService;

    public ActionInterceptorFactory(IDataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public void createInterceptor(InterceptorStack stack, Module arg1, List arg2) {
        final IPObjectFactory delegate = (IPObjectFactory) stack.peek();
        stack.push(new IPObjectFactory() {

            @SuppressWarnings("rawtypes")
            @Override
            public Class getCommonSuperclass() {
                return delegate.getCommonSuperclass();
            }

            @Override
            public IPObject createObjectForURI(SubterraURI uri, IDataService dataService) {
                return delegate.createObjectForURI(uri, ActionInterceptorFactory.this.dataService);
            }

            @Override
            public IPObject createObjectForDAO(IDataObject dao, IDataService dataService) {
                return delegate.createObjectForDAO(dao, ActionInterceptorFactory.this.dataService);
            }

            @Override
            public IPObject createNewObject(IPrototype prototype, IDataService dataService) {
                return delegate.createNewObject(prototype, ActionInterceptorFactory.this.dataService);
            }
        });
        logger.info("Factory created...");
    }
}
