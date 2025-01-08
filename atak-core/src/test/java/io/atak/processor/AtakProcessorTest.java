package io.atak.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;

class AtakProcessorTest {

    private static final String ENTITY_SOURCE = """
            package com.example;

            import io.atak.annotation.AtakEntity;
            import jakarta.persistence.Entity;
            import jakarta.persistence.Id;

            @AtakEntity
            @Entity
            public class Product {
                @Id
                private Long id;
                private String name;
                private Double price;
            }
            """;

    @Test
    void compilesWithoutErrors() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new AtakProcessor())
                .compile(JavaFileObjects.forSourceString("com.example.Product", ENTITY_SOURCE));

        assertThat(compilation).succeeded();
    }

    @Test
    void generatesRequestDto() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new AtakProcessor())
                .compile(JavaFileObjects.forSourceString("com.example.Product", ENTITY_SOURCE));

        assertThat(compilation).generatedSourceFile("atak.generated.dto.ProductRequestDto");
    }

    @Test
    void generatesResponseDto() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new AtakProcessor())
                .compile(JavaFileObjects.forSourceString("com.example.Product", ENTITY_SOURCE));

        assertThat(compilation).generatedSourceFile("atak.generated.dto.ProductResponseDto");
    }

    @Test
    void generatesRepository() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new AtakProcessor())
                .compile(JavaFileObjects.forSourceString("com.example.Product", ENTITY_SOURCE));

        assertThat(compilation).generatedSourceFile("atak.generated.repository.ProductRepository");
    }

    @Test
    void generatesAbstractService() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new AtakProcessor())
                .compile(JavaFileObjects.forSourceString("com.example.Product", ENTITY_SOURCE));

        assertThat(compilation).generatedSourceFile("atak.generated.service.AbstractProductService");
    }

    @Test
    void generatesController() {
        Compilation compilation = Compiler.javac()
                .withProcessors(new AtakProcessor())
                .compile(JavaFileObjects.forSourceString("com.example.Product", ENTITY_SOURCE));

        assertThat(compilation).generatedSourceFile("atak.generated.controller.ProductController");
    }
}
