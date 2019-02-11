package manager;

import db.DBConnectionProvider;
import model.Task;
import model.TaskStatus;
import model.User;
import model.UserType;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public class TaskManager {
    private Connection connection;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private UserManager userManager = new UserManager();

    public TaskManager() {
        this.connection = DBConnectionProvider.getInstance().getConnection();
    }

    public void addTask(Task task) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement("INSERT into task(name ,description, deadline, status, user_id) values ?,?,?,?,?");
            preparedStatement.setString(1, task.getName());
            preparedStatement.setString(2, task.getDescription());
            preparedStatement.setString(3, sdf.format(task.getDeadline()));
            preparedStatement.setString(4, task.getTaskStatus().name());
            preparedStatement.setInt(5, task.getUserId());
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();

            if (resultSet.next()) {
                int id = resultSet.getInt(1);
                task.setId(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Task> getAllTasks() {
        Statement statement = null;
        List<Task> tasks = new LinkedList<Task>();
        try {
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT  * from task");

            tasks = getTasksFromResultSet(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public List<Task> getAllTasksByUserId(int userId) {
        PreparedStatement statement;
        List<Task> tasks = new LinkedList<Task>();
        try {
            statement = connection.prepareStatement("SELECT  * from task where user_id = ?");
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            tasks = getTasksFromResultSet(resultSet);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }


    private List<Task> getTasksFromResultSet(ResultSet resultSet) throws SQLException {
        List<Task> tasks = new LinkedList<Task>();
        while (resultSet.next()) {
            Task task = new Task();
            task.setId(resultSet.getInt("id"));
            task.setName(resultSet.getString("name"));
            task.setDescription(resultSet.getString("description"));
            try {
                task.setDeadline(sdf.parse(resultSet.getString("deadline")));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            task.setTaskStatus(TaskStatus.valueOf(resultSet.getString("status")));
            task.setUserId(resultSet.getInt("user_id"));
            task.setUser(userManager.getUserById(task.getUserId()));
            tasks.add(task);
        }
        return tasks;
    }
}