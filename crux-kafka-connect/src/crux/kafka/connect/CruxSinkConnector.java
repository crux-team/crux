package crux.kafka.connect;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CruxSinkConnector extends SinkConnector {

    public static final String FILE_CONFIG = "file";
    public static final String URL_CONFIG = "url";
    public static final String ID_KEY_CONFIG = "id.key";
    private static final ConfigDef CONFIG_DEF = new ConfigDef()
        .define(FILE_CONFIG, Type.STRING, null, Importance.HIGH, "Destination filename. If not specified, the standard output will be used")
        .define(URL_CONFIG, Type.STRING, "http://localhost:3000", Importance.HIGH, "Destination URL of Crux HTTP end point.")
        .define(ID_KEY_CONFIG, Type.STRING, "crux.db/id", Importance.LOW, "JSON key to use as Crux id.");

    private String filename;
    private String url;
    private String idKey;

    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }

    @Override
    public void start(Map<String, String> props) {
        AbstractConfig parsedConfig = new AbstractConfig(CONFIG_DEF, props);
        filename = parsedConfig.getString(FILE_CONFIG);
        url = parsedConfig.getString(URL_CONFIG);
        idKey = parsedConfig.getString(ID_KEY_CONFIG);
    }

    @Override
    public Class<? extends Task> taskClass() {
        return CruxSinkTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>();
        for (int i = 0; i < maxTasks; i++) {
            Map<String, String> config = new HashMap<>();
            if (filename != null)
                config.put(FILE_CONFIG, filename);
            if (url != null)
                config.put(URL_CONFIG, url);
            if (url != null)
                config.put(ID_KEY_CONFIG, idKey);
            configs.add(config);
        }
        return configs;
    }

    @Override
    public void stop() {
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }
}
