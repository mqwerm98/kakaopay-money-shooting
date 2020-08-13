package kakaopay.money.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.bytebuddy.utility.RandomString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Shooting {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private long totalAmount;

    private int count;

    private String token;

    private LocalDateTime createDate;

    public Shooting(Room room, User user, long totalAmount, int count) {
        this.room = room;
        this.user = user;
        this.totalAmount = totalAmount;
        this.count = count;
        this.token = RandomString.make(3);
        this.createDate = LocalDateTime.now();
    }
}
