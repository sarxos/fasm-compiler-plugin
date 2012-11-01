package com.github.sarxos.fasmc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Os;


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
	 * FASM home (defined in configuration).
	 * 
	 * @parameter expression="${fasmHome}"
	 */
	private File fasmHomeConfig;

	/**
	 * FASM includes directory. By default this will be
	 * 
	 * @parameter expression="${includesPath}"
	 */
	private File fasmIncludesConfig;

	/**
	 * Compiled files extension.
	 * 
	 * @parameter alias="${extension}"
	 */
	private String extension;

	private String fasmVersion = "1.70.03";

	private File tmpFasmDir = null;

	private class Pair {

		File input = null;
		File output = null;
	}

	private String getNameWithoutExtension(String name) {
		int p = name.lastIndexOf(".");
		return name.substring(0, p);
	}

	private String getExecutableFileName() {
		if (Os.isFamily(Os.FAMILY_DOS) || SystemUtils.IS_OS_WINDOWS) {
			return "FASM.EXE";
		} else if (SystemUtils.IS_OS_LINUX) {
			return "fasm";
		} else {
			return "fasm.o";
		}
	}

	private File getFasmIncludesPath(File home) {
		if (fasmIncludesConfig != null && fasmIncludesConfig.exists()) {
			return fasmIncludesConfig;
		}
		if (SystemUtils.IS_OS_WINDOWS) {
			return new File(home, "INCLUDE");
		}
		return home;
	}

	private File getFasmHome() throws MojoExecutionException {

		if (fasmHomeConfig != null && fasmHomeConfig.exists()) {
			getLog().debug("Using fasmHome variable from plugin configuration");
			return fasmHomeConfig;
		}

		if (fasmHome != null && fasmHome.exists()) {
			getLog().debug("Using FASM_HOME from runtime environment");
			return fasmHome;
		}

		getLog().debug("FASM home not set");

		tmpFasmDir = new File(targetDirectory, "fasmtmp-" + System.currentTimeMillis());
		tmpFasmDir.mkdirs();

		if (!tmpFasmDir.exists()) {
			throw new MojoExecutionException(String.format("Cannot create directory '%s'", tmpFasmDir));
		}

		String arch = "";
		if (SystemUtils.IS_OS_WINDOWS) {
			arch = "win";
		} else if (Os.isFamily(Os.FAMILY_DOS)) {
			arch = "dos";
		} else if (SystemUtils.IS_OS_LINUX) {
			arch = "linux";
		} else {
			arch = "elfc";
		}

		File archive = ZipUtils.getFile(String.format("fasm-%s-%s.zip", fasmVersion, arch));

		ZipFile zip = null;
		try {
			zip = new ZipFile(archive);
			zip.extractAll(tmpFasmDir.getAbsolutePath());
		} catch (ZipException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}

		if (getLog().isDebugEnabled()) {
			getLog().debug(String.format("FASM binaries extracted, using directory as FASM home '%s'", tmpFasmDir));
		}

		return tmpFasmDir;
	}

	private void execute0() throws MojoExecutionException {

		File home = getFasmHome();
		File executable = new File(home, getExecutableFileName());
		File includes = getFasmIncludesPath(home);

		if (!executable.exists()) {
			throw new MojoExecutionException(String.format("FASM executable %s does not exist!", executable));
		}

		if (getLog().isDebugEnabled()) {
			getLog().debug(String.format("FASM home: %s", home));
			getLog().debug(String.format("FASM executable: %s", executable));
		}

		if (outputDirectory == null) {
			outputDirectory = new File(targetDirectory, "fasmbin");
		}
		if (!outputDirectory.exists()) {
			boolean created = outputDirectory.mkdirs();
			if (!created) {
				throw new MojoExecutionException(String.format("Canot create directory '%s'", outputDirectory));
			}
		}

		if (getLog().isDebugEnabled()) {
			getLog().debug(String.format("Output directory: '%s'", outputDirectory));
		}

		if (extension == null) {
			extension = ".bin";
		} else if (!extension.startsWith(".")) {
			extension = "." + extension;
		}

		if (!sourceDirectory.exists()) {
			getLog().info("FASM source directory does not exist - nothing to compile");
			return;
		}

		if (getLog().isDebugEnabled()) {
			getLog().debug(String.format("File extension '%s'", extension));
		}

		DirectoryScanner ds = new DirectoryScanner();
		ds.setIncludes(new String[] { "**/*.asm" });
		ds.setBasedir(sourceDirectory);
		ds.setCaseSensitive(false);
		ds.scan();

		List<Pair> pairs = new ArrayList<Pair>();

		String[] files = ds.getIncludedFiles();
		for (String file : files) {
			Pair p = new Pair();
			p.input = new File(sourceDirectory, file);
			p.output = new File(outputDirectory, getNameWithoutExtension(file) + extension);
			pairs.add(p);
		}

		ProcessBuilder pb = new ProcessBuilder();
		pb.redirectErrorStream(true);

		Map<String, String> envs = pb.environment();
		envs.put("include", includes.getAbsolutePath() + SystemUtils.FILE_SEPARATOR);

		for (Pair pair : pairs) {

			String in = pair.input.getAbsolutePath();
			String out = pair.output.getAbsolutePath();

			getLog().info(String.format("Compiling %s to %s", in, out));

			List<String> command = new ArrayList<String>();
			command.add(executable.getAbsolutePath());
			command.add(pair.input.getAbsolutePath());
			command.add(pair.output.getAbsolutePath());

			File parent = new File(pair.output.getParent());
			if (!parent.exists()) {
				boolean created = parent.mkdirs();
				if (!created) {
					throw new MojoExecutionException("Cannot create directory " + parent);
				}
			}

			if (getLog().isDebugEnabled()) {

				String fullcmd = "";
				String delimiter = "";

				for (String cmd : command) {
					fullcmd += delimiter + cmd;
					delimiter = " ";
				}

				getLog().debug(String.format("Invoke command '%s'", fullcmd));
			}

			pb.command(command);

			Process process = null;
			try {
				process = pb.start();
			} catch (IOException e) {
				throw new MojoExecutionException("Cannot start compilation process", e);
			}

			InputStream is = process.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			try {
				String line = null;
				while ((line = br.readLine()) != null) {
					getLog().info(line);
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

			int exit = -1;
			try {
				exit = process.waitFor();
			} catch (InterruptedException e) {
				throw new MojoExecutionException("Thread interrupted while waiting for compilation process to finish", e);
			}

			if (exit != 0) {
				String msg = String.format("Process returned %d. Cannot compile file %s", exit, pair.input);
				getLog().error(msg);
				throw new MojoExecutionException(msg);
			}
		}
	}

	@Override
	public void execute() throws MojoExecutionException {
		try {
			execute0();
		} finally {
			if (tmpFasmDir != null && tmpFasmDir.exists()) {
				removeDirectory(tmpFasmDir);
			}
		}
	}

	private final void removeDirectory(File dir) {

		List<File> files = new ArrayList<File>();
		List<File> dirs = new ArrayList<File>();

		recursiveListing(dir, files, dirs);

		for (int i = files.size() - 1; i >= 0; i--) {
			File f = files.get(i);
			if (!f.delete()) {
				f.deleteOnExit();
			}
			if (getLog().isDebugEnabled()) {
				getLog().debug("Remove file " + f);
			}
		}
		for (int i = dirs.size() - 1; i >= 0; i--) {
			File d = dirs.get(i);
			if (!d.delete()) {
				d.deleteOnExit();
			}
			if (getLog().isDebugEnabled()) {
				getLog().debug("Remove directory " + d);
			}
		}
	}

	private static final void recursiveListing(File file, List<File> files, List<File> dirs) {
		if (file.isDirectory()) {
			dirs.add(file);
			for (File f : file.listFiles()) {
				recursiveListing(f, files, dirs);
			}
		} else {
			files.add(file);
		}
	}

}
