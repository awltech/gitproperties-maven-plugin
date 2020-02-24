package com.github.awltech.mojos.gitproperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Maven MOJO that writes properties into manifest, properties from the project.
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 * @since 0.1.0
 * 
 */
@Mojo(name = "write-properties", requiresProject = true)
public class GitPropertiesWriterMojo extends AbstractMojo {

	/*
	 * Current Maven project, automatically injected, that will be updated.
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	/*
	 * List of the parameters to write
	 */
	@Parameter(property = "parameters", required = true, readonly = true)
	private String[] parameters;

	/*
	 * Folder where the file will be generated. If not specified, will attempt
	 * to build destination
	 */
	@Parameter(property = "destination", required = false, readonly = true)
	private String destination;

	/*
	 * Path to the file itseld, in destination. Defaults to
	 * META-INF/GIT-MANIFEST.MF
	 */
	@Parameter(property = "manifest-path", required = false, readonly = true)
	private String manifestPath = "META-INF" + File.separator + "GIT-MANIFEST.MF";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Pre-checks
		String destination = this.destination != null ? this.destination : getDefaultDestination();
		if (destination == null) {
			throw new MojoFailureException("Could not resolve destination. Maybe you missed the destination parameter ?");
		}
		if (manifestPath == null || manifestPath.length() == 0) {
			throw new MojoFailureException("Name for manifest file cannot be null or empty");
		}

		// Pre-checks
		if (parameters == null || parameters.length == 0) {
			getLog().warn("No parameters specified. Returning...");
			return;
		}

		// Resolves the properties to write to manifest
		Properties projectProperties = this.project.getProperties();
		Map<String, String> propertiesToWrite = new HashMap<String, String>();
		if (projectProperties != null) {
			for (String parameter : parameters) {
				if (parameter != null && projectProperties.containsKey(parameter)) {
					String value = String.valueOf(projectProperties.get(parameter));
					propertiesToWrite.put(parameter, value);
					getLog().info(String.format("Added property into manifest: %s=%s", parameter, value));
				}
			}
		}

		// Create the file & write into it
		String filePath = destination.concat(destination.endsWith(File.separator) ? "" : File.separator).concat(this.manifestPath);
		File file = new File(filePath);
		if (!file.exists()) {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new MojoExecutionException("Failed to create file...", e);
			}
		}

		try {
			FileWriter fileWriter = new FileWriter(file);
			for (Entry<String, String> propertiesEntry : propertiesToWrite.entrySet()) {
				fileWriter.write(String.format("%s=%s\n", propertiesEntry.getKey(), propertiesEntry.getValue()));
			}
			fileWriter.close();
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to write into file...", e);
		}

		// Done !
		getLog().info("File written successfully at " + file.getPath() + ".");
	}

	/**
	 * Returns the project's build's output directory
	 * 
	 * @return
	 */
	private String getDefaultDestination() {
		if (this.project == null) {
			return null;
		}
		Build build = this.project.getBuild();
		if (build == null) {
			return null;
		}
		return build.getOutputDirectory();
	}

}
