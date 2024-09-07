package model;

public class Member {
    private int id;
    private String name;
    private String email;
    private String membershipId;

    // Constructor, Getters, and Setters
    public Member(String name, String email, String membershipId) {
        this.name = name;
        this.email = email;
        this.membershipId = membershipId;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMembershipId() {
        return membershipId;
    }
}
