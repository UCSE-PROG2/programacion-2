package org.example.model;

public class User {

    private Integer id;
    private String name;
    private boolean active;

    public User(Integer id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public Integer getId()                { return id; }
    public String getName()               { return name; }
    public boolean isActive()             { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "User{ id=" + id + ", name='" + name + "', active=" + active + " }";
    }
}
