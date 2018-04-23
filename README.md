# gitproperties-maven-plugin

Build status: ![](https://travis-ci.org/awltech/gitproperties-maven-plugin.svg?branch=master)

## About

This plugin aims at injecting git properties such as the last commit time, commit SHA or branch, into the maven build, so they can be reused, for instance, to inject them into the artifact's manifest (see example below) 


## I'm available on bintray !

To use it, add the following plugin repository in your parent pom


```
<pluginRepositories>
	<pluginRepository>
		<id>mvanbesien-awltech-bintray</id>
		<url>https://dl.bintray.com/mvanbesien/awltech/</url>
	</pluginRepository>
</pluginRepositories>
```

Then you can reference the plugin, with a configuration as follows

```
<plugin>
	<groupId>com.github.awltech</groupId>
	<artifactId>gitproperties-maven-plugin</artifactId>
	<version>0.1.0</version>
	<executions>
		<execution>
			<id>init-git-properties</id>
			<phase>initialize</phase>
			<goals>
				<goal>inject</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

There, the plugin will bind to the "initialize" maven phase, to inject the information. To confirm it works, you can check the logs, to see traces like:

```
[INFO] --- gitproperties-maven-plugin:0.1.0:inject (init-git-properties) @ XXXX ---
[INFO] The date format pattern used is: [yyyy-MM-dd'T'HH:mm:ssZ]
[INFO]  - Injected build-timestamp-string=2018-04-23T15:03:39+0200
[INFO]  - Injected git-remote=XXXXXXXXXXXXXXXXXXXX
[INFO]  - Injected git-commit=XXXXXXXXXXXXXXXXXXXX
[INFO]  - Injected git-branch=refs/heads/master
[INFO]  - Injected git-update=2018-04-23T13:49:21+0200
[INFO]  - Injected git-modified=1
```

Here, git-modified will tell you if the built project has delta with remote. Here, the code was modified once. This can help you know if the code built differs from your origin.


## Injecting the git commit SHA into the jar's manifest

Just add the following into your project's parent pom, and it'll work !

```
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-jar-plugin</artifactId>
	<version>2.6</version>
	<configuration>
		<archive>
			<manifest>
				<addDefaultImplementationEntries>true</addDefaultImplementationEntries>
			</manifest>
			<manifestEntries>
				<Build-GitCommit>${git-commit}</Build-GitCommit>
				<Build-GitDate>${git-update}</Build-GitDate>
				<Build-GitBranch>${git-branch}</Build-GitBranch>
			</manifestEntries>
		</archive>
	</configuration>
</plugin>

```