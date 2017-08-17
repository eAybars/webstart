package net.novalab.webstart.service.component.control;

import net.novalab.webstart.service.component.entity.Component;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by ertunc on 30/05/17.
 */
public interface ComponentSupplier extends Supplier<Stream<? extends Component>> {
}
