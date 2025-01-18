package io.atak.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * <b>atak:eject</b> — Copies all ATAK-generated source files from
 * {@code target/generated-sources/annotations/} into {@code src/main/java/}.
 *
 * <p>After ejecting, the generated code becomes plain source files you own.
 * You can then remove the ATAK processor dependency and edit freely.
 *
 * <h3>Usage</h3>
 * <pre>
 * mvn compile atak:eject
 * </pre>
 *
 * <h3>Options</h3>
 * <table>
 *   <tr><th>Property</th><th>Default</th><th>Description</th></tr>
 *   <tr><td>atak.generatedSourcesDir</td><td>target/generated-sources/annotations</td>
 *       <td>Source of generated files</td></tr>
 *   <tr><td>atak.outputDir</td><td>src/main/java</td><td>Destination directory</td></tr>
 *   <tr><td>atak.overwrite</td><td>false</td><td>Overwrite existing files</td></tr>
 *   <tr><td>atak.dryRun</td><td>false</td><td>Print what would be copied without writing</td></tr>
 * </table>
 *
 * <p><b>Warning:</b> ejecting is irreversible. Commit your current work before running.
 */
@Mojo(name = "eject")
public class EjectMojo extends AbstractMojo {

    @Parameter(property = "atak.generatedSourcesDir",
               defaultValue = "${project.build.directory}/generated-sources/annotations")
    private File generatedSourcesDir;

    @Parameter(property = "atak.outputDir",
               defaultValue = "${project.basedir}/src/main/java")
    private File outputDir;

    @Parameter(property = "atak.overwrite", defaultValue = "false")
    private boolean overwrite;

    @Parameter(property = "atak.dryRun", defaultValue = "false")
    private boolean dryRun;

    @Override
    public void execute() throws MojoExecutionException {
        Path src  = generatedSourcesDir.toPath();
        Path dest = outputDir.toPath();

        if (!Files.exists(src)) {
            throw new MojoExecutionException(
                    "[ATAK] Generated sources directory not found: " + src
                    + "\nRun `mvn compile` first.");
        }

        if (dryRun) {
            getLog().info("[ATAK] DRY RUN — no files will be written.");
        }

        int[] counts = {0, 0, 0}; // copied, skipped, errors

        try {
            Files.walkFileTree(src, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relative = src.relativize(file);
                    Path target   = dest.resolve(relative);

                    if (Files.exists(target) && !overwrite) {
                        getLog().debug("[ATAK] SKIP (exists): " + relative);
                        counts[1]++;
                        return FileVisitResult.CONTINUE;
                    }

                    if (!dryRun) {
                        Files.createDirectories(target.getParent());
                        Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                    }

                    getLog().info("[ATAK] " + (dryRun ? "WOULD COPY" : "COPIED") + " → " + relative);
                    counts[0]++;
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new MojoExecutionException("Eject failed", e);
        }

        getLog().info(String.format(
                "[ATAK] Eject complete — %d copied, %d skipped.", counts[0], counts[1]));

        if (!dryRun && counts[0] > 0) {
            getLog().info("[ATAK] You can now remove the ATAK processor from your annotationProcessorPaths.");
        }
    }
}
