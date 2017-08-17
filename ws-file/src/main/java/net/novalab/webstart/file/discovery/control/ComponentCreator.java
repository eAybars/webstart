package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.component.entity.FileBasedComponent;

import java.io.File;
import java.net.URI;
import java.util.function.BiFunction;

public interface ComponentCreator extends BiFunction<URI, File, FileBasedComponent> {
}
