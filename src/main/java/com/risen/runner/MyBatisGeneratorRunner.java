package com.risen.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sen
 * @email zhongrisen@gmail.com
 * @date 2018/4/9
 */
public class MyBatisGeneratorRunner {

	// change to your own packages, end with dot
	private static final String DEFAULT_TARGET_PACKAGE = "com.risen.";

	private static void generate() throws Exception{

		List<String> warnings = new ArrayList<>();
		DefaultShellCallback callback = new DefaultShellCallback(true);
		Configuration config = new Configuration();
        config.addContext(getDefaultContext());

		MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config,callback, warnings);
		myBatisGenerator.generate(null);

	}

	private static Context getDefaultContext() throws Exception {
		Context context = new Context(ModelType.FLAT);
		// any string
		context.setId("Canary");
		context.setTargetRuntime("MyBatis3DynamicSql");

		// delete it if you don't need this plugin
		PluginConfiguration plugin = new PluginConfiguration();
		plugin.setConfigurationType("com.risen.runner.SwaggerAnnotationPlugin");
		context.addPluginConfiguration(plugin);

		CommentGeneratorConfiguration commentConf = new CommentGeneratorConfiguration();
		commentConf.addProperty("suppressDate", "true");
		context.setCommentGeneratorConfiguration(commentConf);

		JDBCConnectionConfiguration conf = new JDBCConnectionConfiguration();
		String rootPath = System.getProperty("user.dir");
		Path root = Paths.get(rootPath);
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		Files.walk(root)
				.filter(e -> e.getFileName().toString().equals("application.yml"))
				.findFirst().ifPresent(e -> {
			try {
				JsonNode jsonNode = mapper.readTree(e.toFile());
				JsonNode ds = jsonNode.get("spring").get("datasource");
				conf.setConnectionURL(ds.get("url").asText());
				conf.setUserId(ds.get("username").asText());
				conf.setPassword(ds.get("password").asText());
				conf.setDriverClass(ds.get("driverClassName").asText());
				System.out.println(ds.get("url").asText());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		});
		context.setJdbcConnectionConfiguration(conf);

		JavaModelGeneratorConfiguration conf_ = new JavaModelGeneratorConfiguration();
		conf_.addProperty("trimStrings", "true");
		boolean singleModule = Files.list(root).anyMatch(p -> p.getFileName().toString().equals("src"));
		conf_.setTargetPackage(DEFAULT_TARGET_PACKAGE + "model");
		if (singleModule){
			conf_.setTargetProject(formatWindowsPath(rootPath + "/src/main/java"));
		} else {
			// maybe you would like change the module name
			conf_.setTargetProject(formatWindowsPath(rootPath + "/model/src/main/java"));
		}
		context.setJavaModelGeneratorConfiguration(conf_);

		JavaClientGeneratorConfiguration conf__ = new JavaClientGeneratorConfiguration();
		conf__.setConfigurationType("ANNOTATEDMAPPER");
		conf__.setTargetPackage(DEFAULT_TARGET_PACKAGE + "dao");
		if (singleModule){
			conf__.setTargetProject(formatWindowsPath(rootPath + "/src/main/java"));
		} else {
			conf__.setTargetProject(formatWindowsPath(rootPath + "/dao/src/main/java"));
		}
		context.setJavaClientGeneratorConfiguration(conf__);

		Class.forName(conf.getDriverClass());
		Connection conn = DriverManager.getConnection(conf.getConnectionURL(), conf.getUserId(), conf.getPassword());
		DatabaseMetaData metaData = conn.getMetaData();
		ResultSet rs = metaData.getTables(null, null, "%", null);
		while (rs.next()) {
			TableConfiguration tc = new TableConfiguration(context);
			tc.setTableName(rs.getString(3));
			context.addTableConfiguration(tc);
		}

		return context;
	}

	private static String formatWindowsPath(String path) {
		String targetPath = path;
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			targetPath = path.replaceAll("\\\\", "/");
		}
		return targetPath;
	}

	public static void main(String[] args) {
		try {
			MyBatisGeneratorRunner.generate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
