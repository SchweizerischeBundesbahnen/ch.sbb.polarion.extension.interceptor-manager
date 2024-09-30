package ch.sbb.polarion.extension.interceptor_manager.guice;

import ch.sbb.polarion.extension.interceptor_manager.model.IActionHook;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class GuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), IActionHook.class);
    }
}
