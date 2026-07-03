package model;

public class User {
    private String email;
    private String password;
    private String name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User(){}

    public User(String email, String password, String name){
        setEmail(email);
        setPassword(password);
        setName(name);
    }

    public User(String email, String password){
        setEmail(email);
        setPassword(password);
    }

}
