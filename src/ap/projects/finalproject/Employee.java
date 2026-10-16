package ap.projects.finalproject;

import java.io.Serializable;

public class Employee implements Serializable {
    private static final long serialVersionUID = 4L;
    private String name;
    private String employeeId;
    private String username;
    private String password;
    private String role;

    public Employee(String name, String employeeId, String username, String password, String role) {
        this.name = name;
        this.employeeId = employeeId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getName() { return name; }
    public String getEmployeeId() { return employeeId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isManager() {
        return "manager".equalsIgnoreCase(role);
    }

    public boolean isStaff() {
        return "staff".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "Name: " + name +
                " | Employee ID: " + employeeId +
                " | Username: " + username +
                " | Role: " + role;
    }
}
