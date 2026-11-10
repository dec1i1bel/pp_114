package jm.task.core.jdbc.util;

import io.github.cdimascio.dotenv.Dotenv;
import jm.task.core.jdbc.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

import java.util.HashMap;
import java.util.Map;

public class Util {
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String MYSQL_DIALECT = "org.hibernate.dialect.MySQL8Dialect";

    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory != null) {
            return sessionFactory;
        }

        try {
            Dotenv dotenv = Dotenv.load();

            String hostName = dotenv.get("DB_HOSTNAME");
            String dbName = dotenv.get("DB_NAME");
            String userName = dotenv.get("DB_USERNAME");
            String password = dotenv.get("DB_PASSWORD");
            String conUrl = String.format("jdbc:mysql://%s:3306/%s?useSSL=false&serverTimezone=UTC", hostName, dbName);

            Map<String, String> settings = createSettings(conUrl, userName, password);

            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
            registryBuilder.applySettings(settings);
            StandardServiceRegistry registry = registryBuilder.build();

            MetadataSources sources = new MetadataSources(registry);
            sources.addAnnotatedClass(User.class);

            Metadata metadata = sources.getMetadataBuilder().build();
            sessionFactory = metadata.getSessionFactoryBuilder().build();

            return sessionFactory;
        } catch (Exception e) {
            System.out.println("Error building Hibernate session for entity User");
            e.printStackTrace();

            if (sessionFactory != null) {
                StandardServiceRegistryBuilder.destroy(
                        sessionFactory.getSessionFactoryOptions().getServiceRegistry()
                );
            }

            return null;
        }
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    private static Map<String, String> createSettings(String conUrl, String userName, String password) {
        Map<String, String> settings = new HashMap<>();
        settings.put(Environment.DRIVER, MYSQL_DRIVER);
        settings.put(Environment.URL, conUrl);
        settings.put(Environment.USER, userName);
        settings.put(Environment.PASS, password);
        settings.put(Environment.DIALECT, MYSQL_DIALECT);
        settings.put(Environment.HBM2DDL_AUTO, "none");

        return settings;
    }
}
