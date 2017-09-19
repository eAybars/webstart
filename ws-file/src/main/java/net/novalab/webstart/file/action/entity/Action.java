package net.novalab.webstart.file.action.entity;

import net.novalab.webstart.service.artifact.control.ArtifactEvent;

import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;

public class Action {
    private Path domain;
    private ArtifactEvent.Type type;
    private Date lastActionTime;

    public Action(Path domain, ArtifactEvent.Type type) {
        this.domain = Objects.requireNonNull(domain);
        this.type = Objects.requireNonNull(type);
        this.lastActionTime = new Date();
    }

    public Path getDomain() {
        return domain;
    }

    public ArtifactEvent.Type getType() {
        return type;
    }

    public Date getLastActionTime() {
        return lastActionTime;
    }

    public void updateLastActionTimeToNow() {
        this.lastActionTime = new Date();
    }

    public long timeSinceLastAction() {
        return new Date().getTime() - lastActionTime.getTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Action action = (Action) o;

        if (!getDomain().equals(action.getDomain())) return false;
        return getType() == action.getType();
    }

    @Override
    public int hashCode() {
        int result = getDomain().hashCode();
        result = 31 * result + getType().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getType() + " " + getDomain();
    }
}
