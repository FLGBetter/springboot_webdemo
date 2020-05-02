package springboot_webdemo.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Table(name="lf_user")
@Entity
public class User implements Serializable{
	@Id
    @GeneratedValue
    @Getter
	@Setter
	private Long id;
	@Getter
	@Setter
	@Column(nullable = false, unique = true) 
	private String username;
	
	@Getter
	@Setter
	private String password;
	
	@Getter
	@Setter
	private String address;

	@Override
	public String toString() {
		return "User [username=" + username + ", password=" + password + ", address=" + address + "]";
	}
	
}
