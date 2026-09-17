package io.github.qdiaps.domain.command;

public interface Command {
    void execute(String[] payload);
}
