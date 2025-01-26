package io.atak.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>atak:sync</b> — Validates that generated sources are consistent with the current
 * state of the annotation processor output.
 *
 * <p>Compares files in {@code target/generated-sources/annotations/atak/generated/}
 * with files previously ejected to {@code src/main/java/atak/generated/} and reports
 * any divergence (new files, deleted files, changed files).
 *
 * <h3>Usage</h3>
 * <pre>
 * mvn compile atak:sync
 * </pre>
 *
 * <h3>Options</h3>
 * <table>
 *   <tr><th>Property</th><th>Default</th><th>Description</th></tr>
 *   <tr><td>atak.generatedSourcesDir</td><td>target/generated-sources/annotations</td>
 *       <td>Processor output directory</td></tr>
 *   <tr><td>atak.sourceDir</td><td>src/main/java</td><td>Directory to compare against</td></tr>
 *   <tr><td>atak.failOnDrift</td><td>false</td><td>Fail the build when drift is detected</td></tr>
 * </table>
 */
@Mojo(name = "sync")
public class SyncMojo extends AbstractMojo {

    @Parameter(property = "atak.generatedSourcesDir",
               defaultValue = "${project.build.directory}/generated-sources/annotations")
    private File generatedSourcesDir;

    @Parameter(property = "atak.sourceDir",
               defaultValue = "${project.basedir}/src/main/java")
    private File sourceDir;

    @Parameter(property = "atak.failOnDrift", defaultValue = "false")
    private boolean failOnDrift;

    @Override
    public void execute() throws MojoExecutionException {
        Path gen  = generatedSourcesDir.toPath();
        Path src  = sourceDir.toPath();

        if (!Files.exists(gen)) {
            throw new MojoExecutionException(
                    "[ATAK] Generated sources directory not found: " + gen
                    + "\nRun `mvn compile` first.");
        }

        List<String> newFiles      = new ArrayList<>();
        List<String> changedFiles  = new ArrayList<>();
        List<String> missingFiles  = new ArrayList<>();

        try {
            Files.walkFileTree(gen, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relative  = gen.relativize(file);
                    Path inSource  = src.resolve(relative);

                    if (!Files.exists(inSource)) {
                        newFiles.add(relative.toString());
                    } else {
                        byte[] genContent = Files.readAllBytes(file);
                        byte[] srcContent = Files.readAllBytes(inSource);
                        if (!java.util.Arrays.equals(genContent, srcContent)) {
                            changedFiles.add(relative.toString());
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            // Check for files in src that no longer exist in generated
            Path genAtakPkg = gen.resolve("atak").resolve("generated");
            Path srcAtakPkg = src.resolve("atak").resolve("generated");
            if (Files.exists(srcAtakPkg)) {
                Files.walkFileTree(srcAtakPkg, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path relative = src.relativize(file);
                        if (!Files.exists(gen.resolve(relative))) {
                            missingFiles.add(relative.toString());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Sync check failed", e);
        }

        boolean hasDrift = !newFiles.isEmpty() || !changedFiles.isEmpty() || !missingFiles.isEmpty();

        if (!hasDrift) {
            getLog().info("[ATAK] ✓ Generated sources are in sync with the annotation processor output.");
            return;
        }

        getLog().warn("[ATAK] ⚠ Drift detected between generated sources and source tree:");

        if (!newFiles.isEmpty()) {
            getLog().warn("  NEW (in generated, not ejected yet):");
            newFiles.forEach(f -> getLog().warn("    + " + f));
        }
        if (!changedFiles.isEmpty()) {
            getLog().warn("  CHANGED (processor output differs from ejected source):");
            changedFiles.forEach(f -> getLog().warn("    ~ " + f));
        }
        if (!missingFiles.isEmpty()) {
            getLog().warn("  REMOVED (ejected but no longer generated):");
            missingFiles.forEach(f -> getLog().warn("    - " + f));
        }

        getLog().warn("[ATAK] Run `mvn compile atak:eject -Datak.overwrite=true` to re-sync.");

        if (failOnDrift) {
            throw new MojoExecutionException("[ATAK] Build failed due to generated-source drift.");
        }
    }
}
