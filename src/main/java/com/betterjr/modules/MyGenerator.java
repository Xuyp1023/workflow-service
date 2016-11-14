package com.betterjr.modules;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

public class MyGenerator {

    public static void main(final String[] args) throws SQLException, IOException, InterruptedException, XMLParserException, InvalidConfigurationException {
        final List<String> warnings = new ArrayList<String>();
        final boolean overwrite = true;
        final File configFile = new File("E:\\new\\workflow-service\\src\\main\\resources\\generatorConfig.xml");
        final ConfigurationParser cp = new ConfigurationParser(warnings);
        final Configuration config = cp.parseConfiguration(configFile);
        final DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        final MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
        myBatisGenerator.generate(null);
    }

}
