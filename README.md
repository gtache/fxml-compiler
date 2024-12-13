# FXML Compiler

## Introduction

This projects aims at generating Java code from FXML files.

## Requirements

- Java 21 (at least for the plugin, the generated code can be compatible with older Java versions)
- Maven 3.8.0

## Installation

Add the plugin to your project:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>com.github.gtache</groupId>
            <artifactId>fxml-compiler-maven-plugin</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Optionally add dependencies to the plugin (e.g. when using MediaView and controlsfx):

```xml

<build>
    <plugins>
        <plugin>
            <groupId>com.github.gtache</groupId>
            <artifactId>fxml-compiler-maven-plugin</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                    </goals>
                </execution>
            </executions>
            <dependencies>
                <dependency>
                    <groupId>org.openjfx</groupId>
                    <artifactId>javafx-media</artifactId>
                    <version>${javafx.version}</version>
                </dependency>
                <dependency>
                    <groupId>org.controlsfx</groupId>
                    <artifactId>controlsfx</artifactId>
                    <version>${controlsfx.version}</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

## Advantages

- Compile-time validation
- Faster startup speed for the application
- Possibility to use controller factories to instantiate controllers with final fields
- Easier time with JPMS
    - No need to open the controllers packages to javafx.fxml
    - No need to open the resources packages when using use-image-inputstream-constructor (if images or resource bundles
      are in the project resources)

## Disadvantages

- Possible bugs (file an issue if you see one)
- Expression binding is limited
- Probably not fully compatible with all FXML features (file an issue if you need one in specific)

## Parameters

### Field injection

There are four ways to inject fields into a controller:

- `REFLECTION`: Inject fields using reflection (like FXMLLoader)
-
    - ```java
      try {
        final var field = controller.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, object);
      } catch (final NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Error using reflection on " + fieldName, e);
      }
      ```
-
    - Slowest method
-
    - Fully compatible with FXMLLoader, so this allows easy switching between the two.
-
    - This is the default injection method (for compatibility reasons).
- `ASSIGN`: variable assignment
-
    - `controller.field = value`
-
    - This means that the field must be accessible from the view (e.g. package-private).
- `SETTERS`: controller setters methods
-
    - `controller.setField(value)`
- `FACTORY`: controller factory
-
    - `controller = factory.create(fieldMap)`
-
    - `factory` is a `ControllerFactory` instance that is created at runtime and passed to the view.
-
    - `fieldMap` is a map of field name (String) to value (Object) that is computed during the view `load` method.
-
    - This allows the controller to have final fields.

### Method injections

There are two ways to inject methods (meaning use them as event handlers) into a controller:

- `REFLECTION`: Inject methods using reflection (like FXMLLoader)
-
    - ```java
      try {
        final java.lang.reflect.Method method;
        final var methods = java.util.Arrays.stream(controller.getClass().getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName)).toList();
        if (methods.size() > 1) {
            final var eventMethods = methods.stream().filter(m ->
                    m.getParameterCount() == 1 && javafx.event.Event.class.isAssignableFrom(m.getParameterTypes()[0])).toList();
            if (eventMethods.size() == 1) {
                method = eventMethods.getFirst();
            } else {
                final var emptyMethods = methods.stream().filter(m -> m.getParameterCount() == 0).toList();
                if (emptyMethods.size() == 1) {
                    method = emptyMethods.getFirst();
                } else {
                    throw new IllegalArgumentException("Multiple matching methods for " + methodName);
                }
            }
        } else if (methods.size() == 1) {
            method = methods.getFirst();
        } else {
            throw new IllegalArgumentException("No matching method for " + methodName);
        }
        method.setAccessible(true);
        if (method.getParameterCount() == 0) {
            method.invoke(controller);
        } else {
            method.invoke(controller, event);
        }
      } catch (final IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {
        throw new RuntimeException("Error using reflection on " + methodName, ex);
      }
      ```
-
    - Slowest method
-
    - Fully compatible with FXMLLoader, so this allows easy switching between the two.
-
    - This is the default injection method (for compatibility reasons).
- `REFERENCE`: Directly reference the method
-
    - `controller.method(event)`
-
    - This means that the method must be accessible from the view (e.g. package-private).

### Resource bundle injection

There are three ways to inject resource bundles into a controller:

- `CONSTRUCTOR`: Inject resource bundle in the view constructor
-
    - ```java
      view = new View(controller, resourceBundle);
      ```
-
    - This is the default injection method because it is the most similar to FXMLLoader (
      `FXMLLoader.setResources(resourceBundle)`).
- `CONSTRUCTOR_FUNCTION`: Injects a function in the view constructor
-
    - `bundleFunction.apply(key)`
-
    - The function takes a string (the key) and returns a string (the value)
    - This allows using another object than a resource bundle for example
- `GETTER`: Retrieves the resource bundle using a controller getter method
-
    - `controller.resources()`
-
    - The method name (resources) was chosen because it matches the name of the field injected by FXMLLoader.
-
    - The method must be accessible from the view (e.g. package-private).
- `GET-BUNDLE`: Injects the bundle name in the view constructor and retrieves it using
  `ResourceBundle.getBundle(bundleName)`
-
    - `ResourceBundle.getBundle(bundleName)`
    - Also used when fx:include specifies a resource attribute to pass it to the included view.

## View creation

The views are generated in the same packages as the FXML files.  
The name of the class is generated from the name of the FXML file.

The constructor of the view is generated depending on the parameters of the plugin.    
The constructor will have as many arguments as the number of controllers in the FXML tree (recursive fx:include) +
potentially the resource bundle if necessary. If no resource reference (`%key.to.resource`) is found in the FXML tree or
if all the includes using references specify a resources attribute, the argument is not created.

The type of the constructor arguments will either be the controller instance or the controller factory (a function of
fields map -> controller).     
The resource bundle argument will either be the resource bundle instance, the resource bundle name or a function of
string ->
string.

The smallest constructor will have only one argument: The controller (or controller factory).

## Maven Plugin

### Parameters

- output-directory
-
    - The output directory of the generated classes
-
    - default: `${project.build.directory}/generated-sources/java`)
- target-version
-
    - The target Java version for the generated code
    - default: `21`
    - minimum: `8`
    - File an issue if the generated code is not compatible with the target version
- use-image-inputstream-constructor
-
    - Use the InputStream constructor for Image instead of the String (URL) one.
-
    - default: `true`
    - Disables background loading
- field-injection
-
    - The type of field injections to use (see [Field injection](#field-injection))
    - default: `REFLECTION`
- method-injection
-
    - The type of method injections to use (see [Method injection](#method-injection))
    - default: `REFLECTION`
- bundle-injection
-
    - The type of resource bundle injection to use (see [Resource bundle injection](#resource-bundle-injection))
    - default: `CONSTRUCTOR`
- bundle-map
-
    - A map of resource bundle name to resource bundle path
    - Used with `GET-BUNDLE` injection
    - default: `{}`

### Limitations

- Given that the plugin operates during the `generate-sources` phase, it doesn't have access to the classes of the
  application.
-
    - The controller info (fields, methods) is obtained from the source file and may therefore be inaccurate.
-
    - Custom classes instantiated in the FXML files are not available during generation and may therefore cause it to
      fail.
- If the application uses e.g. WebView, the javafx-web dependency must be added to the plugin dependencies.