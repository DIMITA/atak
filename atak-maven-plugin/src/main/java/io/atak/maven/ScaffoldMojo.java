package io.atak.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>atak:scaffold</b> — Generates a ready-to-use entity skeleton annotated with
 * {@code @AtakEntity}.
 *
 * <h3>Usage</h3>
 * <pre>
 * mvn atak:scaffold \
 *   -Datak.entity=Invoice \
 *   -Datak.package=com.example.entity \
 *   -Datak.fields="number:String,amount:Double,status:String"
 * </pre>
 *
 * <h3>Options</h3>
 * <table>
 *   <tr><th>Property</th><th>Default</th><th>Description</th></tr>
 *   <tr><td>atak.entity</td><td><i>required</i></td><td>Entity class name (PascalCase)</td></tr>
 *   <tr><td>atak.package</td><td>com.example.entity</td><td>Target Java package</td></tr>
 *   <tr><td>atak.fields</td><td></td><td>Comma-separated name:Type pairs</td></tr>
 *   <tr><td>atak.outputDir</td><td>src/main/java</td><td>Source root directory</td></tr>
 *   <tr><td>atak.audit</td><td>false</td><td>Add @AtakAudit to the entity</td></tr>
 *   <tr><td>atak.security</td><td>false</td><td>Add @AtakSecurity to the entity</td></tr>
 *   <tr><td>atak.tenant</td><td>false</td><td>Add a tenantId field with @AtakTenant</td></tr>
 * </table>
 */
@Mojo(name = "scaffold")
public class ScaffoldMojo extends AbstractMojo {

    @Parameter(property = "atak.entity", required = true)
    private String entity;

    @Parameter(property = "atak.package", defaultValue = "com.example.entity")
    private String targetPackage;

    /** Comma-separated {@code name:Type} pairs, e.g. {@code name:String,price:Double}. */
    @Parameter(property = "atak.fields", defaultValue = "")
    private String fields;

    @Parameter(property = "atak.outputDir", defaultValue = "${project.basedir}/src/main/java")
    private File outputDir;

    @Parameter(property = "atak.audit", defaultValue = "false")
    private boolean audit;

    @Parameter(property = "atak.security", defaultValue = "false")
    private boolean security;

    @Parameter(property = "atak.tenant", defaultValue = "false")
    private boolean tenant;

    @Override
    public void execute() throws MojoExecutionException {
        String className = capitalize(entity);
        Path targetDir   = outputDir.toPath()
                .resolve(targetPackage.replace('.', File.separatorChar));

        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create directory: " + targetDir, e);
        }

        Path targetFile = targetDir.resolve(className + ".java");
        if (Files.exists(targetFile)) {
            getLog().warn("[ATAK] File already exists — skipping: " + targetFile);
            return;
        }

        String source = buildSource(className);
        try {
            Files.writeString(targetFile, source);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot write file: " + targetFile, e);
        }

        getLog().info("[ATAK] Scaffolded entity → " + targetFile);
        getLog().info("[ATAK] Run `mvn compile` to generate the full API layer.");
    }

    private String buildSource(String className) {
        List<FieldDef> fieldDefs = parseFields();

        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(targetPackage).append(";\n\n");

        // imports
        sb.append("import io.atak.annotation.AtakEntity;\n");
        sb.append("import io.atak.annotation.AtakField;\n");
        if (audit)    sb.append("import io.atak.annotation.AtakAudit;\n");
        if (security) sb.append("import io.atak.annotation.AtakSecurity;\n");
        if (tenant)   sb.append("import io.atak.annotation.AtakTenant;\n");
        sb.append("import jakarta.persistence.*;\n\n");

        // annotations
        if (audit)    sb.append("@AtakAudit\n");
        if (security) sb.append("@AtakSecurity\n");
        sb.append("@AtakEntity\n");
        sb.append("@Entity\n");
        sb.append("@Table(name = \"").append(toSnakeCase(className)).append("s\")\n");
        sb.append("public class ").append(className);
        if (audit) sb.append(" extends ").append(className).append("AuditBase /* generated */");
        sb.append(" {\n\n");

        // id
        sb.append("    @Id\n");
        sb.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
        sb.append("    private Long id;\n\n");

        // tenant field
        if (tenant) {
            sb.append("    @AtakTenant\n");
            sb.append("    @Column(nullable = false)\n");
            sb.append("    private String tenantId;\n\n");
        }

        // user fields
        for (FieldDef f : fieldDefs) {
            sb.append("    @AtakField(description = \"").append(f.name).append("\")\n");
            sb.append("    private ").append(f.type).append(" ").append(f.name).append(";\n\n");
        }

        // getters / setters
        sb.append("    public Long getId() { return id; }\n");
        sb.append("    public void setId(Long id) { this.id = id; }\n\n");
        if (tenant) {
            sb.append("    public String getTenantId() { return tenantId; }\n");
            sb.append("    public void setTenantId(String tenantId) { this.tenantId = tenantId; }\n\n");
        }
        for (FieldDef f : fieldDefs) {
            String cap = capitalize(f.name);
            sb.append("    public ").append(f.type).append(" get").append(cap)
              .append("() { return ").append(f.name).append("; }\n");
            sb.append("    public void set").append(cap).append("(").append(f.type).append(" ")
              .append(f.name).append(") { this.").append(f.name).append(" = ").append(f.name)
              .append("; }\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    private List<FieldDef> parseFields() {
        List<FieldDef> result = new ArrayList<>();
        if (fields == null || fields.isBlank()) return result;

        for (String part : fields.split(",")) {
            String[] kv = part.trim().split(":");
            if (kv.length == 2) {
                result.add(new FieldDef(kv[0].trim(), kv[1].trim()));
            } else if (kv.length == 1 && !kv[0].isBlank()) {
                result.add(new FieldDef(kv[0].trim(), "String"));
            }
        }
        return result;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String toSnakeCase(String s) {
        return s.replaceAll("([A-Z])", "_$1").toLowerCase().replaceFirst("^_", "");
    }

    private record FieldDef(String name, String type) {}
}
