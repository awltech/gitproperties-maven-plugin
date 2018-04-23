package com.github.awltech.mojos.gitproperties;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * Maven MOJO that injects into the Maven Project's properties, parameters that come from the current GIT repository, mainly for binaries tracability.
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 * @since 0.1.0
 * 
 */
@Mojo(name = "inject", requiresProject = true)
public class GitPropertiesInjectorMojo extends AbstractMojo {

	/**
	 * Constant to parse config.
	 */
	private static final String URL = "url";

	/**
	 * Constant to parse config.
	 */
	private static final String REMOTE = "remote";

	/*
	 * Current Maven project, automatically injected, that will be updated.
	 */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	/**
	 * Name of the git branch property. Set as parameter, to make it overridable.
	 */
	@Parameter(property = "gitBranchProperty", required = false, readonly = true)
	private String gitBranchProperty = "git-branch";

	/**
	 * Name of the git remote property. Set as parameter, to make it overridable.
	 */
	@Parameter(property = "gitRemoteProperty", required = false, readonly = true)
	private String gitRemoteProperty = "git-remote";

	/**
	 * Name of the git commit property. Set as parameter, to make it overridable.
	 */
	@Parameter(property = "gitCommitProperty", required = false, readonly = true)
	private String gitCommitProperty = "git-commit";

	/**
	 * Name of the git update property. Set as parameter, to make it overridable.
	 */
	@Parameter(property = "gitUpdateProperty", required = false, readonly = true)
	private String gitUpdateProperty = "git-update";

	/**
	 * Name of the git modified property. Set as parameter, to make it overridable.
	 */
	@Parameter(property = "gitModifiedProperty", required = false, readonly = true)
	private String gitModifiedProperty = "git-modified";

	/**
	 * Name of the the maven buildTimestampString property, Set as parameter to make it overridable.
	 */
	@Parameter(property = "buildTimestampString", required = false, readonly = true)
	private String buildTimestampString = "build-timestamp-string";

	/**
	 * Way to override the Date Format pattern
	 */
	@Parameter(property = "date-format-pattern", required = false, readonly = true)
	private String dateFormatPattern = "yyyy-MM-dd'T'HH:mm:ssZ";

	/**
	 * If value set to true, maven build timestamp as string & git date will be displayed as timestamps
	 */
	@Parameter(property = "useTimestamps", required = false, readonly = true)
	private String useTimestamps = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		boolean useTimestamps = Boolean.parseBoolean(this.useTimestamps);
		if (useTimestamps) {
			getLog().info(Messages.DATE_TIMESTAMP.value());
		} else {
			getLog().info(Messages.DATE_FORMAT.value(dateFormatPattern));
		}
		
		SimpleDateFormat format = new SimpleDateFormat(dateFormatPattern);

		// Checks if project or repository exist
		if (this.project == null) {
			getLog().warn(Messages.NO_PROJECT_RESOLVED.value());
			return;
		}

		// Inject a build timestamp string
		String timestampAsString = useTimestamps ? String.valueOf(System.currentTimeMillis()) : format.format(new Date());
		this.project.getProperties().put(this.buildTimestampString, timestampAsString);
		getLog().info(Messages.INFO_INJECTED_PROPERTY.value(this.buildTimestampString, timestampAsString));

		Repository repository = null;
		RevWalk revWalk = null;

		File basedir = this.project.getBasedir();
		if (!basedir.exists()) {
			getLog().warn(Messages.NO_PROJECT_EXISTING.value());
			return;
		}
		try {

			// Initialize repository
			repository = new FileRepositoryBuilder().readEnvironment().findGitDir(basedir).build();
			if (repository == null || repository.getIndexFile() == null) {
				getLog().warn(Messages.NO_REPOSITORY_IN_PROJECT.value());
				return;
			}

			// Update the GIT remote
			StoredConfig config = repository.getConfig();
			Set<String> subsections = config.getSubsections(REMOTE);
			if (subsections != null && !subsections.isEmpty()) {
				String remote = config.getString(REMOTE, subsections.iterator().next(), URL);
				if (remote != null && remote.length() > 0) {
					this.project.getProperties().put(this.gitRemoteProperty, remote);
					getLog().info(Messages.INFO_INJECTED_PROPERTY.value(this.gitRemoteProperty, remote));
				}
			}

			// Update the GIT commit
			Ref ref = repository.getRef(Constants.HEAD);
			if (ref != null) {
				this.project.getProperties().put(this.gitCommitProperty, ref.getObjectId().name());
				getLog().info(Messages.INFO_INJECTED_PROPERTY.value(this.gitCommitProperty, ref.getObjectId().name()));

			}

			// Update the GIT branch
			String fullBranch = repository.getFullBranch();
			if (fullBranch != null) {
				this.project.getProperties().put(this.gitBranchProperty, fullBranch);
				getLog().info(Messages.INFO_INJECTED_PROPERTY.value(this.gitBranchProperty, fullBranch));
			}

			// Update the GIT update
			revWalk = new RevWalk(repository);
			if (revWalk != null) {
				RevCommit parseCommit = revWalk.parseCommit(ref.getObjectId());
				if (parseCommit != null) {
					long commitTime = parseCommit.getCommitTime() * 1000L;
					String time = useTimestamps ? String.valueOf(commitTime) : format.format(new Date(commitTime));
					this.project.getProperties().put(this.gitUpdateProperty, time);
					getLog().info(Messages.INFO_INJECTED_PROPERTY.value(this.gitUpdateProperty, time));
				}
			}

			Git git = new Git(repository);
			if (git != null) {
				Status status = git.status().call();
				if (status != null) {
					String modified = String.valueOf(status.getUncommittedChanges().size());
					this.project.getProperties().put(this.gitModifiedProperty, modified);
					getLog().info(Messages.INFO_INJECTED_PROPERTY.value(this.gitModifiedProperty, modified));
				}
				git.close();
			}

			// Cleans
			revWalk.close();
			repository.close();

		} catch (Exception e) {
			getLog().warn(Messages.ENCOUNTERED_EXCEPTION.value(), e);
		} finally {
			if (repository != null) {
				repository.close();
			}
			if (revWalk != null) {
				revWalk.close();
			}
		}
	}
}
