package kakaopay.money.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Table(name = "users")
@EqualsAndHashCode(of = "id")
public class User {

    @Id @GeneratedValue
    private Long id;

}
