package com.eaybars.webstart.file.watch.entity;

import com.eaybars.webstart.file.watch.control.PathWatchService;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.*;

public class PathWatchServiceEvent {
    private PathWatchService service;
    private WatchEvent<?> source;
    private Path path;

    public PathWatchServiceEvent(PathWatchService service, WatchEvent<?> source, Path path) {
        this.service = service;
        this.source = source;
        this.path = path;
    }

    public PathWatchService getService() {
        return service;
    }

    public WatchEvent<?> getSource() {
        return source;
    }

    public Path getPath() {
        return path;
    }

    public WatchEvent.Kind getKind() {
        return source.kind();
    }

    public boolean isCreate() {
        return getKind() == ENTRY_CREATE;
    }

    public boolean isModify() {
        return getKind() == ENTRY_MODIFY;
    }

    public boolean isDelete() {
        return getKind() == ENTRY_DELETE;
    }
}
