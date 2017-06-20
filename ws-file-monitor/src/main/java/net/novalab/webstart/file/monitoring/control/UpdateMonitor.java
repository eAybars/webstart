package net.novalab.webstart.file.monitoring.control;/*
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

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Example to watch a directory (or tree) for changes to files.
 */
public class UpdateMonitor {

    private static Logger LOGGER = Logger.getLogger(UpdateMonitor.class.getName());
    private static final BiConsumer<WatchEvent.Kind, Path> EMPTY = (k, p) -> {
    };

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final List<BiConsumer<WatchEvent.Kind, Path>> eventConsumers;
    private final Map<Path, BiConsumer<WatchEvent.Kind, Path>> pathEventConsumers;
    private volatile boolean running;

    /**
     * Creates a WatchService and registers the given directory
     */
    public UpdateMonitor() throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new ConcurrentHashMap<>();
        this.eventConsumers = new CopyOnWriteArrayList<>();
        this.pathEventConsumers = new ConcurrentHashMap<>();
    }

    public void addEventConsumer(BiConsumer<WatchEvent.Kind, Path> c) {
        eventConsumers.add(c);
    }

    public void removeEventConsumer(BiConsumer<WatchEvent.Kind, Path> c) {
        eventConsumers.remove(c);
    }

    public void addEventConsumer(Path path, BiConsumer<WatchEvent.Kind, Path> c) {
        pathEventConsumers.put(path, c);
    }

    public void removeEventConsumer(Path path) {
        pathEventConsumers.remove(path);
    }


    @SuppressWarnings("unchecked")
    private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    public void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    public void unregister(Path dir) {
        for (Iterator<Map.Entry<WatchKey, Path>> iterator = keys.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<WatchKey, Path> entry = iterator.next();
            if (entry.getValue().equals(dir)) {
                iterator.remove();
                entry.getKey().cancel();
            }
        }
    }

    public void unregisterAll(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                unregister(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void unregisterAll() {
        Set<WatchKey> watchKeys = new HashSet<>(keys.keySet());
        watchKeys.forEach(WatchKey::cancel);
        this.keys.keySet().removeAll(watchKeys);
    }


    /**
     * Register the given directory, and all its sub-directories, with the
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
     * Process all events for keys queued to the watcher
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

//                System.out.format("%s: %s\n", event.kind().name(), child);

                //notify consumers
                eventConsumers.forEach(c -> c.accept(kind, child));
                pathEventConsumers.getOrDefault(dir, EMPTY).accept(kind, child);
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }


}