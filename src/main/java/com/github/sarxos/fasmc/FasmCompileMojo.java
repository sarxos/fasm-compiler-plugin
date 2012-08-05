package com.github.sarxos.fasmc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;


/**
 * Goal which compiles FASM sources.
 * 
 * @goal compile
 * @phase compile
 * @author Bartosz Firyn (SarXos)
 */
public class FasmCompileMojo extends AbstractMojo {

	/**
	 * Target directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	/**
	 * Compiled files will be stored here.
	 * 
	 * @parameter expression="${outputDirectory}"
	 */
	private File outputDirectory;

	/**
	 * Sources directory.
	 * 
	 * @parameter alias="sourceDirectory" default-value="src/main/asm"
	 */
	private File sourceDirectory;

	/**
	 * FASM home (defined in env).
	 * 
	 * @parameter expression="${env.FASM_HOME}"
	 */
	private File fasmHome;

	/**
	 * FASM home (defined in property).
	 * 
	 * @parameter expression="${fasm.home}"
	 */
	private File fasmHomeProperty;

	/**
	 * FASM home (defined in configuration).
	 * 
	 * @parameter expression="${fasmHome}"
	 */
	private File fasmHomeConfig;

	/**
	 * FASM executable path.
	 */
	private File fasmExecutable;

	/**
	 * FASM includes.
	 * 
	 * @parameter expression="${fasm.includes}"
	 */
	private File fasmIncludePath;

	/**
	 * Compiled files extension.
	 * 
	 * @parameter alias="${extension}"
	 */
	private String extension;

	private class Pair {

		File input = null;
		File output = null;
	}

	private String getNameWtExtension(String name) {
		int p = name.lastIndexOf(".");
		return name.substring(0, p);
	}

	public void execute() throws MojoExecutionException {

		if (fasmHome != null && fasmHome.exists()) {
			getLog().debug("Using FASM_HOME from runtime environment");
		} else if (fasmHomeProperty != null && fasmHomeProperty.exists()) {
			getLog().debug("Using fasm.home property from POM");
			fasmHome = fasmHomeProperty;
		} else if (fasmHomeConfig != null && fasmHomeConfig.exists()) {
			getLog().debug("Using fasmHome variable from plugin configuration");
			fasmHome = fasmHomeProperty;
		} else {
			throw new MojoExecutionException("FASM home has not been specified. Please either configure FASM_HOME env variable, or add fasm.home property in POM, or add fasmHome configuration element in plugin");
		}

		getLog().debug("FASM home set to " + fasmHome);

		if (fasmIncludePath == null) {
			fasmIncludePath = new File(fasmHome + "/include");
		}

		getLog().debug("FASM include path set to " + fasmIncludePath);

		if (fasmExecutable == null) {
			fasmExecutable = new File(fasmHome + "/FASM.EXE");
		}
		if (!fasmExecutable.exists()) {
			throw new MojoExecutionException("FASM executable does not exist!");
		}

		getLog().debug("FASM executable set to " + fasmExecutable);

		if (outputDirectory == null) {
			outputDirectory = new File(targetDirectory + "/fasmbin");
		}
		if (!outputDirectory.exists()) {
			boolean created = outputDirectory.mkdirs();
			if (!created) {
				throw new MojoExecutionException("Canot create directory '" + outputDirectory + "'");
			}
		}

		getLog().debug("Output directory " + outputDirectory);

		if (extension == null) {
			extension = ".bin";
		}
		if (!extension.startsWith(".")) {
			extension = "." + extension;
		}

		if (!sourceDirectory.exists()) {
			getLog().info("FASM source directory does not exist - nothing to compile");
			return;
		}

		getLog().debug("File extension " + extension);

		DirectoryScanner ds = new DirectoryScanner();
		ds.setIncludes(new String[] { "**/*.asm" });
		ds.setBasedir(sourceDirectory);
		ds.setCaseSensitive(false);
		ds.scan();

		List<Pair> pairs = new ArrayList<Pair>();

		String[] files = ds.getIncludedFiles();
		for (String file : files) {
			Pair p = new Pair();
			p.input = new File(sourceDirectory + "/" + file);
			p.output = new File(outputDirectory + "/" + getNameWtExtension(file) + extension);
			pairs.add(p);
		}

		ProcessBuilder pb = new ProcessBuilder();
		pb.redirectErrorStream(true);

		Map<String, String> envs = pb.environment();
		envs.put("include", fasmIncludePath.getAbsolutePath() + "\\");

		for (Pair p : pairs) {

			getLog().info("Compile " + p.input);

			String in = p.input.getAbsolutePath();
			String out = p.output.getAbsolutePath();

			if (getLog().isDebugEnabled()) {
				getLog().debug("Compiling " + in + " to " + out);
			}

			List<String> command = new ArrayList<String>();
			command.add(fasmExecutable.getAbsolutePath());
			command.add(p.input.getAbsolutePath());
			command.add(p.output.getAbsolutePath());

			if (getLog().isDebugEnabled()) {
				String dbgcmd = "";
				for (String cmd : command) {
					dbgcmd += cmd + " ";
				}
				getLog().debug("Invoke command '" + dbgcmd + "'");
			}

			pb.command(command);

			Process process = null;
			try {
				process = pb.start();
			} catch (IOException e) {
				throw new MojoExecutionException("Cannot start compilation process", e);
			}

			if (getLog().isDebugEnabled()) {
				InputStream is = process.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				try {
					String line = null;
					while ((line = br.readLine()) != null) {
						getLog().debug(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {
							throw new MojoExecutionException("Cannot close reader", e);
						}
					}
				}
			}

			int exit = -1;
			try {
				exit = process.waitFor();
			} catch (InterruptedException e) {
				throw new MojoExecutionException("Thrad interrupted while waiting for compilation process to finish", e);
			}

			if (exit != 0) {
				getLog().error("Cannot compile file " + p.input);
			}
		}
	}
}
