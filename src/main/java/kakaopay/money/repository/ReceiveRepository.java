package kakaopay.money.repository;

import kakaopay.money.entity.Receive;
import kakaopay.money.entity.Shooting;
import kakaopay.money.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceiveRepository extends JpaRepository<Receive, Long> {

    boolean existsByShootingAndUser(Shooting shooting, User user);

    Receive findTop1ByShootingAndReceivedOrderByIdAsc(Shooting shooting, boolean received);

    List<Receive> findByShootingAndReceived(Shooting shooting, boolean received);

    long countByShooting(Shooting shooting);

    List<Receive> findByShooting(Shooting shooting);
}
