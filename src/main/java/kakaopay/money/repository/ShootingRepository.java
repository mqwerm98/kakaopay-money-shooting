package kakaopay.money.repository;

import kakaopay.money.entity.Shooting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShootingRepository extends JpaRepository<Shooting, Long> {
    Shooting findByRoomIdAndToken(UUID roomId, String token);
}
