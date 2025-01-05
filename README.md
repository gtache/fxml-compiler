# FXML Compiler

## The project is not yet published to Maven Central.

## Introduction

This project aims at generating Java code from FXML files.

## Requirements

- Java 21+ for the plugin
    - The generated code can be compatible with older java versions.
- Maven 3.6.3+

## Installation

Add the plugin to your project:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>ch.gtache.fxml-compiler</groupId>
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
            <groupId>ch.gtache.fxml-compiler</groupId>
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
- Very basic support for bidirectional bindings
- Easier time with JPMS
    - No need to open the controllers packages to javafx.fxml
    - No need to open the resources packages when using use-image-inputstream-constructor (if images or resource bundles
      are in the project resources)

## Disadvantages

- `fx:script` is not supported
- Expect some bugs (file an issue if you see one)
- Expression binding support is very basic
- Probably not fully compatible with all FXML features (file an issue if you need one in specific)
- All fxml files must have a `fx:controller` attribute

## Parameters

### Controller injection

There are two ways to inject controllers into a view:

- `INSTANCE`: Inject the controller instance
    - This is the default injection method
- `FACTORY`: Inject the controller factory
    - This injection method is required if the FXML tree contains multiple times the same controller class.
    - By default, the factory is a `Supplier<Controller>`, but if used in conjunction with `field-injection`set to
      `FACTORY`, the factory is a `Function<Map<String, Object>, Controller>`.

### Field injection

There are four ways to inject fields into a controller:

- `REFLECTION`: Inject fields using reflection (like FXMLLoader)
    - Slowest method
    - Fully compatible with FXMLLoader, so this allows easy switching between the two.
    - This is the default injection method (for compatibility reasons).
- `ASSIGN`: variable assignment
    - `controller.field = value`
    - This means that the field must be accessible from the view (e.g. package-private).
- `SETTERS`: controller setters methods
    - `controller.setField(value)`
- `FACTORY`: controller factory
    - `controller = factory.apply(fieldMap)`
    - `factory` is a `Function<Map<String, Object>, Controller>` instance that is created at runtime and passed to the
      view.
    - `fieldMap` is a map of field name (String) to value (Object) that is computed during the view `load` method.
    - This allows the controller to have final fields.
    - This also forces the `controller-injection` method to be `FACTORY`.

### Method injection

There are two ways to inject methods (meaning use them as event handlers) into a controller:

- `REFLECTION`: Inject methods using reflection (like FXMLLoader)
    - Slowest method
    - Fully compatible with FXMLLoader, so this allows easy switching between the two.
    - This is the default injection method (for compatibility reasons).
- `REFERENCE`: Directly reference the method
    - `controller.method(event)`
    - This means that the method must be accessible from the view (e.g. package-private).

### Resource bundle injection

There are five ways to inject resource bundles into a controller:

- `CONSTRUCTOR`: Inject resource bundle in the view constructor
    - `view = new View(controller, resourceBundle)`
    - This is the default injection method.
- `CONSTRUCTOR_FUNCTION`: Injects a function in the view constructor
    - `bundleFunction.apply(key)`
    - The function takes a string (the key) and returns a string (the value).
    - This allows using another object than a resource bundle for example.
- `CONSTRUCTOR_NAME`: Injects the resource bundle name in the view constructor
    - `ResourceBundle.getBundle(bundleName)`
    - Just for the convenience of not having to create the resource bundle instance outside the view.
- `GETTER`: Retrieves the resource bundle using a controller getter method
    - `controller.resources()`
    - The method name (resources) was chosen because it matches the name of the field injected by FXMLLoader.
    - The method must be accessible from the view (e.g. package-private).
    - **This ignores the `resources` attribute of fx:include.**
- `GET-BUNDLE`: Retrieves the resource bundle using a resource path
    - The resource path is passed to the generator (see [Maven Plugin](#maven-plugin)).
    - The resource path will therefore be a constant in the view class.
    - **This ignores the `resources` attribute of fx:include.**

## View creation

The views are generated in the same packages as the FXML files.  
The name of the class is generated from the name of the FXML file.

The constructor of the view is generated depending on the parameters of the plugin.    
The constructor will have as many arguments as the number of controllers in the FXML tree (recursive fx:include) plus
potentially the resource bundle if necessary. If no resource reference (`%key.to.resource`) is found in the FXML tree or
if all the fx:includes using references specify a `resources` attribute, the argument is not created.

The type of the constructor arguments will either be the controller instance or the controller factory.     
The resource bundle argument will either be the resource bundle instance, the resource bundle name or a function of
string -> string.

The smallest constructor will have only one argument: The controller (or controller factory).

## Maven Plugin

### Parameters

- output-directory
    - The output directory of the generated classes
    - default: `${project.build.directory}/generated-sources/java`)
- target-version
    - The target Java version for the generated code
    - default: `21`
    - minimum: `8`
    - File an issue if the generated code is not compatible with the target version
- use-image-inputstream-constructor
    - Use the InputStream constructor for Image instead of the String (URL) one.
    - default: `true`
    - Disables background loading
- controller-injection
    - The type of controller injections to use (see [Controller injection](#controller-injection))
    - default: `INSTANCE`
- field-injection
    - The type of field injections to use (see [Field injection](#field-injection))
    - default: `REFLECTION`
- method-injection
    - The type of method injections to use (see [Method injection](#method-injection))
    - default: `REFLECTION`
- bundle-injection
    - The type of resource bundle injection to use (see [Resource bundle injection](#resource-bundle-injection))
    - default: `CONSTRUCTOR`
- bundle-map
    - A map of resource bundle name to resource bundle path
    - Used with `GET-BUNDLE` injection
    - default: `{}`
- parallelism
    - The number of threads to use for compilation
    - default: `1` (no multithreading)
    - if `<1`, the number of available cores will be used

### Limitations

- Given that the plugin operates during the `generate-sources` phase, it doesn't have access to the classes of the
  application.
    - The controller info (fields, methods) is obtained from the source file and may therefore be inaccurate.
    - Custom classes instantiated in the FXML files are not available during generation and may therefore cause it to
      fail.
        - These classes must therefore be in a separate dependency.
- If the application uses e.g. WebView, the javafx-web dependency must be added to the plugin dependencies.