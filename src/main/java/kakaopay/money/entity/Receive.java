package kakaopay.money.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Receive {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shooting_id")
    private Shooting shooting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private long amount;

    private boolean received = false;

    public Receive(Shooting shooting, long amount) {
        this.shooting = shooting;
        this.amount = amount;
    }

    public long get(User user) {
        this.user = user;
        this.received = true;
        return amount;
    }
}
