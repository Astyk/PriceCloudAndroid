package codes.kevinvanzyl.pricecloud;

/**
 * Created by kev on 2015/06/18.
 */
public class UserItem {

    public String id;
    public String firstname;
    public String lastname;
    public String username;
    public String email;
    public String phone;
    public String created_at;
    public String updated_at;

    public UserItem(String id, String firstname, String lastname, String username, String email, String phone, String created, String updated) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.created_at = created;
        this.updated_at = updated;
    }

    @Override
    public String toString() {
        return id+" "+firstname+" "+lastname;
    }

}
