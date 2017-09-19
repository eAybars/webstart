package net.novalab.webstart.file.watch.control;/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import net.novalab.webstart.file.watch.entity.PathWatchServiceEvent;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Example to watch a directory (or tree) for changes to files.
 */
public class PathWatchService {

    private static Logger LOGGER = Logger.getLogger(PathWatchService.class.getName());

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final NavigableMap<Path, WatchKey> paths;
    private final List<EventListener> eventListeners;
    private volatile boolean running;

    /**
     * Creates a WatchService and registers the given directory
     */
    public PathWatchService() throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new ConcurrentHashMap<>();
        this.paths = new TreeMap<>();
        this.eventListeners = new CopyOnWriteArrayList<>();
    }

    public void addEventListener(EventListener c) {
        eventListeners.add(c);
    }

    public EventListener addEventListener(Consumer<PathWatchServiceEvent> c) {
        EventListener ec = new CompositionEventListener(c);
        eventListeners.add(ec);
        return ec;
    }

    public EventListener addEventListener(Consumer<PathWatchServiceEvent> c, Predicate<PathWatchServiceEvent> p) {
        EventListener ec = new CompositionEventListener(c, p);
        eventListeners.add(ec);
        return ec;
    }

    public <T extends Predicate<PathWatchServiceEvent> & Consumer<PathWatchServiceEvent>> EventListener addEventListener(T c) {
        EventListener ec = new CompositionEventListener(c, c);
        eventListeners.add(ec);
        return ec;
    }

    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    public void register(Path dir) throws IOException {
        if (!paths.containsKey(dir)) {
            WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            keys.put(key, dir);
            paths.put(dir, key);
        }
    }

    public void unregister(Path dir) {
        WatchKey key = paths.remove(dir);
        if (key != null) {
            keys.remove(key);
            key.cancel();
        }
    }

    public void unregisterAll(Path start) {
        NavigableMap<Path, WatchKey> candidates = paths.tailMap(start, true);
        boolean removed = true;
        while (removed && !candidates.isEmpty()) {
            if (removed = candidates.firstKey().startsWith(start)) {
                candidates.remove(candidates.firstKey());
            }
        }
    }

    public void unregisterAll(Set<Path> startingPaths) {
        startingPaths.forEach(this::unregisterAll);
    }

    public void unregisterAll() {
        keys.keySet().forEach(WatchKey::cancel);
        keys.clear();
        paths.clear();
    }


    /**
     * Register the given directory, and stream its sub-directories, with the
     * WatchService.
     */
    public void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }


    /**
     * Process stream events for keys queued to the watcher
     */
    public void start() {
        if (running) {
            return;
        }
        synchronized (this) {
            if (running) {
                return;
            } else {
                running = true;
            }
        }
        for (; !Thread.currentThread().isInterrupted(); ) {
            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                LOGGER.log(Level.WARNING, "WatchKey not recognized", key);
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                if (kind == ENTRY_DELETE) {
                    try {
                        unregister(child);
                    } catch (Exception e) {
                        //ignore failure
                    }
                }

                //notify consumers
                PathWatchServiceEvent e = new PathWatchServiceEvent(this, event, child);
                eventListeners.stream()
                        .filter(c -> c.test(e))
                        .forEach(c -> c.accept(e));
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // stream directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }


    public interface EventListener extends Consumer<PathWatchServiceEvent>, Predicate<PathWatchServiceEvent> {
        @Override
        default boolean test(PathWatchServiceEvent pathWatchServiceEvent) {
            return true;
        }
    }

    private static class CompositionEventListener implements EventListener {
        private Consumer<PathWatchServiceEvent> consumer;
        private Predicate<PathWatchServiceEvent> predicate;

        public CompositionEventListener(Consumer<PathWatchServiceEvent> consumer) {
            this(consumer, e -> true);
        }

        public CompositionEventListener(Consumer<PathWatchServiceEvent> consumer, Predicate<PathWatchServiceEvent> predicate) {
            this.consumer = consumer;
            this.predicate = predicate;
        }

        @Override
        public void accept(PathWatchServiceEvent pathWatchServiceEvent) {
            consumer.accept(pathWatchServiceEvent);
        }

        @Override
        public boolean test(PathWatchServiceEvent pathWatchServiceEvent) {
            return predicate.test(pathWatchServiceEvent);
        }
    }

}