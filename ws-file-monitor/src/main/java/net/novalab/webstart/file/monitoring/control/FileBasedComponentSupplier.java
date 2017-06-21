package net.novalab.webstart.file.monitoring.control;

import net.novalab.webstart.file.discovery.control.ArtifactRoot;
import net.novalab.webstart.file.discovery.control.ComponentScanner;
import net.novalab.webstart.service.application.controller.ComponentEvent;
import net.novalab.webstart.service.application.controller.ComponentSupplier;
import net.novalab.webstart.service.application.controller.ComponentEventImpl;
import net.novalab.webstart.service.application.entity.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.ejb.Timer;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;

/**
 * Created by ertunc on 01/06/17.
 */
@Singleton
@Startup
public class FileBasedComponentSupplier implements ComponentSupplier {
    static final String UPDATE_TIMEOUT_PROPERTY = "UPDATE_TIMEOUT";
    private static final long TRIGGER_DURATION = Long.parseLong(System.getProperty(UPDATE_TIMEOUT_PROPERTY, "60000"));
    private static final Logger LOGGER = Logger.getLogger(FileBasedComponentSupplier.class.getName());

    @Resource
    ManagedThreadFactory threadFactory;
    @Resource
    TimerService timerService;
    @Inject
    Event<Component> componentEvent;
    @Inject
    @ArtifactRoot
    File artifactRoot;


    private Map<String, Task> pendingUpdates = new ConcurrentHashMap<>();
    private volatile List<Component> components = Collections.emptyList();
    private UpdateMonitor monitor;
    private Path root;
    private Function<File, List<? extends Component>> componentScanner;


    @Override
    public Stream<? extends Component> get() {
        return components.stream();
    }

    @PostConstruct
    @Lock(LockType.WRITE)
    public void init(ComponentScanner componentScanner) {
        if (artifactRoot.exists() && artifactRoot.isDirectory()) {
            root = artifactRoot.toPath();
            this.componentScanner = componentScanner.andThen(components -> {
                Event<Component> select = componentEvent.select(new ComponentEventImpl(ComponentEvent.Type.LOADED));
                for (Component component : components) {
                    try {
                        monitor.register(new File(artifactRoot.toURI().resolve(component.getIdentifier())).toPath());
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Cannot register folder listening for component " + component.getIdentifier(), e);
                    }
                }
                components.forEach(select::fire);
                return components;
            });

            try {
                monitor = new UpdateMonitor();
                updateAll();
            } catch (IOException e) {
                throw new RuntimeException(e);//fail deployment
            }

            monitor.addEventConsumer(this::handleCreateEvent);
            monitor.addEventConsumer(this::handleRemoveEvent);

            threadFactory.newThread(monitor::start).start();
        } else if (!artifactRoot.exists()) {
            LOGGER.log(Level.SEVERE, "Artifact root does not exist: " + artifactRoot.getAbsolutePath());
        } else {
            LOGGER.log(Level.SEVERE, "Artifact root is not a directory: " + artifactRoot.getAbsolutePath());
        }
    }


    @Lock(LockType.WRITE)
    public void updateAll() throws IOException {
        pendingUpdates.clear();
        monitor.unregisterAll();
        //monitor root directory for repository creation
        monitor.register(root);

        List<Component> applications = new CopyOnWriteArrayList<>();
        for (File repository : artifactRoot.listFiles(File::isDirectory)) {
            applications.addAll(componentScanner.apply(repository));
        }
        this.components = applications;
    }

    @Timeout
    @Lock(LockType.WRITE)
    public void processPendingUpdates() {
        long now = System.currentTimeMillis();

        for (Iterator<Map.Entry<String, Task>> iterator = pendingUpdates.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Task> entry = iterator.next();
            if (now - entry.getValue().lastAccessed.getTime() > TRIGGER_DURATION) {
                entry.getValue().execute();
                iterator.remove();
            }
        }

        //reset timer if there are some more pending updates to process
        if (!pendingUpdates.isEmpty()) {
            setupTimer();
        }
    }

    private void setupTimer() {
        timerService.getTimers().forEach(Timer::cancel);
        timerService.createTimer(TRIGGER_DURATION, null);
    }

    private void handleCreateEvent(WatchEvent.Kind kind, Path path) {
        if (kind == ENTRY_CREATE) {
            Path relative = root.relativize(path);
            Optional<Task> optionalTask = pendingUpdates.entrySet().stream()
                    .filter(e -> relative.startsWith(e.getKey()))
                    .map(Map.Entry::getValue)
                    .findAny();
            if (optionalTask.isPresent()) {
                Task task = optionalTask.get();
                task.update();
                if (Files.isDirectory(path)) {
                    task.add(path);
                }
            } else if (Files.isDirectory(path)) {
                pendingUpdates.put(relative.toString(), new Task(() -> components.addAll(componentScanner.apply(path.toFile()))));
                setupTimer();
            }
            if (Files.isDirectory(path)) {
                try {
                    monitor.register(path);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Cannot register listening events under: " + relative, e);
                }
            }
        }
    }

    private void handleRemoveEvent(WatchEvent.Kind kind, Path path) {
        if (kind == ENTRY_DELETE) {
            Path relative = root.relativize(path);

            for (Iterator<Map.Entry<String, Task>> iterator = pendingUpdates.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<String, Task> e = iterator.next();
                if (relative.startsWith(e.getKey())) {
                    e.getValue().cancel();
                    iterator.remove();
                }
            }

            Event<Component> select = componentEvent.select(new ComponentEventImpl(ComponentEvent.Type.UNLOADED));
            for (Iterator<Component> iterator = components.iterator(); iterator.hasNext();) {
                Component c = iterator.next();
                if (c.getIdentifier().toString().startsWith(path.toString())) {
                    monitor.unregister(new File(artifactRoot.toURI().resolve(c.getIdentifier())).toPath());
                    iterator.remove();
                    select.fire(c);
                }
            }
        }
    }

    private class Task {
        private Date lastAccessed;
        private List<Path> pathsOfInterest;
        private Runnable task;

        public Task(Runnable task) {
            this.task = task;
            update();
        }

        public void update() {
            lastAccessed = new Date();
            pathsOfInterest = new LinkedList<>();
        }

        public void add(Path path) {
            pathsOfInterest.add(path);
        }

        public void cancel() {
            pathsOfInterest.forEach(monitor::unregister);
        }

        public void execute() {
            pathsOfInterest.forEach(monitor::unregister);
            task.run();
        }
    }


}
