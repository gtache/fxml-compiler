package com.github.gtache.fxml.compiler.loader;

import javafx.fxml.LoadListener;

public class PrintLoadListener implements LoadListener {

    @Override
    public void readImportProcessingInstruction(final String target) {
        System.out.println("import : " + target);
    }

    @Override
    public void readLanguageProcessingInstruction(final String language) {
        System.out.println("language : " + language);
    }

    @Override
    public void readComment(final String comment) {
        System.out.println("comment : " + comment);
    }

    @Override
    public void beginInstanceDeclarationElement(final Class<?> type) {
        System.out.println("begin instance declaration : " + type);
    }

    @Override
    public void beginUnknownTypeElement(final String name) {
        System.out.println("begin unknown type : " + name);
    }

    @Override
    public void beginIncludeElement() {
        System.out.println("beginInclude");
    }

    @Override
    public void beginReferenceElement() {
        System.out.println("beginReference");
    }

    @Override
    public void beginCopyElement() {
        System.out.println("beginCopy");
    }

    @Override
    public void beginRootElement() {
        System.out.println("beginRoot");
    }

    @Override
    public void beginPropertyElement(final String name, final Class<?> sourceType) {
        System.out.println("begin property : " + name + " (" + sourceType + ")");
    }

    @Override
    public void beginUnknownStaticPropertyElement(final String name) {
        System.out.println("begin unknown static property : " + name);
    }

    @Override
    public void beginScriptElement() {
        System.out.println("begin script");
    }

    @Override
    public void beginDefineElement() {
        System.out.println("begin define");
    }

    @Override
    public void readInternalAttribute(final String name, final String value) {
        System.out.println("read internal attribute : " + name + " = " + value);
    }

    @Override
    public void readPropertyAttribute(final String name, final Class<?> sourceType, final String value) {
        System.out.println("read property attribute : " + name + " (" + sourceType + ") = " + value);
    }

    @Override
    public void readUnknownStaticPropertyAttribute(final String name, final String value) {
        System.out.println("read unknown static property attribute : " + name + " = " + value);
    }

    @Override
    public void readEventHandlerAttribute(final String name, final String value) {
        System.out.println("read event handler attribute : " + name + " = " + value);
    }

    @Override
    public void endElement(final Object value) {
        System.out.println("end element : " + value.getClass() + " - " + value);
    }
}
