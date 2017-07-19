package net.novalab.webstart.file.discovery.entity;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class AppFilter implements Predicate<String> {
    private static final List<String> VALID_EXTENSIONS = Arrays.asList(
            ".jnlp",
            ".jar",
            "jar.pack.gz",
            ".properties"
    );

    @Override
    public boolean test(String s) {
        String fileName = s.substring(s.lastIndexOf("/") + 1);
        return VALID_EXTENSIONS.stream().allMatch(fileName::endsWith);
    }
}
