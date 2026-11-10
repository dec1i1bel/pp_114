package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.util.Util;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class UserDaoHibernateImpl implements UserDao {
    public UserDaoHibernateImpl() {

    }

    @Override
    public void createUsersTable() {
        String sql = """
                    CREATE TABLE IF NOT EXISTS user (
                        id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
                        name VARCHAR(50),
                        last_name VARCHAR(50),
                        age TINYINT
                    );
                    """;

        execSqlUpdate(sql);
    }

    @Override
    public void dropUsersTable() {
        execSqlUpdate("DROP TABLE IF EXISTS user");
    }

    @Override
    public void saveUser(String name, String lastName, byte age) {
        SessionFactory sessionFactory = Util.getSessionFactory();

        if (sessionFactory == null) {
            throw new IllegalStateException("Error initializing Hibernate session");
        }

        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(new User(name, lastName, age));
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            System.out.printf(
                    "Error saving user: name - %s, last name: %s, age: %d. %s%n",
                    name, lastName, age, e.getMessage()
            );
        }
    }

    /**
     * Удаление через HQL. Выше производительность - 1 запрос
     */
    @Override
    public void removeUserById(long id) {
        SessionFactory sessionFactory = Util.getSessionFactory();

        if (sessionFactory == null) {
            throw new IllegalStateException("Error initializing Hibernate session");
        }

        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
//            User - имя класса сущности
            String hql = "DELETE FROM User u WHERE u.id = :id";

            int result = session.createQuery(hql)
                    .setParameter("id", id)
                    .executeUpdate();

            transaction.commit();

            System.out.println(result > 0
                    ? String.format("User with id %d removed successfully", id)
                    : String.format("User with id id %d was not found and not removed", id)
            );
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            System.out.printf("Error removing user with id %d: %s%n", id, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Метод, чтобы не забыть о варианте уаления: загрузка сущности + удаление. Лишний SELECT: полезен, если нужны манипуляции с объектом
     */
    public void loadAndRemoveUserById(long id) {
        SessionFactory sessionFactory = Util.getSessionFactory();

        if (sessionFactory == null) {
            throw new IllegalStateException("Error initializing Hibernate session");
        }

        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);

            if (user == null) {
                transaction.rollback();
                System.out.printf("User with id %d does not exist%n", id);
                System.out.println("Removing user transaction has been rolled back");

                return;
            }

//            Hibernate 5.x
            session.delete(user);
//            Hibernate 6.x
//            session.remove(user);

            transaction.commit();

            System.out.printf("User with id %d removed successfully%n", id);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            System.out.printf("Error removing user with id %d: %s%n", id, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<User> getAllUsers() {
        SessionFactory sessionFactory = Util.getSessionFactory();

        if (sessionFactory == null) {
            throw new IllegalStateException("Error initializing Hibernate session");
        }

        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM User", User.class)
                    .getResultList();
        } catch (HibernateException e) {
            System.out.printf("Could not get all users. %s%n", e.getMessage());
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public void cleanUsersTable() {
        execSqlUpdate("TRUNCATE TABLE user");
    }

    private void execSqlUpdate(String sql) {
        SessionFactory sessionFactory = Util.getSessionFactory();

        if (sessionFactory == null) {
            throw new IllegalStateException("Error initializing Hibernate session");
        }

        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.createNativeQuery(sql).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }

            System.out.println("Error manipulating table 'user'");
            e.printStackTrace();
        }
    }
}
