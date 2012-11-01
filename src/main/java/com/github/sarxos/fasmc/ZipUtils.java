package com.github.sarxos.fasmc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.MojoExecutionException;


public class ZipUtils {

	private static URI resourceToLocalURI(String resource, Class<?> ctx) throws ZipException, IOException, URISyntaxException, MojoExecutionException {

		ProtectionDomain domain = ctx.getProtectionDomain();
		CodeSource source = domain.getCodeSource();
		URI where = source.getLocation().toURI();

		File location = new File(where);
		URI uri = null;

		if (location.isDirectory()) {
			uri = URI.create(where.toString() + resource);
		} else {
			ZipFile zip = new ZipFile(location);
			try {
				uri = extract(zip, resource);
			} finally {
				zip.close();
			}
		}

		return uri;
	}

	private static URI extract(ZipFile zip, String filen) throws IOException, MojoExecutionException {

		File tmp = File.createTempFile(filen + ".", ".tmp");
		tmp.deleteOnExit();

		final ZipEntry entry = zip.getEntry(filen);
		if (entry == null) {
			throw new FileNotFoundException(String.format("Cannot find file %s in archive %s", filen, zip.getName()));
		}

		InputStream is = zip.getInputStream(entry);
		OutputStream os = new FileOutputStream(tmp);

		final byte[] buf = new byte[1024];
		int i = 0;
		try {
			while ((i = is.read(buf)) != -1) {
				os.write(buf, 0, i);
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new MojoExecutionException(e.getMessage(), e);
				}
				try {
					os.close();
				} catch (IOException e) {
					throw new MojoExecutionException(e.getMessage(), e);
				}
			}
		}

		return tmp.toURI();
	}

	protected static File getFile(String name) throws MojoExecutionException {
		try {
			return new File(resourceToLocalURI(name, FasmCompileMojo.class));
		} catch (ZipException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

}
