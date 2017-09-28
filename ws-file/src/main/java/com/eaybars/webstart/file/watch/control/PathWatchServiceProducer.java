package com.eaybars.webstart.file.watch.control;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.IOException;

@ApplicationScoped
public class PathWatchServiceProducer {
    @Resource
    ManagedThreadFactory threadFactory;
    @Inject
    Instance<PathWatchService.EventListener> eventConsumers;

    private PathWatchService pathWatchService;

    @PostConstruct
    public void init() {
        try {
            pathWatchService = new PathWatchService();
            eventConsumers.forEach(pathWatchService::addEventListener);
            threadFactory.newThread(pathWatchService::start).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Produces
    @ApplicationScoped
    public PathWatchService getPathWatchService() {
        return pathWatchService;
    }
}
