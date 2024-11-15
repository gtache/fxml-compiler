package com.github.gtache.fxml.compiler.loader;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

@Fork(1)
@State(Scope.Benchmark)
public class CreationBenchmark {

    public static void main(final String[] args) throws java.io.IOException {
        org.openjdk.jmh.Main.main(args);
    }

    @Setup(Level.Trial)
    public void setup() {
        Platform.startup(() -> {
        });
    }

    @Benchmark
    public Object useFXMLLoader() {
        return CompletableFuture.supplyAsync(() -> {
            final var loader = new FXMLLoader(getClass().getResource("/com/github/gtache/fxml/compiler/loader/includeView.fxml"));
            loader.setResources(ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.IncludeBundle"));
            try {
                return loader.load();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }, Platform::runLater).join();
    }

    @Benchmark
    public Object useFXMLLoaderInclude() {
        return CompletableFuture.supplyAsync(() -> {
            final var loader = new FXMLLoader(getClass().getResource("/com/github/gtache/fxml/compiler/loader/testView.fxml"));
            loader.setResources(ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.TestBundle"));
            try {
                return loader.load();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }, Platform::runLater).join();
    }

    @Benchmark
    public Object useReflection() {
        return CompletableFuture.supplyAsync(() -> {
            final var view = new ReflectionIncludeView(new IncludeController(), ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.IncludeBundle"));
            return view.load();
        }, Platform::runLater).join();
    }

    @Benchmark
    public Object useReflectionInclude() {
        return CompletableFuture.supplyAsync(() -> {
            final var view = new ReflectionTestView(Map.of(TestController.class, new TestController(), IncludeController.class, new IncludeController()), Map.of("includeView.fxml", IncludeController.class),
                    Map.of(TestController.class, ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.TestBundle"),
                            IncludeController.class, ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.IncludeBundle")));
            return view.load();
        }, Platform::runLater).join();
    }

    @Benchmark
    public Object useAssignment() {
        return CompletableFuture.supplyAsync(() -> {
            final var view = new AssignIncludeView(new IncludeController(), ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.IncludeBundle"));
            return view.load();
        }, Platform::runLater).join();
    }

    @Benchmark
    public Object useAssignmentInclude() {
        return CompletableFuture.supplyAsync(() -> {
            final var view = new AssignTestView(Map.of(TestController.class, new TestController(), IncludeController.class, new IncludeController()), Map.of("includeView.fxml", IncludeController.class),
                    Map.of(TestController.class, ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.TestBundle"),
                            IncludeController.class, ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.IncludeBundle")));
            return view.load();
        }, Platform::runLater).join();
    }

    @Benchmark
    public Object useFactory() {
        return CompletableFuture.supplyAsync(() -> {
            final var view = new FactoryIncludeView(c -> new IncludeController(), ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.IncludeBundle"));
            return view.load();
        }, Platform::runLater).join();
    }

    @Benchmark
    public Object useFactoryInclude() {
        return CompletableFuture.supplyAsync(() -> {
            final var view = new FactoryIncludeView(Map.of(TestController.class, c -> new TestController(), IncludeController.class, c -> new IncludeController()), Map.of("includeView.fxml", IncludeController.class),
                    Map.of(TestController.class, ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.TestBundle"),
                            IncludeController.class, ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.IncludeBundle")));
            return view.load();
        }, Platform::runLater).join();
    }

    @Benchmark
    public Object useSetters() {
        return CompletableFuture.supplyAsync(() -> {
            final var view = new SettersIncludeView(new IncludeController(), ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.IncludeBundle"));
            return view.load();
        }, Platform::runLater).join();
    }

    @Benchmark
    public Object useSettersInclude() {
        return CompletableFuture.supplyAsync(() -> {
            final var view = new SettersIncludeView(Map.of(TestController.class, new TestController(), IncludeController.class, new IncludeController()), Map.of("includeView.fxml", IncludeController.class),
                    Map.of(TestController.class, ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.TestBundle"),
                            IncludeController.class, ResourceBundle.getBundle("com.github.gtache.fxml.compiler.loader.IncludeBundle")));
            return view.load();
        }, Platform::runLater).join();
    }
}
