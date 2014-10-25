package nc.gouv.dtsi.maven.plugins;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

/**
 * 
 * @author Didier Vanderstoken
 *
 * @goal generate-json-schema
 * @phase process-classes
 */
public class JSonSchemaMojo extends AbstractMojo {

	private static final String EXCEPTION_MSG = "Could not retrive json schema";
	private static final String JSON_SCHEMA_FILE_EXTENSION = ".schema.json";

	/**
	 * A Set of file patterns to parse.
	 * 
	 * @parameter alias="classes"
	 * @required
	 */
	private String[] classes;

	/**
	 * 
	 * 
	 * @parameter sourceFolder="${sourceFolder}"
	 *            default-value="${project.build.directory}/classes"
	 */
	private String sourceFolder;

	/**
	 * The folder name where to store generated json schema file.
	 * 
	 * @parameter outputFolder="${outputFolder}"
	 *            default-value="${project.build.directory}/classes"
	 */
	private String outputFolder;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		ObjectMapper mapper = new ObjectMapper();
		SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();

		File f = new File(sourceFolder);

		try {

			URL[] cp = { f.toURI().toURL() };

			URLClassLoader classLoader = new URLClassLoader(cp);

			for (String oneClass : classes) {

				Class<?> o = Class.forName(oneClass, Boolean.TRUE, classLoader);

				getLog().info(
						"Retrieving json schema for " + o.getSimpleName()
								+ " class.");

				mapper.acceptJsonFormatVisitor(o, visitor);

				com.fasterxml.jackson.module.jsonSchema.JsonSchema schema = visitor
						.finalSchema();

				String stringifiedSchema = mapper
						.writerWithDefaultPrettyPrinter().writeValueAsString(
								schema);

				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(new File(outputFolder + "/"
								+ o.getSimpleName()
								+ JSON_SCHEMA_FILE_EXTENSION)));

				bos.write(stringifiedSchema.getBytes());
				bos.flush();
				bos.close();

			}

		} catch (ClassNotFoundException | IOException ee) {

			getLog().error(EXCEPTION_MSG, ee);

		}

	}

}
